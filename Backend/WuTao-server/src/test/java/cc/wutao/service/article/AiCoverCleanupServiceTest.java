package cc.wutao.service.article;

import cc.wutao.entity.AiCoverAsset;
import cc.wutao.mapper.AiCoverAssetMapper;
import cc.wutao.utils.AliOssUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class AiCoverCleanupServiceTest {

    @Test
    void successfulDeletionRemovesRecordButFailureRemainsRetryable() {
        AiCoverAssetMapper mapper = mock(AiCoverAssetMapper.class);
        AliOssUtil oss = mock(AliOssUtil.class);
        AiCoverAsset first = AiCoverAsset.builder().id("one")
                .objectKey("image/ai-cover-one.png").build();
        AiCoverAsset second = AiCoverAsset.builder().id("two")
                .objectKey("image/ai-cover-two.png").build();
        when(mapper.findDeleting()).thenReturn(List.of(first, second));
        doThrow(new IllegalStateException("OSS unavailable"))
                .when(oss).deleteObject(second.getObjectKey());

        new AiCoverCleanupService(mapper, oss).cleanup();

        verify(mapper).markExpired();
        verify(oss).deleteObject(first.getObjectKey());
        verify(mapper).deleteDeleting("one");
        verify(mapper, never()).deleteDeleting("two");
        verify(mapper).deferRetry("two");
    }
}
