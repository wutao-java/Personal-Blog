package cc.wutao.contract;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseSchemaContractTest {

    private static final String EXPECTED_SCHEMA_SHA256 =
            "0757386c159add23e458a3540d3a5c9928479c72d715a1de923225707f1e1f87";

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
}
