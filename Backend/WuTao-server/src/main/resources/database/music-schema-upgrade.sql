-- Existing database upgrade: run once before deploying the music module.

UPDATE music
SET artist = COALESCE(artist, ''),
    sort = COALESCE(sort, 0),
    is_visible = COALESCE(is_visible, 1),
    has_lyric = CASE
                    WHEN lyric_url IS NULL OR TRIM(lyric_url) = '' THEN 0
                    ELSE 1
                END,
    lyric_type = CASE
                    WHEN lyric_url IS NULL OR TRIM(lyric_url) = '' THEN NULL
                    ELSE lyric_type
                 END;

UPDATE music
SET duration = 0
WHERE duration < 0;

ALTER TABLE music
    ADD COLUMN album VARCHAR(100) NOT NULL DEFAULT '' COMMENT '专辑名称' AFTER artist,
    MODIFY COLUMN title VARCHAR(100) NOT NULL COMMENT '音乐标题',
    MODIFY COLUMN artist VARCHAR(100) NOT NULL DEFAULT '' COMMENT '作者',
    MODIFY COLUMN duration INT UNSIGNED NULL COMMENT '时长，单位：秒',
    MODIFY COLUMN cover_image VARCHAR(1024) NULL COMMENT '封面图片url',
    MODIFY COLUMN music_url VARCHAR(1024) NOT NULL COMMENT '音频文件url',
    MODIFY COLUMN lyric_url VARCHAR(1024) NULL COMMENT '歌词文件url',
    MODIFY COLUMN has_lyric TINYINT NOT NULL DEFAULT 0 COMMENT '是否有歌词，0-否，1-是',
    MODIFY COLUMN lyric_type VARCHAR(16) NULL COMMENT '歌词类型,lrc,json,txt',
    MODIFY COLUMN sort INT NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    MODIFY COLUMN is_visible TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见',
    DROP INDEX idx_sort_visible,
    ADD INDEX idx_visible_sort (is_visible, sort, id DESC);
