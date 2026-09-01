@file:Suppress("SpellCheckingInspection")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "autojs6-plugin-apk-builder-template"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("io.github.supermonster003.autojs6-platform-versions") version "1.6.0"
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    }
}

plugins {
    id("io.github.supermonster003.autojs6-platform-versions")
    // @Hint by SuperMonster003 on Sep 14, 2025.
    //  ! Enable JDK auto-resolution/download capability for build modules.
    //  ! zh-CN: 让构建模块具备 JDK 自动解析/下载能力.
    id("org.gradle.toolchains.foojay-resolver-convention")
}

includeBuild("build-logic")

private val modules = listOf(
    "apk-signer",
    "android-axml",
    "android-arsc-editor",
)

private val libs = emptyList<String>()

private val pluginApi = listOf(
    "common-plugin-api",
    "apk-builder-template",
)

include(
    ":app",
    *modules.map { ":modules:$it" }.toTypedArray(),
    *libs.map { ":libs:$it" }.toTypedArray(),
    *pluginApi.map { ":plugin-api:$it" }.toTypedArray(),
)

modules.forEach {
    project(":modules:$it").projectDir = File("modules", it)
}

pluginApi.forEach {
    project(":plugin-api:$it").projectDir = File("plugin-api", it)
}
