package dev.fabianbarney.aiagents.quality;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

final class Crap4JavaGateRunner {

    private static final String GIT_REMOTE_URL = "https://github.com/unclebob/crap4java.git";
    private static final String CRAP4JAVA_COMMIT = "69b561209f130ece728f19b0001e90df5a117c3a";
    private static final String JACOCO_VERSION = System.getProperty("jacoco.version", "0.8.13");
    private static final String MAIN_CLASS = "crap4java.Main";
    private static final long PROCESS_TIMEOUT_SECONDS = 180;

    int run(Path projectDirectory) throws Exception {
        Path workDirectory = Crap4JavaCompatibilityPatcher.resolveWorkDirectory(
            projectDirectory,
            CRAP4JAVA_COMMIT,
            JACOCO_VERSION
        );
        prepareCheckout(workDirectory);
        Crap4JavaCompatibilityPatcher.applyCompatibilityPatches(workDirectory);
        Path jarFile = buildJar(workDirectory);
        return runGate(projectDirectory, jarFile);
    }

    private void prepareCheckout(Path workDirectory) throws Exception {
        if (checkoutMatches(workDirectory)) {
            return;
        }

        recreateWorkDirectory(workDirectory);
        runCommand(List.of("git", "init", "-q", workDirectory.toString()), null);
        runCommand(List.of("git", "-C", workDirectory.toString(), "remote", "add", "origin", GIT_REMOTE_URL), null);
        runCommand(List.of("git", "-C", workDirectory.toString(), "fetch", "--depth", "1", "origin", CRAP4JAVA_COMMIT), null);
        runCommand(List.of("git", "-C", workDirectory.toString(), "checkout", "--detach", "FETCH_HEAD"), null);
    }

    private boolean checkoutMatches(Path workDirectory) throws Exception {
        if (!Files.isDirectory(workDirectory.resolve(".git"))) {
            return false;
        }

        String currentCommit = readCommandOutput(
            List.of("git", "-C", workDirectory.toString(), "rev-parse", "HEAD"),
            null
        );
        return CRAP4JAVA_COMMIT.equals(currentCommit);
    }

    private void recreateWorkDirectory(Path workDirectory) throws IOException {
        deleteDirectoryIfPresent(workDirectory);
        Files.createDirectories(workDirectory);
    }

    Path buildJar(Path workDirectory) throws IOException {
        Path sourceDirectory = workDirectory.resolve(Path.of("src", "crap4java"));
        Path classesDirectory = workDirectory.resolve(Path.of("build", "classes"));
        Path jarFile = workDirectory.resolve(Path.of("build", "crap4java.jar"));
        compileSources(sourceDirectory, classesDirectory);
        createJar(classesDirectory, jarFile);
        return jarFile;
    }

    private void compileSources(Path sourceDirectory, Path classesDirectory) throws IOException {
        recreateWorkDirectory(classesDirectory);

        JavaCompiler compiler = requireSystemJavaCompiler();
        List<Path> sourceFiles = collectSourceFiles(sourceDirectory);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(sourceFiles);
            List<String> options = List.of("--release", "17", "-d", classesDirectory.toString());
            Boolean success = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call();
            ensureCompilationSucceeded(success, diagnostics);
        }
    }

    private void createJar(Path classesDirectory, Path jarFile) throws IOException {
        Files.createDirectories(jarFile.getParent());
        Files.deleteIfExists(jarFile);

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MAIN_CLASS);

        try (
            OutputStream outputStream = Files.newOutputStream(jarFile);
            JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest);
            var paths = Files.walk(classesDirectory)
        ) {
            for (Path file : paths.filter(Files::isRegularFile).sorted().toList()) {
                String entryName = classesDirectory.relativize(file).toString().replace('\\', '/');
                jarOutputStream.putNextEntry(new java.util.jar.JarEntry(entryName));
                Files.copy(file, jarOutputStream);
                jarOutputStream.closeEntry();
            }
        }
    }

    private int runGate(Path projectDirectory, Path jarFile) throws Exception {
        List<String> command = List.of(
            Crap4JavaCompatibilityPatcher.javaCommand().toString(),
            "-jar",
            jarFile.toString()
        );
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(projectDirectory.toFile());
        builder.inheritIO();
        builder.environment().put("CRAP4JAVA_SKIP_BUILD", "true");
        builder.environment().put(
            "CRAP4JAVA_BUILD_CMD",
            Crap4JavaCompatibilityPatcher.wrapperCommand(Crap4JavaCompatibilityPatcher.isWindows())
        );
        return waitForProcess(builder.start(), command);
    }

    private void runCommand(List<String> command, Path directory) throws Exception {
        String output = readCommandOutput(command, directory);
        if (!output.isBlank()) {
            System.out.println(output);
        }
    }

    private String readCommandOutput(List<String> command, Path directory) throws Exception {
        Path outputFile = Files.createTempFile("crap4java-gate-", ".log");
        try {
            Process process = startCommand(command, directory, outputFile);
            int exitCode = waitForProcess(process, command);
            String output = Files.readString(outputFile, StandardCharsets.UTF_8).trim();
            ensureSuccessfulExit(exitCode, command, output);
            return output;
        } finally {
            Files.deleteIfExists(outputFile);
        }
    }

    private void deleteDirectoryIfPresent(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        deleteDirectory(directory);
    }

    private void deleteDirectory(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            deletePaths(paths.sorted(Comparator.reverseOrder()).toList());
        }
    }

    private void deletePaths(List<Path> paths) throws IOException {
        for (Path path : paths) {
            Files.deleteIfExists(path);
        }
    }

    private JavaCompiler requireSystemJavaCompiler() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("A JDK is required to compile crap4java sources and build the gate jar.");
        }
        return compiler;
    }

    private List<Path> collectSourceFiles(Path sourceDirectory) throws IOException {
        try (var paths = Files.walk(sourceDirectory)) {
            return paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .sorted()
                .toList();
        }
    }

    private void ensureCompilationSucceeded(Boolean success, DiagnosticCollector<JavaFileObject> diagnostics) {
        if (Boolean.TRUE.equals(success)) {
            return;
        }

        String messages = diagnostics.getDiagnostics().stream()
            .map(Object::toString)
            .collect(Collectors.joining(System.lineSeparator()));
        throw new IllegalStateException("Failed to compile crap4java sources:%n%s".formatted(messages));
    }

    private Process startCommand(List<String> command, Path directory, Path outputFile) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (directory != null) {
            builder.directory(directory.toFile());
        }
        builder.redirectErrorStream(true);
        builder.redirectOutput(outputFile.toFile());
        return builder.start();
    }

    private int waitForProcess(Process process, List<String> command) throws InterruptedException {
        if (process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            return process.exitValue();
        }
        process.destroyForcibly();
        throw new IllegalStateException(
            "Command timed out after %d seconds: %s".formatted(PROCESS_TIMEOUT_SECONDS, String.join(" ", command))
        );
    }

    private void ensureSuccessfulExit(int exitCode, List<String> command, String output) {
        if (exitCode == 0) {
            return;
        }
        throw new IllegalStateException(
            "Command failed (%d): %s%n%s".formatted(exitCode, String.join(" ", command), output)
        );
    }
}
