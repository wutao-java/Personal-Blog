package cc.wutao.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.net.URI;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AliOssUtilTest {

    @Test
    void deleteUsesRecordedObjectKeyAndAlwaysClosesClient() {
        OSS client = mock(OSS.class);
        try (MockedConstruction<OSSClientBuilder> ignored = mockConstruction(
                OSSClientBuilder.class,
                (builder, context) -> when(builder.build("endpoint", "access-key", "secret"))
                        .thenReturn(client))) {
            new AliOssUtil("endpoint", "access-key", "secret", "bucket")
                    .deleteObject("image/ai-cover-123.png");
            verify(client).deleteObject("bucket", "image/ai-cover-123.png");
            verify(client).shutdown();
        }
    }

    @Test
    void privateOssObjectUrlsAreSignedInsideResponseText() throws Exception {
        OSS ossClient = mock(OSS.class);
        when(ossClient.generatePresignedUrl(
                eq("bucket"), anyString(), any(Date.class)))
                .thenAnswer(invocation -> URI.create(
                        "https://signed.example/" + invocation.getArgument(1)
                                + "?signature=ok").toURL());
        try (MockedConstruction<OSSClientBuilder> ignored = mockConstruction(
                OSSClientBuilder.class,
                (builder, context) -> when(builder.build("https://endpoint", "access-key", "secret"))
                        .thenReturn(ossClient))) {
            AliOssUtil oss = new AliOssUtil("endpoint", "access-key", "secret", "bucket");
            String content = "cover=https://bucket.endpoint/image/cover.webp "
                    + "markdown=![](https://bucket.endpoint/image/content.webp)";

            String result = oss.generateAccessUrls(content);

            verify(ossClient).generatePresignedUrl(
                    eq("bucket"), eq("image/cover.webp"), any(Date.class));
            verify(ossClient).generatePresignedUrl(
                    eq("bucket"), eq("image/content.webp"), any(Date.class));
            verify(ossClient).shutdown();
            assertEquals("cover=https://signed.example/image/cover.webp?signature=ok "
                    + "markdown=![](https://signed.example/image/content.webp?signature=ok)", result);
        }
    }

    @Test
    void nestedOssObjectKeyIsSignedWithoutTruncation() throws Exception {
        OSS ossClient = mock(OSS.class);
        when(ossClient.generatePresignedUrl(
                eq("bucket"), anyString(), any(Date.class)))
                .thenAnswer(invocation -> URI.create(
                        "https://signed.example/" + invocation.getArgument(1)
                                + "?signature=ok").toURL());
        try (MockedConstruction<OSSClientBuilder> ignored = mockConstruction(
                OSSClientBuilder.class,
                (builder, context) -> when(builder.build("https://endpoint", "access-key", "secret"))
                        .thenReturn(ossClient))) {
            AliOssUtil oss = new AliOssUtil("endpoint", "access-key", "secret", "bucket");
            String url = "https://bucket.endpoint/image/article-covers/2026-10-09/article-001.webp";

            String result = oss.generateAccessUrls("cover=" + url + " markdown=![](" + url + ")");

            verify(ossClient, org.mockito.Mockito.times(2)).generatePresignedUrl(
                    eq("bucket"), eq("image/article-covers/2026-10-09/article-001.webp"), any(Date.class));
            assertEquals("cover=https://signed.example/image/article-covers/2026-10-09/article-001.webp?signature=ok "
                    + "markdown=![](https://signed.example/image/article-covers/2026-10-09/article-001.webp?signature=ok)",
                    result);
        }
    }
}
