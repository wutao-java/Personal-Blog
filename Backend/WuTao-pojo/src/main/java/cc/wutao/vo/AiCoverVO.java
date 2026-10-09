package cc.wutao.vo;

import java.time.LocalDateTime;

public record AiCoverVO(String id, String imageUrl, String previewUrl, LocalDateTime expiresAt) {
}
