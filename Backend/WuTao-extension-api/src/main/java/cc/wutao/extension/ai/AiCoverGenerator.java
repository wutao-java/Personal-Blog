package cc.wutao.extension.ai;

/**
 * Optional image-provider boundary. Implementations return image bytes, never remote URLs.
 */
public interface AiCoverGenerator {

    boolean isAvailable();

    byte[] generate(String title, String contentMarkdown);
}
