package org.autojs.build

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.util.Properties

class Signs @JvmOverloads constructor(
    project: Project,
    filePath: String = "${project.rootDir}/sign.properties",
) {

    companion object {
        private val propertyEnvironmentVariables = linkedMapOf(
            "storeFile" to "SIGNING_KEY_STORE_FILE",
            "storePassword" to "SIGNING_KEY_STORE_PASSWORD",
            "keyAlias" to "SIGNING_KEY_ALIAS",
            "keyPassword" to "SIGNING_KEY_PASSWORD",
        )
    }

    var isValid = false
        private set

    val properties = Properties()

    init {
        val environmentValues = propertyEnvironmentVariables.mapValues { (_, environmentVariable) ->
            project.providers.environmentVariable(environmentVariable).orNull?.takeUnless(String::isEmpty)
        }
        val suppliedEnvironmentVariables = environmentValues.filterValues { it != null }

        when {
            suppliedEnvironmentVariables.isEmpty() -> {
                File(filePath).takeIf(File::exists)?.inputStream()?.use(properties::load)
                validateIfConfigured("$filePath")
            }

            suppliedEnvironmentVariables.size != propertyEnvironmentVariables.size -> {
                val missing = environmentValues
                    .filterValues { it == null }
                    .keys
                    .map(propertyEnvironmentVariables::getValue)
                throw GradleException(
                    "Incomplete signing environment. Missing variables: ${missing.joinToString()}",
                )
            }

            else -> {
                environmentValues.forEach { (propertyName, value) ->
                    properties.setProperty(propertyName, value!!)
                }
                validateIfConfigured("environment variables")
            }
        }
    }

    private fun validateIfConfigured(source: String) {
        if (properties.isEmpty()) return

        val missing = propertyEnvironmentVariables.keys.filter { propertyName ->
            properties.getProperty(propertyName).isNullOrEmpty()
        }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Incomplete signing configuration from $source. Missing properties: ${missing.joinToString()}",
            )
        }
        isValid = true
    }
}
