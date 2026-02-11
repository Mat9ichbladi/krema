package build.krema.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import build.krema.core.platform.Platform;
import build.krema.core.platform.PlatformDetector;

/**
 * Locates the native webview library on the filesystem.
 */
class NativeLibraryFinder {

    private static final String LIBRARY_BASE_NAME = "webview";

    private static String[] searchDirs() {
        List<String> dirs = new ArrayList<>(List.of(
            "lib",
            "../lib",
            "../krema-core/lib",
            "../../krema/krema-core/lib"
        ));
        String libraryPath = System.getProperty("java.library.path", "");
        if (!libraryPath.isEmpty()) {
            for (String entry : libraryPath.split(File.pathSeparator)) {
                if (!entry.isBlank()) {
                    dirs.add(entry);
                }
            }
        }
        return dirs.toArray(String[]::new);
    }

    /**
     * Finds the directory containing the native webview library.
     * Searches both flat directories and {platform}/{arch}/ subdirectories.
     *
     * @return the directory path, or null if not found
     */
    static Path find() {
        Platform platform = Platform.current();
        String archSubdir = platform.name().toLowerCase() + "/" + PlatformDetector.getArch();

        for (String searchPath : searchDirs()) {
            Path libDir = Path.of(searchPath);

            // Check platform/arch subdirectory first (structured layout)
            if (Files.isDirectory(libDir)) {
                Path archDir = libDir.resolve(archSubdir);
                if (Files.isDirectory(archDir)) {
                    try (var stream = Files.list(archDir)) {
                        if (stream.anyMatch(p -> p.toString().contains(LIBRARY_BASE_NAME))) {
                            return archDir;
                        }
                    } catch (IOException ignored) {
                    }
                }

                // Fall back to flat directory
                try (var stream = Files.list(libDir)) {
                    if (stream.anyMatch(p -> p.toString().contains(LIBRARY_BASE_NAME))) {
                        return libDir;
                    }
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * Finds the native webview library directory within a specific base directory.
     * Checks both flat layout and {platform}/{arch}/ subdirectories.
     *
     * @return the directory containing the library, or null if not found
     */
    static Path findIn(Path baseDir) {
        if (!Files.isDirectory(baseDir)) return null;

        Platform platform = Platform.current();
        String archSubdir = platform.name().toLowerCase() + "/" + PlatformDetector.getArch();

        // Check platform/arch subdirectory first
        Path archDir = baseDir.resolve(archSubdir);
        if (Files.isDirectory(archDir)) {
            try (var stream = Files.list(archDir)) {
                if (stream.anyMatch(p -> p.toString().contains(LIBRARY_BASE_NAME))) {
                    return archDir;
                }
            } catch (IOException ignored) {}
        }

        // Fall back to flat directory
        try (var stream = Files.list(baseDir)) {
            if (stream.anyMatch(p -> p.toString().contains(LIBRARY_BASE_NAME))) {
                return baseDir;
            }
        } catch (IOException ignored) {}

        return null;
    }

    /**
     * Finds the native webview library file itself.
     * Searches both flat directories and {platform}/{arch}/ subdirectories.
     *
     * @return the library file path, or null if not found
     */
    static Path findLibrary() {
        Platform platform = Platform.current();
        String libraryName = platform.formatLibraryName(LIBRARY_BASE_NAME);
        String archSubdir = platform.name().toLowerCase() + "/" + PlatformDetector.getArch();

        for (String searchPath : searchDirs()) {
            // Check platform/arch subdirectory first (structured layout)
            Path archFile = Path.of(searchPath, archSubdir, libraryName);
            if (Files.exists(archFile)) {
                return archFile;
            }

            // Fall back to flat directory
            Path libFile = Path.of(searchPath, libraryName);
            if (Files.exists(libFile)) {
                return libFile;
            }
        }
        return null;
    }
}
