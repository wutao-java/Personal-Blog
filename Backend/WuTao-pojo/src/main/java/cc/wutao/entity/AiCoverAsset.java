package cc.wutao.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCoverAsset {

    public static final String UPLOADING = "UPLOADING";
    public static final String CANDIDATE = "CANDIDATE";
    public static final String ATTACHED = "ATTACHED";
    public static final String DELETING = "DELETING";

    private String id;
    private Long ownerId;
    private Long articleId;
    private String objectKey;
    private String imageUrl;
    private String status;
    private LocalDateTime expiresAt;
}
