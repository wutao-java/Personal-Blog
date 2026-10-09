<script setup>
import { ref, inject, onMounted } from 'vue'
import { getArticleArchive } from '@/api/article'

const { articleTitle, articleMeta } = inject('setHero')
const yearGroups = ref([])
const loading = ref(false)
const totalCount = ref(0)

const load = async () => {
  loading.value = true
  try {
    const res = await getArticleArchive()
    const list = res.data.data ?? []
    const map = new Map()
    let count = 0
    list.forEach((group) => {
      const y = group.year
      if (!map.has(y)) map.set(y, [])
      const items = (group.articles ?? []).map((a) => ({
        ...a,
        month: group.month,
        displayDate: `${String(group.month).padStart(2, '0')}.${String(a.publishDay).padStart(2, '0')}`
      }))
      map.get(y).push(...items)
      count += items.length
    })
    yearGroups.value = [...map.entries()]
      .sort((a, b) => b[0] - a[0])
      .map(([year, items]) => ({
        year,
        items: items.sort((a, b) => {
          const da = new Date(a.publishTime)
          const db = new Date(b.publishTime)
          return db - da
        })
      }))
    totalCount.value = count
  } catch {
    yearGroups.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  articleTitle.value = '归档'
  articleMeta.value = '时光轴上的足迹'
  load()
})
</script>

<template>
  <div class="archive-page">
    <div class="archive-layout">
      <div class="archive-main">
        <div class="archive-content">
          <div class="card-header">
            <i class="iconfont icon-guidang" />
            <span>共 {{ totalCount }} 篇文章</span>
          </div>

          <div v-if="loading" class="placeholder">
            <div v-for="i in 4" :key="i" class="sk-line" />
          </div>

          <div v-else class="timeline">
            <div v-for="g in yearGroups" :key="g.year" class="year-group">
              <h2 class="year-label">{{ g.year }}</h2>
              <ul class="year-list">
                <li v-for="a in g.items" :key="a.id">
                  <router-link :to="`/article/${a.slug}`" class="archive-item">
                    <span class="item-date">{{ a.displayDate }}</span>
                    <span class="item-title">{{ a.title }}</span>
                  </router-link>
                </li>
              </ul>
            </div>
          </div>

          <p v-if="!loading && !yearGroups.length" class="empty">暂无归档</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.archive-page {
  width: 100%;
}
.archive-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.archive-main {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--blog-text3);
  margin-bottom: 18px;
}
.card-header .iconfont {
  font-size: 16px;
  color: var(--blog-text2);
}

.placeholder {
  padding: 20px 0;
}
@keyframes sk-shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
.sk-line {
  height: 14px;
  border-radius: 4px;
  margin-bottom: 12px;
  width: 60%;
  background: linear-gradient(90deg, #ebeef5 25%, #f5f7fa 50%, #ebeef5 75%);
  background-size: 200% 100%;
  animation: sk-shimmer 1.5s ease-in-out infinite;
}

.timeline {
  display: grid;
  gap: 24px;
}
.year-label {
  font-family: var(--blog-serif);
  font-size: 18px;
  font-weight: 700;
  color: var(--blog-text);
  margin: 0 0 12px;
}
.year-list {
  display: grid;
  gap: 8px;
  list-style: none;
  padding: 0;
  margin: 0;
}
.archive-item {
  display: flex;
  align-items: center;
  gap: 24px;
  min-height: 52px;
  padding: 12px 24px;
  background: var(--blog-card);
  border-radius: 16px;
  transition: background-color 0.15s;
}
.archive-item:hover {
  background: var(--blog-hover);
}
.item-date {
  flex: 0 0 44px;
  font-size: 13px;
  color: var(--blog-text3);
  font-family: var(--blog-serif);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.item-title {
  flex: 1;
  min-width: 0;
  font-size: 16px;
  line-height: 1.5;
  color: var(--blog-text);
  overflow-wrap: anywhere;
}

.empty {
  text-align: center;
  color: var(--blog-text3);
  padding: 40px 0;
  font-size: 14px;
}

@media (max-width: 960px) {
  .archive-layout {
    flex-direction: column;
  }
  .archive-main {
    width: 100%;
  }
}
@media (max-width: 600px) {
  .archive-item {
    gap: 14px;
    min-height: 48px;
    padding: 10px 16px;
    border-radius: 14px;
  }
  .item-date {
    flex-basis: 38px;
    font-size: 12px;
  }
  .item-title {
    font-size: 15px;
  }
}
</style>
