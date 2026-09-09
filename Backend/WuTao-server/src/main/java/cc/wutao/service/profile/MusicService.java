package cc.wutao.service.profile;

import cc.wutao.dto.MusicDTO;
import cc.wutao.dto.MusicPageQueryDTO;
import cc.wutao.entity.Music;
import cc.wutao.exception.MusicException;
import cc.wutao.exception.ValidationException;
import cc.wutao.mapper.MusicMapper;
import cc.wutao.result.PageResult;
import cc.wutao.vo.MusicVO;
import cc.wutao.constant.MessageConstant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicMapper musicMapper;

    /**
     * 添加音乐
     * @param music
     */
    @CacheEvict(value = "musicList", allEntries = true)
    public void addMusic(MusicDTO musicDTO) {
        musicMapper.insert(toMusic(musicDTO));
    }

    /**
     * 分页查询音乐列表
     * @param musicPageQueryDTO
     * @return
     */
    public PageResult<Music> pageQuery(MusicPageQueryDTO musicPageQueryDTO) {
        PageHelper.startPage(musicPageQueryDTO.getPage(), musicPageQueryDTO.getPageSize());
        Page<Music> page = musicMapper.pageQuery(musicPageQueryDTO);
        long total = page.getTotal();
        List<Music> records = page.getResult();
        return new PageResult<>(total, records);
    }

    /**
     * 更新音乐
     * @param music
     */
    @CacheEvict(value = "musicList", allEntries = true)
    public void updateMusic(MusicDTO musicDTO) {
        if (musicDTO.getId() == null) {
            throw new ValidationException(MessageConstant.MUSIC_ID_REQUIRED);
        }
        getById(musicDTO.getId());
        musicMapper.update(toMusic(musicDTO));
    }

    /**
     * 批量删除音乐
     * @param ids
     */
    @CacheEvict(value = "musicList", allEntries = true)
    public void batchDelete(List<Long> ids) {
        musicMapper.batchDelete(ids);
    }

    /**
     * 根据ID查询音乐
     * @param id
     * @return
     */
    public Music getById(Long id) {
        Music music = musicMapper.getById(id);
        if (music == null) {
            throw new MusicException(MessageConstant.MUSIC_NOT_FOUND);
        }
        return music;
    }

    /**
     * 获取所有可见的音乐
     * @return
     */
    @Cacheable(value = "musicList", key = "'visible'")
    public List<MusicVO> getAllVisibleMusic() {
        List<Music> musicList = musicMapper.getAllVisibleMusic();
        if(musicList != null && !musicList.isEmpty()) {
            // 转换为VO
            List<MusicVO> musicVOList = musicList.stream().map(music -> MusicVO.builder()
                    .id(music.getId())
                    .title(music.getTitle())
                    .artist(music.getArtist())
                    .album(music.getAlbum())
                    .duration(music.getDuration())
                    .coverImage(music.getCoverImage())
                    .musicUrl(music.getMusicUrl())
                    .lyricUrl(music.getLyricUrl())
                    .hasLyric(hasLyric(music.getLyricUrl()))
                    .lyricType(resolveLyricType(music.getLyricUrl(), music.getLyricType()))
                    .build()
            ).toList();
            return musicVOList;
        }
        return Collections.emptyList();
    }

    private Music toMusic(MusicDTO musicDTO) {
        Music music = new Music();
        BeanUtils.copyProperties(musicDTO, music);
        music.setArtist(StringUtils.hasText(musicDTO.getArtist()) ? musicDTO.getArtist().trim() : "");
        music.setAlbum(StringUtils.hasText(musicDTO.getAlbum()) ? musicDTO.getAlbum().trim() : "");
        String lyricUrl = StringUtils.hasText(musicDTO.getLyricUrl()) ? musicDTO.getLyricUrl().trim() : null;
        music.setLyricUrl(lyricUrl);
        music.setHasLyric(hasLyric(lyricUrl));
        music.setLyricType(resolveLyricType(lyricUrl, musicDTO.getLyricType()));
        music.setSort(musicDTO.getSort() == null ? 0 : musicDTO.getSort());
        music.setIsVisible(musicDTO.getIsVisible() == null ? 1 : musicDTO.getIsVisible());
        return music;
    }

    private int hasLyric(String lyricUrl) {
        return StringUtils.hasText(lyricUrl) ? 1 : 0;
    }

    private String resolveLyricType(String lyricUrl, String lyricType) {
        if (!StringUtils.hasText(lyricUrl)) {
            return null;
        }
        if (StringUtils.hasText(lyricType)) {
            return lyricType.trim().toLowerCase(Locale.ROOT);
        }

        String path = lyricUrl.split("\\?", 2)[0];
        int extensionIndex = path.lastIndexOf('.');
        return extensionIndex < 0 ? null : path.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }
}
