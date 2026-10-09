<script setup>
import { ref, computed, provide, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import BlogHeader from '@/components/BlogHeader.vue'
import BlogFooter from '@/components/BlogFooter.vue'
import HeroBanner from '@/components/HeroBanner.vue'
import SidebarCard from '@/components/SidebarCard.vue'
import { useBlogStore, useVisitorStore, useThemeStore } from '@/stores'

const route = useRoute()
const blogStore = useBlogStore()
const visitorStore = useVisitorStore()
const themeStore = useThemeStore()
const showHero = computed(() =>
  ['article', 'category', 'tag'].includes(route.name)
)
const showSidebar = computed(() => route.name !== 'article')

watch(
  () => blogStore.personalInfo.avatar,
  (avatar) => {
    const favicon = document.querySelector('link[rel="icon"]')
    if (favicon) favicon.href = avatar || '/favicon.ico'
  },
  { immediate: true }
)

/* 文章详情页会通过 provide/inject 传递封面和标题 */
const articleCover = ref('')
const articleTitle = ref('')
const articleMeta = ref('')
const articleHasToc = ref(false)

provide('setHero', { articleCover, articleTitle, articleMeta, articleHasToc })

/* 路由切换时重置 hero 数据，子页面的 onMounted 会重新设置 */
watch(
  () => route.fullPath,
  () => {
    articleCover.value = ''
    articleTitle.value = ''
    articleMeta.value = ''
    articleHasToc.value = false
  }
)

/* 回到顶部 */
const showBackTop = ref(false)
const onScroll = () => {
  showBackTop.value = window.scrollY > 400
}
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  themeStore.applyTheme()
  blogStore.init()
  visitorStore.record()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <div class="blog-layout">
    <BlogHeader />
    <HeroBanner
      v-if="showHero"
      :cover-image="articleCover"
      :title="articleTitle"
      :meta="articleMeta"
      :has-toc="articleHasToc"
    />
    <main class="blog-main" :class="{ 'content-only': !showHero }">
      <div class="main-inner" :class="{ 'home-inner': route.name === 'home' }">
        <div class="page-content">
          <router-view v-slot="{ Component, route: viewRoute }">
            <transition name="page-fade" mode="out-in">
              <component :is="Component" :key="viewRoute.path" />
            </transition>
          </router-view>
        </div>
        <SidebarCard v-show="showSidebar" />
      </div>
    </main>
    <BlogFooter v-if="route.name !== 'music'" />

    <!-- 回到顶部 -->
    <transition name="backtop-fade">
      <button
        v-show="showBackTop && route.name !== 'music'"
        class="back-to-top"
        title="回到顶部"
        @click="scrollToTop"
      >
        <svg
          viewBox="0 0 24 24"
          width="20"
          height="20"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <polyline points="18 15 12 9 6 15" />
        </svg>
      </button>
    </transition>
  </div>
</template>

<style scoped>
.blog-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.blog-main {
  flex: 1;
  width: 100%;
  background: var(--blog-bg);
}
.blog-main.content-only {
  padding-top: 124px;
}
.main-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 28px 40px;
  display: flex;
  align-items: flex-start;
  gap: 24px;
}
.main-inner.home-inner {
  gap: 28px;
}
.page-content {
  flex: 1;
  min-width: 0;
  width: 100%;
}
/* 页面切换过渡动画 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

/* 回到顶部 */
.back-to-top {
  position: fixed;
  right: 28px;
  bottom: 36px;
  z-index: 99;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: 1px solid var(--blog-border);
  background: var(--blog-card);
  color: var(--blog-text2);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    color 0.2s,
    box-shadow 0.2s,
    border-color 0.2s;
}
.back-to-top:hover {
  color: var(--blog-text);
  border-color: var(--blog-text3);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}
.backtop-fade-enter-active,
.backtop-fade-leave-active {
  transition:
    opacity 0.25s,
    transform 0.25s;
}
.backtop-fade-enter-from,
.backtop-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 960px) {
  .main-inner {
    flex-direction: column;
  }
}
@media (max-width: 768px) {
  .main-inner {
    padding: 0 18px 28px;
  }
  .back-to-top {
    right: 16px;
    bottom: 20px;
    width: 38px;
    height: 38px;
  }
}
@media (max-width: 600px) {
  .blog-main.content-only {
    padding-top: 100px;
  }
}
</style>
