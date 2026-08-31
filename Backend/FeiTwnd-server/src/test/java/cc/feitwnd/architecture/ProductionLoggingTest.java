package cc.feitwnd.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionLoggingTest {

    @Test
    void productionCodeMustUseStructuredLogging() throws IOException {
        Path backendRoot = findBackendRoot();
        List<String> violations = new ArrayList<>();

        for (String module : List.of("FeiTwnd-common", "FeiTwnd-server")) {
            Path sourceRoot = backendRoot.resolve(module).resolve("src/main/java");
            try (var paths = Files.walk(sourceRoot)) {
                paths.filter(path -> path.toString().endsWith(".java"))
                        .forEach(path -> collectViolations(backendRoot, path, violations));
            }
        }

        assertTrue(violations.isEmpty(),
                () -> "Production code must not write directly to stdout/stderr:\n"
                        + String.join("\n", violations));
    }

    private Path findBackendRoot() {
        Path current = Path.of("").toAbsolutePath();
        if (Files.isDirectory(current.resolve("FeiTwnd-common"))) {
            return current;
        }
        Path parent = current.getParent();
        if (parent != null && Files.isDirectory(parent.resolve("FeiTwnd-common"))) {
            return parent;
        }
        throw new IllegalStateException("Cannot locate Backend module root from " + current);
    }

    private void collectViolations(Path backendRoot, Path path, List<String> violations) {
        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains("System.out.")
                        || line.contains("System.err.")
                        || line.contains(".printStackTrace(")) {
                    violations.add(backendRoot.relativize(path) + ":" + (i + 1));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to inspect " + path, e);
        }
    }
}
