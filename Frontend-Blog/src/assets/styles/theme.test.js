import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import test from 'node:test'
import { fileURLToPath } from 'node:url'
import { parse as parseVue } from 'vue/compiler-sfc'
import { compile } from 'sass'

const require = createRequire(import.meta.url)
const { parse } = createRequire(require.resolve('vite/package.json'))('postcss')
const stylesheet = parse(
  compile(fileURLToPath(new URL('./main.scss', import.meta.url))).css
)
const variables = (selector) =>
  new Map(
    stylesheet.nodes
      .find((node) => node.selector === selector)
      .nodes.filter((node) => node.type === 'decl')
      .map((node) => [node.prop, node.value])
  )
const dark = variables('html.dark')
const light = variables(':root')

test('dark mode uses neutral near-black surfaces with distinct card layers', () => {
  assert.equal(dark.get('--blog-bg'), '#181818')
  assert.equal(dark.get('--blog-card'), '#232323')
  assert.equal(dark.get('--blog-hover'), '#2a2a2a')
})

test('dark theme colors and translucent surfaces do not have a green tint', () => {
  for (const [name, value] of dark) {
    if (
      (name.startsWith('--blog-') || name.startsWith('--el-color-primary')) &&
      value.startsWith('#')
    ) {
      assert.match(value, /^#([0-9a-f]{2})\1\1$/i, name)
    }
  }
  for (const name of ['--blog-glass', '--blog-glass-border']) {
    assert.match(dark.get(name), /^rgb\((\d+) \1 \1 \/ \d+%\)$/, name)
  }
})

test('Element Plus uses the dark blog surfaces and accent', () => {
  assert.equal(dark.get('--el-bg-color'), 'var(--blog-card)')
  assert.equal(dark.get('--el-bg-color-page'), 'var(--blog-bg)')
  assert.equal(dark.get('--el-bg-color-overlay'), 'var(--blog-card)')
  assert.equal(dark.get('--el-color-primary'), 'var(--blog-accent)')
  assert.equal(
    dark.get('--el-color-primary-light-9'),
    'var(--blog-accent-soft)'
  )
})

test('the light theme keeps its existing surfaces and green accent', () => {
  assert.equal(light.get('--blog-bg'), '#f3f5f4')
  assert.equal(light.get('--blog-card'), '#ffffff')
  assert.equal(light.get('--blog-accent'), '#396954')
})

test('category and tag dialogs share the search dialog glass surface', () => {
  const glassRule = stylesheet.nodes.find(
    (node) => node.selector?.includes('.blog-glass-dialog') &&
      node.selector.includes('.blog-search-dialog')
  )
  assert.equal(
    glassRule.nodes.find((node) => node.prop === 'background')?.value,
    'var(--blog-glass)'
  )
  const reduced = stylesheet.nodes.find(
    (node) => node.name === 'media' &&
      node.params === '(prefers-reduced-transparency: reduce)'
  )
  assert.ok(
    reduced.nodes.some((node) =>
      node.selector?.includes('.blog-glass-dialog') &&
      node.nodes.some(
        (declaration) =>
          declaration.prop === 'background' &&
          declaration.value === 'var(--blog-card)'
      )
    )
  )
})

test('header popovers use glass with a solid reduced-transparency fallback', () => {
  const header = parseVue(
    readFileSync(new URL('../../components/BlogHeader.vue', import.meta.url), 'utf8')
  ).descriptor.styles[0].content
  const css = parse(header)
  for (const selector of ['.music-panel', '.nav-mobile']) {
    let glass = false
    let solid = false
    css.walkRules((rule) => {
      if (!rule.selector.split(',').map((part) => part.trim()).includes(selector)) return
      const declarations = rule.nodes.filter((node) => node.type === 'decl')
      if (
        declarations.some(
          (node) => node.prop === 'background' && node.value === 'var(--blog-glass)'
        ) &&
        declarations.some((node) => node.prop === 'backdrop-filter')
      ) {
        glass = true
      }
      if (
        rule.parent.name === 'media' &&
        rule.parent.params === '(prefers-reduced-transparency: reduce)' &&
        declarations.some(
          (node) => node.prop === 'background' && node.value === 'var(--blog-card)'
        )
      ) {
        solid = true
      }
    })
    assert.ok(glass, `${selector} should use the glass surface`)
    assert.ok(solid, `${selector} should have a solid fallback`)
  }
})
