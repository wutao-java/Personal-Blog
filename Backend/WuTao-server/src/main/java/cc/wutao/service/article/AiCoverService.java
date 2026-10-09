package cc.wutao.service.article;

import cc.wutao.context.BaseContext;
import cc.wutao.dto.AiCoverGenerateDTO;
import cc.wutao.entity.AiCoverAsset;
import cc.wutao.exception.ArticleException;
import cc.wutao.extension.ai.AiCoverGenerator;
import cc.wutao.mapper.AiCoverAssetMapper;
import cc.wutao.utils.AliOssUtil;
import cc.wutao.vo.AiCoverStatusVO;
import cc.wutao.vo.AiCoverVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCoverService {

    private final AiCoverAssetMapper mapper;
    private final AliOssUtil oss;
    private final ObjectProvider<AiCoverGenerator> generatorProvider;

    public AiCoverStatusVO status() {
        AiCoverGenerator generator = generatorProvider.getIfAvailable();
        boolean available = generator != null && generator.isAvailable();
        return new AiCoverStatusVO(available, available ? "" : "AI 生图平台尚未配置或未启用");
    }

    // Provider and OSS calls deliberately stay outside a database transaction.
    public AiCoverVO generate(AiCoverGenerateDTO request) {
        Long ownerId = requireAdmin();
        AiCoverGenerator generator = generatorProvider.getIfAvailable();
        if (generator == null || !generator.isAvailable()) {
            throw new ArticleException("AI 生图平台尚未配置或未启用");
        }
        byte[] image;
        try {
            image = normalizeImage(generator.generate(request.title(), request.contentMarkdown()));
        } catch (ArticleException e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("AI cover generation failed: adminId={}, type={}", ownerId, e.getClass().getSimpleName());
            throw new ArticleException("AI 封面生成失败，请稍后重试");
        }
        String id = UUID.randomUUID().toString();
        String fileName = "ai-cover-" + id + ".png";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        // Reserve the key first so interrupted/failed uploads can still be reclaimed.
        mapper.insert(AiCoverAsset.builder().id(id).ownerId(ownerId)
                .objectKey("image/" + fileName).status(AiCoverAsset.UPLOADING)
                .expiresAt(expiresAt).build());
        try {
            String imageUrl = oss.upload(image, "png", fileName);
            if (mapper.completeUpload(id, imageUrl) != 1) {
                throw new ArticleException("候选封面已失效，请重新生成");
            }
            return new AiCoverVO(id, imageUrl, previewUrl(imageUrl), expiresAt);
        } catch (RuntimeException e) {
            mapper.markDeleting(id);
            throw e;
        }
    }

    /**
     * Called in the article transaction, after acquiring the article row lock.
     * Only a confirmed candidate or this article's existing AI cover may be used.
     */
    public void bindCover(Long articleId, String imageUrl, String candidateId) {
        if (imageUrl == null && candidateId == null) {
            return;
        }
        AiCoverAsset asset;
        if (candidateId != null) {
            asset = mapper.getByIdForUpdate(candidateId);
            if (asset == null || !Objects.equals(asset.getImageUrl(), imageUrl)) {
                throw new ArticleException("候选封面与图片地址不匹配，请重新生成");
            }
            if (AiCoverAsset.CANDIDATE.equals(asset.getStatus())) {
                if (!Objects.equals(asset.getOwnerId(), requireAdmin())
                        || !asset.getExpiresAt().isAfter(LocalDateTime.now())) {
                    throw new ArticleException("候选封面已过期或不属于当前用户，请重新生成");
                }
                mapper.attach(asset.getId(), articleId);
            } else if (!isAttachedTo(asset, articleId)) {
                throw new ArticleException("候选封面不可用，请重新生成");
            }
        } else {
            String canonicalUrl = imageUrl == null ? "" : imageUrl.split("\\?", 2)[0];
            asset = canonicalUrl.isBlank() ? null : mapper.getByImageUrlForUpdate(canonicalUrl);
            if (asset != null && (!isAttachedTo(asset, articleId)
                    || !Objects.equals(asset.getImageUrl(), imageUrl))) {
                throw new ArticleException("请通过 AI 封面预览中的采用按钮选择图片");
            }
            if (asset == null && canonicalUrl.contains("/image/ai-cover-")) {
                throw new ArticleException("AI 封面不可直接复用，请重新生成");
            }
        }
        mapper.markReplaced(articleId, asset == null ? null : asset.getId());
    }

    public void markArticleDeleted(Long articleId) {
        mapper.markReplaced(articleId, null);
    }

    @Transactional
    public void discard(String id) {
        AiCoverAsset asset = mapper.getByIdForUpdate(id);
        if (asset == null) {
            return;
        }
        if (!Objects.equals(asset.getOwnerId(), requireAdmin())
                || !AiCoverAsset.CANDIDATE.equals(asset.getStatus())) {
            throw new ArticleException("只能放弃当前用户尚未采用的候选封面");
        }
        mapper.markDeleting(id);
    }

    public String previewUrl(String imageUrl) {
        return oss.generateAccessUrls(imageUrl);
    }

    private boolean isAttachedTo(AiCoverAsset asset, Long articleId) {
        return AiCoverAsset.ATTACHED.equals(asset.getStatus())
                && Objects.equals(articleId, asset.getArticleId());
    }

    private Long requireAdmin() {
        Long id = BaseContext.getCurrentId();
        if (id == null) {
            throw new ArticleException("请先登录管理端");
        }
        return id;
    }

    private byte[] normalizeImage(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > 20 * 1024 * 1024) {
            throw new ArticleException("生图平台返回的图片为空或超过20MB");
        }
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new ArticleException("生图平台未返回有效图片");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName();
                if (!"png".equalsIgnoreCase(format) && !"jpeg".equalsIgnoreCase(format)) {
                    throw new ArticleException("生图平台必须返回 PNG 或 JPEG 图片");
                }
                if (reader.getWidth(0) > 4096 || reader.getHeight(0) > 4096) {
                    throw new ArticleException("生成图片尺寸不能超过4096像素");
                }
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ImageIO.write(reader.read(0), "png", output);
                if (output.size() > 20 * 1024 * 1024) {
                    throw new ArticleException("生成图片超过20MB，请调整平台输出尺寸");
                }
                return output.toByteArray();
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new ArticleException("生图平台返回的图片无法读取");
        }
    }
}
