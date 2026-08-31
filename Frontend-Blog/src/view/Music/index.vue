<script setup>
import { computed, nextTick, ref } from 'vue'
import { useBlogStore } from '@/stores'
import demoCover from '@/assets/images/bgc.webp'

const demoTracks = [
  {
    id: 'demo-1',
    title: '晨间循环',
    artist: 'SoundHelix 示例',
    duration: 372,
    coverImage: demoCover,
    coverFilter: 'none',
    musicUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3'
  },
  {
    id: 'demo-2',
    title: '城市夜行',
    artist: 'SoundHelix 示例',
    duration: 425,
    coverImage: demoCover,
    coverFilter: 'hue-rotate(70deg) saturate(1.1)',
    musicUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3'
  },
  {
    id: 'demo-3',
    title: '窗边雨声',
    artist: 'SoundHelix 示例',
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
const ambientFilter = computed(() => {
  const coverFilter = currentTrack.value?.coverFilter
  return coverFilter && coverFilter !== 'none'
    ? `${coverFilter} blur(34px)`
    : 'blur(34px)'
})
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
    <section class="music-hero" aria-labelledby="music-title">
      <div class="music-hero-inner">
        <div class="hero-copy">
          <h1 id="music-title">私人音乐馆</h1>
          <p class="hero-intro">让喜欢的旋律，在这里多停留一会儿。</p>

          <div v-if="currentTrack" class="hero-track liquid-glass">
            <span class="hero-track-copy">
              <span class="hero-track-label">
                {{ usingDemoTracks ? '示例试听' : '本周常听' }}
              </span>
              <strong>{{ currentTrack.title }}</strong>
              <span>{{ currentTrack.artist || '未知音乐人' }}</span>
            </span>
            <button
              type="button"
              class="hero-track-control"
              :title="isPlaying ? '暂停' : '播放'"
              :aria-label="isPlaying ? '暂停当前歌曲' : '播放当前歌曲'"
              @click="togglePlayback"
            >
              <i
                class="iconfont"
                :class="isPlaying ? 'icon-zanting' : 'icon-play-full'"
              />
            </button>
          </div>
        </div>

        <div class="record-stage" aria-hidden="true">
          <img
            v-if="currentTrack?.coverImage"
            class="record-ambient"
            :src="currentTrack.coverImage"
            alt=""
            :style="{ filter: ambientFilter }"
          />
          <div class="vinyl" :class="{ spinning: isPlaying }">
            <span class="vinyl-ring ring-one" />
            <span class="vinyl-ring ring-two" />
            <span class="vinyl-ring ring-three" />
            <div class="record-label">
              <img
                v-if="currentTrack?.coverImage"
                :src="currentTrack.coverImage"
                :alt="currentTrack.title"
                :style="{ filter: currentTrack.coverFilter }"
              />
              <span v-else>{{ currentTrack?.title?.slice(0, 1) || 'M' }}</span>
            </div>
            <span class="record-hole" />
          </div>
          <div class="cover-art">
            <img
              v-if="currentTrack?.coverImage"
              :src="currentTrack.coverImage"
              :alt="currentTrack.title"
              :style="{ filter: currentTrack.coverFilter }"
            />
            <div v-else class="cover-fallback">
              <i class="iconfont icon-yinle" />
              <span>WUTAO MUSIC</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="playlist" aria-labelledby="playlist-title">
      <div class="playlist-inner">
        <div class="playlist-heading">
          <h2 id="playlist-title">
            {{ usingDemoTracks ? '先听听这些' : '最近在听' }}
          </h2>
          <p class="track-count">共 {{ tracks.length }} 首，点击曲目即可播放</p>
        </div>

        <div v-if="tracks.length" class="track-list">
          <button
            v-for="(track, index) in tracks"
            :key="track.id"
            type="button"
            class="track-row"
            :class="{ active: index === currentIndex }"
            :aria-label="`${index === currentIndex && isPlaying ? '暂停' : '播放'} ${track.title}`"
            :aria-pressed="index === currentIndex"
            @click="selectTrack(index)"
          >
            <span class="track-index">{{ index + 1 }}</span>
            <span class="track-main">
              <span class="track-cover">
                <img
                  v-if="track.coverImage"
                  :src="track.coverImage"
                  :alt="track.title"
                  :style="{ filter: track.coverFilter }"
                />
                <i v-else class="iconfont icon-yinle" />
              </span>
              <span class="track-copy">
                <span class="track-title">{{ track.title }}</span>
                <span class="track-artist">
                  {{ track.artist || '未知音乐人' }}
                </span>
              </span>
            </span>
            <span class="track-duration">{{ formatTime(track.duration) }}</span>
            <span
              class="row-play"
              :title="index === currentIndex && isPlaying ? '暂停' : '播放'"
            >
              <i
                class="iconfont"
                :class="
                  index === currentIndex && isPlaying
                    ? 'icon-zanting'
                    : 'icon-play-full'
                "
              />
            </span>
          </button>
        </div>

        <div v-else class="playlist-empty">
          <div class="empty-disc"><i class="iconfont icon-yinle" /></div>
          <h3>歌单还是空的</h3>
          <p>公开歌曲后，它们会出现在这里。</p>
        </div>
      </div>
    </section>

    <div
      v-if="currentTrack"
      class="player-dock liquid-glass"
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
            aria-label="播放进度"
            @input="seek"
          />
          <span>{{ formatTime(duration) }}</span>
        </div>
      </div>

      <label class="volume-control">
        <span>音量</span>
        <input
          type="range"
          min="0"
          max="1"
          step="0.05"
          :value="volume"
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
  --music-green: #1fbd75;
  --music-green-dark: #148653;
  --music-ink: #142019;
  --music-soft: #eef6f1;
  --music-line: rgba(39, 62, 50, 0.1);
  --music-muted: #68766f;
  position: relative;
  isolation: isolate;
  min-height: 100dvh;
  padding-bottom: 64px;
  overflow-x: hidden;
  background: #fff;
  color: var(--music-ink);
}

:global(html.dark) .music-page {
  --music-ink: #142019;
  --music-soft: #eef6f1;
  --music-line: rgba(39, 62, 50, 0.1);
  --music-muted: #68766f;
}

.music-hero {
  position: relative;
  overflow: hidden;
  min-height: min(72vh, 660px);
  display: flex;
  align-items: center;
  background: #fff;
  color: var(--music-ink);
}

.music-hero-inner {
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
  padding: 92px 48px 64px;
  display: grid;
  grid-template-columns: minmax(320px, 0.82fr) minmax(430px, 1.18fr);
  align-items: center;
  gap: 84px;
}

.hero-copy {
  position: relative;
  z-index: 2;
}

.hero-copy h1 {
  margin: 0;
  font-family: var(--blog-sans);
  font-size: 56px;
  line-height: 1.08;
  font-weight: 760;
  letter-spacing: 0;
}

.hero-intro {
  max-width: 28em;
  margin: 18px 0 32px;
  color: #5f6e66;
  font-size: 17px;
  line-height: 1.7;
}

.hero-track {
  width: min(100%, 410px);
  min-height: 104px;
  padding: 17px 18px 17px 20px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 50px;
  align-items: center;
  gap: 20px;
  border-radius: 8px;
}

.hero-track-copy {
  min-width: 0;
  display: grid;
  gap: 3px;
}

.hero-track-label {
  margin-bottom: 2px;
  color: var(--music-green-dark);
  font-size: 12px;
  font-weight: 650;
}

.hero-track-copy strong,
.hero-track-copy > span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hero-track-copy strong {
  color: var(--music-ink);
  font-size: 23px;
  line-height: 1.3;
}

.hero-track-copy > span:last-child {
  color: var(--music-muted);
  font-size: 13px;
}

.hero-track-control {
  width: 50px;
  height: 50px;
  padding: 0;
  display: grid;
  place-items: center;
  align-items: center;
  border: 0;
  border-radius: 50%;
  background: var(--music-green);
  color: #0c2518;
  font-size: 17px;
  cursor: pointer;
  transition:
    background 0.25s,
    transform 0.25s;
}

.hero-track-control:hover {
  background: #49d99a;
  transform: scale(1.05);
}

.record-stage {
  position: relative;
  width: 100%;
  height: 372px;
  justify-self: end;
}

.record-ambient {
  position: absolute;
  inset: 64px 40px 28px 94px;
  width: calc(100% - 134px);
  height: calc(100% - 92px);
  object-fit: cover;
  opacity: 0.24;
  transform: scale(1.12);
  pointer-events: none;
}

.vinyl {
  position: absolute;
  top: 18px;
  right: 8px;
  width: 324px;
  height: 324px;
  border: 1px solid rgba(18, 28, 23, 0.2);
  border-radius: 50%;
  background:
    repeating-radial-gradient(
      circle at center,
      rgba(255, 255, 255, 0.055) 0,
      rgba(255, 255, 255, 0.055) 1px,
      transparent 2px,
      transparent 7px
    ),
    #111713;
  box-shadow: 0 30px 84px rgba(30, 62, 45, 0.2);
}

.vinyl.spinning {
  animation: vinyl-spin 8s linear infinite;
}

.vinyl-ring {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 50%;
}

.ring-one {
  inset: 16px;
}

.ring-two {
  inset: 34px;
}

.ring-three {
  inset: 53px;
}

.record-label {
  position: absolute;
  inset: 102px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 50%;
  background: var(--music-green);
  color: #0c2518;
  font-size: 28px;
  font-weight: 800;
}

.record-label img,
.cover-art img,
.track-cover img,
.dock-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.record-hole {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #17201c;
  transform: translate(-50%, -50%);
}

.cover-art {
  position: absolute;
  left: 4px;
  bottom: 6px;
  z-index: 2;
  width: 228px;
  height: 228px;
  overflow: hidden;
  border: 1px solid rgba(31, 55, 43, 0.14);
  border-radius: 6px;
  background: #25312b;
  box-shadow: 0 28px 72px rgba(29, 57, 42, 0.24);
  transform: rotate(-4deg);
  transition: transform 0.35s ease;
}

.record-stage:hover .cover-art {
  transform: rotate(-2deg) translateY(-5px);
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 14px;
  color: var(--music-green);
}

.cover-fallback .iconfont {
  font-size: 42px;
}

.cover-fallback span {
  color: #b6c3bc;
  font-size: 10px;
}

.playlist {
  padding: 64px 0 136px;
  background: linear-gradient(180deg, #eef6f1 0%, #f8fbf9 62%, #fff 100%);
}

.playlist-inner {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 48px;
}

.playlist-heading {
  max-width: 560px;
  margin-bottom: 30px;
}

.playlist-heading h2 {
  margin: 0;
  color: var(--music-ink);
  font-size: 32px;
  font-weight: 680;
  line-height: 1.2;
  letter-spacing: 0;
}

.track-count {
  margin: 10px 0 0;
  color: var(--music-muted);
  font-size: 13px;
  line-height: 1.6;
}

.track-list {
  display: grid;
  padding: 8px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.54);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.94),
    0 24px 70px rgba(44, 82, 61, 0.1);
  backdrop-filter: blur(20px) saturate(1.25);
  -webkit-backdrop-filter: blur(20px) saturate(1.25);
}

.track-row {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) 70px 40px;
  align-items: center;
  gap: 14px;
  width: 100%;
  min-height: 78px;
  padding: 10px 14px 10px 10px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--music-ink);
  font: inherit;
  text-align: left;
  cursor: pointer;
  transition:
    background 0.18s,
    transform 0.18s;
}

.track-row:hover {
  background: rgba(255, 255, 255, 0.76);
  transform: translateX(3px);
}

.track-row.active {
  background: rgba(221, 246, 232, 0.88);
  box-shadow: inset 3px 0 0 var(--music-green);
}

.track-row.active .track-title {
  color: var(--music-green);
}

.track-index {
  color: #8b9991;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  text-align: center;
}

.track-row.active .track-index {
  color: var(--music-green-dark);
  font-weight: 700;
}

.track-main {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 16px;
}

.track-cover,
.dock-cover {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  overflow: hidden;
  background: #dfe7e2;
  color: #748078;
}

.track-cover {
  width: 54px;
  height: 54px;
  border-radius: 6px;
}

.track-copy {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.track-title,
.track-artist {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.track-title {
  color: var(--music-ink);
  font-size: 15px;
  font-weight: 650;
}

.track-artist {
  color: var(--music-muted);
  font-size: 12px;
}

.track-duration {
  color: var(--music-muted);
  font-size: 12px;
  text-align: right;
}

.row-play {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border: 1px solid var(--music-line);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.64);
  color: var(--music-ink);
}

.track-row.active .row-play {
  border-color: transparent;
  background: var(--music-green);
  color: #0c2518;
}

.playlist-empty {
  min-height: 240px;
  display: grid;
  place-items: center;
  align-content: center;
  color: var(--music-muted);
  text-align: center;
}

.empty-disc {
  width: 70px;
  height: 70px;
  margin-bottom: 14px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--music-ink);
  color: var(--music-green);
}

.playlist-empty h3 {
  margin: 0;
  color: var(--music-ink);
  font-size: 17px;
}

.playlist-empty p {
  margin: 4px 0 0;
  font-size: 13px;
}

.liquid-glass {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.88);
  background:
    linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.82),
      rgba(255, 255, 255, 0.48) 46%,
      rgba(233, 247, 239, 0.54)
    ),
    rgba(255, 255, 255, 0.52);
  background-blend-mode: luminosity;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.98),
    inset 0 -1px 0 rgba(32, 84, 56, 0.11),
    inset 1px 0 0 rgba(255, 255, 255, 0.46),
    0 24px 64px rgba(36, 75, 53, 0.15);
  backdrop-filter: blur(30px) saturate(1.55) contrast(1.02);
  -webkit-backdrop-filter: blur(30px) saturate(1.55) contrast(1.02);
}

.liquid-glass::before {
  position: absolute;
  inset: 0;
  z-index: 0;
  padding: 1px;
  border-radius: inherit;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.96),
    rgba(255, 255, 255, 0.44) 28%,
    rgba(87, 139, 109, 0.1) 72%,
    rgba(44, 89, 65, 0.22)
  );
  content: '';
  pointer-events: none;
  -webkit-mask:
    linear-gradient(#fff 0 0) content-box,
    linear-gradient(#fff 0 0);
  mask:
    linear-gradient(#fff 0 0) content-box,
    linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
}

.liquid-glass::after {
  position: absolute;
  inset: 1px;
  z-index: 0;
  border-radius: inherit;
  background: linear-gradient(
    118deg,
    rgba(255, 255, 255, 0.42),
    transparent 28%,
    transparent 74%,
    rgba(255, 255, 255, 0.16)
  );
  content: '';
  pointer-events: none;
}

.liquid-glass > * {
  position: relative;
  z-index: 1;
}

.player-dock {
  position: fixed;
  left: 50%;
  bottom: 16px;
  z-index: 20;
  width: calc(100% - 96px);
  max-width: 1060px;
  min-height: 82px;
  margin: 0;
  padding: 11px 18px;
  display: grid;
  grid-template-columns: minmax(180px, 0.8fr) minmax(320px, 1.4fr) minmax(
      120px,
      0.6fr
    );
  align-items: center;
  gap: 24px;
  border-radius: 8px;
  color: var(--music-ink);
  transform: translateX(-50%);
}

.dock-track {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.dock-cover {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  background: #e5eee9;
  color: var(--music-green);
}

.dock-info {
  min-width: 0;
  display: grid;
}

.dock-info strong,
.dock-info small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dock-info strong {
  font-size: 13px;
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
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  padding: 0;
  border: 0;
  background: transparent;
  color: #53615a;
  cursor: pointer;
  transition:
    color 0.2s,
    transform 0.2s;
}

.dock-controls button:hover {
  color: var(--music-ink);
  transform: scale(1.08);
}

.dock-controls .main-control {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--music-green);
  color: #102419;
}

.previous-icon {
  transform: rotate(180deg);
}

.progress-row {
  display: grid;
  grid-template-columns: 38px minmax(100px, 1fr) 38px;
  align-items: center;
  gap: 8px;
  color: #819088;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 10px;
}

input[type='range'] {
  width: 100%;
  height: 3px;
  accent-color: var(--music-green);
  cursor: pointer;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 9px;
  color: #718078;
  font-size: 11px;
}

.music-page button:focus-visible,
.music-page input:focus-visible {
  outline: 2px solid var(--music-green);
  outline-offset: 3px;
}

.music-page button:active {
  transform: scale(0.98);
}

@keyframes vinyl-spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes music-reveal {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: no-preference) {
  .hero-copy,
  .record-stage,
  .playlist-heading,
  .track-list {
    animation: music-reveal 0.7s cubic-bezier(0.16, 1, 0.3, 1) both;
  }

  .record-stage {
    animation-delay: 0.1s;
  }

  .playlist-heading {
    animation-delay: 0.16s;
  }

  .track-list {
    animation-delay: 0.22s;
  }
}

@media (prefers-reduced-motion: reduce) {
  .vinyl.spinning,
  .cover-art,
  .track-row,
  .dock-controls button,
  .hero-track-control {
    animation: none;
    transition: none;
  }
}

@media (prefers-reduced-transparency: reduce) {
  .liquid-glass,
  .track-list {
    background: rgba(255, 255, 255, 0.96);
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
  }
}

@media (max-width: 900px) {
  .music-hero-inner {
    grid-template-columns: minmax(0, 1fr) 320px;
    gap: 40px;
    padding: 88px 32px 58px;
  }

  .record-stage {
    height: 276px;
  }

  .vinyl {
    width: 250px;
    height: 250px;
  }

  .record-label {
    inset: 79px;
  }

  .cover-art {
    width: 172px;
    height: 172px;
  }

  .record-ambient {
    inset: 54px 28px 24px 72px;
    width: calc(100% - 100px);
    height: calc(100% - 78px);
  }

  .playlist-inner {
    padding: 0 32px;
  }

  .player-dock {
    width: calc(100% - 64px);
    grid-template-columns: minmax(160px, 0.8fr) minmax(280px, 1.4fr);
  }

  .volume-control {
    display: none;
  }
}

@media (max-width: 700px) {
  .music-page {
    padding-bottom: 32px;
  }

  .music-hero {
    min-height: auto;
  }

  .music-hero-inner {
    padding: 82px 20px 34px;
    grid-template-columns: 1fr;
    gap: 22px;
  }

  .hero-copy h1 {
    font-size: 42px;
  }

  .hero-intro {
    margin: 12px 0 22px;
    font-size: 14px;
  }

  .hero-track {
    min-height: 88px;
    padding: 13px 14px 13px 16px;
    grid-template-columns: minmax(0, 1fr) 46px;
  }

  .hero-track-copy strong {
    font-size: 19px;
  }

  .hero-track-control {
    width: 46px;
    height: 46px;
  }

  .record-stage {
    width: min(100%, 320px);
    height: 198px;
    margin: 0 auto;
  }

  .vinyl {
    top: 2px;
    right: 12px;
    width: 178px;
    height: 178px;
  }

  .record-label {
    inset: 57px;
    font-size: 20px;
  }

  .cover-art {
    left: 10px;
    width: 134px;
    height: 134px;
  }

  .record-ambient {
    inset: 42px 40px 16px 66px;
    width: calc(100% - 106px);
    height: calc(100% - 58px);
  }

  .playlist {
    padding: 46px 0 112px;
  }

  .playlist-inner {
    padding: 0 16px;
  }

  .playlist-heading h2 {
    font-size: 25px;
  }

  .track-row {
    grid-template-columns: 24px minmax(0, 1fr) 36px;
    gap: 9px;
    min-height: 72px;
    padding: 8px 9px 8px 5px;
  }

  .track-cover {
    width: 50px;
    height: 50px;
  }

  .track-duration {
    display: none;
  }

  .player-dock {
    bottom: 12px;
    width: calc(100% - 48px);
    min-height: 68px;
    padding: 8px 12px;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 8px;
  }

  .dock-cover {
    width: 44px;
    height: 44px;
  }

  .dock-center {
    display: flex;
    align-items: center;
  }

  .dock-controls {
    gap: 8px;
  }

  .progress-row {
    display: none;
  }
}

@media (max-width: 360px) {
  .player-dock {
    width: calc(100% - 24px);
  }

  .dock-controls button:not(.main-control) {
    display: none;
  }
}
</style>
