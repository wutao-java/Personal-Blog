package cc.wutao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
@Slf4j
public class WuTaoBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(WuTaoBackendApplication.class, args);
        log.info("WuTao Backend server started");
    }
}
