package cc.wutao.contract;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseSchemaContractTest {

    private static final String EXPECTED_SCHEMA_SHA256 =
            "793a50c8dc846ee0ddd1e23ff17cf1a44eb9d5c60058ad60f59624dd56b2a934";

    @Test
    void shouldKeepExistingDatabaseSchema() throws Exception {
        try (InputStream schema = getClass().getClassLoader()
                .getResourceAsStream("database/wutao.sql")) {
            assertNotNull(schema, "database/wutao.sql must be packaged");
            String actualHash = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(schema.readAllBytes()));
            assertEquals(EXPECTED_SCHEMA_SHA256, actualHash);
        }
    }

    @Test
    void aiCoverUpgradeMustMatchBothInitializationScripts() throws Exception {
        java.nio.file.Path backend = java.nio.file.Path.of("").toAbsolutePath();
        if (!java.nio.file.Files.isDirectory(backend.resolve("WuTao-server"))) {
            backend = backend.getParent();
        }
        String schema = java.nio.file.Files.readString(backend.resolve(
                "WuTao-server/src/main/resources/database/wutao.sql"));
        String dockerSchema = java.nio.file.Files.readString(backend.getParent().resolve(
                "docker/mysql/init/wutao.sql"));
        String upgrade = java.nio.file.Files.readString(backend.resolve(
                "WuTao-server/src/main/resources/database/ai-cover-schema-upgrade.sql"));
        assertEquals(aiCoverDefinition(schema), aiCoverDefinition(dockerSchema));
        assertEquals(aiCoverDefinition(schema), aiCoverDefinition(upgrade));
    }

    private String aiCoverDefinition(String sql) {
        String normalized = sql.replace("if not exists ", "").replace("\r\n", "\n");
        int start = normalized.indexOf("create table ai_cover_assets");
        return normalized.substring(start, normalized.indexOf(";", start));
    }
}
