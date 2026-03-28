package dev.fabianbarney.aiagents.quality;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Crap4JavaCompatibilityPatcherTest {

    @Test
    void resolvesWorkDirectoryUnderBuildRoot(@TempDir Path tempDir) {
        Path resolved = Crap4JavaCompatibilityPatcher.resolveWorkDirectory(tempDir, "abc123", "0.8.13");

        assertEquals(
            tempDir.resolve(Path.of("build", "crap4java", "abc123-jacoco-0.8.13")).toAbsolutePath().normalize(),
            resolved
        );
    }

    @Test
    void rejectsWorkDirectoryOutsideTheBuildRoot(@TempDir Path tempDir) {
        Path unsafe = tempDir.resolve("..").resolve("outside").normalize();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Crap4JavaCompatibilityPatcher.validateWorkDirectory(tempDir, unsafe)
        );

        assertTrue(exception.getMessage().contains("must stay under"));
    }

    @Test
    void choosesTheCorrectWrapperCommandForEachPlatform() {
        assertEquals("gradlew.bat", Crap4JavaCompatibilityPatcher.wrapperCommand(true));
        assertEquals("./gradlew", Crap4JavaCompatibilityPatcher.wrapperCommand(false));
    }

    @Test
    void patchesUpstreamSourcesForGradleCompatibility(@TempDir Path tempDir) throws IOException {
        Path sourceRoot = tempDir.resolve(Path.of("src", "crap4java"));
        Files.createDirectories(sourceRoot);
        writeSampleSources(sourceRoot);

        Crap4JavaCompatibilityPatcher.applyCompatibilityPatches(tempDir);
        Crap4JavaCompatibilityPatcher.applyCompatibilityPatches(tempDir);

        assertTrue(Files.readString(sourceRoot.resolve("CliApplication.java"))
            .contains("build/reports/jacoco/test/jacocoTestReport.xml"));
        assertTrue(Files.readString(sourceRoot.resolve("CliApplication.java"))
            .contains("Files.exists(current.resolve(\"build.gradle\"))"));
        assertTrue(Files.readString(sourceRoot.resolve("CoverageRunner.java"))
            .contains("CRAP4JAVA_SKIP_BUILD"));
        assertTrue(Files.readString(sourceRoot.resolve("CoverageRunner.java"))
            .contains("\"jacocoTestReport\""));
        assertTrue(Files.readString(sourceRoot.resolve("SourceFileFinder.java"))
            .contains("Path.of(\"src\", \"main\", \"java\")"));
        assertTrue(Files.readString(sourceRoot.resolve("ChangedFileDetector.java"))
            .contains("Path.of(\"src\", \"main\", \"java\")"));
        assertTrue(Files.readString(sourceRoot.resolve("Main.java"))
            .contains("Analyze all Java files under src/main/java/"));
    }

    private void writeSampleSources(Path sourceRoot) throws IOException {
        Files.writeString(sourceRoot.resolve("CliApplication.java"), """
            package crap4java;

            import java.nio.file.Files;

            final class CliApplication {
                void analyze() {
                    Path jacocoXml = moduleRoot.resolve("target/site/jacoco/jacoco.xml");
                }

                static java.nio.file.Path moduleRootFor(java.nio.file.Path workspaceRoot, java.nio.file.Path file) {
                    java.nio.file.Path current = file;
                    if (Files.exists(current.resolve("pom.xml"))) {
                        return current;
                    }
                    return workspaceRoot;
                }
            }
            """);
        Files.writeString(sourceRoot.resolve("CoverageRunner.java"), """
            package crap4java;

            import java.nio.file.Path;
            import java.util.List;

            final class CoverageRunner {
                void generateCoverage(Path projectRoot) throws Exception {
                    deleteIfExists(projectRoot.resolve("target/site/jacoco"));
                    deleteIfExists(projectRoot.resolve("target/jacoco.exec"));

                    int exit = executor.run(List.of(
                            "mvn", "-q",
                            "org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent",
                            "test",
                            "org.jacoco:jacoco-maven-plugin:0.8.12:report"
                    ), projectRoot);
                    if (exit != 0) {
                        throw new IllegalStateException();
                    }
                }
            }
            """);
        Files.writeString(sourceRoot.resolve("SourceFileFinder.java"), """
            package crap4java;

            import java.nio.file.Path;

            final class SourceFileFinder {
                static void findAllJavaFilesUnderSrc(Path projectRoot) {
                    Path src = projectRoot.resolve("src");
                }
            }
            """);
        Files.writeString(sourceRoot.resolve("ChangedFileDetector.java"), """
            package crap4java;

            final class ChangedFileDetector {
                static void changedJavaFilesUnderSrc(java.nio.file.Path projectRoot) {
                    changedJavaFiles(projectRoot).stream()
                            .filter(path -> path.normalize().startsWith(projectRoot.resolve("src").normalize()))
                            .toList();
                }
            }
            """);
        Files.writeString(sourceRoot.resolve("Main.java"), """
            package crap4java;

            final class Main {
                static String usage() {
                    return "Usage:\\n"
                            + "  crap4java            Analyze all Java files under src/\\n"
                            + "  crap4java --changed  Analyze changed Java files under src/\\n"
                            + "  crap4java <path...>  Analyze files, or for directory args analyze <dir>/src/**/*.java\\n"
                            + "  crap4java --help     Print this help message\\n";
                }
            }
            """);
    }
}
