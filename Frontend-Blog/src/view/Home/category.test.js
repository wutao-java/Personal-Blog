import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'
import { nextTick, reactive, ref, watch } from 'vue'
import { babelParse, parse } from 'vue/compiler-sfc'

const source = readFileSync(new URL('./index.vue', import.meta.url), 'utf8')
const descriptor = parse(source).descriptor
const script = descriptor.scriptSetup.content
const body = babelParse(script, { sourceType: 'module' })
  .program.body.filter((node) => node.type !== 'ImportDeclaration')
  .map((node) => script.slice(node.start, node.end))
  .join('\n')

const response = (records = [], total = records.length) => ({
  data: { data: { records, total } }
})

const setup = (t, { search, categoryRequest } = {}) => {
  const route = reactive({ query: { search } })
  const mounted = []
  const api = {
    getArticlePage: t.mock.fn(async () => response([{ id: 10 }], 43)),
    getArticlesByCategory: t.mock.fn(
      categoryRequest || (async () => response([{ id: 20 }], 12))
    ),
    searchArticles: t.mock.fn(async () => response([{ id: 30 }], 1))
  }
  const dependencies = {
    ref,
    watch: (source, callback) => {
      t.after(watch(source, callback))
    },
    onMounted: (callback) => mounted.push(callback),
    useRoute: () => route,
    useRouter: () => ({ push: t.mock.fn() }),
    useBlogStore: () => ({ categories: [{ id: 5, name: 'rag' }] }),
    window: { scrollTo: t.mock.fn() },
    ...api
  }
  const state = new Function(
    ...Object.keys(dependencies),
    `${body}\nreturn {
      articles, total, page, loading, selectedCategoryId, searchKeyword,
      loadArticles, handleCategoryChange, handlePageChange
    }
    `
  )(...Object.values(dependencies))
  return { state, route, api, mount: () => mounted[0]() }
}

test('the homepage initially loads all articles', async (t) => {
  const { state, api, mount } = setup(t)
  await mount()
  assert.equal(state.selectedCategoryId.value, null)
  assert.deepEqual(api.getArticlePage.mock.calls[0].arguments, [1, 10])
  assert.equal(api.getArticlesByCategory.mock.callCount(), 0)
  assert.equal(state.total.value, 43)
})

test('choosing a category resets pagination and loads its articles', async (t) => {
  const { state, api, mount } = setup(t)
  await mount()
  state.page.value = 3
  await state.handleCategoryChange(5)
  assert.equal(state.selectedCategoryId.value, 5)
  assert.equal(state.page.value, 1)
  assert.deepEqual(
    api.getArticlesByCategory.mock.calls[0].arguments,
    [5, 1, 10]
  )
  assert.equal(state.total.value, 12)
  assert.equal(state.articles.value[0].id, 20)
})

test('the toolbar count describes the current API total', () => {
  assert.match(
    descriptor.template.content,
    /loading \? '加载中\.\.\.' : `共 \$\{total\} 篇文章`/
  )
})

test('pagination keeps the selected category', async (t) => {
  const { state, api, mount } = setup(t)
  await mount()
  await state.handleCategoryChange(5)
  await state.handlePageChange(2)
  assert.deepEqual(
    api.getArticlesByCategory.mock.calls[1].arguments,
    [5, 2, 10]
  )
  assert.equal(state.page.value, 2)
})

test('the all filter restores the full list on page one', async (t) => {
  const { state, api, mount } = setup(t)
  await mount()
  await state.handleCategoryChange(5)
  await state.handlePageChange(2)
  await state.handleCategoryChange(null)
  assert.equal(state.selectedCategoryId.value, null)
  assert.equal(state.page.value, 1)
  assert.deepEqual(api.getArticlePage.mock.calls[1].arguments, [1, 10])
  assert.equal(state.total.value, 43)
})

test('choosing the active filter does not repeat a request', async (t) => {
  const { state, api, mount } = setup(t)
  await mount()
  await state.handleCategoryChange(null)
  assert.equal(api.getArticlePage.mock.callCount(), 1)
  await state.handleCategoryChange(5)
  await state.handleCategoryChange(5)
  assert.equal(api.getArticlesByCategory.mock.callCount(), 1)
})

test('a direct search still uses the search endpoint', async (t) => {
  const { state, api, mount } = setup(t, { search: 'rag' })
  await mount()
  assert.deepEqual(api.searchArticles.mock.calls[0].arguments, ['rag', 1, 10])
  assert.equal(api.getArticlePage.mock.callCount(), 0)
  assert.equal(state.total.value, 1)
})

test('searching and clearing search reset the category and page', async (t) => {
  const { state, route, api, mount } = setup(t)
  await mount()
  await state.handleCategoryChange(5)
  state.page.value = 2
  route.query.search = 'rag'
  await nextTick()
  assert.equal(state.selectedCategoryId.value, null)
  assert.equal(state.page.value, 1)
  assert.deepEqual(api.searchArticles.mock.calls[0].arguments, ['rag', 1, 10])
  route.query.search = undefined
  await nextTick()
  assert.equal(state.searchKeyword.value, '')
  assert.deepEqual(api.getArticlePage.mock.calls[1].arguments, [1, 10])
})

test('a slower previous category cannot replace the current results', async (t) => {
  const pending = []
  const { state, mount } = setup(t, {
    categoryRequest: () =>
      new Promise((resolve) => {
        pending.push(resolve)
      })
  })
  await mount()
  state.handleCategoryChange(5)
  state.handleCategoryChange(1)
  pending[0](response([{ id: 50 }], 5))
  await Promise.resolve()
  assert.equal(state.loading.value, true)
  assert.equal(state.selectedCategoryId.value, 1)
  assert.equal(state.articles.value[0].id, 10)
  pending[1](response([{ id: 60 }], 6))
  await Promise.resolve()
  assert.equal(state.loading.value, false)
  assert.equal(state.articles.value[0].id, 60)
  assert.equal(state.total.value, 6)
})

test('a failed category request clears both articles and the count', async (t) => {
  const { state, mount } = setup(t, {
    categoryRequest: async () => {
      throw new Error('Request failed')
    }
  })
  await mount()
  await state.handleCategoryChange(5)
  assert.equal(state.articles.value.length, 0)
  assert.equal(state.total.value, 0)
  assert.equal(state.loading.value, false)
})
