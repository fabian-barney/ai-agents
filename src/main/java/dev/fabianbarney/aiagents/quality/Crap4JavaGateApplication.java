package dev.fabianbarney.aiagents.quality;

import java.nio.file.Path;

public final class Crap4JavaGateApplication {

    private Crap4JavaGateApplication() {
    }

    public static void main(String[] args) throws Exception {
        Path projectDirectory = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        System.exit(new Crap4JavaGateRunner().run(projectDirectory));
    }
}
