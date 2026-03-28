package dev.fabianbarney.aiagents.quality;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class Crap4JavaCompatibilityPatcher {

    private static final String WORK_DIRECTORY_PREFIX = "build/crap4java";
    private static final String ORIGINAL_JACOCO_XML = "moduleRoot.resolve(\"target/site/jacoco/jacoco.xml\")";
    private static final String GRADLE_JACOCO_XML =
        "moduleRoot.resolve(\"build/reports/jacoco/test/jacocoTestReport.xml\")";
    private static final String ORIGINAL_MODULE_MARKER = "if (Files.exists(current.resolve(\"pom.xml\"))) {";
    private static final String GRADLE_MODULE_MARKER =
        "if (Files.exists(current.resolve(\"pom.xml\")) || Files.exists(current.resolve(\"build.gradle\")) "
            + "|| Files.exists(current.resolve(\"build.gradle.kts\"))) {";
    private static final String LEGACY_GRADLE_MODULE_MARKER = """
        if (
                    Files.exists(current.resolve("pom.xml"))
                    || Files.exists(current.resolve("build.gradle"))
                    || Files.exists(current.resolve("build.gradle.kts"))
                ) {
        """;
    private static final String ORIGINAL_SOURCE_ROOT = "Path src = projectRoot.resolve(\"src\");";
    private static final String GRADLE_SOURCE_ROOT = "Path src = projectRoot.resolve(Path.of(\"src\", \"main\", \"java\"));";
    private static final String ORIGINAL_CHANGED_FILTER =
        ".filter(path -> path.normalize().startsWith(projectRoot.resolve(\"src\").normalize()))";
    private static final String GRADLE_CHANGED_FILTER =
        ".filter(path -> path.normalize().startsWith(projectRoot.resolve(Path.of(\"src\", \"main\", \"java\")).normalize()))";
    private static final String ORIGINAL_JACOCO_DIR = "deleteIfExists(projectRoot.resolve(\"target/site/jacoco\"));";
    private static final String GRADLE_JACOCO_DIR =
        "if (Boolean.parseBoolean(System.getenv().getOrDefault(\"CRAP4JAVA_SKIP_BUILD\", \"false\"))) {\n"
            + "            return;\n"
            + "        }\n\n"
            + "        deleteIfExists(projectRoot.resolve(\"build/reports/jacoco/test\"));";
    private static final String ORIGINAL_COVERAGE_GUARD = "deleteIfExists(projectRoot.resolve(\"target/jacoco.exec\"));";
    private static final String GRADLE_COVERAGE_GUARD = "deleteIfExists(projectRoot.resolve(\"build/jacoco/test.exec\"));";
    private static final String ORIGINAL_BUILD_COMMAND = "\"mvn\", \"-q\"";
    private static final String GRADLE_BUILD_COMMAND = """
                System.getenv().getOrDefault(
                        "CRAP4JAVA_BUILD_CMD",
                        System.getProperty("os.name").toLowerCase().contains("win") ? "gradlew.bat" : "./gradlew"
                ),
                "--no-daemon",
                "-q"
        """;
    private static final String ORIGINAL_PREPARE_GOAL = "\"org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent\",";
    private static final String GRADLE_PREPARE_GOAL = "";
    private static final String ORIGINAL_REPORT_GOAL = "\"org.jacoco:jacoco-maven-plugin:0.8.12:report\"";
    private static final String GRADLE_REPORT_GOAL = "\"jacocoTestReport\"";

    private Crap4JavaCompatibilityPatcher() {
    }

    static Path resolveWorkDirectory(Path projectDirectory, String commit, String jacocoVersion) {
        Path candidate = projectDirectory.resolve(
            Path.of("build", "crap4java", "%s-jacoco-%s".formatted(commit, jacocoVersion))
        );
        return validateWorkDirectory(projectDirectory, candidate);
    }

    static Path validateWorkDirectory(Path projectDirectory, Path candidate) {
        Path workRoot = projectDirectory.resolve(WORK_DIRECTORY_PREFIX).toAbsolutePath().normalize();
        Path normalizedCandidate = candidate.toAbsolutePath().normalize();
        if (!normalizedCandidate.startsWith(workRoot)) {
            throw new IllegalArgumentException(
                "Crap4java work directory must stay under %s but was %s".formatted(workRoot, normalizedCandidate)
            );
        }
        return normalizedCandidate;
    }

    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    static String wrapperCommand(boolean windows) {
        return windows ? "gradlew.bat" : "./gradlew";
    }

    static Path javaCommand() {
        return Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java")
            .toAbsolutePath()
            .normalize();
    }

    static void applyCompatibilityPatches(Path checkoutDirectory) throws IOException {
        patchCliApplication(checkoutDirectory.resolve(Path.of("src", "crap4java", "CliApplication.java")));
        patchCoverageRunner(checkoutDirectory.resolve(Path.of("src", "crap4java", "CoverageRunner.java")));
        patchSourceFinder(checkoutDirectory.resolve(Path.of("src", "crap4java", "SourceFileFinder.java")));
        patchChangedFiles(checkoutDirectory.resolve(Path.of("src", "crap4java", "ChangedFileDetector.java")));
        patchUsage(checkoutDirectory.resolve(Path.of("src", "crap4java", "Main.java")));
    }

    private static void patchCliApplication(Path file) throws IOException {
        String content = Files.readString(file);
        content = content.replace(LEGACY_GRADLE_MODULE_MARKER, GRADLE_MODULE_MARKER);
        content = replaceRequired(content, ORIGINAL_JACOCO_XML, GRADLE_JACOCO_XML, file);
        content = replaceRequired(content, ORIGINAL_MODULE_MARKER, GRADLE_MODULE_MARKER, file);
        Files.writeString(file, content);
    }

    private static void patchCoverageRunner(Path file) throws IOException {
        String content = Files.readString(file);
        content = replaceRequired(content, ORIGINAL_JACOCO_DIR, GRADLE_JACOCO_DIR, file);
        content = replaceRequired(content, ORIGINAL_COVERAGE_GUARD, GRADLE_COVERAGE_GUARD, file);
        content = replaceRequired(content, ORIGINAL_BUILD_COMMAND, GRADLE_BUILD_COMMAND, file);
        content = replaceRequired(content, ORIGINAL_PREPARE_GOAL, GRADLE_PREPARE_GOAL, file);
        content = replaceRequired(content, ORIGINAL_REPORT_GOAL, GRADLE_REPORT_GOAL, file);
        Files.writeString(file, content);
    }

    private static void patchSourceFinder(Path file) throws IOException {
        String content = Files.readString(file);
        content = replaceRequired(content, ORIGINAL_SOURCE_ROOT, GRADLE_SOURCE_ROOT, file);
        Files.writeString(file, content);
    }

    private static void patchChangedFiles(Path file) throws IOException {
        String content = Files.readString(file);
        content = replaceRequired(content, ORIGINAL_CHANGED_FILTER, GRADLE_CHANGED_FILTER, file);
        Files.writeString(file, content);
    }

    private static void patchUsage(Path file) throws IOException {
        String content = Files.readString(file);
        content = content.replace("Analyze all Java files under src/", "Analyze all Java files under src/main/java/");
        content = content.replace("Analyze changed Java files under src/", "Analyze changed Java files under src/main/java/");
        content = content.replace("<dir>/src/**/*.java", "<dir>/src/main/java/**/*.java");
        Files.writeString(file, content);
    }

    private static String replaceRequired(String content, String original, String replacement, Path file) {
        if (replacement.isEmpty()) {
            return removeRequired(content, original);
        }
        if (content.contains(replacement)) {
            return content;
        }
        if (content.contains(original)) {
            return content.replace(original, replacement);
        }
        throw new IllegalStateException(
            "Unable to find expected upstream snippet in %s.%nExpected:%n%s".formatted(file, original)
        );
    }

    private static String removeRequired(String content, String original) {
        if (!content.contains(original)) {
            return content;
        }
        return content.replace(original, "");
    }
}
