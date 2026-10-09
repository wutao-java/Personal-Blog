package cc.wutao.service.article;

import cc.wutao.entity.AiCoverAsset;
import cc.wutao.mapper.AiCoverAssetMapper;
import cc.wutao.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCoverCleanupService {

    private final AiCoverAssetMapper mapper;
    private final AliOssUtil oss;

    @Async("taskExecutor")
    public void cleanupAsync() {
        cleanup();
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void cleanup() {
        mapper.markExpired();
        for (AiCoverAsset asset : mapper.findDeleting()) {
            try {
                oss.deleteObject(asset.getObjectKey());
                mapper.deleteDeleting(asset.getId());
            } catch (RuntimeException e) {
                log.warn("AI cover cleanup will retry: assetId={}", asset.getId(), e);
                mapper.deferRetry(asset.getId());
            }
        }
    }
}
