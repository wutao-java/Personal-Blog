import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'
import { parse } from 'vue/compiler-sfc'

const readTemplate = (path) => {
  const source = readFileSync(new URL(path, import.meta.url), 'utf8')
  return parse(source).descriptor.template.ast
}

const findElement = (node, predicate) => {
  if (node.type === 1 && predicate(node)) return node
  for (const child of node.children ?? []) {
    const match = findElement(child, predicate)
    if (match) return match
  }
}

test('the shared sidebar stays outside the routed page transition', () => {
  const template = readTemplate('./index.vue')
  const main = findElement(template, (node) =>
    node.props.some(
      (prop) => prop.name === 'class' && prop.value?.content === 'main-inner'
    )
  )
  const sidebar = main.children.find((node) => node.tag === 'SidebarCard')
  assert.ok(sidebar, 'The layout must own the shared sidebar')
  assert.ok(
    sidebar.props.some((prop) => prop.type === 7 && prop.name === 'show'),
    'Hide the sidebar without destroying its state or WebSocket connection'
  )
  assert.equal(
    sidebar.props.some((prop) => prop.type === 7 && prop.name === 'if'),
    false
  )
  const transition = findElement(main, (node) => node.tag === 'transition')
  assert.ok(transition, 'Keep the routed content transition')
  assert.equal(
    findElement(transition, (node) => node.tag === 'SidebarCard'),
    undefined
  )
})

test('routed pages do not recreate the shared sidebar', () => {
  for (const page of [
    'Home',
    'Archive',
    'Links',
    'Message',
    'About',
    'Music',
    'Category',
    'Tag'
  ]) {
    const template = readTemplate(`../${page}/index.vue`)
    assert.equal(
      findElement(template, (node) => node.tag === 'SidebarCard'),
      undefined,
      `${page} must reuse the layout sidebar`
    )
  }
})
