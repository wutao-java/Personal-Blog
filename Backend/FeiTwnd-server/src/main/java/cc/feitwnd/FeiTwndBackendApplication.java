package cc.feitwnd;

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
public class FeiTwndBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeiTwndBackendApplication.class, args);
        log.info("WuTao Backend server started");
    }
}
