package cc.feitwnd.contract;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseSchemaContractTest {

    private static final String EXPECTED_SCHEMA_SHA256 =
            "90bf9eccff35e83a8dd2b0db5d08136c2e55b7c1e196b1e633d857228cc726f9";

    @Test
    void shouldKeepExistingDatabaseSchema() throws Exception {
        try (InputStream schema = getClass().getClassLoader()
                .getResourceAsStream("database/feitwnd.sql")) {
            assertNotNull(schema, "database/feitwnd.sql must be packaged");
            String actualHash = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(schema.readAllBytes()));
            assertEquals(EXPECTED_SCHEMA_SHA256, actualHash);
        }
    }
}
