package cc.wutao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 音乐DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicDTO implements Serializable {

    private Long id;

    // 音乐标题
    @NotBlank(message = "音乐标题不能为空")
    @Size(max = 100, message = "音乐标题不能超过100字")
    private String title;

    // 作者
    @Size(max = 100, message = "作者名称不能超过100字")
    private String artist;

    // 专辑
    @Size(max = 100, message = "专辑名称不能超过100字")
    private String album;

    // 时长，单位：秒
    @Min(value = 0, message = "时长不能小于0")
    private Integer duration;

    // 封面图片url
    @Size(max = 1024, message = "封面地址不能超过1024个字符")
    private String coverImage;

    // 音频文件url
    @NotBlank(message = "音频文件不能为空")
    @Size(max = 1024, message = "音频地址不能超过1024个字符")
    private String musicUrl;

    // 歌词文件url
    @Size(max = 1024, message = "歌词地址不能超过1024个字符")
    private String lyricUrl;

    // 是否有歌词，0-否，1-是
    private Integer hasLyric;

    // 歌词类型,lrc,json,txt
    @Size(max = 16, message = "歌词类型不能超过16字")
    private String lyricType;

    // 排序，越小越靠前
    @Min(value = 0, message = "排序不能小于0")
    private Integer sort;

    // 是否可见
    @Min(value = 0, message = "可见状态只能是0或1")
    @Max(value = 1, message = "可见状态只能是0或1")
    private Integer isVisible;
}
