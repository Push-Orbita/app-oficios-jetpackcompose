package com.cocido.oficios.core.designsystem.a11y

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NoHardcodedHexInFeaturesTest {

    @Test
    fun featureModulesDoNotHardcodeComposeHexColors() {
        val workspace = generateSequence(File("").canonicalFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() || File(it, "settings.gradle").exists() }
        val featureRoot = File(workspace, "feature")
        assertTrue("No existe $featureRoot", featureRoot.isDirectory)
        val pattern = Regex("""Color\(0x""")
        val offenders = featureRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    if (pattern.containsMatchIn(line) && !line.trimStart().startsWith("//")) {
                        "${file.relativeTo(workspace)}:${index + 1}: $line"
                    } else {
                        null
                    }
                }
            }
            .toList()
        assertTrue(
            "Colores hardcodeados en feature/*:\n${offenders.joinToString("\n")}",
            offenders.isEmpty(),
        )
    }
}
