package org.autojs.plugin.apkbuilder.template.impl

import android.content.Context
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import org.autojs.plugin.apkbuilder.template.ApkBuildProgress
import org.autojs.plugin.apkbuilder.template.ApkBuildRequest
import org.autojs.plugin.apkbuilder.template.ApkBuildRequestExtraKeys
import org.autojs.plugin.apkbuilder.template.ApkBuildResult
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateInfo
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateCompatibilityPolicy
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateProtocol
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateResult
import org.autojs.plugin.apkbuilder.template.IApkBuildCallback
import org.autojs.plugin.apkbuilder.template.IApkBuildSession
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class RemoteApkBuildSession(
    context: Context,
    private val request: ApkBuildRequest,
    private val callback: IApkBuildCallback?,
    private val executor: Executor,
    private val remoteBuildEnabled: Boolean = BuildConfig.ENABLE_REMOTE_BUILD,
    private val usableSpaceProvider: (File) -> Long = { directory -> directory.usableSpace },
) : IApkBuildSession.Stub() {

    private val appContext = context.applicationContext
    private val started = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)
    private val closed = AtomicBoolean(false)
    private val requestInputsClosed = AtomicBoolean(false)
    private val sensitiveRequestDataCleared = AtomicBoolean(false)
    private val workspaceLifecycleLock = Any()

    @Volatile
    private var workerFinished = false

    @Volatile
    private var workspace: RemoteApkBuildWorkspace? = null

    @Volatile
    private var progress = ApkBuildProgress()

    override fun start() {
        if (!started.compareAndSet(false, true)) {
            return
        }
        executor.execute { runSession() }
    }

    override fun cancel() {
        cancelled.set(true)
    }

    override fun close() {
        closed.set(true)
        cancelled.set(true)
        closeRequestInputFds()
        clearSensitiveRequestData()
        synchronized(workspaceLifecycleLock) {
            if (workerFinished) {
                closeWorkspaceLocked()
            }
        }
    }

    override fun getProgress(): ApkBuildProgress = progress

    private fun runSession() {
        var keepWorkspaceForResult = false
        val requestWarnings = arrayListOf<String>()
        notifyStarted(updateProgress(ApkBuildProgress.STEP_PREPARE, "Preparing remote build workspace", null))
        runCatching {
            ensureActive()
            ensureRemoteBuildEnabled()
            RemoteApkBuildRequestPolicy.validate(request)
            val sourceKind = request.extras
                ?.getString(ApkBuildRequestExtraKeys.SOURCE_KIND)
                ?: throw IllegalStateException("Validated project source kind is missing.")
            val projectConfig = RemoteProjectConfigParser.parse(request.projectConfigJson, sourceKind)
            val templateInfo = ApkBuilderTemplateMetadata.templateInfo(appContext)
            requestWarnings += validateHostRequest(templateInfo)
            val storageEstimate = RemoteBuildStoragePolicy.estimate(
                request = request,
                templateArchiveBytes = templateInfo.templateSizeBytes,
            )
            RemoteBuildStoragePolicy.requireAvailable(
                availableBytes = usableSpaceProvider(appContext.cacheDir),
                estimate = storageEstimate,
            )
            notifyProgress(updateProgress(ApkBuildProgress.STEP_PREPARE, "Reading remote build request", request.outputFileName))

            val preparedWorkspace = RemoteApkBuildWorkspace.prepare(appContext, request, cancelled)
            synchronized(workspaceLifecycleLock) {
                workspace = preparedWorkspace
            }
            RemoteEmbeddedNodePackagingPolicy.validateProjectSource(projectConfig, preparedWorkspace.sourcePath)
            notifyProgress(updateProgress(ApkBuildProgress.STEP_PREPARE, "Remote build request validated", preparedWorkspace.sourcePath.path))

            ensureActive()
            val output = RemoteApkLightweightBuilder(
                context = appContext,
                request = request,
                workspace = preparedWorkspace,
                projectConfig = projectConfig,
                cancelSignal = cancelled,
                progress = { progress ->
                    this.progress = progress
                    notifyProgress(progress)
                },
            ).build()
            val outputFd = ParcelFileDescriptor.open(output.apkFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val warnings = (requestWarnings + output.warnings).filter { it.isNotBlank() }.distinct()
            val result = ApkBuildResult(
                status = ApkBuildResult.STATUS_OK,
                outputApkFd = outputFd,
                outputFileName = output.outputFileName,
                outputSizeBytes = output.apkFile.length(),
                outputSha256 = output.sha256,
                updatedProjectConfigJson = output.updatedProjectConfigJson,
                compatibilityLevel = if (warnings.isEmpty()) {
                    ApkBuilderTemplateResult.LEVEL_OK
                } else {
                    ApkBuilderTemplateResult.LEVEL_WARN
                },
                warnings = ArrayList(warnings),
                extras = Bundle().apply {
                    putAll(preparedWorkspace.toExtras())
                    putAll(output.extras)
                },
            )
            keepWorkspaceForResult = notifyCompleted(result)
            closeOutputFd(outputFd)
        }.onFailure { error ->
            val unsupported = error.findUnsupportedCause()
            val status = when {
                cancelled.get() -> ApkBuildResult.STATUS_CANCELLED
                unsupported != null -> ApkBuildResult.STATUS_UNSUPPORTED
                else -> ApkBuildResult.STATUS_FAILED
            }
            val result = ApkBuildResult(
                status = status,
                compatibilityLevel = if (unsupported != null) {
                    ApkBuilderTemplateResult.LEVEL_WARN
                } else {
                    ApkBuilderTemplateResult.LEVEL_BLOCK
                },
                warnings = ArrayList(
                    (requestWarnings + listOfNotNull(unsupported?.message))
                        .filter { it.isNotBlank() }
                        .distinct(),
                ),
                errors = if (unsupported == null && !cancelled.get()) {
                    arrayListOf(error.message ?: error.toString())
                } else {
                    arrayListOf()
                },
            )
            if (cancelled.get()) {
                notifyCancelled(result)
            } else {
                notifyFailed(result)
            }
        }.also {
            closeRequestInputFds()
            clearSensitiveRequestData()
            finishWorkspaceLifecycle(keepWorkspaceForResult)
            if (!closed.get()) {
                progress = updateProgress(ApkBuildProgress.STEP_FINISH, "Remote build session finished", null)
            }
        }
    }

    private fun finishWorkspaceLifecycle(keepWorkspaceForResult: Boolean) {
        synchronized(workspaceLifecycleLock) {
            workerFinished = true
            if (!keepWorkspaceForResult || closed.get()) {
                notifyProgress(updateProgress(ApkBuildProgress.STEP_CLEAN, "Cleaning remote build workspace", null))
                closeWorkspaceLocked()
            }
        }
    }

    private fun closeWorkspaceLocked() {
        workspace?.close()
        workspace = null
    }

    private fun notifyStarted(progress: ApkBuildProgress) {
        notifyCallback("started") { callback?.onStarted(progress) }
    }

    private fun notifyProgress(progress: ApkBuildProgress) {
        notifyCallback("progress") { callback?.onProgress(progress) }
    }

    private fun notifyCompleted(result: ApkBuildResult): Boolean {
        return notifyTerminalCallback("completed") { callback!!.onCompleted(result) }
    }

    private fun notifyFailed(result: ApkBuildResult): Boolean {
        return notifyTerminalCallback("failed") { callback!!.onFailed(result) }
    }

    private fun notifyCancelled(result: ApkBuildResult): Boolean {
        return notifyTerminalCallback("cancelled") { callback!!.onCancelled(result) }
    }

    private fun notifyTerminalCallback(event: String, block: () -> Unit): Boolean {
        if (callback == null) {
            Log.w(TAG, "Remote build $event callback is missing.")
            return false
        }
        return notifyCallback(event, block)
    }

    private fun notifyCallback(event: String, block: () -> Unit): Boolean {
        return runCatching {
            block()
            true
        }.getOrElse { error ->
            Log.w(TAG, "Failed to send remote build $event callback.", error)
            false
        }
    }

    private fun closeOutputFd(outputFd: ParcelFileDescriptor) {
        runCatching {
            outputFd.close()
        }.onFailure {
            Log.w(TAG, "Failed to close remote build output fd.", it)
        }
    }

    private fun closeRequestInputFds() {
        if (!requestInputsClosed.compareAndSet(false, true)) {
            return
        }
        listOf(
            request.projectArchiveFd,
            request.nativeLibrariesArchiveFd,
            request.keyStoreFd,
        ).forEach { inputFd ->
            inputFd ?: return@forEach
            runCatching {
                inputFd.close()
            }.onFailure {
                Log.w(TAG, "Failed to close remote build input fd.", it)
            }
        }
        request.projectArchiveFd = null
        request.nativeLibrariesArchiveFd = null
        request.keyStoreFd = null
    }

    private fun clearSensitiveRequestData() {
        if (!sensitiveRequestDataCleared.compareAndSet(false, true)) {
            return
        }
        try {
            request.extras?.let { extras ->
                try {
                    extras
                        .getByteArray(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY)
                        ?.fill(0)
                } catch (_: RuntimeException) {
                    Log.w(TAG, "Remote build staging key could not be read during sensitive-data cleanup.")
                }
                listOf(
                    ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY,
                    ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION,
                    ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS,
                ).forEach { key ->
                    runCatching { extras.remove(key) }
                }
            }
        } finally {
            request.keyStorePassword = null
            request.keyAliasPassword = null
        }
    }

    private fun Throwable.findUnsupportedCause(): RemoteApkBuildUnsupportedException? {
        var current: Throwable? = this
        while (current != null) {
            if (current is RemoteApkBuildUnsupportedException) {
                return current
            }
            current = current.cause
        }
        return null
    }

    private fun validateHostRequest(info: ApkBuilderTemplateInfo): List<String> {
        val warnings = arrayListOf<String>()
        if (request.requiredProtocolVersion > ApkBuilderTemplateProtocol.REMOTE_BUILD_VERSION) {
            throw IllegalStateException(
                "Host requires newer remote build protocol: host=${request.requiredProtocolVersion}, plugin=${ApkBuilderTemplateProtocol.REMOTE_BUILD_VERSION}"
            )
        }
        val compatibilityDecision = request.hostVersionCode
            .takeIf { it > 0L }
            ?.let { hostVersionCode ->
                ApkBuilderTemplateCompatibilityPolicy.evaluate(info, hostVersionCode)
            }
        if (compatibilityDecision?.level == ApkBuilderTemplateCompatibilityPolicy.Level.BLOCKED) {
            throw IllegalStateException(compatibilityDecision.blockedCompatibilityMessage())
        }
        if (request.hostPackageName.isNotBlank() && request.hostPackageName != info.hostPackageName) {
            warnings += handleRiskyHostMismatch("Host package mismatch: plugin targets ${info.hostPackageName}, host=${request.hostPackageName}")
        }
        if (request.hostVersionName.isNotBlank() && request.hostVersionName != info.hostVersionName) {
            val message = "Host versionName mismatch: plugin=${info.hostVersionName}, host=${request.hostVersionName}"
            warnings += if (compatibilityDecision?.level == ApkBuilderTemplateCompatibilityPolicy.Level.PATCH_COMPATIBLE) {
                message
            } else {
                handleRiskyHostMismatch(message)
            }
        }
        if (compatibilityDecision?.level == ApkBuilderTemplateCompatibilityPolicy.Level.PATCH_COMPATIBLE) {
            warnings += compatibilityDecision.patchCompatibilityMessage()
        }
        return warnings
    }

    private fun ensureRemoteBuildEnabled() {
        if (!remoteBuildEnabled) {
            throw RemoteApkBuildUnsupportedException(
                "Remote APK build is disabled in this plugin build."
            )
        }
    }

    private fun ApkBuilderTemplateCompatibilityPolicy.Decision.patchCompatibilityMessage(): String {
        return "Host versionCode differs within the declared patch-compatible range: " +
                "builtFor=${declaration.builtForHostVersionCode}, host=$actualHostVersionCode, " +
                "range=${declaration.minHostVersionCode}..${declaration.maxHostVersionCode}"
    }

    private fun ApkBuilderTemplateCompatibilityPolicy.Decision.blockedCompatibilityMessage(): String {
        return "Host versionCode is outside the declared compatibility contract: " +
                "builtFor=${declaration.builtForHostVersionCode}, host=$actualHostVersionCode, " +
                "range=${declaration.minHostVersionCode}..${declaration.maxHostVersionCode}, " +
                "allowPatchVersionMismatch=${declaration.allowPatchVersionMismatch}, reason=$reason"
    }

    private fun handleRiskyHostMismatch(message: String): String {
        if (!request.allowRiskyBuild) {
            throw IllegalStateException(message)
        }
        return message
    }

    private fun ensureActive() {
        if (closed.get() || cancelled.get()) {
            throw InterruptedException("Remote APK build session was cancelled.")
        }
    }

    private fun updateProgress(step: Int, title: String, detail: String?): ApkBuildProgress {
        return ApkBuildProgress(
            step = step,
            title = title,
            detail = detail,
        ).also { progress = it }
    }

    companion object {
        private const val TAG = "RemoteApkBuildSession"
    }
}
