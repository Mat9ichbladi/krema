package build.krema.cli;

import java.io.IOException;
import java.util.Properties;

public final class CliVersion {

    private static final String VERSION;

    static {
        var props = new Properties();
        try (var in = CliVersion.class.getResourceAsStream("/krema-cli.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
        }
        VERSION = props.getProperty("version", "0.0.0");
    }

    public static String get() {
        return VERSION;
    }

    private CliVersion() {
    }
}
