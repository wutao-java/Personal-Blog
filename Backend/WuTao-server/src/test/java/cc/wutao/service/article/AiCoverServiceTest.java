package cc.wutao.service.article;

import cc.wutao.context.BaseContext;
import cc.wutao.dto.AiCoverGenerateDTO;
import cc.wutao.entity.AiCoverAsset;
import cc.wutao.exception.ArticleException;
import cc.wutao.extension.ai.AiCoverGenerator;
import cc.wutao.mapper.AiCoverAssetMapper;
import cc.wutao.utils.AliOssUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiCoverServiceTest {

    private AiCoverAssetMapper mapper;
    private AliOssUtil oss;
    private ObjectProvider<AiCoverGenerator> provider;
    private AiCoverService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mapper = mock(AiCoverAssetMapper.class);
        oss = mock(AliOssUtil.class);
        provider = mock(ObjectProvider.class);
        service = new AiCoverService(mapper, oss, provider);
        BaseContext.setCurrentId(7L);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void missingProviderIsExplicitlyUnavailableAndDoesNotUpload() {
        assertFalse(service.status().enabled());
        assertThrows(ArticleException.class, () -> service.generate(request()));
        verifyNoInteractions(mapper, oss);
    }

    @Test
    void generationCreatesCandidateWithoutChangingAnyArticle() throws Exception {
        AiCoverGenerator generator = mock(AiCoverGenerator.class);
        when(provider.getIfAvailable()).thenReturn(generator);
        when(generator.isAvailable()).thenReturn(true);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB), "png", bytes);
        when(generator.generate(anyString(), anyString())).thenReturn(bytes.toByteArray());
        when(oss.upload(any(), eq("png"), anyString())).thenReturn("https://bucket/image/cover.png");
        when(oss.generateAccessUrls(anyString())).thenReturn("https://signed/preview.png");
        when(mapper.completeUpload(anyString(), anyString())).thenReturn(1);

        var result = service.generate(request());

        assertNotNull(result.id());
        assertEquals("https://signed/preview.png", result.previewUrl());
        verify(mapper).insert(argThat(asset -> asset.getArticleId() == null
                && asset.getOwnerId().equals(7L)
                && asset.getObjectKey().matches("image/ai-cover-[a-f0-9-]+\\.png")));
        verify(mapper, never()).attach(anyString(), anyLong());
    }

    @Test
    void invalidImageNeverReachesOss() {
        AiCoverGenerator generator = mock(AiCoverGenerator.class);
        when(provider.getIfAvailable()).thenReturn(generator);
        when(generator.isAvailable()).thenReturn(true);
        when(generator.generate(anyString(), anyString())).thenReturn("<svg/>".getBytes());

        assertThrows(ArticleException.class, () -> service.generate(request()));

        verifyNoInteractions(oss, mapper);
    }

    @Test
    void selectedCandidateIsAttachedAndOldAiCoverIsQueuedForDeletion() {
        AiCoverAsset asset = candidate();
        when(mapper.getByIdForUpdate("candidate")).thenReturn(asset);

        service.bindCover(12L, asset.getImageUrl(), "candidate");

        verify(mapper).attach("candidate", 12L);
        verify(mapper).markReplaced(12L, "candidate");
        verifyNoInteractions(oss);
    }

    @Test
    void foreignOrExpiredCandidateCannotBeAdopted() {
        AiCoverAsset asset = candidate();
        when(mapper.getByIdForUpdate("candidate")).thenReturn(asset);
        asset.setOwnerId(8L);
        assertThrows(ArticleException.class,
                () -> service.bindCover(12L, asset.getImageUrl(), "candidate"));
        asset.setOwnerId(7L);
        asset.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        assertThrows(ArticleException.class,
                () -> service.bindCover(12L, asset.getImageUrl(), "candidate"));
        verify(mapper, never()).attach(anyString(), anyLong());
        verify(mapper, never()).markReplaced(anyLong(), any());
    }

    @Test
    void candidateCannotBeAdoptedByPastingUrlWithoutConfirmationId() {
        AiCoverAsset asset = candidate();
        when(mapper.getByImageUrlForUpdate(asset.getImageUrl())).thenReturn(asset);
        assertThrows(ArticleException.class,
                () -> service.bindCover(12L, asset.getImageUrl(), null));
    }

    @Test
    void unchangedAttachedAiCoverCanBeSavedByAnotherAdmin() {
        AiCoverAsset asset = candidate();
        asset.setStatus(AiCoverAsset.ATTACHED);
        asset.setArticleId(12L);
        asset.setOwnerId(8L);
        when(mapper.getByImageUrlForUpdate(asset.getImageUrl())).thenReturn(asset);

        service.bindCover(12L, asset.getImageUrl(), null);

        verify(mapper).markReplaced(12L, "candidate");
        verify(mapper, never()).attach(anyString(), anyLong());
    }

    @Test
    void manualCoverOnlyQueuesPreviousAiCoverAndNullPreservesExistingCover() {
        service.bindCover(12L, "https://external/upload.jpg", null);
        verify(mapper).markReplaced(12L, null);
        verifyNoInteractions(oss);
        clearInvocations(mapper);
        service.bindCover(12L, null, null);
        verifyNoInteractions(mapper);
    }

    @Test
    void discardCannotDeleteAttachedOrForeignImages() {
        AiCoverAsset asset = candidate();
        asset.setStatus(AiCoverAsset.ATTACHED);
        when(mapper.getByIdForUpdate("candidate")).thenReturn(asset);
        assertThrows(ArticleException.class, () -> service.discard("candidate"));
        asset.setStatus(AiCoverAsset.CANDIDATE);
        asset.setOwnerId(8L);
        assertThrows(ArticleException.class, () -> service.discard("candidate"));
        verify(mapper, never()).markDeleting(anyString());
    }

    private AiCoverGenerateDTO request() {
        return new AiCoverGenerateDTO("title", "article content");
    }

    private AiCoverAsset candidate() {
        return AiCoverAsset.builder().id("candidate").ownerId(7L)
                .status(AiCoverAsset.CANDIDATE)
                .imageUrl("https://bucket/image/ai-cover-123.png")
                .objectKey("image/ai-cover-123.png")
                .expiresAt(LocalDateTime.now().plusHours(24)).build();
    }
}
