import groovy.json.JsonSlurper
import java.security.MessageDigest
import java.util.zip.ZipFile
import org.gradle.api.provider.Property

plugins {
    id("org.autojs.build.versions")
    id("org.autojs.build.signs")
    id("org.autojs.build.jvm-convention")
    id("com.android.application")
}

val buildTypeRelease = "release"
val apkFileExtension = "apk"
val enableRemoteBuildProperty = "autojs.apkBuilder.templatePlugin.enableRemoteBuild"
val runtimeKitDirProperty = "autojs.apkBuilder.templatePlugin.runtimeKitDir"
val remoteBuildTemplateExpansionMultiplier = 4L
val enableRemoteBuild = providers.gradleProperty(enableRemoteBuildProperty)
    .map { value ->
        value.trim().lowercase().let { it in setOf("true", "1", "yes", "on") }
    }
    .orElse(false)

// Composite plugin identity (docs/versioning.md): the plugin carries its own version line,
// while versionCode/versionName still encode the paired host so pairing stays readable
// and same-host re-releases remain Android upgrades.
val hostVersionSlug = versions.appVersionName.replace("\\s".toRegex(), "-").lowercase()
val pluginVersionNameFull = "${versions.pluginVersionName}+autojs6-$hostVersionSlug"
val pluginVersionCode = run {
    require(versions.pluginReleaseSeq in 0..99) {
        "PLUGIN_RELEASE_SEQ must be within 0..99, got ${versions.pluginReleaseSeq}"
    }
    versions.appVersionCode * 100 + versions.pluginReleaseSeq
}

private fun Any.reflectedNoArgMethod(name: String): Any? =
    javaClass.methods.firstOrNull { it.name == name && it.parameterTypes.isEmpty() }?.invoke(this)

@Suppress("UNCHECKED_CAST")
private fun Any.reflectedOutputFileNameProperty(): Property<String>? =
    reflectedNoArgMethod("getOutputFileName") as? Property<String>

fun resolveSigningStoreFile(path: String): File {
    val candidate = File(path)
    if (candidate.isAbsolute) {
        return candidate
    }
    return listOf(
        file(path),
        rootProject.file(path),
        rootProject.file("app/$path"),
    ).firstOrNull { it.exists() } ?: file(path)
}

fun resolveRuntimeKitRoot(candidate: File): File {
    if (candidate.resolve("runtime-kit.json").isFile) {
        return candidate
    }
    val nested = candidate.listFiles()
        ?.filter { it.isDirectory && it.resolve("runtime-kit.json").isFile }
        ?.minByOrNull { it.name }
    return nested ?: candidate
}

@Suppress("UNCHECKED_CAST")
fun readRuntimeKitVariantName(candidate: File): String {
    val metadataFile = resolveRuntimeKitRoot(candidate).resolve("runtime-kit.json")
    if (!metadataFile.isFile) return "inrt-universal"
    val metadata = JsonSlurper().parse(metadataFile) as? Map<String, Any?>
        ?: return "inrt-universal"
    val template = metadata["template"] as? Map<String, Any?>
    return template?.get("variant")?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        ?: "inrt-universal"
}

val supportedRuntimeKitVariantSlugs = setOf(
    "universal",
    "arm64-v8a",
    "armeabi-v7a",
    "x86_64",
    "x86",
)
val runtimeKitValidationRules = rootProject.file("scripts/runtime_kit_validation_rules.json")
val defaultRuntimeKitDir = rootProject.file("runtime-kit")
val configuredRuntimeKitDir = providers.gradleProperty(runtimeKitDirProperty).orNull?.let { path ->
    File(path).takeIf { it.isAbsolute } ?: rootProject.file(path)
}
val runtimeKitSourceDir = configuredRuntimeKitDir ?: defaultRuntimeKitDir
val runtimeKitVariantName = providers.provider { readRuntimeKitVariantName(runtimeKitSourceDir) }
val runtimeKitVariantSlug = runtimeKitVariantName.map { variantName ->
    require(variantName.startsWith("inrt-")) { "Unsupported Runtime Kit variant: $variantName" }
    val slug = variantName.removePrefix("inrt-")
    require(slug in supportedRuntimeKitVariantSlugs) { "Unsupported Runtime Kit variant: $variantName" }
    slug
}

android {
    namespace = "org.autojs.plugin.apkbuilder.template.impl"
    compileSdk = versions.sdkVersionCompile

    defaultConfig {
        applicationId = "org.autojs.plugin.apkbuilder.template"
        minSdk = versions.sdkVersionMin
        targetSdk = versions.sdkVersionTarget
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = pluginVersionCode
        versionName = pluginVersionNameFull

        buildConfigField("String", "PLUGIN_ID", "\"autojs6-apk-builder-template\"")
        buildConfigField("String", "PLUGIN_VERSION_NAME", "\"${versions.pluginVersionName}\"")
        buildConfigField("int", "PLUGIN_VERSION_BUILD", "${versions.pluginVersionBuild}")
        buildConfigField("String", "HOST_PACKAGE_NAME", "\"org.autojs.autojs6\"")
        buildConfigField("String", "HOST_VERSION_NAME", "\"${versions.appVersionName}\"")
        buildConfigField("long", "HOST_VERSION_CODE", "${versions.appVersionCode}L")
        buildConfigField("int", "PROTOCOL_VERSION", "2")
        buildConfigField("boolean", "ENABLE_REMOTE_BUILD", enableRemoteBuild.get().toString())
        buildConfigField("int", "REMOTE_BUILD_PROTOCOL_VERSION", "3")
        buildConfigField(
            "long",
            "REMOTE_BUILD_TEMPLATE_EXPANSION_MULTIPLIER",
            "${remoteBuildTemplateExpansionMultiplier}L",
        )
        buildConfigField("String", "TEMPLATE_PACKAGE_NAME", "\"org.autojs.autojs6.inrt\"")
    }

    sourceSets {
        getByName("main") {
            assets.directories.add(layout.buildDirectory.dir("generated/assets/apkBuilderTemplate").get().asFile.absolutePath)
        }
    }

    signingConfigs {
        if (signs.isValid) {
            create(buildTypeRelease) {
                storeFile = signs.properties["storeFile"]?.let { resolveSigningStoreFile(it as String) }
                keyPassword = signs.properties["keyPassword"] as String
                keyAlias = signs.properties["keyAlias"] as String
                storePassword = signs.properties["storePassword"] as String
            }
        }
    }

    buildTypes {
        val releaseSigningConfig = if (signs.isValid) signingConfigs.getByName(buildTypeRelease) else null

        release {
            isMinifyEnabled = false
            releaseSigningConfig?.let { signingConfig = it }
        }
    }

    buildFeatures {
        buildConfig = true
        aidl = true
    }

    lint {
        abortOnError = false
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.reflectedOutputFileNameProperty()?.set(
                runtimeKitVariantSlug.map { variantSlug ->
                    "autojs6-apk-builder-template-v${versions.pluginVersionName.lowercase()}-autojs6-v$hostVersionSlug-$variantSlug.$apkFileExtension"
                }
            )
        }
    }
}

dependencies {
    implementation(project(":plugin-api:apk-builder-template"))
    implementation(project(":plugin-api:common-plugin-api"))
    implementation(project(":modules:android-axml"))
    implementation(project(":modules:android-arsc-editor"))
    implementation(project(":modules:apk-signer"))
    implementation(libs.arsclib)
    testImplementation(libs.junit)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.test.runner)
}

fun File.sha256String(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
}

fun copyDirectoryContents(source: File, target: File) {
    source.walkTopDown()
        .filter { it.isFile }
        .forEach { file ->
            val dest = target.resolve(file.relativeTo(source).path)
            dest.parentFile?.mkdirs()
            file.copyTo(dest, overwrite = true)
        }
}

fun readMetadataPath(metadata: Map<*, *>, path: String): Any? {
    var current: Any? = metadata
    path.split('.').forEach { segment ->
        current = (current as? Map<*, *>)?.get(segment) ?: return null
    }
    return current
}

fun requiredInt(value: Any?, label: String): Int {
    return when (value) {
        is Number -> value.toInt()
        else -> value?.toString()?.toIntOrNull()
    } ?: throw IllegalArgumentException("Runtime Kit validation rules do not declare a valid $label")
}

@Suppress("UNCHECKED_CAST")
fun validateRuntimeKitVariantMetadata(
    metadata: Map<String, Any?>,
    rules: Map<String, Any?>,
): Pair<String, List<String>> {
    val variantRules = rules["templateVariant"] as? Map<String, Any?>
        ?: throw IllegalArgumentException("Runtime Kit validation rules do not declare templateVariant")
    fun ruleValue(key: String): String = variantRules[key]?.toString()?.takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("Runtime Kit templateVariant rules do not declare $key")

    val variantValue = readMetadataPath(metadata, ruleValue("variantMetadata"))
    val supportedAbisValue = readMetadataPath(metadata, ruleValue("supportedAbisMetadata"))
    val universalVariant = ruleValue("universalVariant")
    if (variantValue == null && supportedAbisValue == null) {
        require(variantRules["allowLegacyMissingMetadata"] == true) {
            "Runtime Kit must explicitly declare template.variant and template.supportedAbis"
        }
        return universalVariant to emptyList()
    }

    val variant = variantValue?.toString()?.trim().orEmpty()
    require(variant.isNotEmpty()) {
        "Runtime Kit metadata does not declare a valid template.variant"
    }
    val supportedAbis = (supportedAbisValue as? List<*>)
        ?.map { value -> value?.toString()?.trim().orEmpty() }
        ?: throw IllegalArgumentException(
            "Runtime Kit metadata does not declare template.supportedAbis as an array"
        )
    require(supportedAbis.all { it.isNotEmpty() }) {
        "Runtime Kit template.supportedAbis contains an invalid ABI"
    }
    require(supportedAbis.distinct().size == supportedAbis.size) {
        "Runtime Kit template.supportedAbis contains duplicate ABIs"
    }

    val allowedAbis = (variantRules["allowedAbis"] as? List<*>)
        ?.map { it?.toString().orEmpty() }
        .orEmpty()
    if (variant == universalVariant) {
        require(supportedAbis == allowedAbis) {
            "Universal Runtime Kit $universalVariant must declare template.supportedAbis=$allowedAbis"
        }
        return variant to supportedAbis
    }

    val prefix = ruleValue("variantPrefix")
    require(variant.startsWith(prefix)) { "Unsupported Runtime Kit template.variant: $variant" }
    val abi = variant.removePrefix(prefix)
    require(abi in allowedAbis) { "Unsupported Runtime Kit ABI variant: $abi" }
    require(supportedAbis == listOf(abi)) {
        "Runtime Kit $variant must declare template.supportedAbis=[$abi]"
    }
    return variant to supportedAbis
}

@Suppress("UNCHECKED_CAST")
fun validateRuntimeKitCompatibilityMetadata(
    metadata: Map<String, Any?>,
    rules: Map<String, Any?>,
): Pair<IntRange, Boolean> {
    val compatibilityRules = rules["compatibility"] as? Map<String, Any?>
        ?: throw IllegalArgumentException("Runtime Kit validation rules do not declare compatibility")
    fun ruleValue(key: String): String = compatibilityRules[key]?.toString()?.takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("Runtime Kit compatibility rules do not declare $key")
    fun metadataInt(path: String, label: String): Int {
        val value = readMetadataPath(metadata, path)
        return when (value) {
            is Number -> value.toInt()
            else -> null
        }?.takeIf { it > 0 }
            ?: throw IllegalArgumentException("Runtime Kit $label must be a positive integer")
    }

    val builtFor = metadataInt(
        ruleValue("builtForHostVersionCodeMetadata"),
        "host.versionCode",
    )
    val minimumPath = ruleValue("minHostVersionCodeMetadata")
    val maximumPath = ruleValue("maxHostVersionCodeMetadata")
    val allowPatchPath = ruleValue("allowPatchVersionMismatchMetadata")
    val minimumValue = readMetadataPath(metadata, minimumPath)
    val maximumValue = readMetadataPath(metadata, maximumPath)
    val allowPatchValue = readMetadataPath(metadata, allowPatchPath)
    if (minimumValue == null && maximumValue == null && allowPatchValue == null) {
        require(compatibilityRules["allowLegacyMissingMetadata"] == true) {
            "Runtime Kit must explicitly declare its host compatibility contract"
        }
        return (builtFor..builtFor) to false
    }

    val minimum = metadataInt(minimumPath, "compatibility.minHostVersionCode")
    val maximum = metadataInt(maximumPath, "compatibility.maxHostVersionCode")
    require(allowPatchValue is Boolean) {
        "Runtime Kit compatibility.allowPatchVersionMismatch must be a boolean"
    }
    require(minimum <= maximum) {
        "Runtime Kit host compatibility range is invalid: $minimum..$maximum"
    }
    require(builtFor in minimum..maximum) {
        "Runtime Kit built-for host $builtFor is outside its compatibility range $minimum..$maximum"
    }
    require(minimum == maximum || allowPatchValue) {
        "A widened Runtime Kit host range requires allowPatchVersionMismatch=true"
    }
    return (minimum..maximum) to allowPatchValue
}

@Suppress("UNCHECKED_CAST")
fun verifyRuntimeKit(
    runtimeKitDir: File,
    rulesFile: File,
    templateExpansionMultiplier: Long,
) {
    require(rulesFile.isFile) { "Missing Runtime Kit validation rules: $rulesFile" }
    val rules = JsonSlurper().parse(rulesFile) as? Map<String, Any?>
        ?: throw IllegalArgumentException("Runtime Kit validation rules must be a JSON object: $rulesFile")
    require(requiredInt(rules["schemaVersion"], "schemaVersion") == 1) {
        "Unsupported Runtime Kit validation rules schemaVersion: ${rules["schemaVersion"]}"
    }

    val kitRules = rules["runtimeKit"] as? Map<String, Any?>
        ?: throw IllegalArgumentException("Runtime Kit validation rules do not declare runtimeKit")
    val metadataFileName = kitRules["metadataFile"]?.toString()?.takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("Runtime Kit validation rules do not declare runtimeKit.metadataFile")
    val requiredFiles = (rules["requiredFiles"] as? List<*>)
        ?.map { it?.toString().orEmpty() }
        ?.filter { it.isNotBlank() }
        .orEmpty()
    require(requiredFiles.isNotEmpty()) { "Runtime Kit validation rules do not declare requiredFiles" }
    requiredFiles.forEach { name ->
        require(runtimeKitDir.resolve(name).isFile) {
            "Missing Runtime Kit file: ${runtimeKitDir.resolve(name)}"
        }
    }

    val metadataFile = runtimeKitDir.resolve(metadataFileName)
    val metadata = JsonSlurper().parse(metadataFile) as? Map<String, Any?>
        ?: throw IllegalArgumentException("Runtime Kit metadata must be a JSON object: $metadataFile")
    val expectedSchema = requiredInt(kitRules["schemaVersion"], "runtimeKit.schemaVersion")
    require(requiredInt(metadata["schemaVersion"], "Runtime Kit schemaVersion") == expectedSchema) {
        "Unsupported Runtime Kit schemaVersion: ${metadata["schemaVersion"]}"
    }
    val contract = metadata["contract"] as? Map<String, Any?> ?: emptyMap()
    val protocol = requiredInt(contract["apkBuilderProtocolVersion"] ?: 0, "contract.apkBuilderProtocolVersion")
    val minimumProtocol = requiredInt(
        kitRules["minimumApkBuilderProtocolVersion"],
        "runtimeKit.minimumApkBuilderProtocolVersion",
    )
    require(protocol >= minimumProtocol) {
        "Runtime Kit protocol too old: $protocol < $minimumProtocol"
    }
    val (templateVariant, templateSupportedAbis) = validateRuntimeKitVariantMetadata(metadata, rules)
    validateRuntimeKitCompatibilityMetadata(metadata, rules)

    val sha256Pattern = Regex("[0-9a-f]{64}")
    val artifactRules = rules["sha256Artifacts"] as? List<*>
        ?: throw IllegalArgumentException("Runtime Kit validation rules do not declare sha256Artifacts")
    require(artifactRules.isNotEmpty()) { "Runtime Kit validation rules declare no sha256Artifacts" }
    artifactRules.forEach { rawArtifactRules ->
        val artifactRule = rawArtifactRules as? Map<String, Any?>
            ?: throw IllegalArgumentException("Runtime Kit sha256Artifacts entries must be JSON objects")
        fun ruleValue(key: String): String = artifactRule[key]?.toString()?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Runtime Kit artifact rules do not declare $key")

        val artifactName = ruleValue("path")
        val artifact = runtimeKitDir.resolve(artifactName)
        require(artifact.length() > 0L) { "Runtime Kit artifact is empty: $artifact" }

        val metadataSizePath = ruleValue("metadataSizeBytes")
        val expectedSize = when (val value = readMetadataPath(metadata, metadataSizePath)) {
            is Number -> value.toLong()
            else -> value?.toString()?.toLongOrNull()
        } ?: throw IllegalArgumentException(
            "Runtime Kit metadata does not declare a valid $metadataSizePath"
        )
        require(expectedSize == artifact.length()) {
            "$artifactName size mismatch in $metadataFileName: expected=$expectedSize actual=${artifact.length()}"
        }

        val metadataShaPath = ruleValue("metadataSha256")
        val expectedSha = readMetadataPath(metadata, metadataShaPath)
            ?.toString()
            ?.trim()
            ?.lowercase()
            .orEmpty()
        require(sha256Pattern.matches(expectedSha)) {
            "Runtime Kit metadata does not declare a valid $metadataShaPath"
        }
        val sidecar = runtimeKitDir.resolve(ruleValue("sidecar"))
        val sidecarSha = sidecar.readText(Charsets.UTF_8).trim().lowercase()
        require(sha256Pattern.matches(sidecarSha)) {
            "Runtime Kit SHA-256 sidecar is invalid: $sidecar"
        }

        val actualSha = artifact.sha256String().lowercase()
        require(expectedSha == actualSha) {
            "$artifactName SHA-256 mismatch in $metadataFileName"
        }
        require(sidecarSha == actualSha) {
            "$artifactName SHA-256 mismatch in ${sidecar.name}"
        }
    }

    val templateRules = rules["templateApk"] as? Map<String, Any?>
        ?: throw IllegalArgumentException("Runtime Kit validation rules do not declare templateApk")
    val templatePath = templateRules["path"]?.toString()?.takeIf { it.isNotBlank() }
        ?: throw IllegalArgumentException("Runtime Kit validation rules do not declare templateApk.path")
    val requiredEntries = (templateRules["requiredEntries"] as? List<*>)
        ?.map { it?.toString().orEmpty() }
        ?.filter { it.isNotBlank() }
        .orEmpty()
    require(requiredEntries.isNotEmpty()) {
        "Runtime Kit validation rules do not declare templateApk.requiredEntries"
    }
    val templateApk = runtimeKitDir.resolve(templatePath)
    ZipFile(templateApk).use { zip ->
        var expandedBytes = 0L
        zip.entries().asSequence()
            .filterNot { it.isDirectory }
            .forEach { entry ->
                require(entry.size >= 0L) {
                    "Runtime Kit template APK entry has an unknown expanded size: ${entry.name}"
                }
                require(expandedBytes <= Long.MAX_VALUE - entry.size) {
                    "Runtime Kit template APK expanded size overflows a signed 64-bit value"
                }
                expandedBytes += entry.size
            }
        val maximumExpandedBytes = if (templateApk.length() > Long.MAX_VALUE / templateExpansionMultiplier) {
            Long.MAX_VALUE
        } else {
            templateApk.length() * templateExpansionMultiplier
        }
        require(expandedBytes <= maximumExpandedBytes) {
            "Runtime Kit template APK expands beyond the remote-build storage estimate: " +
                    "expanded=$expandedBytes max=$maximumExpandedBytes " +
                    "multiplier=$templateExpansionMultiplier"
        }
        requiredEntries.forEach { entryName ->
            require(zip.getEntry(entryName) != null) {
                "Runtime Kit template APK is missing required entry: $entryName"
            }
        }
        if (templateSupportedAbis.isNotEmpty()) {
            val nativeAbis = zip.entries().asSequence()
                .filterNot { it.isDirectory }
                .map { it.name.split('/') }
                .filter { parts ->
                    parts.size >= 3 && parts.first() == "lib" && parts.last().endsWith(".so")
                }
                .map { parts -> parts[1] }
                .toSet()
            require(nativeAbis == templateSupportedAbis.toSet()) {
                "Runtime Kit template APK native ABI mismatch for $templateVariant: " +
                        "declared=$templateSupportedAbis actual=${nativeAbis.sorted()}"
            }
        }
    }
}

val generatedTemplateAssetsDir = layout.buildDirectory.dir("generated/assets/apkBuilderTemplate")
val refreshTemplateBeforePackagingProperty = "autojs.apkBuilder.templatePlugin.refreshTemplate"
val refreshTemplateBeforePackaging = providers.gradleProperty(refreshTemplateBeforePackagingProperty)
    .map { value ->
        value.trim().lowercase().let { it in setOf("true", "1", "yes", "on") }
    }
    .orElse(false)

val verifyApkBuilderRuntimeKit = tasks.register("verifyApkBuilderRuntimeKit") {
    group = "verification"
    description = "Verifies the AutoJs6 Runtime Kit using the shared validation rules."

    inputs.property(runtimeKitDirProperty, configuredRuntimeKitDir?.path ?: "")
    inputs.file(runtimeKitValidationRules)
    inputs.dir(runtimeKitSourceDir)

    doLast {
        val runtimeKitDir = resolveRuntimeKitRoot(runtimeKitSourceDir)
        require(runtimeKitDir.isDirectory) {
            "Runtime Kit directory does not exist. Download/unpack autojs6-runtime-kit-*.zip to runtime-kit or pass -P$runtimeKitDirProperty=<dir>: $runtimeKitDir"
        }
        verifyRuntimeKit(
            runtimeKitDir,
            runtimeKitValidationRules,
            remoteBuildTemplateExpansionMultiplier,
        )
        logger.lifecycle("Runtime Kit verified with shared rules: $runtimeKitValidationRules")
    }
}

val prepareApkBuilderTemplateAssets = tasks.register("prepareApkBuilderTemplateAssets") {
    group = "build"
    description = "Copies a verified AutoJs6 Runtime Kit into the APK builder template plugin assets."

    dependsOn(verifyApkBuilderRuntimeKit)

    inputs.property(refreshTemplateBeforePackagingProperty, refreshTemplateBeforePackaging)
    inputs.property(runtimeKitDirProperty, configuredRuntimeKitDir?.path ?: "")
    inputs.file(runtimeKitValidationRules)
    inputs.dir(runtimeKitSourceDir)
    outputs.dir(generatedTemplateAssetsDir)

    doLast {
        val runtimeKitDir = resolveRuntimeKitRoot(runtimeKitSourceDir)

        val assetsRoot = generatedTemplateAssetsDir.get().asFile
        project.delete(assetsRoot)
        val targetRuntimeKitDir = assetsRoot.resolve("runtime-kit").apply { mkdirs() }
        copyDirectoryContents(runtimeKitDir, targetRuntimeKitDir)
    }
}

tasks.matching {
    val taskName = it.name
    taskName.contains("lint", ignoreCase = true) || (taskName.startsWith("merge") && taskName.endsWith("Assets"))
}.configureEach {
    dependsOn(prepareApkBuilderTemplateAssets)
}
