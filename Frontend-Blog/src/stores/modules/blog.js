import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getPersonalInfo } from '@/api/personalInfo'
import { getBlogReport } from '@/api/report'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { getMusicList } from '@/api/music'

export const useBlogStore = defineStore('blog', () => {
  const personalInfo = ref({})
  const report = ref({})
  const categories = ref([])
  const tags = ref([])
  const musics = ref([])
  const loaded = ref(false)

  /* 通过 slug 反查分类 id */
  const getCategoryIdBySlug = (slug) => {
    const cat = categories.value.find((c) => c.slug === slug)
    return cat ? cat.id : null
  }

  /* 通过 slug 反查标签 id */
  const getTagIdBySlug = (slug) => {
    const tag = tags.value.find((t) => t.slug === slug)
    return tag ? tag.id : null
  }

  /* 通过 slug 取分类名 */
  const getCategoryNameBySlug = (slug) => {
    const cat = categories.value.find((c) => c.slug === slug)
    return cat ? cat.name : ''
  }

  /* 通过 slug 取分类描述 */
  const getCategoryDescBySlug = (slug) => {
    const cat = categories.value.find((c) => c.slug === slug)
    return cat ? cat.description || '' : ''
  }

  /* 通过 slug 取标签名 */
  const getTagNameBySlug = (slug) => {
    const tag = tags.value.find((t) => t.slug === slug)
    return tag ? tag.name : ''
  }

  const init = async () => {
    if (loaded.value) return
    const [infoResult, reportResult, categoryResult, tagResult, musicResult] =
      await Promise.allSettled([
        getPersonalInfo(),
        getBlogReport(),
        getCategories(),
        getTags(),
        getMusicList()
      ])

    if (infoResult.status === 'fulfilled') {
      personalInfo.value = infoResult.value.data.data ?? {}
    }
    if (reportResult.status === 'fulfilled') {
      report.value = reportResult.value.data.data ?? {}
    }
    if (categoryResult.status === 'fulfilled') {
      categories.value = categoryResult.value.data.data ?? []
    }
    if (tagResult.status === 'fulfilled') {
      tags.value = tagResult.value.data.data ?? []
    }
    if (musicResult.status === 'fulfilled') {
      musics.value = musicResult.value.data.data ?? []
    }

    const failures = [
      infoResult,
      reportResult,
      categoryResult,
      tagResult,
      musicResult
    ].filter((result) => result.status === 'rejected')
    if (failures.length > 0) {
      console.error(
        '部分博客数据加载失败',
        failures.map((failure) => failure.reason)
      )
    }
    loaded.value = failures.length === 0
  }

  return {
    personalInfo,
    report,
    categories,
    tags,
    musics,
    loaded,
    init,
    getCategoryIdBySlug,
    getTagIdBySlug,
    getCategoryNameBySlug,
    getCategoryDescBySlug,
    getTagNameBySlug
  }
})
