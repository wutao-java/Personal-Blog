package cc.wutao.service.system;

import cc.wutao.exception.UploadFileErrorException;
import cc.wutao.properties.ImageProperties;
import cc.wutao.utils.AliOssUtil;
import cc.wutao.utils.ImageCompressUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonServiceTest {

    private AliOssUtil oss;
    private ImageCompressUtil imageCompressUtil;
    private ImageProperties imageProperties;
    private CommonService service;

    @BeforeEach
    void setUp() {
        oss = mock(AliOssUtil.class);
        imageCompressUtil = mock(ImageCompressUtil.class);
        imageProperties = mock(ImageProperties.class);
        service = new CommonService(oss, imageCompressUtil, imageProperties);
    }

    @Test
    void missingOriginalFilenameIsReportedAsUploadError() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        assertThrows(UploadFileErrorException.class, () -> service.uploadFile(file));
    }

    @Test
    void uppercaseImageExtensionIsNormalizedBeforeCompression() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        byte[] original = {1};
        byte[] compressed = {2};
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("PHOTO.PNG");
        when(file.getBytes()).thenReturn(original);
        when(oss.getFileCategory("png")).thenReturn("image");
        when(imageCompressUtil.compress(file)).thenReturn(compressed);
        when(imageProperties.getOutPutFormat()).thenReturn("webp");
        when(oss.upload(eq(compressed), eq("webp"), anyString())).thenReturn("https://cdn/image.webp");

        String result = service.uploadFile(file);

        assertEquals("https://cdn/image.webp", result);
        verify(oss).upload(eq(compressed), eq("webp"), endsWith(".webp"));
    }
}
