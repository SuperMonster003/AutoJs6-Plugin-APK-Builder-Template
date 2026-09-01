package org.autojs.plugin.apkbuilder.template.impl

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.reandroid.arsc.chunk.TableBlock
import org.autojs.plugin.apkbuilder.template.ApkBuildProgress
import org.autojs.plugin.apkbuilder.template.ApkBuildRequest
import org.autojs.plugin.apkbuilder.template.ApkBuildRequestExtraKeys
import org.autojs.plugin.apkbuilder.template.ApkBuildResult
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateCapabilityKeys
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateProtocol
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateResult
import org.autojs.plugin.apkbuilder.template.ApkKeyStoreRequest
import org.autojs.plugin.apkbuilder.template.IApkBuildCallback
import org.autojs.plugin.apkbuilder.template.TypeScriptBuildStagingCipher
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.ArrayDeque
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.ZipEntry
import java.util.zip.CRC32
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class RemoteApkBuildSessionInstrumentedTest {

    private lateinit var context: Context
    private lateinit var fixtureRoot: File
    private lateinit var executor: ExecutorService
    private val sessions = mutableListOf<RemoteApkBuildSession>()

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        fixtureRoot = File(
            context.cacheDir,
            "remote-build-instrumented/${UUID.randomUUID()}",
        ).apply {
            check(mkdirs()) { "Failed to create test fixture root: $path" }
        }
        executor = Executors.newSingleThreadExecutor()
    }

    @After
    fun tearDown() {
        sessions.forEach(RemoteApkBuildSession::close)
        executor.shutdownNow()
        executor.awaitTermination(10, TimeUnit.SECONDS)
        fixtureRoot.deleteRecursively()
    }

    @Test
    fun metadataAdvertisesTheFormalPluginManagedBuildContract() {
        val capabilities = requireNotNull(ApkBuilderTemplateMetadata.pluginInfo(context).capabilities)

        assertTrue(capabilities.getBoolean(ApkBuilderTemplateCapabilityKeys.SUPPORTS_APK_BUILD))
        assertEquals(
            ApkBuilderTemplateProtocol.APK_BUILD_VERSION,
            capabilities.getInt(ApkBuilderTemplateCapabilityKeys.APK_BUILD_PROTOCOL_VERSION),
        )
        assertEquals(
            ApkBuilderTemplateProtocol.APK_BUILD_EXECUTION_MODE_ON_DEVICE_PLUGIN,
            capabilities.getString(ApkBuilderTemplateCapabilityKeys.APK_BUILD_EXECUTION_MODE),
        )
        assertEquals(
            BuildConfig.ENABLE_REMOTE_BUILD,
            capabilities.getBoolean(ApkBuilderTemplateCapabilityKeys.SUPPORTS_REMOTE_BUILD),
        )
        assertTrue(capabilities.getBoolean(ApkBuilderTemplateCapabilityKeys.SUPPORTS_KEYSTORE_OPERATIONS))
        assertEquals(
            ApkBuilderTemplateProtocol.KEYSTORE_VERSION,
            capabilities.getInt(ApkBuilderTemplateCapabilityKeys.KEYSTORE_API_VERSION),
        )
    }

    @Test
    fun pluginKeyStoreManagerCreatesAndVerifiesBksAndJks() {
        listOf(ApkKeyStoreRequest.TYPE_BKS, ApkKeyStoreRequest.TYPE_JKS).forEach { type ->
            val keyStore = File(fixtureRoot, "plugin-keystore.${type.lowercase()}")
            val createResult = PluginKeyStoreManager.execute(
                context,
                keyStoreRequest(
                    operation = ApkKeyStoreRequest.OPERATION_CREATE,
                    type = type,
                    keyStoreFd = ParcelFileDescriptor.open(
                        keyStore,
                        ParcelFileDescriptor.MODE_CREATE or
                            ParcelFileDescriptor.MODE_TRUNCATE or
                            ParcelFileDescriptor.MODE_READ_WRITE,
                    ),
                ),
            )

            assertTrue("Create $type failed: ${createResult.message}", createResult.isSuccess)
            assertTrue("Created $type keystore is empty.", keyStore.length() > 0L)

            val verifyResult = PluginKeyStoreManager.execute(
                context,
                keyStoreRequest(
                    operation = ApkKeyStoreRequest.OPERATION_VERIFY,
                    type = type,
                    keyStoreFd = ParcelFileDescriptor.open(
                        keyStore,
                        ParcelFileDescriptor.MODE_READ_ONLY,
                    ),
                ),
            )
            assertTrue("Verify $type failed: ${verifyResult.message}", verifyResult.isSuccess)

            val wrongAliasResult = PluginKeyStoreManager.execute(
                context,
                keyStoreRequest(
                    operation = ApkKeyStoreRequest.OPERATION_VERIFY,
                    type = type,
                    keyStoreFd = ParcelFileDescriptor.open(
                        keyStore,
                        ParcelFileDescriptor.MODE_READ_ONLY,
                    ),
                ).apply {
                    keyAlias = "missing-alias"
                },
            )
            assertFalse("Unknown $type alias must fail closed.", wrongAliasResult.isSuccess)
        }
    }

    private fun keyStoreRequest(
        operation: Int,
        type: String,
        keyStoreFd: ParcelFileDescriptor,
    ) = ApkKeyStoreRequest(
        operation = operation,
        keyStoreFd = keyStoreFd,
        keyStoreType = type,
        keyStorePassword = DEFAULT_KEY_STORE_PASSWORD,
        keyAlias = DEFAULT_KEY_ALIAS,
        keyAliasPassword = DEFAULT_KEY_ALIAS_PASSWORD,
        commonName = "AutoJs6 Plugin Test",
        organization = "AutoJs6",
        country = "CN",
    )

    @Test
    fun directorySourceWithRiskyHostNameMismatchCompletesWithWarning() {
        val pairedHost = ApkBuilderTemplateMetadata.templateInfo(context)
        val request = createRequest(
            sourceKind = ApkBuildRequestExtraKeys.SOURCE_KIND_DIRECTORY,
            sourcePath = "project",
            entries = mapOf(
                "project/main.js" to "console.log('directory source');",
                "project/data/message.txt" to "hello",
            ),
        ).apply {
            hostVersionName = "${pairedHost.hostVersionName}-mismatch"
            allowRiskyBuild = true
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertSuccessfulBuild(
            observation,
            "assets/project/main.js",
            "assets/project/data/message.txt",
        )
        assertEquals(ApkBuilderTemplateResult.LEVEL_WARN, observation.result.compatibilityLevel)
        assertTrue(
            observation.result.warnings.any { warning ->
                warning.contains("Host versionName mismatch")
            },
        )
        val updatedConfig = JSONObject(observation.result.updatedProjectConfigJson!!)
        assertEquals("main.js", updatedConfig.getString("main"))
        assertEquals(2L, updatedConfig.getJSONObject("build").getLong("number"))
        assertRequestInputsClosed(request)
    }

    @Test
    fun directorySourceOutsideDeclaredRangeFailsEvenWhenRiskyBuildIsAllowed() {
        val pairedHost = ApkBuilderTemplateMetadata.templateInfo(context)
        val declaredMaxHostVersionCode = pairedHost.capabilities?.getLong(
            ApkBuilderTemplateCapabilityKeys.COMPATIBILITY_MAX_HOST_VERSION_CODE,
            pairedHost.hostVersionCode,
        ) ?: pairedHost.hostVersionCode
        val request = createRequest(
            sourceKind = ApkBuildRequestExtraKeys.SOURCE_KIND_DIRECTORY,
            sourcePath = "project",
            entries = mapOf(
                "project/main.js" to "console.log('directory source');",
                "project/data/message.txt" to "hello",
            ),
        ).apply {
            hostVersionCode = declaredMaxHostVersionCode + 1L
            allowRiskyBuild = true
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_FAILED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_BLOCK, observation.result.compatibilityLevel)
        assertTrue(
            observation.result.errors.any { error ->
                error.contains("outside the declared compatibility contract")
            },
        )
        assertNoOutput(observation)
        assertRequestInputsClosed(request)
    }

    @Test
    fun fileSourceCompletes() {
        val request = createRequest(
            sourceKind = ApkBuildRequestExtraKeys.SOURCE_KIND_FILE,
            sourcePath = "source.js",
            entries = mapOf("source.js" to "console.log('single file source');"),
        )

        val observation = execute(request, remoteBuildEnabled = true)

        assertSuccessfulBuild(observation, "assets/project/main.js")
        val updatedConfig = JSONObject(observation.result.updatedProjectConfigJson!!)
        assertEquals("main.js", updatedConfig.getString("main"))
        assertEquals(
            TEST_VERSION_CODE.toLong(),
            updatedConfig.getJSONObject("build").getLong("number"),
        )
        ZipFile(observation.outputCopy).use { apk ->
            val configEntry = requireNotNull(apk.getEntry("assets/project/project.json"))
            val packagedConfig = JSONObject(
                apk.getInputStream(configEntry).bufferedReader().use { it.readText() },
            )
            assertEquals(
                TEST_VERSION_CODE.toLong(),
                packagedConfig.getJSONObject("build").getLong("number"),
            )
        }
    }

    @Test
    fun defaultKeyStoreProducesV1SignedApk() {
        val request = createRequest(
            projectConfig = createProjectConfig(signatureScheme = "V1"),
        )

        val observation = execute(request, remoteBuildEnabled = true)

        assertSuccessfulBuild(observation, "assets/project/main.js")
        ZipFile(observation.outputCopy).use { apk ->
            val names = apk.entries().asSequence().map { entry -> entry.name }.toSet()
            assertTrue("V1 APK is missing META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF" in names)
            assertTrue(
                "V1 APK is missing a certificate block.",
                names.any { name ->
                    name.startsWith("META-INF/", ignoreCase = true) &&
                        (name.endsWith(".RSA", true) ||
                            name.endsWith(".DSA", true) ||
                            name.endsWith(".EC", true))
                },
            )
        }
        assertRequestInputsClosed(request)
    }

    @Test
    fun customKeyStoreV2IconAndAbiSelectionComplete() {
        val selectedAbi = ApkBuilderTemplateMetadata.templateInfo(context)
            .capabilities
            ?.getStringArray(ApkBuilderTemplateCapabilityKeys.TEMPLATE_SUPPORTED_ABIS)
            ?.firstOrNull()
            ?: error("Runtime Kit does not declare a supported ABI.")
        val requiredNativeLibraries = listOf(
            "libjackpal-androidterm5.so",
            "libjackpal-termexec2.so",
            "libc++_shared.so",
        )
        val iconWidth = 19
        val iconHeight = 17
        val iconColor = 0xff12ab34.toInt()
        val iconBytes = createSolidPng(iconWidth, iconHeight, iconColor)
        val request = createRequest(
            binaryEntries = mapOf("icon.png" to iconBytes),
            projectConfig = createProjectConfig(
                abis = listOf(selectedAbi),
                libs = emptyList(),
                permissions = listOf("android.permission.INTERNET", "android.permission.WAKE_LOCK"),
                signatureScheme = "V2",
            ),
        ).apply {
            extras!!.putString(ApkBuildRequestExtraKeys.ICON_PATH, "icon.png")
            attachBundledKeyStoreAsCustomInput()
            attachNativeLibrariesArchive(selectedAbi, requiredNativeLibraries)
        }
        iconBytes.fill(0)

        val observation = execute(request, remoteBuildEnabled = true)

        assertSuccessfulBuild(observation, "assets/project/main.js")
        assertRequestedPermissions(
            observation.outputCopy,
            "android.permission.INTERNET",
            "android.permission.WAKE_LOCK",
        )
        assertIconReplaced(observation.outputCopy, iconWidth, iconHeight, iconColor)
        ZipFile(observation.outputCopy).use { apk ->
            val names = apk.entries().asSequence().map { entry -> entry.name }.toSet()
            val packagedAbis = names
                .filter { name -> name.startsWith("lib/") && name.endsWith(".so") }
                .map { name -> name.split('/')[1] }
                .toSet()
            assertEquals(setOf(selectedAbi), packagedAbis)
            requiredNativeLibraries.forEach { library ->
                assertTrue(
                    "Output APK is missing lib/$selectedAbi/$library",
                    "lib/$selectedAbi/$library" in names,
                )
            }
            assertFalse(
                "V2-only APK unexpectedly contains a V1 certificate block.",
                names.any { name ->
                    name.startsWith("META-INF/", ignoreCase = true) &&
                        (name.endsWith(".RSA", true) ||
                            name.endsWith(".DSA", true) ||
                            name.endsWith(".EC", true))
                },
            )
        }
        assertRequestInputsClosed(request)
        assertNull(request.keyStorePassword)
        assertNull(request.keyAliasPassword)
    }

    @Test
    fun strictHostMismatchFailsBeforeWorkspaceCreation() {
        val pairedHost = ApkBuilderTemplateMetadata.templateInfo(context)
        val request = createRequest().apply {
            hostVersionName = "${pairedHost.hostVersionName}-mismatch"
            allowRiskyBuild = false
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_FAILED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_BLOCK, observation.result.compatibilityLevel)
        assertTrue(observation.result.warnings.isEmpty())
        assertTrue(
            observation.result.errors.any { error ->
                error.contains("Host versionName mismatch")
            },
        )
        assertNoOutput(observation)
        assertRequestInputsClosed(request)
    }

    @Test
    fun insufficientStorageFailsBeforeWorkspaceCreation() {
        val request = createRequest()

        val observation = execute(
            request = request,
            remoteBuildEnabled = true,
            usableSpaceProvider = { 0L },
        )

        assertBlockedInputFailure(observation, "Insufficient storage space")
        assertFalse(
            "Storage preflight rejection reached a build/sign phase.",
            observation.progress.any { progress ->
                progress.step == ApkBuildProgress.STEP_BUILD ||
                    progress.step == ApkBuildProgress.STEP_SIGN
            },
        )
        assertRequestInputsClosed(request)
        Log.i(
            "RemoteBuildStorageGate",
            "M3_G4_STORAGE_PREFLIGHT_RESULT rejected=1 output_count=0 workspace_entries=0",
        )
    }

    @Test
    fun nativeArchiveWithZeroExpandedSizeFailsBeforeWorkspaceCreation() {
        val request = createRequest().apply {
            attachOpaqueNativeArchive()
            extras?.putLong(
                ApkBuildRequestExtraKeys.NATIVE_LIBRARIES_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
                0L,
            )
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "uncompressed size must be positive")
        assertFalse(
            "Invalid native storage declaration reached a build/sign phase.",
            observation.progress.any { progress ->
                progress.step == ApkBuildProgress.STEP_BUILD ||
                    progress.step == ApkBuildProgress.STEP_SIGN
            },
        )
        assertRequestInputsClosed(request)
    }

    @Test
    fun embeddedNodeMetadataAndDirectiveReturnUnsupportedWithoutBuildOutput() {
        val requests = listOf(
            createRequest(
                projectConfig = createProjectConfig(libs = listOf("  EMBEDDED-NODEJS  ")),
            ),
            createRequest(
                entries = mapOf(
                    "source.js" to "\"nodejs\";\nconsole.log('plugin node gate');\n",
                ),
            ),
        )

        requests.forEach { request ->
            val observation = execute(request, remoteBuildEnabled = true)

            assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
            assertEquals(ApkBuildResult.STATUS_UNSUPPORTED, observation.result.status)
            assertEquals(ApkBuilderTemplateResult.LEVEL_WARN, observation.result.compatibilityLevel)
            assertTrue(observation.result.errors.isEmpty())
            assertTrue(
                observation.result.warnings.any { warning ->
                    warning.contains("external runtime plugin")
                },
            )
            assertFalse(
                "Embedded Node request reached a build/sign phase.",
                observation.progress.any { progress ->
                    progress.step == ApkBuildProgress.STEP_BUILD ||
                        progress.step == ApkBuildProgress.STEP_SIGN
                },
            )
            assertNoOutput(observation)
            assertRequestInputsClosed(request)
            val workspaceRoot = File(context.cacheDir, "remote-apk-build")
            assertTrue(
                "Embedded Node request left a remote build workspace behind.",
                waitUntil { workspaceRoot.listFiles().isNullOrEmpty() },
            )
        }

        Log.i(
            "RemoteEmbeddedNodeGate",
            "M3_G3_PLUGIN_NODE_GATE_RESULT rejected=2 output_count=0 workspace_entries=0",
        )
    }

    @Test
    fun missingRequiredNativeInputReturnsUnsupported() {
        val request = createRequest(
            projectConfig = createProjectConfig(abis = listOf("arm64-v8a")),
        )

        val observation = execute(request, remoteBuildEnabled = true)

        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_UNSUPPORTED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_WARN, observation.result.compatibilityLevel)
        assertTrue(observation.result.errors.isEmpty())
        assertTrue(
            observation.result.warnings.any { warning ->
                warning.contains("did not provide a build input archive")
            },
        )
        assertNoOutput(observation)
        assertRequestInputsClosed(request)
    }

    @Test
    fun olderRequiredProtocolReachesPostNegotiationValidation() {
        check(ApkBuilderTemplateProtocol.REMOTE_BUILD_VERSION > 1) {
            "The lower-protocol compatibility fixture requires a protocol version above 1."
        }
        val request = createRequest(
            projectConfig = createProjectConfig(abis = listOf("arm64-v8a")),
        ).apply {
            requiredProtocolVersion = ApkBuilderTemplateProtocol.REMOTE_BUILD_VERSION - 1
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_UNSUPPORTED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_WARN, observation.result.compatibilityLevel)
        assertTrue(observation.result.errors.isEmpty())
        assertTrue(
            "The lower protocol was not accepted through post-negotiation validation.",
            observation.result.warnings.any { warning ->
                warning.contains("did not provide a build input archive")
            },
        )
        assertFalse(
            "The plugin incorrectly rejected a lower required protocol version.",
            observation.result.warnings.any { warning ->
                warning.contains("requires newer remote build protocol")
            },
        )
        assertNoOutput(observation)
        assertRequestInputsClosed(request)
    }

    @Test
    fun cancellationBeforeStartReturnsCancelled() {
        val request = createRequest()

        val observation = execute(
            request = request,
            remoteBuildEnabled = true,
            cancelBeforeStart = true,
        )

        assertEquals(TerminalEvent.CANCELLED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_CANCELLED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_BLOCK, observation.result.compatibilityLevel)
        assertTrue(observation.result.warnings.isEmpty())
        assertTrue(observation.result.errors.isEmpty())
        assertNoOutput(observation)
        assertRequestInputsClosed(request)
    }

    @Test
    fun closeWhileUnsignedApkIsBeingWrittenEventuallyCleansWorkspace() {
        val request = createRequest()
        val outputCopy = File(fixtureRoot, "close-during-build-output.apk")
        val callback = RecordingCallback(outputCopy)
        val closerStarted = AtomicBoolean(false)
        val closerFinished = CountDownLatch(1)
        val closerFailure = AtomicReference<Throwable?>()
        val workspaceRoot = File(context.cacheDir, "remote-apk-build")
        lateinit var session: RemoteApkBuildSession
        callback.progressObserver = { update ->
            if (
                update.step == ApkBuildProgress.STEP_SIGN &&
                update.title == "Creating unsigned APK" &&
                closerStarted.compareAndSet(false, true)
            ) {
                Thread {
                    runCatching {
                        check(waitUntil {
                            workspaceRoot.walkTopDown().any { file ->
                                file.isFile && file.name.endsWith(".unsigned.apk")
                            }
                        }) { "Unsigned APK was not observed before concurrent close()." }
                        session.cancel()
                        session.close()
                    }.onFailure(closerFailure::set)
                    closerFinished.countDown()
                }.start()
            }
        }
        session = RemoteApkBuildSession(
            context = context,
            request = request,
            callback = callback,
            executor = executor,
            remoteBuildEnabled = true,
        ).also(sessions::add)

        session.start()

        assertTrue("Concurrent close() helper did not finish.", closerFinished.await(30, TimeUnit.SECONDS))
        assertNull("Concurrent close() helper failed.", closerFailure.get())
        assertTrue("Remote build did not reach a terminal callback.", callback.awaitTerminal())
        assertEquals(TerminalEvent.CANCELLED, callback.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_CANCELLED, callback.result?.status)
        assertFalse(outputCopy.exists())
        assertRequestInputsClosed(request)
        assertTrue(
            "Concurrent close() left a remote build workspace behind.",
            waitUntil { workspaceRoot.listFiles().isNullOrEmpty() },
        )
    }

    @Test
    fun disabledPluginBuildReturnsUnsupported() {
        val stagingKey = ByteArray(32) { index -> (index + 1).toByte() }
        val request = createRequest().apply {
            keyStorePassword = "test-store-password"
            keyAliasPassword = "test-alias-password"
            extras!!.putInt(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION, 1)
            extras!!.putByteArray(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY, stagingKey)
            extras!!.putStringArrayList(
                ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS,
                arrayListOf("source.js"),
            )
        }

        val observation = execute(request, remoteBuildEnabled = false)

        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_UNSUPPORTED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_WARN, observation.result.compatibilityLevel)
        assertTrue(observation.result.errors.isEmpty())
        assertTrue(
            observation.result.warnings.any { warning ->
                warning.contains("disabled in this plugin build")
            },
        )
        assertNoOutput(observation)
        assertRequestInputsClosed(request)
        assertTrue("TypeScript staging key was not zeroed.", stagingKey.all { it == 0.toByte() })
        assertFalse(
            request.extras!!.containsKey(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY),
        )
        assertFalse(
            request.extras!!.containsKey(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION),
        )
        assertFalse(
            request.extras!!.containsKey(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS),
        )
        assertNull(request.keyStorePassword)
        assertNull(request.keyAliasPassword)
    }

    @Test
    fun typeScriptStagingEnvelopeAuthenticatesAndIsReEncryptedForTheFinalApk() {
        val cleartext = "console.log('authenticated TypeScript staging');".toByteArray()
        val stagingKey = TypeScriptBuildStagingCipher.generateKey()
        val envelope = TypeScriptBuildStagingCipher.encrypt(cleartext, stagingKey)
        val request = createRequest(
            sourceKind = ApkBuildRequestExtraKeys.SOURCE_KIND_DIRECTORY,
            sourcePath = "project",
            entries = emptyMap(),
            binaryEntries = mapOf("project/main.js" to envelope),
        ).apply {
            putTypeScriptStagingMetadata(stagingKey, "main.js")
        }
        envelope.fill(0)

        val observation = execute(request, remoteBuildEnabled = true)

        assertSuccessfulBuild(observation, "assets/project/main.js")
        assertTypeScriptMetadataCleared(request, stagingKey)
        assertRequestInputsClosed(request)
        ZipFile(observation.outputCopy).use { apk ->
            val packaged = apk.getInputStream(apk.getEntry("assets/project/main.js")).use { it.readBytes() }
            try {
                assertFalse(
                    "The final APK retained the TypeScript transport envelope.",
                    TypeScriptBuildStagingCipher.isEncrypted(packaged),
                )
                assertFalse(
                    "The final APK retained TypeScript staging cleartext.",
                    packaged.contentEquals(cleartext),
                )
            } finally {
                packaged.fill(0)
                cleartext.fill(0)
            }
        }
    }

    @Test
    fun sensitiveInputsDoNotEscapeCallbacksOutputsOrClosedWorkspaces() {
        val probes = linkedMapOf(
            "typescript-cleartext" to SENSITIVE_TYPESCRIPT_CLEARTEXT.toByteArray(Charsets.UTF_8),
            "keystore-password" to SENSITIVE_KEY_STORE_PASSWORD.toByteArray(Charsets.UTF_8),
            "alias-password" to SENSITIVE_KEY_ALIAS_PASSWORD.toByteArray(Charsets.UTF_8),
            "typescript-staging-key" to sensitiveStagingKey(),
        )
        try {
            val successRequest = createSensitiveTypeScriptRequest(
                tamperEnvelope = false,
                useValidKeyStore = true,
            )
            val success = execute(successRequest.request, remoteBuildEnabled = true)
            assertSuccessfulBuild(success, "assets/project/main.js")
            assertSensitiveDataAbsent(success, probes)
            assertSensitiveRequestDataCleared(successRequest)

            val failureRequest = createSensitiveTypeScriptRequest(
                tamperEnvelope = true,
                useValidKeyStore = true,
            )
            val failure = execute(failureRequest.request, remoteBuildEnabled = true)
            assertBlockedInputFailure(failure, "Unable to authenticate TypeScript staging entry")
            assertSensitiveDataAbsent(failure, probes)
            assertSensitiveRequestDataCleared(failureRequest)

            val cancelledRequest = createSensitiveTypeScriptRequest(
                tamperEnvelope = false,
                useValidKeyStore = false,
            )
            val cancelled = execute(
                request = cancelledRequest.request,
                remoteBuildEnabled = true,
                cancelBeforeStart = true,
            )
            assertEquals(TerminalEvent.CANCELLED, cancelled.terminalEvent)
            assertEquals(ApkBuildResult.STATUS_CANCELLED, cancelled.result.status)
            assertNoOutput(cancelled)
            assertSensitiveDataAbsent(cancelled, probes)
            assertSensitiveRequestDataCleared(cancelledRequest)

            val workspaceRoot = File(context.cacheDir, "remote-apk-build")
            assertTrue(
                "Sensitive-data qualification left a remote build workspace behind.",
                waitUntil { workspaceRoot.listFiles().isNullOrEmpty() },
            )
            Log.i(
                SENSITIVE_AUDIT_TAG,
                "M3_G5_SENSITIVE_DATA_RESULT success=1 failure=1 cancel=1 " +
                    "callback_findings=0 output_findings=0 workspace_entries=0 " +
                    "keys_zeroed=3 passwords_cleared=3",
            )
        } finally {
            probes.values.forEach { value -> value.fill(0) }
        }
    }

    @Test
    fun typeScriptStagingTamperedAuthenticationTagFailsClosed() {
        val stagingKey = TypeScriptBuildStagingCipher.generateKey()
        val envelope = TypeScriptBuildStagingCipher.encrypt(
            "console.log('tampered TypeScript staging');".toByteArray(),
            stagingKey,
        ).apply {
            this[lastIndex] = (this[lastIndex].toInt() xor 0x01).toByte()
        }
        val request = createRequest(
            sourceKind = ApkBuildRequestExtraKeys.SOURCE_KIND_DIRECTORY,
            sourcePath = "project",
            entries = emptyMap(),
            binaryEntries = mapOf("project/main.js" to envelope),
        ).apply {
            putTypeScriptStagingMetadata(stagingKey, "main.js")
        }
        envelope.fill(0)

        val observation = execute(request, remoteBuildEnabled = true)

        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_FAILED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_BLOCK, observation.result.compatibilityLevel)
        assertTrue(
            "Unexpected remote build errors: ${observation.result.errors}",
            observation.result.errors.any { error ->
                error.contains("Unable to authenticate TypeScript staging entry: main.js")
            },
        )
        assertNoOutput(observation)
        assertTypeScriptMetadataCleared(request, stagingKey)
        assertRequestInputsClosed(request)
    }

    @Test
    fun typeScriptStagingEnvelopeOmittedFromPathInventoryFailsClosed() {
        val stagingKey = TypeScriptBuildStagingCipher.generateKey()
        val envelope = TypeScriptBuildStagingCipher.encrypt(
            "console.log('unlisted TypeScript staging');".toByteArray(),
            stagingKey,
        )
        val request = createRequest(
            sourceKind = ApkBuildRequestExtraKeys.SOURCE_KIND_DIRECTORY,
            sourcePath = "project",
            entries = emptyMap(),
            binaryEntries = mapOf("project/main.js" to envelope),
        ).apply {
            putTypeScriptStagingMetadata(stagingKey, "different.js")
        }
        envelope.fill(0)

        val observation = execute(request, remoteBuildEnabled = true)

        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_FAILED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_BLOCK, observation.result.compatibilityLevel)
        assertTrue(
            "Unexpected remote build errors: ${observation.result.errors}",
            observation.result.errors.any { error ->
                error.contains("absent from the path inventory: main.js")
            },
        )
        assertNoOutput(observation)
        assertTypeScriptMetadataCleared(request, stagingKey)
        assertRequestInputsClosed(request)
    }

    @Test
    fun typeScriptStagingPathInventoryWithMissingEntryFailsClosed() {
        val stagingKey = TypeScriptBuildStagingCipher.generateKey()
        val envelope = TypeScriptBuildStagingCipher.encrypt(
            "console.log('extra TypeScript staging inventory');".toByteArray(),
            stagingKey,
        )
        val request = createRequest(
            sourceKind = ApkBuildRequestExtraKeys.SOURCE_KIND_DIRECTORY,
            sourcePath = "project",
            entries = emptyMap(),
            binaryEntries = mapOf("project/main.js" to envelope),
        ).apply {
            putTypeScriptStagingMetadata(stagingKey, "main.js", "missing.js")
        }
        envelope.fill(0)

        val observation = execute(request, remoteBuildEnabled = true)

        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_FAILED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_BLOCK, observation.result.compatibilityLevel)
        assertTrue(
            "Unexpected remote build errors: ${observation.result.errors}",
            observation.result.errors.any { error ->
                error.contains("Encrypted TypeScript staging entries are missing: missing.js")
            },
        )
        assertNoOutput(observation)
        assertTypeScriptMetadataCleared(request, stagingKey)
        assertRequestInputsClosed(request)
    }

    @Test
    fun newerRequiredProtocolFails() {
        val request = createRequest().apply {
            requiredProtocolVersion = ApkBuilderTemplateProtocol.REMOTE_BUILD_VERSION + 1
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_FAILED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_BLOCK, observation.result.compatibilityLevel)
        assertTrue(observation.result.warnings.isEmpty())
        assertTrue(
            observation.result.errors.any { error ->
                error.contains("Host requires newer APK build protocol")
            },
        )
        assertNoOutput(observation)
        assertRequestInputsClosed(request)
    }

    @Test
    fun projectArchiveSizeMismatchFailsClosed() {
        val request = createRequest().apply {
            projectArchiveSizeBytes += 1L
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "project archive size mismatch")
        assertRequestInputsClosed(request)
    }

    @Test
    fun projectArchiveSha256MismatchFailsClosed() {
        val request = createRequest().apply {
            projectArchiveSha256 = INVALID_SHA256
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "project archive SHA-256 mismatch")
        assertRequestInputsClosed(request)
    }

    @Test
    fun projectArchiveDeclaredCompressedSizeLimitFailsClosed() {
        val request = createRequest().apply {
            projectArchiveSizeBytes = RemoteZipExtractor.PROJECT_ARCHIVE_LIMITS.maxArchiveBytes + 1L
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "archive size exceeds limit")
        assertRequestInputsClosed(request)
    }

    @Test
    fun nativeArchiveSizeMismatchFailsClosed() {
        val request = createRequest().apply {
            attachOpaqueNativeArchive()
            nativeLibrariesArchiveSizeBytes += 1L
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "native libraries archive size mismatch")
        assertRequestInputsClosed(request)
    }

    @Test
    fun nativeArchiveSha256MismatchFailsClosed() {
        val request = createRequest().apply {
            attachOpaqueNativeArchive()
            nativeLibrariesArchiveSha256 = INVALID_SHA256
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "native libraries archive SHA-256 mismatch")
        assertRequestInputsClosed(request)
    }

    @Test
    fun keyStoreSizeMismatchFailsClosed() {
        val request = createRequest().apply {
            attachOpaqueKeyStore()
            keyStoreSizeBytes += 1L
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "keystore size mismatch")
        assertRequestInputsClosed(request)
    }

    @Test
    fun keyStoreSha256MismatchFailsClosed() {
        val request = createRequest().apply {
            attachOpaqueKeyStore()
            keyStoreSha256 = INVALID_SHA256
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "keystore SHA-256 mismatch")
        assertRequestInputsClosed(request)
    }

    @Test
    fun projectArchiveTraversalEntryFailsClosed() {
        val request = createRequest(
            entries = linkedMapOf(
                "../outside.js" to "console.log('must not escape');",
                "source.js" to "console.log('fixture');",
            ),
        )

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(
            observation,
            "Unsafe project archive entry",
            "Invalid zip entry path",
        )
        assertRequestInputsClosed(request)
    }

    @Test
    fun projectArchivePosixAbsoluteEntryFailsClosed() {
        val request = createRequest(
            entries = linkedMapOf(
                "/outside.js" to "console.log('must not escape');",
                "source.js" to "console.log('fixture');",
            ),
        )

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(
            observation,
            "Unsafe project archive entry path",
            "Invalid zip entry path",
        )
        assertRequestInputsClosed(request)
    }

    @Test
    fun projectArchiveWindowsAbsoluteEntryFailsClosed() {
        val sensitiveEntryName = "C:\\SCRIPT_PLAINTEXT_SENTINEL.js"
        val request = createRequest(
            entries = linkedMapOf(
                sensitiveEntryName to "console.log('must not escape');",
                "source.js" to "console.log('fixture');",
            ),
        )

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "Unsafe project archive entry path")
        assertFalse(
            observation.result.errors.any { error -> error.contains("SCRIPT_PLAINTEXT_SENTINEL") },
        )
        assertRequestInputsClosed(request)
    }

    @Test
    fun projectArchiveEntryCountLimitFailsClosed() {
        val request = createRequest()
        val archive = createZipWithEntryCount(
            "project-entry-count-${UUID.randomUUID()}.zip",
            RemoteZipExtractor.PROJECT_ARCHIVE_LIMITS.maxEntries + 1,
        )
        request.replaceProjectArchive(archive)

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "entry count exceeds limit")
        assertRequestInputsClosed(request)
    }

    @Test
    fun projectArchiveCompressionRatioLimitFailsClosed() {
        val highExpansionPayload = ByteArray(
            (RemoteZipExtractor.PROJECT_ARCHIVE_LIMITS.compressionRatioMinimumBytes * 2L).toInt(),
        )
        val request = createRequest(
            entries = emptyMap(),
            binaryEntries = mapOf("source.js" to highExpansionPayload),
        )

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "compression ratio exceeds limit")
        assertRequestInputsClosed(request)
    }

    @Test
    fun nativeArchiveUnexpectedTopLevelFailsClosed() {
        val request = createRequest().apply {
            attachNativeBuildInputArchive(
                mapOf("dex/classes.dex" to byteArrayOf(0x64, 0x65, 0x78)),
            )
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "Unexpected remote build input archive entry")
        assertRequestInputsClosed(request)
    }

    @Test
    fun projectSourceTraversalPathFailsClosed() {
        val request = createRequest(sourcePath = "../source.js")

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "Unsafe project source path")
        assertRequestInputsClosed(request)
    }

    @Test
    fun malformedProjectArchiveFailsClosed() {
        val malformedArchive = File(fixtureRoot, "malformed-${UUID.randomUUID()}.zip").also { file ->
            FileOutputStream(file).use { output ->
                output.write("this is not a ZIP archive".toByteArray(Charsets.UTF_8))
            }
        }
        val request = createRequest().apply {
            replaceProjectArchive(
                malformedArchive,
                uncompressedSizeBytes = malformedArchive.length(),
            )
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(
            observation,
            "Project source file does not exist in archive",
            "zip",
            "archive",
        )
        assertRequestInputsClosed(request)
    }

    @Test
    fun malformedProjectConfigJsonFailsClosed() {
        val request = createRequest(projectConfig = "{")

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation)
        assertTrue(
            "Malformed JSON did not expose a deterministic parse failure: ${observation.result.errors}",
            observation.result.errors.any { error ->
                error.contains("input", ignoreCase = true) ||
                    error.contains("JSON", ignoreCase = true) ||
                    error.contains("character", ignoreCase = true)
            },
        )
        assertRequestInputsClosed(request)
    }

    @Test
    fun deterministicParcelableRequestCorpusFailsClosed() {
        val cases = listOf(
            RequestFuzzCase("negative-host-version-code") { hostVersionCode = -1L },
            RequestFuzzCase("zero-protocol-version") { requiredProtocolVersion = 0 },
            RequestFuzzCase("missing-project-fd") {
                projectArchiveFd?.close()
                projectArchiveFd = null
            },
            RequestFuzzCase("negative-project-size") { projectArchiveSizeBytes = -1L },
            RequestFuzzCase("oversized-project-size") {
                projectArchiveSizeBytes = RemoteZipExtractor.PROJECT_ARCHIVE_LIMITS.maxArchiveBytes + 1L
            },
            RequestFuzzCase("malformed-project-digest") { projectArchiveSha256 = "not-a-sha256" },
            RequestFuzzCase("negative-native-size") { nativeLibrariesArchiveSizeBytes = -1L },
            RequestFuzzCase("native-metadata-without-fd") { nativeLibrariesArchiveSizeBytes = 1L },
            RequestFuzzCase("keystore-metadata-without-fd") {
                keyStorePassword = FUZZ_SECRET_SENTINEL
            },
            RequestFuzzCase("keystore-missing-password") {
                attachOpaqueKeyStore()
                keyStorePassword = null
            },
            RequestFuzzCase("keystore-overlong-password") {
                attachOpaqueKeyStore()
                keyStorePassword = "p".repeat(RemoteApkBuildRequestPolicy.MAX_PASSWORD_CHARACTERS + 1)
            },
            RequestFuzzCase("keystore-missing-alias") {
                attachOpaqueKeyStore()
                keyAlias = null
            },
            RequestFuzzCase("keystore-overlong-alias") {
                attachOpaqueKeyStore()
                keyAlias = "a".repeat(RemoteApkBuildRequestPolicy.MAX_ALIAS_UTF8_BYTES + 1)
            },
            RequestFuzzCase("host-name-unpaired-surrogate") { hostVersionName = "version-\uD800" },
            RequestFuzzCase("output-name-control-character") {
                outputFileName = "safe\u0000$FUZZ_SECRET_SENTINEL.apk"
            },
            RequestFuzzCase("output-path-overlong") {
                outputFileName = "o".repeat(RemoteApkBuildRequestPolicy.MAX_OUTPUT_PATH_UTF8_BYTES + 1)
            },
            RequestFuzzCase("missing-extras") { extras = null },
            RequestFuzzCase("archive-version-wrong-type") {
                extras!!.putLong(ApkBuildRequestExtraKeys.ARCHIVE_FORMAT_VERSION, 1L)
            },
            RequestFuzzCase("archive-version-unsupported") {
                extras!!.putInt(ApkBuildRequestExtraKeys.ARCHIVE_FORMAT_VERSION, 2)
            },
            RequestFuzzCase("source-kind-wrong-type") {
                extras!!.putInt(ApkBuildRequestExtraKeys.SOURCE_KIND, 1)
            },
            RequestFuzzCase("source-kind-unsupported") {
                extras!!.putString(ApkBuildRequestExtraKeys.SOURCE_KIND, "unsupported")
            },
            RequestFuzzCase("source-path-wrong-type") {
                extras!!.putInt(ApkBuildRequestExtraKeys.SOURCE_PATH, 1)
            },
            RequestFuzzCase("source-path-empty-segment") {
                extras!!.putString(ApkBuildRequestExtraKeys.SOURCE_PATH, "project//main.js")
            },
            RequestFuzzCase("source-path-overlong-segment") {
                extras!!.putString(
                    ApkBuildRequestExtraKeys.SOURCE_PATH,
                    "s".repeat(RemoteZipExtractor.MAX_SEGMENT_UTF8_BYTES + 1),
                )
            },
            RequestFuzzCase("source-path-excessive-depth") {
                extras!!.putString(
                    ApkBuildRequestExtraKeys.SOURCE_PATH,
                    List(RemoteZipExtractor.MAX_PATH_SEGMENTS + 1) { "s" }.joinToString("/"),
                )
            },
            RequestFuzzCase("source-path-c1-control") {
                extras!!.putString(ApkBuildRequestExtraKeys.SOURCE_PATH, "project/\u0085main.js")
            },
            RequestFuzzCase("source-path-unpaired-surrogate") {
                extras!!.putString(ApkBuildRequestExtraKeys.SOURCE_PATH, "project/\uD800main.js")
            },
            RequestFuzzCase("icon-path-wrong-type") {
                extras!!.putInt(ApkBuildRequestExtraKeys.ICON_PATH, 1)
            },
            RequestFuzzCase("source-root-wrong-type") {
                extras!!.putInt(ApkBuildRequestExtraKeys.SOURCE_ROOT_PATH, 1)
            },
            RequestFuzzCase("host-output-name-wrong-type") {
                extras!!.putInt(ApkBuildRequestExtraKeys.HOST_OUTPUT_FILE_NAME, 1)
            },
            RequestFuzzCase("unsupported-extra-key") {
                extras!!.putString("unsupportedFuzzKey", FUZZ_SECRET_SENTINEL)
            },
            RequestFuzzCase("extras-key-count") {
                repeat(RemoteApkBuildRequestPolicy.MAX_EXTRAS_KEYS) { index ->
                    extras!!.putInt("fuzzExtra$index", index)
                }
            },
            RequestFuzzCase("incomplete-typescript-metadata") {
                extras!!.putInt(
                    ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION,
                    TypeScriptBuildStagingCipher.VERSION,
                )
            },
            RequestFuzzCase("typescript-metadata-on-v2") {
                requiredProtocolVersion = 2
                putTypeScriptStagingMetadata(ByteArray(32) { 1 }, "main.js")
            },
            RequestFuzzCase("typescript-version-wrong-type") {
                putTypeScriptStagingMetadata(ByteArray(32) { 2 }, "main.js")
                extras!!.putString(
                    ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION,
                    "1",
                )
            },
            RequestFuzzCase("typescript-key-wrong-length") {
                putTypeScriptStagingMetadata(ByteArray(31) { 3 }, "main.js")
            },
            RequestFuzzCase("typescript-paths-wrong-type") {
                putTypeScriptStagingMetadata(ByteArray(32) { 4 }, "main.js")
                extras!!.putString(
                    ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS,
                    "main.js",
                )
            },
            RequestFuzzCase("typescript-path-count") {
                putTypeScriptStagingMetadata(ByteArray(32) { 5 }, "main.js")
                extras!!.putStringArrayList(
                    ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS,
                    ArrayList(
                        List(RemoteApkBuildRequestPolicy.MAX_TYPESCRIPT_PATHS + 1) { index ->
                            "entry-$index.js"
                        },
                    ),
                )
            },
            RequestFuzzCase("typescript-path-duplicate") {
                putTypeScriptStagingMetadata(ByteArray(32) { 6 }, "main.js", "main.js")
            },
            RequestFuzzCase("typescript-path-extension") {
                putTypeScriptStagingMetadata(ByteArray(32) { 7 }, "main.txt")
            },
        )

        cases.forEach { case ->
            assertRejectedFuzzCase(case.name, createRequest().apply(case.mutate))
        }
    }

    @Test
    fun deterministicJsonAndBinaryEditorCorpusFailsClosed() {
        val cases = listOf(
            JsonFuzzCase("top-level-array", "[]"),
            JsonFuzzCase("top-level-null", "null"),
            JsonFuzzCase("trailing-document", "{} true"),
            JsonFuzzCase("adjacent-root-objects", "{}{}"),
            JsonFuzzCase(
                "excessive-nesting",
                deeplyNestedProjectConfig(RemoteProjectConfigParser.MAX_JSON_NESTING_DEPTH),
            ),
            JsonFuzzCase("oversized-json", oversizedProjectConfig()),
            JsonFuzzCase("name-number", projectConfigWith { put("name", 7) }),
            JsonFuzzCase("name-blank", projectConfigWith { put("name", " ") }),
            JsonFuzzCase(
                "name-overlong-utf8",
                projectConfigWith { put("name", "é".repeat(RemoteProjectConfigParser.MAX_NAME_UTF8_BYTES / 2 + 1)) },
            ),
            JsonFuzzCase("name-control-character", projectConfigWith { put("name", "bad\u0000name") }),
            JsonFuzzCase("name-unpaired-surrogate", projectConfigWith { put("name", "bad-\uD800") }),
            JsonFuzzCase("package-object", projectConfigWith { put("packageName", JSONObject()) }),
            JsonFuzzCase("package-single-segment", projectConfigWith { put("packageName", "single") }),
            JsonFuzzCase(
                "package-overlong-for-arsc",
                projectConfigWith {
                    put(
                        "packageName",
                        "a." + "b".repeat(RemoteProjectConfigParser.MAX_PACKAGE_UTF8_BYTES - 1),
                    )
                },
            ),
            JsonFuzzCase("version-name-boolean", projectConfigWith { put("versionName", true) }),
            JsonFuzzCase(
                "version-name-overlong",
                projectConfigWith {
                    put("versionName", "v".repeat(RemoteProjectConfigParser.MAX_VERSION_NAME_UTF8_BYTES + 1))
                },
            ),
            JsonFuzzCase("version-code-string", projectConfigWith { put("versionCode", "1") }),
            JsonFuzzCase("version-code-double", projectConfigWithRawValue("versionCode", "1.0")),
            JsonFuzzCase("version-code-zero", projectConfigWith { put("versionCode", 0) }),
            JsonFuzzCase(
                "main-traversal",
                projectConfigWith { put("main", "../main.js") },
                directorySource = true,
            ),
            JsonFuzzCase(
                "main-windows-absolute",
                projectConfigWith { put("main", "C:\\main.js") },
                directorySource = true,
            ),
            JsonFuzzCase("abis-string", projectConfigWith { put("abis", "x86") }),
            JsonFuzzCase("abis-unsupported", projectConfigWith { put("abis", JSONArray(listOf("mips"))) }),
            JsonFuzzCase(
                "abis-duplicate",
                projectConfigWith { put("abis", JSONArray(listOf("x86", "x86"))) },
            ),
            JsonFuzzCase("libs-non-string", projectConfigWith { put("libs", JSONArray().put(1)) }),
            JsonFuzzCase(
                "libs-entry-count",
                projectConfigWith {
                    put(
                        "libs",
                        JSONArray(List(RemoteProjectConfigParser.MAX_ARRAY_ENTRIES + 1) { index -> "lib-$index" }),
                    )
                },
            ),
            JsonFuzzCase(
                "permissions-overlong-entry",
                projectConfigWith {
                    put(
                        "permissions",
                        JSONArray().put("p".repeat(RemoteProjectConfigParser.MAX_ARRAY_VALUE_UTF8_BYTES + 1)),
                    )
                },
            ),
            JsonFuzzCase("signature-unsupported", projectConfigWith { put("signatureScheme", "V1 + V5") }),
            JsonFuzzCase("launch-config-string", projectConfigWith { put("launchConfig", "visible") }),
            JsonFuzzCase(
                "splash-visible-string",
                projectConfigWith { put("launchConfig", JSONObject().put("splashVisible", "true")) },
            ),
            JsonFuzzCase("build-array", projectConfigWith { put("build", JSONArray()) }),
            JsonFuzzCase(
                "build-number-overflow",
                projectConfigWith { put("build", JSONObject().put("number", Long.MAX_VALUE)) },
            ),
        )

        cases.forEach { case ->
            val request = if (case.directorySource) {
                createRequest(
                    sourceKind = ApkBuildRequestExtraKeys.SOURCE_KIND_DIRECTORY,
                    sourcePath = "project",
                    entries = mapOf("project/main.js" to "console.log('fixture');"),
                    projectConfig = case.projectConfig,
                )
            } else {
                createRequest(projectConfig = case.projectConfig)
            }
            assertRejectedFuzzCase(case.name, request)
        }
    }

    @Test
    fun keyStoreActualByteLimitFailsClosed() {
        val oversizedKeyStore = File(fixtureRoot, "oversized-${UUID.randomUUID()}.bin")
        RandomAccessFile(oversizedKeyStore, "rw").use { file ->
            file.setLength(RemoteApkBuildRequestPolicy.MAX_KEYSTORE_BYTES + 1L)
        }
        val request = createRequest().apply {
            keyStoreFd = ParcelFileDescriptor.open(oversizedKeyStore, ParcelFileDescriptor.MODE_READ_ONLY)
            keyStoreSizeBytes = RemoteApkBuildRequestPolicy.MAX_KEYSTORE_BYTES
            keyStoreSha256 = null
            keyStorePassword = "unused"
            keyAlias = "unused"
            keyAliasPassword = "unused"
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "keystore size exceeds limit")
        assertFalse(
            "Oversized keystore unexpectedly reached the APK build phase.",
            observation.progress.any { progress -> progress.step == ApkBuildProgress.STEP_BUILD },
        )
        assertRequestInputsClosed(request)
    }

    @Test
    fun malformedIconFailsClosed() {
        val iconBytes = "not an image".toByteArray(Charsets.UTF_8)
        val request = createRequest(binaryEntries = mapOf("icon.bin" to iconBytes)).apply {
            extras!!.putString(ApkBuildRequestExtraKeys.ICON_PATH, "icon.bin")
        }
        iconBytes.fill(0)

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "icon dimensions", "icon could not be decoded")
        assertRequestInputsClosed(request)
    }

    @Test
    fun iconPixelLimitFailsClosedBeforeBitmapAllocation() {
        val side = 2_049
        val iconBytes = createPngWithDeclaredDimensions(side, side)
        val request = createRequest(binaryEntries = mapOf("icon.png" to iconBytes)).apply {
            extras!!.putString(ApkBuildRequestExtraKeys.ICON_PATH, "icon.png")
        }
        iconBytes.fill(0)

        val observation = execute(request, remoteBuildEnabled = true)

        assertBlockedInputFailure(observation, "icon dimensions")
        assertRequestInputsClosed(request)
    }

    @Test
    fun iconCompressedFileSizeLimitRejectsBeforeMetadataDecode() {
        val oversizedIcon = File(fixtureRoot, "oversized-icon-${UUID.randomUUID()}.png")
        RandomAccessFile(oversizedIcon, "rw").use { file ->
            file.setLength(RemoteApkIconPolicy.MAX_FILE_BYTES + 1L)
        }

        val error = try {
            RemoteApkIconPolicy.decode(oversizedIcon).also(Bitmap::recycle)
            null
        } catch (error: java.io.IOException) {
            error
        }

        assertTrue("Expected oversized icon rejection", error is java.io.IOException)
        assertTrue(error?.message.orEmpty().contains("size limit", ignoreCase = true))
    }

    @Test
    fun manifestAndArscBoundaryValuesComplete() {
        val boundaryAppName = "n".repeat(RemoteProjectConfigParser.MAX_NAME_UTF8_BYTES)
        val boundaryPackageName = "a." +
            "b".repeat(RemoteProjectConfigParser.MAX_PACKAGE_UTF8_BYTES - 2)
        val boundaryVersionName = "v".repeat(RemoteProjectConfigParser.MAX_VERSION_NAME_UTF8_BYTES)
        val request = createRequest(
            projectConfig = projectConfigWith {
                put("name", boundaryAppName)
                put("packageName", boundaryPackageName)
                put("versionName", boundaryVersionName)
            },
        ).apply {
            outputFileName = "o".repeat(RemoteApkBuildRequestPolicy.MAX_OUTPUT_FILE_NAME_UTF8_BYTES)
        }

        val observation = execute(request, remoteBuildEnabled = true)

        assertSuccessfulBuildWithIdentity(
            observation = observation,
            expectedAppName = boundaryAppName,
            expectedPackageName = boundaryPackageName,
            expectedVersionName = boundaryVersionName,
            expectedVersionCode = TEST_VERSION_CODE,
            expectedEntries = arrayOf("assets/project/main.js"),
        )
        assertEquals(
            "o".repeat(RemoteApkBuildRequestPolicy.MAX_OUTPUT_FILE_NAME_UTF8_BYTES) + ".apk",
            observation.result.outputFileName,
        )
        assertRequestInputsClosed(request)
    }

    private fun createRequest(
        sourceKind: String = ApkBuildRequestExtraKeys.SOURCE_KIND_FILE,
        sourcePath: String = "source.js",
        entries: Map<String, String> = mapOf("source.js" to "console.log('fixture');"),
        binaryEntries: Map<String, ByteArray>? = null,
        projectConfig: String = createProjectConfig(),
    ): ApkBuildRequest {
        val pairedHost = ApkBuilderTemplateMetadata.templateInfo(context)
        val archiveEntries = entries
            .mapValues { (_, content) -> content.toByteArray(Charsets.UTF_8) }
            .toMutableMap()
            .apply {
                binaryEntries?.let(::putAll)
            }
        val archive = createZip("project-${UUID.randomUUID()}.zip", archiveEntries)
        return ApkBuildRequest(
            hostPackageName = pairedHost.hostPackageName,
            hostVersionName = pairedHost.hostVersionName,
            hostVersionCode = pairedHost.hostVersionCode,
            requiredProtocolVersion = ApkBuilderTemplateProtocol.REMOTE_BUILD_VERSION,
            projectArchiveFd = ParcelFileDescriptor.open(
                archive,
                ParcelFileDescriptor.MODE_READ_ONLY,
            ),
            projectArchiveSizeBytes = archive.length(),
            projectArchiveSha256 = sha256(archive),
            projectConfigJson = projectConfig,
            outputFileName = "remote-build-${UUID.randomUUID()}.apk",
            allowRiskyBuild = false,
            extras = Bundle().apply {
                putInt(ApkBuildRequestExtraKeys.ARCHIVE_FORMAT_VERSION, 1)
                putString(ApkBuildRequestExtraKeys.SOURCE_KIND, sourceKind)
                putString(ApkBuildRequestExtraKeys.SOURCE_PATH, sourcePath)
                putLong(
                    ApkBuildRequestExtraKeys.PROJECT_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
                    archiveEntries.values.sumOf { bytes -> bytes.size.toLong() },
                )
                putLong(
                    ApkBuildRequestExtraKeys.NATIVE_LIBRARIES_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
                    0L,
                )
            },
        )
    }

    private fun createSensitiveTypeScriptRequest(
        tamperEnvelope: Boolean,
        useValidKeyStore: Boolean,
    ): SensitiveRequest {
        val cleartext = "console.log('$SENSITIVE_TYPESCRIPT_CLEARTEXT');".toByteArray(Charsets.UTF_8)
        val stagingKey = sensitiveStagingKey()
        val envelope = try {
            TypeScriptBuildStagingCipher.encrypt(cleartext, stagingKey).apply {
                if (tamperEnvelope) {
                    this[lastIndex] = (this[lastIndex].toInt() xor 0x01).toByte()
                }
            }
        } finally {
            cleartext.fill(0)
        }
        return try {
            val request = createRequest(
                sourceKind = ApkBuildRequestExtraKeys.SOURCE_KIND_DIRECTORY,
                sourcePath = "project",
                entries = emptyMap(),
                binaryEntries = mapOf("project/main.js" to envelope),
            ).apply {
                if (useValidKeyStore) {
                    attachBundledKeyStoreAsCustomInput()
                } else {
                    keyStorePassword = SENSITIVE_KEY_STORE_PASSWORD
                    keyAliasPassword = SENSITIVE_KEY_ALIAS_PASSWORD
                }
                putTypeScriptStagingMetadata(stagingKey, "main.js")
            }
            SensitiveRequest(request, stagingKey)
        } finally {
            envelope.fill(0)
        }
    }

    private fun assertSensitiveRequestDataCleared(sensitiveRequest: SensitiveRequest) {
        val request = sensitiveRequest.request
        assertNull(request.keyStorePassword)
        assertNull(request.keyAliasPassword)
        assertFalse(
            request.extras!!.containsKey(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY),
        )
        assertFalse(
            request.extras!!.containsKey(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION),
        )
        assertFalse(
            request.extras!!.containsKey(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS),
        )
        assertTrue(
            "TypeScript staging key was not zeroed.",
            sensitiveRequest.stagingKey.all { it == 0.toByte() },
        )
    }

    @Suppress("DEPRECATION")
    private fun assertSensitiveDataAbsent(
        observation: Observation,
        probes: Map<String, ByteArray>,
    ) {
        val matcher = SensitiveProbeMatcher(probes)
        val textSurfaces = mutableListOf<ByteArray>()
        val binarySurfaces = mutableListOf<ByteArray>()
        observation.result.warnings.forEach { textSurfaces += it.toByteArray(Charsets.UTF_8) }
        observation.result.errors.forEach { textSurfaces += it.toByteArray(Charsets.UTF_8) }
        listOfNotNull(
            observation.result.outputFileName,
            observation.result.outputSha256,
            observation.result.updatedProjectConfigJson,
        ).forEach { textSurfaces += it.toByteArray(Charsets.UTF_8) }
        observation.progress.forEach { progress ->
            progress.title?.let { textSurfaces += it.toByteArray(Charsets.UTF_8) }
            progress.detail?.let { textSurfaces += it.toByteArray(Charsets.UTF_8) }
        }
        observation.result.extras?.let { extras ->
            extras.keySet().forEach { key ->
                textSurfaces += key.toByteArray(Charsets.UTF_8)
                when (val value = extras.get(key)) {
                    is ByteArray -> binarySurfaces += value
                    is CharSequence -> textSurfaces += value.toString().toByteArray(Charsets.UTF_8)
                    is ArrayList<*> -> value.filterIsInstance<CharSequence>().forEach { item ->
                        textSurfaces += item.toString().toByteArray(Charsets.UTF_8)
                    }
                    null -> Unit
                    else -> textSurfaces += value.toString().toByteArray(Charsets.UTF_8)
                }
            }
        }
        textSurfaces.forEach { surface ->
            matcher.assertAbsent(ByteArrayInputStream(surface), "callback surface")
            surface.fill(0)
        }
        binarySurfaces.forEach { surface ->
            matcher.assertAbsent(ByteArrayInputStream(surface), "callback surface")
        }

        if (observation.outputCopy.isFile) {
            FileInputStream(observation.outputCopy).use { input ->
                matcher.assertAbsent(input, "raw output APK")
            }
            ZipFile(observation.outputCopy).use { archive ->
                val entries = archive.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (!entry.isDirectory) {
                        archive.getInputStream(entry).use { input ->
                            matcher.assertAbsent(input, "output APK entry")
                        }
                    }
                }
            }
        }
    }

    private fun sensitiveStagingKey(): ByteArray = SENSITIVE_STAGING_KEY_HEX
        .chunked(2)
        .map { value -> value.toInt(16).toByte() }
        .toByteArray()

    private fun createProjectConfig(
        abis: List<String>? = null,
        libs: List<String>? = null,
        permissions: List<String>? = null,
        signatureScheme: String = "V1 + V2",
    ): String {
        return JSONObject().apply {
            put("name", TEST_APP_NAME)
            put("packageName", TEST_PACKAGE_NAME)
            put("versionName", TEST_VERSION_NAME)
            put("versionCode", TEST_VERSION_CODE)
            put("main", "main.js")
            put("signatureScheme", signatureScheme)
            put(
                "build",
                JSONObject()
                    .put("id", "instrumented-test")
                    .put("number", 1)
                    .put("time", 1L),
            )
            abis?.let { values -> put("abis", JSONArray(values)) }
            libs?.let { values -> put("libs", JSONArray(values)) }
            permissions?.let { values -> put("permissions", JSONArray(values)) }
        }.toString()
    }

    private fun projectConfigWith(mutator: JSONObject.() -> Unit): String {
        return JSONObject(createProjectConfig()).apply(mutator).toString()
    }

    private fun projectConfigWithRawValue(key: String, rawValue: String): String {
        val marker = "__M3_G5_RAW_JSON_VALUE__"
        return projectConfigWith { put(key, marker) }
            .replace("\"$marker\"", rawValue)
    }

    private fun deeplyNestedProjectConfig(arrayDepth: Int): String {
        var nested: Any = "leaf"
        repeat(arrayDepth) {
            nested = JSONArray().put(nested)
        }
        return JSONObject(createProjectConfig())
            .put("fuzzNested", nested)
            .toString()
    }

    private fun oversizedProjectConfig(): String {
        return "{\"padding\":\"" +
            "a".repeat(RemoteProjectConfigParser.MAX_JSON_UTF8_BYTES) +
            "\"}"
    }

    private fun ApkBuildRequest.attachBundledKeyStoreAsCustomInput() {
        val keyStore = File(fixtureRoot, "custom-${UUID.randomUUID()}.bks")
        context.assets.open(ApkBuilderTemplateMetadata.DEFAULT_KEY_STORE_ASSET).use { input ->
            FileOutputStream(keyStore).use(input::copyTo)
        }
        keyStoreFd = ParcelFileDescriptor.open(keyStore, ParcelFileDescriptor.MODE_READ_ONLY)
        keyStoreSizeBytes = keyStore.length()
        keyStoreSha256 = sha256(keyStore)
        keyStorePassword = DEFAULT_KEY_STORE_PASSWORD
        keyAlias = DEFAULT_KEY_ALIAS
        keyAliasPassword = DEFAULT_KEY_ALIAS_PASSWORD
    }

    private fun ApkBuildRequest.attachOpaqueKeyStore() {
        val keyStore = File(fixtureRoot, "opaque-${UUID.randomUUID()}.bin")
        FileOutputStream(keyStore).use { output ->
            output.write("negative keystore fixture".toByteArray(Charsets.UTF_8))
        }
        keyStoreFd = ParcelFileDescriptor.open(keyStore, ParcelFileDescriptor.MODE_READ_ONLY)
        keyStoreSizeBytes = keyStore.length()
        keyStoreSha256 = sha256(keyStore)
        keyStorePassword = "unused"
        keyAlias = "unused"
        keyAliasPassword = "unused"
    }

    private fun ApkBuildRequest.attachNativeLibrariesArchive(
        abi: String,
        libraryNames: List<String>,
    ) {
        val expectedEntries = libraryNames.mapTo(linkedSetOf()) { library ->
            "lib/$abi/$library"
        }
        val foundEntries = linkedSetOf<String>()
        val archive = File(fixtureRoot, "native-${UUID.randomUUID()}.zip")
        ZipInputStream(
            context.assets.open(ApkBuilderTemplateMetadata.TEMPLATE_APK_ASSET).buffered(),
        ).use { template ->
            ZipOutputStream(FileOutputStream(archive).buffered()).use { output ->
                while (true) {
                    val entry = template.nextEntry ?: break
                    if (!entry.isDirectory && entry.name in expectedEntries) {
                        output.putNextEntry(ZipEntry(entry.name))
                        template.copyTo(output)
                        output.closeEntry()
                        foundEntries += entry.name
                    }
                    template.closeEntry()
                }
            }
        }
        assertEquals("Runtime Kit is missing native test fixtures.", expectedEntries, foundEntries)
        nativeLibrariesArchiveFd = ParcelFileDescriptor.open(
            archive,
            ParcelFileDescriptor.MODE_READ_ONLY,
        )
        nativeLibrariesArchiveSizeBytes = archive.length()
        nativeLibrariesArchiveSha256 = sha256(archive)
        extras?.putLong(
            ApkBuildRequestExtraKeys.NATIVE_LIBRARIES_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
            zipUncompressedSizeBytes(archive),
        )
    }

    private fun ApkBuildRequest.attachOpaqueNativeArchive() {
        attachNativeBuildInputArchive(
            mapOf("lib/x86/libnegative.so" to byteArrayOf(1, 2, 3, 4)),
        )
    }

    private fun ApkBuildRequest.attachNativeBuildInputArchive(entries: Map<String, ByteArray>) {
        val archive = createZip("native-negative-${UUID.randomUUID()}.zip", entries)
        nativeLibrariesArchiveFd = ParcelFileDescriptor.open(
            archive,
            ParcelFileDescriptor.MODE_READ_ONLY,
        )
        nativeLibrariesArchiveSizeBytes = archive.length()
        nativeLibrariesArchiveSha256 = sha256(archive)
        extras?.putLong(
            ApkBuildRequestExtraKeys.NATIVE_LIBRARIES_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
            zipUncompressedSizeBytes(archive),
        )
    }

    private fun ApkBuildRequest.replaceProjectArchive(
        archive: File,
        uncompressedSizeBytes: Long = zipUncompressedSizeBytes(archive),
    ) {
        projectArchiveFd?.close()
        projectArchiveFd = ParcelFileDescriptor.open(archive, ParcelFileDescriptor.MODE_READ_ONLY)
        projectArchiveSizeBytes = archive.length()
        projectArchiveSha256 = sha256(archive)
        extras?.putLong(
            ApkBuildRequestExtraKeys.PROJECT_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
            uncompressedSizeBytes,
        )
    }

    private fun createSolidPng(width: Int, height: Int, color: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return try {
            bitmap.eraseColor(color)
            ByteArrayOutputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Failed to encode test icon."
                }
                output.toByteArray()
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun createPngWithDeclaredDimensions(width: Int, height: Int): ByteArray {
        require(width > 0 && height > 0)
        val png = createSolidPng(1, 1, 0xff123456.toInt())
        check(png.size >= 33 && String(png, 12, 4, Charsets.US_ASCII) == "IHDR") {
            "Test PNG does not contain an IHDR chunk at the expected offset."
        }
        writeBigEndianInt(png, 16, width)
        writeBigEndianInt(png, 20, height)
        val checksum = CRC32().apply { update(png, 12, 17) }.value.toInt()
        writeBigEndianInt(png, 29, checksum)
        return png
    }

    private fun writeBigEndianInt(target: ByteArray, offset: Int, value: Int) {
        target[offset] = (value ushr 24).toByte()
        target[offset + 1] = (value ushr 16).toByte()
        target[offset + 2] = (value ushr 8).toByte()
        target[offset + 3] = value.toByte()
    }

    private fun createZip(name: String, entries: Map<String, ByteArray>): File {
        val archive = File(fixtureRoot, name)
        ZipOutputStream(FileOutputStream(archive).buffered()).use { zip ->
            entries.forEach { (path, content) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(content)
                zip.closeEntry()
            }
        }
        return archive
    }

    private fun zipUncompressedSizeBytes(archive: File): Long {
        return ZipFile(archive).use { zip ->
            zip.entries().asSequence()
                .filterNot { entry -> entry.isDirectory }
                .sumOf { entry -> entry.size }
        }
    }

    private fun createZipWithEntryCount(name: String, entryCount: Int): File {
        require(entryCount > 0)
        val archive = File(fixtureRoot, name)
        ZipOutputStream(FileOutputStream(archive).buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("source.js"))
            zip.write("console.log('fixture');".toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            repeat(entryCount - 1) { index ->
                zip.putNextEntry(ZipEntry("entries/fixture-${index.toString().padStart(5, '0')}.js"))
                zip.closeEntry()
            }
        }
        return archive
    }

    private fun ApkBuildRequest.putTypeScriptStagingMetadata(
        stagingKey: ByteArray,
        vararg encryptedPaths: String,
    ) {
        extras!!.putInt(
            ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION,
            TypeScriptBuildStagingCipher.VERSION,
        )
        extras!!.putByteArray(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY, stagingKey)
        extras!!.putStringArrayList(
            ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS,
            ArrayList(encryptedPaths.asList()),
        )
    }

    private fun assertTypeScriptMetadataCleared(
        request: ApkBuildRequest,
        stagingKey: ByteArray,
    ) {
        assertTrue("TypeScript staging key was not zeroed.", stagingKey.all { it == 0.toByte() })
        assertFalse(
            request.extras!!.containsKey(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY),
        )
        assertFalse(
            request.extras!!.containsKey(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION),
        )
        assertFalse(
            request.extras!!.containsKey(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS),
        )
    }

    private fun execute(
        request: ApkBuildRequest,
        remoteBuildEnabled: Boolean,
        cancelBeforeStart: Boolean = false,
        usableSpaceProvider: (File) -> Long = { directory -> directory.usableSpace },
    ): Observation {
        val outputCopy = File(fixtureRoot, "callback-output-${UUID.randomUUID()}.apk")
        val callback = RecordingCallback(outputCopy)
        val session = RemoteApkBuildSession(
            context = context,
            request = request,
            callback = callback,
            executor = executor,
            remoteBuildEnabled = remoteBuildEnabled,
            usableSpaceProvider = usableSpaceProvider,
        ).also(sessions::add)

        if (cancelBeforeStart) {
            session.cancel()
        }
        session.start()

        assertTrue("Remote build did not reach a terminal callback.", callback.awaitTerminal())
        assertNull("Terminal callback failed while copying its result.", callback.callbackFailure)
        assertTrue(
            "Remote build did not reach STEP_FINISH.",
            waitUntil { session.progress.step == ApkBuildProgress.STEP_FINISH },
        )

        return Observation(
            session = session,
            terminalEvent = requireNotNull(callback.terminalEvent),
            result = requireNotNull(callback.result),
            progress = callback.progress.toList(),
            outputCopy = outputCopy,
        )
    }

    private fun assertRejectedFuzzCase(name: String, request: ApkBuildRequest) {
        val parceledRequest = try {
            parcelRoundTrip(request)
        } catch (error: Throwable) {
            closeDetachedRequest(request)
            throw AssertionError("Parcelable round-trip failed for fuzz case $name", error)
        }
        try {
            val observation = execute(parceledRequest, remoteBuildEnabled = true)
            try {
                assertBlockedInputFailure(observation)
            } catch (error: AssertionError) {
                throw AssertionError("Fuzz case $name did not fail closed.", error)
            }
            assertRequestInputsClosed(parceledRequest)
            val callbackText = buildList {
                addAll(observation.result.warnings)
                addAll(observation.result.errors)
                observation.progress.forEach { progress ->
                    progress.title?.let(::add)
                    progress.detail?.let(::add)
                }
            }
            assertFalse(
                "Fuzz case $name reflected a protected sentinel into callback surfaces.",
                callbackText.any { value -> value.contains(FUZZ_SECRET_SENTINEL) },
            )
            Log.i(
                FUZZ_AUDIT_TAG,
                "case=$name terminal=${observation.terminalEvent} status=${observation.result.status}",
            )
        } finally {
            closeDetachedRequest(request)
        }
    }

    @Suppress("DEPRECATION")
    private fun parcelRoundTrip(request: ApkBuildRequest): ApkBuildRequest {
        val parcel = Parcel.obtain()
        return try {
            parcel.writeParcelable(request, 0)
            parcel.setDataPosition(0)
            requireNotNull(
                parcel.readParcelable<ApkBuildRequest>(ApkBuildRequest::class.java.classLoader),
            ) { "Parcelable round-trip returned null." }
        } finally {
            parcel.recycle()
        }
    }

    private fun closeDetachedRequest(request: ApkBuildRequest) {
        listOf(
            request.projectArchiveFd,
            request.nativeLibrariesArchiveFd,
            request.keyStoreFd,
        ).forEach { descriptor ->
            descriptor ?: return@forEach
            runCatching { descriptor.close() }
        }
        request.projectArchiveFd = null
        request.nativeLibrariesArchiveFd = null
        request.keyStoreFd = null
        runCatching {
            request.extras
                ?.getByteArray(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY)
                ?.fill(0)
        }
        request.keyStorePassword = null
        request.keyAliasPassword = null
    }

    private fun assertRequestedPermissions(apkFile: File, vararg expected: String) {
        val packageInfo = parsePackageArchive(apkFile)
        val actual = packageInfo.requestedPermissions.orEmpty().toSet()
        expected.forEach { permission ->
            assertTrue("Output APK is missing permission $permission; actual=$actual", permission in actual)
        }
    }

    private fun assertIconReplaced(
        apkFile: File,
        expectedWidth: Int,
        expectedHeight: Int,
        expectedColor: Int,
    ) {
        val resourcesFile = File(fixtureRoot, "resources-${UUID.randomUUID()}.arsc")
        val iconPath = ZipFile(apkFile).use { apk ->
            val resourcesEntry = checkNotNull(apk.getEntry("resources.arsc")) {
                "Output APK is missing resources.arsc"
            }
            apk.getInputStream(resourcesEntry).use { input ->
                FileOutputStream(resourcesFile).use(input::copyTo)
            }
            TableBlock.load(resourcesFile)
                .getOrCreatePackage(0x7f, TEST_PACKAGE_NAME)
                .getOrCreate("", "mipmap", "ic_launcher")
                .resValue
                .decodeValue()
        }
        ZipFile(apkFile).use { apk ->
            val iconEntry = checkNotNull(apk.getEntry(iconPath)) {
                "Output APK is missing resolved launcher icon $iconPath"
            }
            val bitmap = apk.getInputStream(iconEntry).use(BitmapFactory::decodeStream)
            checkNotNull(bitmap) { "Unable to decode output launcher icon $iconPath" }
            try {
                assertEquals(expectedWidth, bitmap.width)
                assertEquals(expectedHeight, bitmap.height)
                assertEquals(expectedColor, bitmap.getPixel(expectedWidth / 2, expectedHeight / 2))
            } finally {
                bitmap.recycle()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun parsePackageArchive(
        apkFile: File,
        expectedAppName: String = TEST_APP_NAME,
        expectedPackageName: String = TEST_PACKAGE_NAME,
        expectedVersionName: String = TEST_VERSION_NAME,
        expectedVersionCode: Int = TEST_VERSION_CODE,
    ): android.content.pm.PackageInfo {
        val packageManager = context.packageManager
        val packageInfo = checkNotNull(
            packageManager.getPackageArchiveInfo(
                apkFile.path,
                PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNATURES,
            ),
        ) { "PackageManager could not parse output APK: ${apkFile.path}" }
        assertEquals(expectedPackageName, packageInfo.packageName)
        assertEquals(expectedVersionName, packageInfo.versionName)
        assertEquals(expectedVersionCode, packageInfo.versionCode)
        assertTrue(
            "PackageManager did not expose an output signing certificate.",
            !packageInfo.signatures.isNullOrEmpty(),
        )
        val applicationInfo = checkNotNull(packageInfo.applicationInfo) {
            "Output APK does not contain applicationInfo."
        }.apply {
            sourceDir = apkFile.path
            publicSourceDir = apkFile.path
        }
        assertEquals(expectedAppName, applicationInfo.loadLabel(packageManager).toString())
        return packageInfo
    }

    private fun assertSuccessfulBuild(observation: Observation, vararg expectedEntries: String) {
        assertSuccessfulBuildWithIdentity(
            observation = observation,
            expectedAppName = TEST_APP_NAME,
            expectedPackageName = TEST_PACKAGE_NAME,
            expectedVersionName = TEST_VERSION_NAME,
            expectedVersionCode = TEST_VERSION_CODE,
            expectedEntries = expectedEntries,
        )
    }

    private fun assertSuccessfulBuildWithIdentity(
        observation: Observation,
        expectedAppName: String,
        expectedPackageName: String,
        expectedVersionName: String,
        expectedVersionCode: Int,
        expectedEntries: Array<out String>,
    ) {
        val failureDetail = "Remote build errors: ${observation.result.errors}"
        assertEquals(failureDetail, TerminalEvent.COMPLETED, observation.terminalEvent)
        assertEquals(failureDetail, ApkBuildResult.STATUS_OK, observation.result.status)
        assertTrue(failureDetail, observation.result.errors.isEmpty())
        assertTrue(
            observation.result.warnings.any { warning ->
                warning.contains("lightweight plugin-side builder")
            },
        )
        assertTrue(observation.outputCopy.isFile)
        assertEquals(observation.result.outputSizeBytes, observation.outputCopy.length())
        assertEquals(observation.result.outputSha256, sha256(observation.outputCopy))
        assertFalse(observation.result.updatedProjectConfigJson.isNullOrBlank())
        assertTrue(
            observation.progress.any { progress -> progress.step == ApkBuildProgress.STEP_BUILD },
        )
        assertTrue(
            observation.progress.any { progress -> progress.step == ApkBuildProgress.STEP_SIGN },
        )
        ZipFile(observation.outputCopy).use { apk ->
            listOf(
                "AndroidManifest.xml",
                "resources.arsc",
                "classes.dex",
                "assets/project/project.json",
                *expectedEntries,
            ).forEach { entry ->
                assertTrue("Output APK is missing $entry", apk.getEntry(entry) != null)
            }
        }
        parsePackageArchive(
            apkFile = observation.outputCopy,
            expectedAppName = expectedAppName,
            expectedPackageName = expectedPackageName,
            expectedVersionName = expectedVersionName,
            expectedVersionCode = expectedVersionCode,
        )

        val workspacePath = observation.result.extras?.getString("workspace")
        check(!workspacePath.isNullOrBlank()) { "Successful result did not report its workspace." }
        val workspace = File(workspacePath)
        assertTrue("Result workspace should remain readable until close().", workspace.isDirectory)
        observation.session.close()
        assertTrue(
            "Result workspace was not removed by close().",
            waitUntil { !workspace.exists() },
        )
    }

    private fun assertNoOutput(observation: Observation) {
        assertNull(observation.result.outputApkFd)
        assertFalse(observation.outputCopy.exists())
    }

    private fun assertBlockedInputFailure(
        observation: Observation,
        vararg expectedErrorFragments: String,
    ) {
        assertEquals(TerminalEvent.FAILED, observation.terminalEvent)
        assertEquals(ApkBuildResult.STATUS_FAILED, observation.result.status)
        assertEquals(ApkBuilderTemplateResult.LEVEL_BLOCK, observation.result.compatibilityLevel)
        assertTrue(observation.result.warnings.isEmpty())
        assertTrue("Input rejection did not report an error.", observation.result.errors.isNotEmpty())
        if (expectedErrorFragments.isNotEmpty()) {
            assertTrue(
                "Expected an error containing one of ${expectedErrorFragments.toList()}; " +
                    "actual=${observation.result.errors}",
                observation.result.errors.any { error ->
                    expectedErrorFragments.any { fragment ->
                        error.contains(fragment, ignoreCase = true)
                    }
                },
            )
        }
        assertNoOutput(observation)
        val workspaceRoot = File(context.cacheDir, "remote-apk-build")
        assertTrue(
            "Failed input left a remote build workspace behind: ${workspaceRoot.listFiles()?.toList()}",
            waitUntil { workspaceRoot.listFiles().isNullOrEmpty() },
        )
    }

    private fun assertRequestInputsClosed(request: ApkBuildRequest) {
        assertNull(request.projectArchiveFd)
        assertNull(request.nativeLibrariesArchiveFd)
        assertNull(request.keyStoreFd)
        assertNull(request.keyStorePassword)
        assertNull(request.keyAliasPassword)
    }

    private fun waitUntil(
        timeoutSeconds: Long = 10L,
        condition: () -> Boolean,
    ): Boolean {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds)
        while (System.nanoTime() < deadline) {
            if (condition()) return true
            Thread.sleep(25L)
        }
        return condition()
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
    }

    private data class Observation(
        val session: RemoteApkBuildSession,
        val terminalEvent: TerminalEvent,
        val result: ApkBuildResult,
        val progress: List<ApkBuildProgress>,
        val outputCopy: File,
    )

    private data class RequestFuzzCase(
        val name: String,
        val mutate: ApkBuildRequest.() -> Unit,
    )

    private data class JsonFuzzCase(
        val name: String,
        val projectConfig: String,
        val directorySource: Boolean = false,
    )

    private data class SensitiveRequest(
        val request: ApkBuildRequest,
        val stagingKey: ByteArray,
    )

    private class SensitiveProbeMatcher(probes: Map<String, ByteArray>) {

        private val nodes = mutableListOf(Node())

        init {
            probes.forEach { (label, probe) ->
                require(probe.isNotEmpty())
                var state = 0
                probe.forEach { byte ->
                    val symbol = byte.toInt() and 0xff
                    val next = nodes[state].transitions[symbol]
                    state = if (next >= 0) {
                        next
                    } else {
                        nodes.size.also { created ->
                            nodes += Node()
                            nodes[state].transitions[symbol] = created
                        }
                    }
                }
                check(nodes[state].label == null) { "Sensitive probes must be unique." }
                nodes[state].label = label
            }
            buildFailureTransitions()
        }

        fun assertAbsent(input: InputStream, surface: String) {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var state = 0
            try {
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    repeat(read) { index ->
                        state = nodes[state].transitions[buffer[index].toInt() and 0xff]
                        nodes[state].label?.let { label ->
                            throw AssertionError("Sensitive rule $label escaped through $surface.")
                        }
                    }
                }
            } finally {
                buffer.fill(0)
            }
        }

        private fun buildFailureTransitions() {
            val queue = ArrayDeque<Int>()
            repeat(256) { symbol ->
                val next = nodes[0].transitions[symbol]
                if (next >= 0) {
                    nodes[next].failure = 0
                    queue.addLast(next)
                } else {
                    nodes[0].transitions[symbol] = 0
                }
            }
            while (queue.isNotEmpty()) {
                val state = queue.removeFirst()
                repeat(256) { symbol ->
                    val next = nodes[state].transitions[symbol]
                    if (next >= 0) {
                        val fallback = nodes[nodes[state].failure].transitions[symbol]
                        nodes[next].failure = fallback
                        if (nodes[next].label == null) {
                            nodes[next].label = nodes[fallback].label
                        }
                        queue.addLast(next)
                    } else {
                        nodes[state].transitions[symbol] =
                            nodes[nodes[state].failure].transitions[symbol]
                    }
                }
            }
        }

        private data class Node(
            val transitions: IntArray = IntArray(256) { -1 },
            var failure: Int = 0,
            var label: String? = null,
        )
    }

    private enum class TerminalEvent {
        COMPLETED,
        FAILED,
        CANCELLED,
    }

    private class RecordingCallback(
        private val outputCopy: File,
    ) : IApkBuildCallback.Stub() {

        val progress = CopyOnWriteArrayList<ApkBuildProgress>()

        @Volatile
        var progressObserver: ((ApkBuildProgress) -> Unit)? = null

        @Volatile
        var terminalEvent: TerminalEvent? = null

        @Volatile
        var result: ApkBuildResult? = null

        @Volatile
        var callbackFailure: Throwable? = null

        private val terminal = CountDownLatch(1)

        override fun onStarted(progress: ApkBuildProgress) {
            this.progress += progress
        }

        override fun onProgress(progress: ApkBuildProgress) {
            this.progress += progress
            progressObserver?.invoke(progress)
        }

        override fun onCompleted(result: ApkBuildResult) {
            runCatching {
                val outputFd = checkNotNull(result.outputApkFd) {
                    "Completed result did not contain outputApkFd."
                }
                val duplicate = ParcelFileDescriptor.dup(outputFd.fileDescriptor)
                ParcelFileDescriptor.AutoCloseInputStream(duplicate).use { input ->
                    FileOutputStream(outputCopy).use { output -> input.copyTo(output) }
                }
            }.onFailure { error ->
                callbackFailure = error
            }
            complete(TerminalEvent.COMPLETED, result)
        }

        override fun onFailed(result: ApkBuildResult) {
            complete(TerminalEvent.FAILED, result)
        }

        override fun onCancelled(result: ApkBuildResult) {
            complete(TerminalEvent.CANCELLED, result)
        }

        fun awaitTerminal(): Boolean = terminal.await(3, TimeUnit.MINUTES)

        private fun complete(event: TerminalEvent, result: ApkBuildResult) {
            this.terminalEvent = event
            this.result = result
            terminal.countDown()
        }
    }

    companion object {
        private const val TEST_APP_NAME = "Remote Build Instrumented Test"
        private const val TEST_PACKAGE_NAME = "org.autojs.remote.build.test"
        private const val TEST_VERSION_NAME = "1.0.0"
        private const val TEST_VERSION_CODE = 1
        private const val DEFAULT_KEY_STORE_PASSWORD = "AutoJs6"
        private const val DEFAULT_KEY_ALIAS = "AutoJs6"
        private const val DEFAULT_KEY_ALIAS_PASSWORD = "AutoJs6"
        private const val INVALID_SHA256 =
            "0000000000000000000000000000000000000000000000000000000000000000"
        private const val SENSITIVE_AUDIT_TAG = "RemoteSensitiveAudit"
        private const val FUZZ_AUDIT_TAG = "RemoteFuzzAudit"
        private const val FUZZ_SECRET_SENTINEL = "M3_G5_FUZZ_SECRET_DO_NOT_ECHO_20260831"
        private const val SENSITIVE_TYPESCRIPT_CLEARTEXT =
            "M3_G5_TYPESCRIPT_CLEARTEXT_DO_NOT_EXPOSE_20260831"
        private const val SENSITIVE_KEY_STORE_PASSWORD =
            "M3_G5_KEYSTORE_PASSWORD_DO_NOT_EXPOSE_20260831"
        private const val SENSITIVE_KEY_ALIAS_PASSWORD =
            "M3_G5_ALIAS_PASSWORD_DO_NOT_EXPOSE_20260831"
        private const val SENSITIVE_STAGING_KEY_HEX =
            "4d3347355f53544147494e475f4b45595f32303236303833315f333242595445"
    }
}
