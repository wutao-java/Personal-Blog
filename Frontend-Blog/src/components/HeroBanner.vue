<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useBlogStore } from '@/stores'

const route = useRoute()
const blogStore = useBlogStore()
defineProps({
  coverImage: { type: String, default: '' },
  title: { type: String, default: '' },
  meta: { type: String, default: '' },
  hasToc: { type: Boolean, default: false }
})
const isHome = computed(() => route.name === 'home')
</script>

<template>
  <section
    class="hero-banner"
    :class="{
      'article-heading': route.name === 'article',
      'article-with-toc': route.name === 'article' && hasToc
    }"
  >
    <div class="hero-content">
      <p class="heading-caption">
        {{
          isHome
            ? '文字与生活'
            : (blogStore.personalInfo.nickname || 'WuTao') +
              ' / ' +
              (route.meta.title || '')
        }}
      </p>
      <template v-if="isHome">
        <h1 class="hero-title">
          {{ blogStore.personalInfo.nickname || 'WuTao' }} 的博客<span
            class="heading-dot"
            >.</span
          >
        </h1>
        <p class="hero-desc">
          随便坐坐，看看我写的字。些许技术、心得、生活日常和胡思乱想。
        </p>
      </template>
      <template v-else>
        <h1 class="hero-page-title">
          {{ title || route.meta.title || ''
          }}<span class="heading-dot">.</span>
        </h1>
        <p v-if="meta" class="hero-article-meta" v-html="meta" />
      </template>
      <img
        v-if="route.name === 'article' && coverImage"
        :src="coverImage"
        :alt="title"
        class="article-cover"
      />
    </div>
  </section>
</template>

<style scoped>
.hero-banner {
  width: 100%;
  padding: 145px 28px 36px;
}
.hero-content {
  max-width: 1144px;
  margin: 0 auto;
}
.heading-caption {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 17px;
  color: var(--blog-text2);
  font-size: 12px;
  overflow-wrap: anywhere;
}
.heading-caption::before {
  content: '';
  width: 20px;
  height: 1px;
  background: var(--blog-accent);
  flex-shrink: 0;
}
.hero-title,
.hero-page-title {
  margin: 0 0 15px;
  color: var(--blog-text);
  font-size: 42px;
  line-height: 1.4;
  font-weight: 700;
  overflow-wrap: anywhere;
}
.heading-dot {
  color: var(--blog-accent);
  margin-left: 3px;
}
.hero-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.9;
  color: var(--blog-text2);
}
.hero-article-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px 8px;
  margin: 0;
  color: var(--blog-text2);
  font-size: 13px;
  line-height: 1.9;
}
.hero-article-meta :deep(.meta-item) {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  overflow-wrap: anywhere;
  min-width: 0;
}
.hero-article-meta :deep(.meta-dot) {
  color: var(--blog-text3);
}
.article-cover {
  display: block;
  width: 100%;
  height: 300px;
  margin-top: 28px;
  border-radius: 26px;
  object-fit: cover;
}
.article-heading .hero-content {
  max-width: 800px;
}
.article-heading.article-with-toc .hero-content {
  max-width: 1144px;
  padding-right: 284px;
}
.article-heading .hero-page-title {
  font-size: 36px;
}
@media (max-width: 960px) {
  .article-heading.article-with-toc .hero-content {
    padding-right: 0;
  }
}
@media (max-width: 768px) {
  .hero-banner.article-heading {
    padding-inline: 18px;
  }
}
@media (max-width: 600px) {
  .hero-banner {
    padding: 111px 22px 26px;
  }
  .heading-caption {
    font-size: 11px;
    margin-bottom: 13px;
  }
  .hero-title,
  .hero-page-title {
    font-size: 30px;
    margin-bottom: 12px;
  }
  .hero-desc {
    font-size: 13px;
  }
  .article-heading .hero-page-title {
    font-size: 27px;
  }
  .hero-article-meta {
    font-size: 12px;
  }
  .article-cover {
    height: 200px;
    border-radius: 20px;
    margin-top: 22px;
  }
}
</style>
