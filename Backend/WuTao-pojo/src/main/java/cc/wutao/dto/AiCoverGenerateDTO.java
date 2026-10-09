package cc.wutao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiCoverGenerateDTO(
        @NotBlank(message = "请先填写文章标题")
        @Size(max = 50, message = "文章标题不能超过50字") String title,
        @NotBlank(message = "请先填写文章内容")
        @Size(max = 100000, message = "用于生成封面的内容不能超过十万字") String contentMarkdown) {
}
