package build.krema.cli;

import build.krema.core.platform.PlatformDetector;

import java.io.IOException;

/**
 * Resolves the Maven command for the current platform.
 * On Windows, Maven ships as {@code mvn.cmd}; on Unix as {@code mvn}.
 */
public final class MavenResolver {

    private MavenResolver() {}

    /**
     * Returns the Maven command name for the current platform.
     */
    public static String command() {
        return PlatformDetector.isWindows() ? "mvn.cmd" : "mvn";
    }

    /**
     * Checks that Maven is available on PATH.
     * Prints an error and returns false if not found.
     */
    public static boolean checkAvailable() {
        String whichCmd = PlatformDetector.isWindows() ? "where" : "which";
        try {
            var pb = new ProcessBuilder(whichCmd, command());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            // Drain output to prevent blocking
            process.getInputStream().readAllBytes();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true;
            }
        } catch (IOException | InterruptedException ignored) {}

        System.err.println("[Krema] Maven is required but was not found on PATH.");
        System.err.println("[Krema] Install Maven from: https://maven.apache.org/download.cgi");
        System.err.println("[Krema] and ensure 'mvn' is available in your terminal.");
        return false;
    }
}
