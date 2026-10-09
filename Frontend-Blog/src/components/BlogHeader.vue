<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Sunny, Moon, Menu, Close, Search } from '@element-plus/icons-vue'
import { useBlogStore, useThemeStore } from '@/stores'
import { getConfigByKey } from '@/api/systemConfig'

const router = useRouter()
const route = useRoute()
const blogStore = useBlogStore()
const themeStore = useThemeStore()
const scrolled = ref(false)
const handleScroll = () => {
  scrolled.value = window.scrollY > 60
}
onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  fetchFootprintConfig()
})
onUnmounted(() => window.removeEventListener('scroll', handleScroll))

const searchVisible = ref(false)
const keyword = ref('')
const searchInputRef = ref(null)
const mobileNavVisible = ref(false)
const footprintEnabled = ref(false)

const fetchFootprintConfig = async () => {
  try {
    const res = await getConfigByKey('use-footprint')
    footprintEnabled.value = res?.data?.data?.configValue === 'true'
  } catch {
    /* keep false */
  }
}
watch(
  () => route.path,
  () => {
    mobileNavVisible.value = false
    musicListVisible.value = false
    fetchFootprintConfig()
  }
)

/* Keep the header player independent from the full music page. */
const isPlaying = ref(false)
const audioRef = ref(null)
const musicIndex = ref(0)
const musicListVisible = ref(false)
const currentTrack = computed(() => blogStore.musics[musicIndex.value] || null)
const togglePlay = async () => {
  if (!audioRef.value || !currentTrack.value) return
  if (isPlaying.value) {
    audioRef.value.pause()
  } else {
    try {
      await audioRef.value.play()
    } catch {
      isPlaying.value = false
    }
  }
}
const playTrack = (index) => {
  musicIndex.value = index
  nextTick(async () => {
    if (!audioRef.value) return
    audioRef.value.load()
    try {
      await audioRef.value.play()
    } catch {
      isPlaying.value = false
    }
  })
}
const nextTrack = () => {
  if (blogStore.musics.length) {
    playTrack((musicIndex.value + 1) % blogStore.musics.length)
  }
}
const navItems = computed(() => {
  const items = [
    { label: '博客', icon: 'icon-boke', to: '/' },
    { label: '归档', icon: 'icon-guidang', to: '/archive' },
    { label: '友链', icon: 'icon-lianjie', to: '/links' },
    { label: '留言', icon: 'icon-liuyan', to: '/message' }
  ]
  if (footprintEnabled.value) {
    items.push({ label: '足迹', icon: 'icon-zuji', to: '/footprint' })
  }
  items.push({ label: '音乐', icon: 'icon-yinle', to: '/music' })
  items.push({ label: '主页', icon: 'icon-zhuye', to: '/about' })
  return items
})
const doSearch = () => {
  const kw = keyword.value.trim()
  if (!kw) return
  searchVisible.value = false
  router.push({ path: '/', query: { search: kw } })
}
const openSearch = () => {
  mobileNavVisible.value = false
  searchVisible.value = true
}
</script>

<template>
  <header class="site-header" :class="{ scrolled }">
    <div class="header-inner">
      <router-link to="/" class="site-title">
        <img
          v-if="blogStore.personalInfo.avatar"
          :src="blogStore.personalInfo.avatar"
          alt=""
        />
        <span>WuTao's Blog</span>
      </router-link>
      <nav class="nav-desktop" aria-label="主导航">
        <router-link
          v-for="item in navItems"
          :key="item.label"
          :to="item.to"
          class="nav-link"
          :class="{ active: route.path === item.to }"
          >{{ item.label }}</router-link
        >
      </nav>
      <div class="header-right">
        <div
          v-if="currentTrack && route.name !== 'music'"
          class="mini-player-wrap"
        >
          <button
            class="mini-player"
            title="播放列表"
            aria-label="播放列表"
            :aria-expanded="musicListVisible"
            @click="musicListVisible = !musicListVisible"
          >
            <img
              v-if="currentTrack.coverImage"
              :src="currentTrack.coverImage"
              alt=""
              class="player-cover"
              :class="{ spinning: isPlaying }"
            />
            <span class="player-title">{{ currentTrack.title }}</span>
          </button>
          <button
            class="player-btn"
            @click="togglePlay"
            :title="isPlaying ? '暂停' : '播放'"
            :aria-label="isPlaying ? '暂停' : '播放'"
          >
            <i
              class="iconfont"
              :class="isPlaying ? 'icon-zanting' : 'icon-play-full'"
            />
          </button>
          <button
            class="player-btn"
            @click="nextTrack"
            title="下一首"
            aria-label="下一首"
          >
            <i class="iconfont icon-next" />
          </button>
          <audio
            ref="audioRef"
            :src="currentTrack.musicUrl"
            preload="none"
            @ended="nextTrack"
            @play="isPlaying = true"
            @pause="isPlaying = false"
            @error="isPlaying = false"
          />
          <transition name="fade">
            <div v-if="musicListVisible" class="music-panel">
              <div class="music-panel-header">
                <span>播放列表</span
                ><span class="music-panel-count"
                  >{{ blogStore.musics.length }} 首</span
                >
              </div>
              <ul class="music-panel-list">
                <li v-for="(m, idx) in blogStore.musics" :key="m.id">
                  <button
                    class="music-panel-item"
                    :class="{ active: idx === musicIndex }"
                    @click="playTrack(idx)"
                  >
                    <img
                      v-if="m.coverImage"
                      :src="m.coverImage"
                      alt=""
                      class="music-panel-cover"
                    />
                    <span class="music-panel-info"
                      ><span class="music-panel-name">{{ m.title }}</span
                      ><span class="music-panel-artist">{{
                        m.artist
                      }}</span></span
                    >
                    <i
                      v-if="idx === musicIndex && isPlaying"
                      class="iconfont icon-yinle playing-icon"
                    />
                  </button>
                </li>
              </ul>
            </div>
          </transition>
        </div>
        <button
          class="theme-toggle icon-button"
          :title="themeStore.isDark ? '切换到浅色模式' : '切换到暗色模式'"
          :aria-label="themeStore.isDark ? '切换到浅色模式' : '切换到暗色模式'"
          @click="themeStore.toggle"
        >
          <Sunny v-if="themeStore.isDark" /><Moon v-else />
        </button>
        <button
          class="search-toggle icon-button"
          @click="openSearch"
          title="搜索文章"
          aria-label="搜索文章"
        >
          <Search />
        </button>
        <button
          class="mobile-menu-btn icon-button"
          @click="mobileNavVisible = !mobileNavVisible"
          :aria-label="mobileNavVisible ? '关闭导航' : '打开导航'"
          :title="mobileNavVisible ? '关闭导航' : '打开导航'"
          :aria-expanded="mobileNavVisible"
          aria-controls="mobile-nav"
        >
          <Close v-if="mobileNavVisible" /><Menu v-else />
        </button>
      </div>
    </div>
    <nav
      v-if="mobileNavVisible"
      id="mobile-nav"
      class="nav-mobile"
      aria-label="移动端导航"
    >
      <router-link
        v-for="item in navItems"
        :key="item.label"
        :to="item.to"
        class="nav-mobile-link"
        :class="{ active: route.path === item.to }"
        ><i :class="['iconfont', item.icon]" />{{ item.label }}</router-link
      >
    </nav>
  </header>
  <el-dialog
    v-model="searchVisible"
    title="搜索文章"
    width="560px"
    class="blog-search-dialog"
    append-to-body
    @opened="searchInputRef?.focus()"
    @closed="keyword = ''"
  >
    <form class="search-field" role="search" @submit.prevent="doSearch">
      <Search />
      <input
        ref="searchInputRef"
        v-model="keyword"
        type="search"
        aria-label="搜索文章"
        placeholder="输入关键词..."
        class="search-input"
      />
      <button
        type="submit"
        class="search-submit"
        title="搜索"
        aria-label="搜索"
      >
        <Search />
      </button>
    </form>
  </el-dialog>
</template>

<style scoped>
.site-header {
  position: fixed;
  top: 20px;
  left: 28px;
  right: 28px;
  max-width: 1144px;
  margin: 0 auto;
  z-index: 100;
  background: var(--blog-glass);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid var(--blog-glass-border);
  border-radius: 23px;
  box-shadow: 0 4px 20px rgb(27 49 36 / 3%);
  transition: box-shadow 0.2s;
}
.site-header.scrolled {
  box-shadow: 0 8px 30px rgb(27 49 36 / 8%);
}
.header-inner {
  height: 72px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}
.site-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--blog-text);
  font-size: 20px;
  font-weight: 700;
  white-space: nowrap;
  min-width: 0;
  max-width: 200px;
}
.site-title > span {
  overflow: hidden;
  text-overflow: ellipsis;
}
.site-title img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.nav-desktop {
  display: flex;
  flex-shrink: 0;
  gap: 12px;
  align-items: center;
}
.nav-link {
  padding: 10px 11px;
  border-radius: 12px;
  color: var(--blog-text2);
  font-size: 13px;
  white-space: nowrap;
  transition:
    background 0.15s,
    color 0.15s,
    box-shadow 0.15s;
}
.nav-link:hover {
  background: var(--blog-accent-soft);
  color: var(--blog-accent);
}
.nav-link.active {
  font-weight: 600;
  color: var(--blog-text);
  background: var(--blog-accent-soft);
  box-shadow: 0 3px 10px rgb(27 49 36 / 8%);
}
:global(html.dark .nav-link.active) {
  box-shadow: 0 3px 10px rgb(0 0 0 / 20%);
}
.header-right {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: 4px;
}
.icon-button,
.player-btn {
  width: 38px;
  height: 40px;
  display: grid;
  place-items: center;
  background: none;
  border: 0;
  border-radius: 12px;
  color: var(--blog-text2);
  cursor: pointer;
  flex-shrink: 0;
}
.icon-button:hover,
.player-btn:hover {
  color: var(--blog-accent);
  background: var(--blog-accent-soft);
}
.icon-button svg {
  width: 19px;
  height: 19px;
}
.mini-player-wrap {
  position: relative;
  display: flex;
  align-items: center;
  gap: 2px;
  margin-right: 10px;
}
.mini-player {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 36px;
  padding: 3px 10px 3px 3px;
  border: 1px solid var(--blog-border);
  border-radius: 20px;
  background: var(--blog-hover);
  color: var(--blog-text2);
  cursor: pointer;
  font: inherit;
}
.player-cover {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}
.player-cover.spinning {
  animation: spin 8s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
.player-title {
  max-width: 70px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
}
.player-btn {
  width: 28px;
  height: 34px;
}
.music-panel {
  position: absolute;
  top: calc(100% + 22px);
  right: 0;
  width: 280px;
  background: var(--blog-glass);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid var(--blog-glass-border);
  border-radius: 22px;
  box-shadow: var(--blog-shadow);
  overflow: hidden;
}
.music-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px;
  border-bottom: 1px solid var(--blog-border);
  color: var(--blog-text);
  font-size: 13px;
  font-weight: 600;
}
.music-panel-count {
  color: var(--blog-text3);
  font-weight: 400;
  font-size: 12px;
}
.music-panel-list {
  list-style: none;
  margin: 0;
  padding: 8px;
  max-height: 300px;
  overflow-y: auto;
}
.music-panel-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 0;
  border-radius: 12px;
  background: none;
  text-align: left;
  cursor: pointer;
  font: inherit;
}
.music-panel-item:hover,
.music-panel-item.active {
  background: var(--blog-accent-soft);
}
.music-panel-cover {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  object-fit: cover;
}
.music-panel-info {
  flex: 1;
  min-width: 0;
}
.music-panel-name,
.music-panel-artist {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.music-panel-name {
  color: var(--blog-text);
  font-size: 13px;
}
.music-panel-artist {
  color: var(--blog-text3);
  font-size: 11px;
}
.playing-icon {
  color: var(--blog-accent);
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.mobile-menu-btn,
.nav-mobile {
  display: none;
}
.search-field {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border: 1px solid var(--blog-border);
  border-radius: 16px;
  background: var(--blog-bg);
}
.search-field:focus-within {
  border-color: var(--blog-accent);
}
.search-field > svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  color: var(--blog-text3);
}
.search-input {
  width: 100%;
  min-width: 0;
  border: 0;
  padding: 7px 0;
  color: var(--blog-text);
  background: none;
  outline: none;
  font: inherit;
  font-size: 14px;
}
.search-input:focus-visible {
  outline: none;
}
.search-submit {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 10px;
  background: var(--blog-accent);
  color: var(--blog-accent-contrast);
  cursor: pointer;
}
.search-submit svg {
  width: 17px;
  height: 17px;
}
@media (max-width: 1180px) {
  .nav-desktop {
    display: none;
  }
  .mobile-menu-btn {
    display: grid;
  }
  .nav-mobile {
    position: absolute;
    top: calc(100% + 12px);
    left: 0;
    right: 0;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 4px;
    padding: 12px;
    max-height: calc(100dvh - 120px);
    overflow-y: auto;
    background: var(--blog-glass);
    backdrop-filter: blur(18px);
    -webkit-backdrop-filter: blur(18px);
    border: 1px solid var(--blog-glass-border);
    border-radius: 22px;
    box-shadow: var(--blog-shadow);
  }
  .nav-mobile-link {
    display: flex;
    align-items: center;
    gap: 9px;
    padding: 12px 14px;
    color: var(--blog-text2);
    font-size: 14px;
    border-radius: 12px;
  }
  .nav-mobile-link:last-child {
    grid-column: 2;
  }
  .nav-mobile-link:hover,
  .nav-mobile-link.active {
    background: var(--blog-accent-soft);
    color: var(--blog-accent);
  }
}
@media (max-width: 600px) {
  .site-header {
    top: 12px;
    left: 18px;
    right: 18px;
    border-radius: 19px;
  }
  .header-inner {
    height: 61px;
    padding: 0 12px;
    gap: 8px;
  }
  .site-title {
    font-size: 17px;
    gap: 7px;
    max-width: calc(100% - 112px);
  }
  .site-title img {
    width: 30px;
    height: 30px;
  }
  .header-right {
    gap: 0;
  }
  .icon-button {
    width: 35px;
  }
  .mini-player-wrap {
    display: none;
  }
}
@media (prefers-reduced-transparency: reduce) {
  .site-header,
  .music-panel,
  .nav-mobile {
    background: var(--blog-card);
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
    border-color: var(--blog-border);
  }
}
@supports not (backdrop-filter: blur(1px)) {
  .site-header,
  .music-panel,
  .nav-mobile {
    background: var(--blog-card);
    border-color: var(--blog-border);
  }
}
</style>
