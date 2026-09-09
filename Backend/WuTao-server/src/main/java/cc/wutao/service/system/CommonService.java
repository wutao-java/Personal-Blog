package cc.wutao.service.system;

import cc.wutao.constant.MessageConstant;
import cc.wutao.exception.UploadFileErrorException;
import cc.wutao.properties.ImageProperties;
import cc.wutao.utils.AliOssUtil;
import cc.wutao.utils.ImageCompressUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommonService {

    private final AliOssUtil aliOssUtil;
    private final ImageCompressUtil imageCompressUtil;
    private final ImageProperties imageProperties;

    /**
     * 文件上传
     * @param file
     */
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new UploadFileErrorException(MessageConstant.FILE_EMPTY);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new UploadFileErrorException(MessageConstant.UPLOAD_FAILED);
        }
        try {
            int extensionIndex = originalFilename.lastIndexOf('.');
            String extension = extensionIndex >= 0
                    ? originalFilename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT)
                    : "";
            byte[] bytes = file.getBytes();

            // 如果是图片，先压缩再上传
            if ("image".equals(aliOssUtil.getFileCategory(extension))) {
                bytes = imageCompressUtil.compress(file);
                extension = imageProperties.getOutPutFormat().toLowerCase(Locale.ROOT);
            }

            String uuidFileName = UUID.randomUUID()
                    + (extension.isEmpty() ? "" : "." + extension);
            return aliOssUtil.upload(bytes, extension, uuidFileName);

        } catch (IOException e) {
            throw new UploadFileErrorException(MessageConstant.UPLOAD_FAILED);
        }
    }
}
