<script setup>
defineProps({
  article: { type: Object, required: true }
})
const fmtDate = (d) => (d ? d.slice(0, 10) : '')
</script>

<template>
  <router-link :to="`/article/${article.slug}`" class="article-card">
    <div class="card-body">
      <div class="card-top">
        <span v-if="article.categoryName" class="card-category">{{
          article.categoryName
        }}</span>
        <time class="card-date" :datetime="article.publishTime">{{
          fmtDate(article.publishTime)
        }}</time>
        <span v-if="article.isTop" class="card-pin" title="置顶"
          ><i class="iconfont icon-zhiding"
        /></span>
      </div>
      <h3 class="card-title">{{ article.title }}</h3>
      <p v-if="article.summary" class="card-summary">{{ article.summary }}</p>
      <div v-if="article.tagNames?.length" class="card-tags">
        <span v-for="tag in article.tagNames" :key="tag" class="card-tag"
          ># {{ tag }}</span
        >
      </div>
      <div class="card-meta">
        <span title="浏览"
          ><i class="iconfont icon-eye" /> {{ article.viewCount ?? 0 }}</span
        >
        <span title="评论"
          ><i class="iconfont icon-pinglun" />
          {{ article.commentCount ?? 0 }}</span
        >
        <span title="点赞"
          ><i class="iconfont icon-dianzan" />
          {{ article.likeCount ?? 0 }}</span
        >
        <span v-if="article.wordCount">{{ article.wordCount }} 字</span>
        <span v-if="article.readingTime">{{ article.readingTime }} 分钟</span>
      </div>
    </div>
    <div v-if="article.coverImage" class="card-cover">
      <img :src="article.coverImage" :alt="article.title" loading="lazy" />
    </div>
  </router-link>
</template>

<style scoped>
.article-card {
  display: flex;
  align-items: stretch;
  gap: 24px;
  padding: 22px;
  background: var(--blog-card);
  border: 1px solid transparent;
  border-radius: 26px;
  box-shadow: var(--blog-shadow);
  color: inherit;
  margin-bottom: 20px;
  transition:
    transform 0.18s,
    box-shadow 0.18s,
    border-color 0.18s;
}
.article-card:hover {
  transform: translateY(-3px);
  border-color: var(--blog-border);
  box-shadow: 0 12px 32px rgb(27 49 36 / 7%);
}
.card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 3px 0 2px 2px;
}
.card-top {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--blog-text3);
  font-size: 12px;
  margin-bottom: 12px;
  min-height: 24px;
}
.card-category {
  color: var(--blog-accent);
  background: var(--blog-accent-soft);
  font-weight: 500;
  padding: 3px 8px;
  border-radius: 6px;
  overflow-wrap: anywhere;
  max-width: 100%;
}
.card-pin {
  margin-left: auto;
  color: var(--blog-accent);
}
.card-pin .iconfont {
  font-size: 17px;
}
.card-title {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.65;
  color: var(--blog-text);
  margin: 0 0 8px;
  overflow-wrap: anywhere;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-summary {
  font-size: 14px;
  line-height: 1.9;
  color: var(--blog-text2);
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  overflow-wrap: anywhere;
}
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}
.card-tag {
  color: var(--blog-text2);
  background: var(--blog-secondary-soft);
  border-radius: 6px;
  padding: 2px 7px;
  font-size: 11px;
  max-width: 100%;
  overflow-wrap: anywhere;
}
.card-meta {
  display: flex;
  align-items: center;
  gap: 7px 14px;
  flex-wrap: wrap;
  margin-top: auto;
  color: var(--blog-text2);
  font-size: 11px;
  font-variant-numeric: tabular-nums;
}
.card-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.card-meta .iconfont {
  font-size: 12px;
}
.card-cover {
  width: 218px;
  min-height: 176px;
  align-self: stretch;
  flex-shrink: 0;
  border-radius: 17px;
  overflow: hidden;
  background: var(--blog-hover);
}
.card-cover img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.25s;
}
.article-card:hover .card-cover img {
  transform: scale(1.025);
}
@media (min-width: 961px) and (max-width: 1080px) {
  .card-cover {
    width: 175px;
  }
  .article-card {
    padding: 20px;
    gap: 18px;
  }
  .card-title {
    font-size: 18px;
  }
}
@media (max-width: 600px) {
  .article-card {
    flex-direction: column-reverse;
    padding: 15px;
    gap: 20px;
    border-radius: 24px;
    margin-bottom: 18px;
  }
  .card-cover {
    width: 100%;
    height: 175px;
    min-height: 175px;
    border-radius: 16px;
  }
  .card-body {
    padding: 0 5px 3px;
  }
  .card-title {
    font-size: 18px;
  }
  .card-summary {
    font-size: 13px;
  }
  .card-top {
    font-size: 11px;
    margin-bottom: 9px;
    gap: 8px;
  }
  .card-tags {
    margin-bottom: 12px;
  }
  .card-meta {
    gap: 7px 12px;
  }
}
</style>
