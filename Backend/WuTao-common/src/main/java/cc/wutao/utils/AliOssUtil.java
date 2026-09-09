package cc.wutao.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import cc.wutao.constant.MessageConstant;
import cc.wutao.exception.UploadFileErrorException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private static final long ACCESS_URL_TTL_HOURS = 24;

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     * @param bytes 文件字节数组
     * @param extension 文件后缀
     * @param fileName 文件名
     * @return
     */
    public String upload(byte[] bytes, String extension, String fileName) {

        String objectName = getFileCategory(extension) + "/" + fileName;

        OSS ossClient = null;

        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            log.error("OSS上传失败: objectName={}, errorCode={}, requestId={}",
                    objectName, oe.getErrorCode(), oe.getRequestId(), oe);
            throw new UploadFileErrorException(MessageConstant.UPLOAD_FAILED);
        } catch (ClientException ce) {
            log.error("OSS客户端异常: objectName={}", objectName, ce);
            throw new UploadFileErrorException(MessageConstant.UPLOAD_FAILED);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder);

        return stringBuilder.toString();
    }

    public String generateAccessUrls(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }

        String urlPrefix = "https://" + bucketName + "." + endpoint + "/";
        Pattern urlPattern = Pattern.compile(
                Pattern.quote(urlPrefix) + "([a-z0-9_-]+/[a-z0-9._-]+)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(content);
        if (!matcher.find()) {
            return content;
        }

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build("https://" + endpoint, accessKeyId, accessKeySecret);
            Date expiration = Date.from(Instant.now().plus(ACCESS_URL_TTL_HOURS, ChronoUnit.HOURS));
            StringBuffer result = new StringBuffer();
            do {
                String signedUrl = ossClient
                        .generatePresignedUrl(bucketName, matcher.group(1), expiration)
                        .toString();
                matcher.appendReplacement(result, Matcher.quoteReplacement(signedUrl));
            } while (matcher.find());
            matcher.appendTail(result);
            return result.toString();
        } catch (ClientException e) {
            log.error("OSS签名URL生成失败", e);
            return content;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 获取文件分类
     * @param extension
     * @return
     */
    public String getFileCategory(String extension) {
        switch (extension){
            // 图片
            case "jpg":
            case "png":
            case "gif":
            case "bmp":
            case "webp":
            case "jpeg":
            case "svg":
            case "ico":
            case "tiff":
                return "image";

            // 视频
            case "mp4":
            case "avi":
            case "mov":
            case "mkv":
            case "wmv":
            case "flv":
            case "webm":
            case "m4v":
            case "3gp":
                return "video";

            // 音频
            case "mp3":
            case "wav":
            case "wma":
            case "ogg":
            case "aac":
            case "flac":
            case "m4a":
            case "ape":
            case "mid":
            case "midi":
                return "audio";

            // 歌词
            case "lrc":
            case "lrcx":
            case "krc":
            case "qrc":
            case "trc":
            case "ksc":
                return "lyric";

            // 文档
            case "txt":
            case "md":
            case "rtf":
                return "text";

            case "pdf":
                return "pdf";

            case "doc":
            case "docx":
            case "dot":
            case "dotx":
                return "word";

            case "xls":
            case "xlsx":
            case "xlt":
            case "xltx":
                return "excel";

            // 压缩文件
            case "zip":
            case "rar":
            case "7z":
            case "tar":
            case "gz":
            case "bz2":
                return "archive";

            // 字体
            case "ttf":
            case "otf":
            case "woff":
            case "woff2":
            case "eot":
                return "font";

            default:
                return "other";
        }
    }
}
