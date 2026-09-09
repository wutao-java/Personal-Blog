package cc.wutao;

import cc.wutao.service.auth.EncryptPasswordService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WuTaoBackendApplicationTests {

    @Test
    void passwordHashRemainsCompatibleWithStoredCredentials() throws Exception {
        EncryptPasswordService service = new EncryptPasswordService();

        assertEquals(
                "958d51602bbfbd18b2a084ba848a827c29952bfef170c936419b0922994c0589",
                service.hashPassword("123456", "123456")
        );
    }
}
