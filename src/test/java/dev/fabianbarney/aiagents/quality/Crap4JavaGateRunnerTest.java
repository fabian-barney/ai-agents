package dev.fabianbarney.aiagents.quality;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Crap4JavaGateRunnerTest {

    private final Crap4JavaGateRunner runner = new Crap4JavaGateRunner();

    @Test
    void buildJarProducesARunnableCrap4JavaJar(@TempDir Path tempDir) throws Exception {
        Path sourceRoot = tempDir.resolve(Path.of("src", "crap4java"));
        Files.createDirectories(sourceRoot);
        Files.writeString(sourceRoot.resolve("Main.java"), """
            package crap4java;

            public final class Main {
                private Main() {
                }

                public static void main(String[] args) {
                    System.out.print("runner-test-ok");
                }
            }
            """);

        Path jarFile = runner.buildJar(tempDir);

        assertTrue(Files.exists(jarFile));
        try (JarFile jar = new JarFile(jarFile.toFile())) {
            assertEquals("crap4java.Main", jar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS));
        }

        Process process = new ProcessBuilder(
            Crap4JavaCompatibilityPatcher.javaCommand().toString(),
            "-jar",
            jarFile.toString()
        ).redirectErrorStream(true).start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
        }
        assertTrue(finished, "Timed out waiting for the runnable crap4java jar to exit.");
        String output;
        try (var stream = process.getInputStream()) {
            output = new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
        }

        assertEquals(0, process.exitValue());
        assertEquals("runner-test-ok", output);
    }
}
