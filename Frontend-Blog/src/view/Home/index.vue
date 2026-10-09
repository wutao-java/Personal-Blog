<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getArticlePage,
  getArticlesByCategory,
  searchArticles
} from '@/api/article'
import { useBlogStore } from '@/stores'
import ArticleCard from '@/components/ArticleCard.vue'

const route = useRoute()
const router = useRouter()
const blogStore = useBlogStore()

const articles = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 10
const loading = ref(false)
const searchKeyword = ref('')
const selectedCategoryId = ref(null)
let requestId = 0

const loadArticles = async () => {
  const currentRequestId = ++requestId
  loading.value = true
  try {
    let res
    if (searchKeyword.value) {
      res = await searchArticles(searchKeyword.value, page.value, pageSize)
    } else if (selectedCategoryId.value !== null) {
      res = await getArticlesByCategory(
        selectedCategoryId.value,
        page.value,
        pageSize
      )
    } else {
      res = await getArticlePage(page.value, pageSize)
    }
    if (currentRequestId !== requestId) return
    const d = res.data.data
    articles.value = d.records ?? []
    total.value = d.total ?? 0
  } catch {
    if (currentRequestId !== requestId) return
    articles.value = []
    total.value = 0
  } finally {
    if (currentRequestId === requestId) loading.value = false
  }
}

const handleCategoryChange = (categoryId) => {
  if (selectedCategoryId.value === categoryId) return
  selectedCategoryId.value = categoryId
  page.value = 1
  loadArticles()
}

const handlePageChange = (p) => {
  page.value = p
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(
  () => route.query.search,
  (kw) => {
    searchKeyword.value = kw || ''
    selectedCategoryId.value = null
    page.value = 1
    loadArticles()
  }
)

onMounted(() => {
  searchKeyword.value = route.query.search || ''
  loadArticles()
})
</script>

<template>
  <div class="home-page">
    <div class="home-content">
      <!-- 左侧: 文章列表 -->
      <div class="article-col">
        <div v-if="!searchKeyword" class="article-toolbar">
          <div class="category-filters" role="group" aria-label="文章分类">
            <button
              type="button"
              class="category-filter"
              :class="{ active: selectedCategoryId === null }"
              :aria-pressed="selectedCategoryId === null"
              @click="handleCategoryChange(null)"
            >
              全部
            </button>
            <button
              v-for="category in blogStore.categories"
              :key="category.id"
              type="button"
              class="category-filter"
              :class="{ active: selectedCategoryId === category.id }"
              :aria-pressed="selectedCategoryId === category.id"
              @click="handleCategoryChange(category.id)"
            >
              {{ category.name }}
            </button>
          </div>
          <span class="article-count" role="status">
            {{ loading ? '加载中...' : `共 ${total} 篇文章` }}
          </span>
        </div>

        <div v-if="searchKeyword" class="search-result-tip">
          <span
            >搜索: <strong>{{ searchKeyword }}</strong></span
          >
          <span class="search-count">{{ total }} 篇结果</span>
          <button
            class="clear-search"
            aria-label="清除搜索"
            @click="router.push('/')"
          >
            &times; 清除
          </button>
        </div>

        <div v-if="loading" class="loading-placeholder">
          <div v-for="i in 4" :key="i" class="skeleton-card">
            <div class="skeleton-cover" />
            <div class="skeleton-body">
              <div class="skeleton-line w60" />
              <div class="skeleton-line w90" />
              <div class="skeleton-line w40" />
            </div>
          </div>
        </div>

        <template v-else-if="articles.length">
          <ArticleCard v-for="a in articles" :key="a.id" :article="a" />
          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              :current-page="page"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              background
              @current-change="handlePageChange"
            />
          </div>
        </template>

        <div v-else class="empty-tip">暂无文章</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  width: 100%;
}
.home-content {
  display: flex;
  gap: 28px;
  align-items: flex-start;
}
.article-col {
  flex: 1;
  min-width: 0;
}
.article-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 22px;
}
.category-filters {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: 2px;
  min-width: 0;
}
.category-filter {
  min-height: 42px;
  max-width: 100%;
  padding: 10px 14px;
  border: 0;
  border-radius: 12px;
  background: none;
  color: var(--blog-text2);
  font: inherit;
  font-size: 14px;
  font-weight: 500;
  overflow-wrap: anywhere;
  cursor: pointer;
  transition:
    background 0.15s,
    color 0.15s;
}
.category-filter:hover:not(.active) {
  background: var(--blog-hover);
  color: var(--blog-text);
}
.category-filter.active {
  background: var(--blog-text);
  color: var(--blog-card);
}
.category-filter.active:focus-visible {
  outline-color: var(--blog-card);
  outline-offset: -4px;
}
.article-count {
  flex-shrink: 0;
  color: var(--blog-text3);
  font-size: 13px;
  white-space: nowrap;
}

/* 搜索提示 */
.search-result-tip {
  padding: 12px 16px;
  font-size: 14px;
  color: var(--blog-text2);
  background: var(--blog-card);
  border-radius: 18px;
  border: 1px solid var(--blog-border);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.search-count {
  color: #909399;
  font-size: 13px;
}
.clear-search {
  color: var(--blog-accent);
  background: none;
  border: 0;
  font: inherit;
  cursor: pointer;
  font-weight: 600;
  margin-left: auto;
}

/* 骨架屏 */
@keyframes sk-shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
.skeleton-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  flex-direction: row-reverse;
  background: var(--blog-card);
  border-radius: 26px;
  margin-bottom: 20px;
  border: 1px solid var(--blog-border);
}
.skeleton-cover {
  width: 200px;
  height: 130px;
  border-radius: 6px;
  flex-shrink: 0;
  background: linear-gradient(90deg, #ebeef5 25%, #f5f7fa 50%, #ebeef5 75%);
  background-size: 200% 100%;
  animation: sk-shimmer 1.5s ease-in-out infinite;
}
.skeleton-body {
  flex: 1;
}
.skeleton-line {
  height: 14px;
  border-radius: 4px;
  margin-bottom: 10px;
  background: linear-gradient(90deg, #ebeef5 25%, #f5f7fa 50%, #ebeef5 75%);
  background-size: 200% 100%;
  animation: sk-shimmer 1.5s ease-in-out infinite;
}
.w60 {
  width: 60%;
}
.w90 {
  width: 90%;
}
.w40 {
  width: 40%;
}

/* 分页 */
.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.empty-tip {
  padding: 60px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

@media (max-width: 960px) {
  .home-content {
    flex-direction: column;
  }
  .article-col {
    width: 100%;
  }
}
@media (max-width: 600px) {
  .article-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
    margin-bottom: 18px;
  }
  .category-filters {
    flex-wrap: nowrap;
    overflow-x: auto;
    gap: 2px;
  }
  .category-filter {
    flex: none;
    max-width: none;
    min-height: 40px;
    padding: 9px 12px;
    font-size: 13px;
    white-space: nowrap;
  }
  .article-count {
    align-self: flex-end;
    font-size: 12px;
  }
  .search-result-tip {
    font-size: 13px;
    padding: 10px 12px;
  }
  .skeleton-card {
    flex-direction: column;
  }
  .skeleton-cover {
    width: 100%;
    height: 160px;
  }
}
</style>
