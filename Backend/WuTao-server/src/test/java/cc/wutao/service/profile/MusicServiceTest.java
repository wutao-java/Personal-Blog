package cc.wutao.service.profile;

import cc.wutao.dto.MusicDTO;
import cc.wutao.entity.Music;
import cc.wutao.exception.MusicException;
import cc.wutao.mapper.MusicMapper;
import cc.wutao.vo.MusicVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MusicServiceTest {

    private MusicMapper musicMapper;
    private MusicService musicService;

    @BeforeEach
    void setUp() {
        musicMapper = mock(MusicMapper.class);
        musicService = new MusicService(musicMapper);
    }

    @Test
    void addMusicShouldDeriveLyricMetadataAndDefaultValues() {
        MusicDTO musicDTO = MusicDTO.builder()
                .title("Song")
                .artist("Artist")
                .album("Album")
                .musicUrl("https://cdn.example.com/audio/song.mp3")
                .lyricUrl("https://cdn.example.com/lyric/song.lrc")
                .build();

        musicService.addMusic(musicDTO);

        ArgumentCaptor<Music> captor = ArgumentCaptor.forClass(Music.class);
        verify(musicMapper).insert(captor.capture());
        Music savedMusic = captor.getValue();
        assertEquals("Album", savedMusic.getAlbum());
        assertEquals(1, savedMusic.getHasLyric());
        assertEquals("lrc", savedMusic.getLyricType());
        assertEquals(0, savedMusic.getSort());
        assertEquals(1, savedMusic.getIsVisible());
    }

    @Test
    void addMusicShouldClearLyricTypeWhenThereIsNoLyricUrl() {
        MusicDTO musicDTO = MusicDTO.builder()
                .title("Song")
                .musicUrl("https://cdn.example.com/audio/song.mp3")
                .lyricType("lrc")
                .build();

        musicService.addMusic(musicDTO);

        ArgumentCaptor<Music> captor = ArgumentCaptor.forClass(Music.class);
        verify(musicMapper).insert(captor.capture());
        Music savedMusic = captor.getValue();
        assertEquals("", savedMusic.getArtist());
        assertEquals("", savedMusic.getAlbum());
        assertEquals(0, savedMusic.getHasLyric());
        assertNull(savedMusic.getLyricType());
    }

    @Test
    void getAllVisibleMusicShouldReturnAlbumAndDerivedLyricFlag() {
        Music music = Music.builder()
                .id(1L)
                .title("Song")
                .artist("Artist")
                .album("Album")
                .musicUrl("https://cdn.example.com/audio/song.mp3")
                .lyricUrl("https://cdn.example.com/lyric/song.lrc")
                .hasLyric(0)
                .build();
        when(musicMapper.getAllVisibleMusic()).thenReturn(List.of(music));

        List<MusicVO> result = musicService.getAllVisibleMusic();

        assertEquals(1, result.size());
        assertEquals("Album", result.getFirst().getAlbum());
        assertEquals(1, result.getFirst().getHasLyric());
    }

    @Test
    void getByIdShouldRejectMissingMusic() {
        when(musicMapper.getById(1L)).thenReturn(null);

        assertThrows(MusicException.class, () -> musicService.getById(1L));
    }
}
