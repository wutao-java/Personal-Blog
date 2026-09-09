<script setup>
import { computed, nextTick, ref } from 'vue'
import { useBlogStore } from '@/stores'
import SidebarCard from '@/components/SidebarCard.vue'
import demoCover from '@/assets/images/bgc.webp'

const demoTracks = [
  {
    id: 'demo-1',
    title: '晨间循环',
    artist: 'SoundHelix 示例',
    album: '清晨选集',
    duration: 372,
    coverImage: demoCover,
    coverFilter: 'none',
    musicUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3'
  },
  {
    id: 'demo-2',
    title: '城市夜行',
    artist: 'SoundHelix 示例',
    album: '午夜电台',
    duration: 425,
    coverImage: demoCover,
    coverFilter: 'hue-rotate(70deg) saturate(1.1)',
    musicUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3'
  },
  {
    id: 'demo-3',
    title: '窗边雨声',
    artist: 'SoundHelix 示例',
    album: '雨天手记',
    duration: 315,
    coverImage: demoCover,
    coverFilter: 'hue-rotate(155deg) saturate(0.9)',
    musicUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3'
  }
]

const blogStore = useBlogStore()
const audioRef = ref(null)
const currentIndex = ref(0)
const isPlaying = ref(false)
const currentTime = ref(0)
const audioDuration = ref(0)
const volume = ref(0.8)

const usingDemoTracks = computed(() => blogStore.musics.length === 0)
const tracks = computed(() =>
  usingDemoTracks.value ? demoTracks : blogStore.musics
)
const currentTrack = computed(() => tracks.value[currentIndex.value] || null)
const duration = computed(
  () => audioDuration.value || currentTrack.value?.duration || 0
)
const progress = computed(() =>
  duration.value ? (currentTime.value / duration.value) * 100 : 0
)

const formatTime = (seconds) => {
  const value = Number(seconds)
  if (!Number.isFinite(value) || value < 0) return '00:00'
  const minutes = Math.floor(value / 60)
  const remain = Math.floor(value % 60)
  return `${String(minutes).padStart(2, '0')}:${String(remain).padStart(2, '0')}`
}

const playCurrent = async () => {
  if (!audioRef.value || !currentTrack.value) return
  try {
    await audioRef.value.play()
  } catch {
    isPlaying.value = false
  }
}

const selectTrack = (index) => {
  if (index === currentIndex.value) {
    if (isPlaying.value) audioRef.value?.pause()
    else playCurrent()
    return
  }

  currentIndex.value = index
  currentTime.value = 0
  audioDuration.value = 0
  nextTick(() => {
    audioRef.value?.load()
    playCurrent()
  })
}

const togglePlayback = () => {
  if (isPlaying.value) audioRef.value?.pause()
  else playCurrent()
}

const playPrevious = () => {
  if (!tracks.value.length) return
  selectTrack(
    (currentIndex.value - 1 + tracks.value.length) % tracks.value.length
  )
}

const playNext = () => {
  if (!tracks.value.length) return
  selectTrack((currentIndex.value + 1) % tracks.value.length)
}

const updateProgress = () => {
  if (audioRef.value) currentTime.value = audioRef.value.currentTime
}

const setDuration = () => {
  if (audioRef.value && Number.isFinite(audioRef.value.duration)) {
    audioDuration.value = audioRef.value.duration
    audioRef.value.volume = volume.value
  }
}

const seek = (event) => {
  if (!audioRef.value || !duration.value) return
  const nextTime = (Number(event.target.value) / 100) * duration.value
  audioRef.value.currentTime = nextTime
  currentTime.value = nextTime
}

const setVolume = (event) => {
  volume.value = Number(event.target.value)
  if (audioRef.value) audioRef.value.volume = volume.value
}
</script>

<template>
  <div class="music-page">
    <div class="music-content">
      <section class="playlist" aria-labelledby="playlist-title">
        <div class="playlist-inner">
          <h1 id="playlist-title" class="visually-hidden">音乐</h1>

          <div v-if="tracks.length" class="playlist-columns" aria-hidden="true">
            <span>歌曲 / 歌手</span>
            <span>专辑</span>
            <span>时长</span>
          </div>

          <div v-if="tracks.length" class="track-list">
            <button
              v-for="(track, index) in tracks"
              :key="track.id"
              type="button"
              class="track-row"
              :class="{
                active: index === currentIndex,
                playing: index === currentIndex && isPlaying
              }"
              :aria-label="`${index === currentIndex && isPlaying ? '暂停' : '播放'} ${track.title}`"
              :aria-pressed="index === currentIndex"
              @click="selectTrack(index)"
            >
              <span class="track-main">
                <span class="track-cover">
                  <img
                    v-if="track.coverImage"
                    :src="track.coverImage"
                    :alt="track.title"
                    :style="{ filter: track.coverFilter }"
                  />
                  <i v-else class="iconfont icon-yinle" />
                  <span class="cover-play" aria-hidden="true">
                    <i
                      class="iconfont"
                      :class="
                        index === currentIndex && isPlaying
                          ? 'icon-zanting'
                          : 'icon-play-full'
                      "
                    />
                  </span>
                </span>
                <span class="track-copy">
                  <span class="track-title">{{ track.title }}</span>
                  <span class="track-artist">
                    {{ track.artist || '未知音乐人' }}
                  </span>
                </span>
              </span>
              <span class="track-album">{{ track.album || '单曲' }}</span>
              <span class="track-duration">{{
                formatTime(track.duration)
              }}</span>
            </button>
          </div>

          <div v-else class="playlist-empty">
            <i class="iconfont icon-yinle" />
            <h2>歌单还是空的</h2>
            <p>公开歌曲后，它们会出现在这里。</p>
          </div>
        </div>
      </section>

      <SidebarCard />
    </div>

    <div
      v-if="currentTrack"
      class="player-dock"
      role="region"
      aria-label="音乐播放器"
    >
      <div class="dock-track">
        <span class="dock-cover">
          <img
            v-if="currentTrack.coverImage"
            :src="currentTrack.coverImage"
            :alt="currentTrack.title"
            :style="{ filter: currentTrack.coverFilter }"
          />
          <i v-else class="iconfont icon-yinle" />
        </span>
        <span class="dock-info">
          <strong>{{ currentTrack.title }}</strong>
          <small>{{ currentTrack.artist || '未知音乐人' }}</small>
        </span>
      </div>

      <div class="dock-center">
        <div class="dock-controls">
          <button type="button" title="上一首" @click="playPrevious">
            <i class="iconfont icon-next previous-icon" />
          </button>
          <button
            type="button"
            class="main-control"
            :title="isPlaying ? '暂停' : '播放'"
            @click="togglePlayback"
          >
            <i
              class="iconfont"
              :class="isPlaying ? 'icon-zanting' : 'icon-play-full'"
            />
          </button>
          <button type="button" title="下一首" @click="playNext">
            <i class="iconfont icon-next" />
          </button>
        </div>
        <div class="progress-row">
          <span>{{ formatTime(currentTime) }}</span>
          <input
            type="range"
            min="0"
            max="100"
            step="0.1"
            :value="progress"
            :style="{ '--range-progress': `${progress}%` }"
            aria-label="播放进度"
            @input="seek"
          />
          <span>{{ formatTime(duration) }}</span>
        </div>
      </div>

      <label class="volume-control" title="音量">
        <i class="iconfont icon-yinle" aria-hidden="true" />
        <input
          type="range"
          min="0"
          max="1"
          step="0.05"
          :value="volume"
          :style="{ '--range-progress': `${volume * 100}%` }"
          aria-label="音量"
          @input="setVolume"
        />
      </label>
    </div>

    <audio
      ref="audioRef"
      :src="currentTrack?.musicUrl"
      preload="metadata"
      @loadedmetadata="setDuration"
      @timeupdate="updateProgress"
      @play="isPlaying = true"
      @pause="isPlaying = false"
      @ended="playNext"
    />
  </div>
</template>

<style scoped>
.music-page {
  --music-accent: #1fbd75;
  --music-accent-hover: #18aa68;
  --music-ink: #252a27;
  --music-muted: #68706b;
  --music-line: #e6e9e7;
  --music-row: #f3f5f4;
  --music-row-hover: #e9eeeb;
  min-height: 0;
  padding: 0 0 90px;
  overflow-x: hidden;
  background: transparent;
  color: var(--music-ink);
  color-scheme: light;
}

:global(html.dark) .music-page {
  --music-ink: #252a27;
  --music-muted: #68706b;
  --music-line: #e6e9e7;
  background: transparent;
}

.music-content {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.playlist {
  min-width: 0;
  flex: 1;
  padding: 0;
}

.playlist-inner {
  width: 100%;
  margin: 0 auto;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.playlist-columns,
.track-row {
  display: grid;
  grid-template-columns: minmax(320px, 1.45fr) minmax(180px, 0.65fr) 72px;
  align-items: center;
  column-gap: clamp(24px, 5vw, 88px);
}

.playlist-columns {
  min-height: 30px;
  padding: 0 14px;
  color: var(--music-muted);
  font-size: 12px;
}

.playlist-columns span:last-child {
  text-align: right;
}

.track-list {
  display: grid;
  gap: 2px;
}

.track-row {
  width: 100%;
  min-height: 62px;
  padding: 8px 14px;
  border: 0;
  border-radius: 4px;
  background: #fff;
  color: var(--music-ink);
  font: inherit;
  text-align: left;
  cursor: pointer;
  transition:
    background 0.18s ease,
    box-shadow 0.18s ease;
}

.track-row:nth-child(odd) {
  background: var(--music-row);
}

.track-row:hover {
  background: var(--music-row-hover);
}

.track-row.active {
  background: #e9f7ef;
  box-shadow: inset 3px 0 0 var(--music-accent);
}

.track-main {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.track-cover,
.dock-cover {
  position: relative;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 4px;
  background: #e4e8e6;
  color: var(--music-muted);
}

.track-cover {
  width: 44px;
  height: 44px;
}

.track-cover img,
.dock-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-play {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(22, 27, 24, 0.56);
  color: #fff;
  opacity: 0;
  transition: opacity 0.18s ease;
}

.track-row:hover .cover-play,
.track-row.playing .cover-play {
  opacity: 1;
}

.cover-play .iconfont {
  font-size: 14px;
}

.track-copy,
.dock-info {
  min-width: 0;
  display: grid;
}

.track-title,
.track-artist,
.track-album,
.dock-info strong,
.dock-info small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.track-title {
  color: var(--music-ink);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.45;
}

.track-row.active .track-title {
  color: #0e7446;
}

.track-artist,
.track-album,
.track-duration {
  color: var(--music-muted);
  font-size: 12px;
}

.track-duration {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  text-align: right;
}

.playlist-empty {
  min-height: calc(100dvh - 200px);
  display: grid;
  place-items: center;
  align-content: center;
  color: var(--music-muted);
  text-align: center;
}

.playlist-empty .iconfont {
  margin-bottom: 12px;
  color: var(--music-accent);
  font-size: 34px;
}

.playlist-empty h2 {
  margin: 0;
  color: var(--music-ink);
  font-size: 17px;
}

.playlist-empty p {
  margin: 4px 0 0;
  font-size: 13px;
}

.player-dock {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 90;
  min-height: 90px;
  padding: 8px clamp(18px, 2.25vw, 42px);
  display: grid;
  grid-template-columns: minmax(220px, 1fr) minmax(360px, 2fr) minmax(
      180px,
      1fr
    );
  align-items: center;
  gap: clamp(18px, 3vw, 56px);
  border-top: 1px solid var(--music-line);
  background: rgba(252, 253, 252, 0.96);
  box-shadow: 0 -8px 26px rgba(31, 44, 36, 0.06);
  backdrop-filter: blur(16px) saturate(1.15);
  -webkit-backdrop-filter: blur(16px) saturate(1.15);
}

.dock-track {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.dock-cover {
  width: 52px;
  height: 52px;
  background: #e5e9e7;
  color: var(--music-accent);
}

.dock-info {
  gap: 1px;
}

.dock-info strong {
  color: var(--music-ink);
  font-size: 13px;
  font-weight: 600;
}

.dock-info small {
  color: var(--music-muted);
  font-size: 11px;
}

.dock-center {
  min-width: 0;
}

.dock-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
}

.dock-controls button {
  width: 30px;
  height: 30px;
  padding: 0;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: #323834;
  cursor: pointer;
  transition:
    color 0.18s ease,
    background 0.18s ease,
    transform 0.18s ease;
}

.dock-controls button:hover {
  background: #edf0ee;
  color: #111512;
}

.dock-controls .main-control {
  width: 40px;
  height: 40px;
  background: var(--music-accent);
  color: #0d2618;
}

.dock-controls .main-control:hover {
  background: var(--music-accent-hover);
  color: #07170e;
}

.previous-icon {
  transform: rotate(180deg);
}

.progress-row {
  display: grid;
  grid-template-columns: 42px minmax(120px, 1fr) 42px;
  align-items: center;
  gap: 9px;
  color: var(--music-muted);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 10px;
}

.progress-row span:last-child {
  text-align: right;
}

.music-page input[type='range'] {
  width: 100%;
  height: 16px;
  margin: 0;
  appearance: none;
  background: transparent;
  cursor: pointer;
}

.music-page input[type='range']::-webkit-slider-runnable-track {
  height: 3px;
  border-radius: 2px;
  background: linear-gradient(
    to right,
    var(--music-accent) 0 var(--range-progress),
    #dfe3e1 var(--range-progress) 100%
  );
}

.music-page input[type='range']::-webkit-slider-thumb {
  width: 10px;
  height: 10px;
  margin-top: -3.5px;
  appearance: none;
  border: 0;
  border-radius: 50%;
  background: #202622;
}

.music-page input[type='range']::-moz-range-track {
  height: 3px;
  border-radius: 2px;
  background: #dfe3e1;
}

.music-page input[type='range']::-moz-range-progress {
  height: 3px;
  border-radius: 2px;
  background: var(--music-accent);
}

.music-page input[type='range']::-moz-range-thumb {
  width: 10px;
  height: 10px;
  border: 0;
  border-radius: 50%;
  background: #202622;
}

.volume-control {
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  color: var(--music-muted);
}

.volume-control .iconfont {
  font-size: 16px;
}

.volume-control input {
  max-width: 118px;
}

.music-page button:focus-visible,
.music-page input:focus-visible {
  outline: 2px solid #148653;
  outline-offset: 2px;
}

.music-page button:active {
  transform: scale(0.98);
}

@media (prefers-reduced-motion: reduce) {
  .track-row,
  .cover-play,
  .dock-controls button {
    transition: none;
  }
}

@media (prefers-reduced-transparency: reduce) {
  .player-dock {
    background: #fcfdfc;
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
  }
}

@media (max-width: 960px) {
  .music-content {
    flex-direction: column;
  }

  .playlist {
    width: 100%;
  }

  .playlist-columns,
  .track-row {
    grid-template-columns: minmax(260px, 1.3fr) minmax(140px, 0.6fr) 64px;
    column-gap: 28px;
  }

  .player-dock {
    grid-template-columns: minmax(190px, 0.9fr) minmax(320px, 1.6fr) 130px;
    gap: 20px;
  }
}

@media (max-width: 760px) {
  .music-page {
    padding-bottom: 76px;
  }

  .playlist {
    padding: 0;
  }

  .playlist-columns,
  .track-row {
    grid-template-columns: minmax(0, 1fr) 64px;
    column-gap: 14px;
  }

  .playlist-columns {
    padding: 0 10px;
  }

  .playlist-columns span:nth-child(2),
  .track-album {
    display: none;
  }

  .track-row {
    min-height: 64px;
    padding: 8px 10px;
  }

  .track-cover {
    width: 46px;
    height: 46px;
  }

  .player-dock {
    min-height: 76px;
    padding: 8px 14px;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 12px;
  }

  .dock-cover {
    width: 48px;
    height: 48px;
  }

  .dock-center {
    display: flex;
    align-items: center;
  }

  .dock-controls {
    gap: 6px;
  }

  .progress-row,
  .volume-control {
    display: none;
  }
}

@media (max-width: 480px) {
  .playlist-columns {
    display: none;
  }

  .track-row {
    grid-template-columns: minmax(0, 1fr);
  }

  .track-duration {
    display: none;
  }

  .dock-info strong,
  .dock-info small {
    max-width: 128px;
  }
}

@media (max-width: 360px) {
  .dock-info small,
  .dock-controls button:not(.main-control) {
    display: none;
  }
}
</style>
