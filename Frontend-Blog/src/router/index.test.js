import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'
import { babelParse, parse } from 'vue/compiler-sfc'

const readSource = (path) =>
  readFileSync(new URL(path, import.meta.url), 'utf8')
const parseScript = (source) => babelParse(source, { sourceType: 'module' })

function* nodes(node) {
  if (!node || typeof node !== 'object') return
  if (node.type) yield node
  for (const value of Object.values(node)) {
    if (Array.isArray(value)) {
      for (const child of value) yield* nodes(child)
    } else if (value && typeof value === 'object') {
      yield* nodes(value)
    }
  }
}

const property = (node, name) =>
  node.properties?.find((item) => item.key?.name === name)?.value

const routerNodes = [...nodes(parseScript(readSource('./index.js')))]
const route = (path) =>
  routerNodes.find(
    (node) =>
      node.type === 'ObjectExpression' && property(node, 'path')?.value === path
  )

test('the retired personal homepage redirects to the retained about page', () => {
  const legacyRoute = route('/home')
  assert.ok(legacyRoute)
  assert.equal(property(legacyRoute, 'redirect')?.value, '/about')
  assert.equal(property(legacyRoute, 'component'), undefined)
})

test('the retained personal page uses the homepage title', () => {
  const title = '\u4e3b\u9875'
  assert.equal(
    property(property(route('about'), 'meta'), 'title')?.value,
    title
  )
  const script = parse(readSource('../view/About/index.vue')).descriptor
    .scriptSetup
  const assignment = [...nodes(parseScript(script.content))].find(
    (node) =>
      node.type === 'AssignmentExpression' &&
      node.left.object?.name === 'articleTitle' &&
      node.left.property?.name === 'value'
  )
  assert.equal(assignment?.right.value, title)
})

test('the blog article listing remains the root page', () => {
  const blogRoute = route('')
  assert.equal(property(blogRoute, 'name')?.value, 'home')
  assert.equal(
    property(blogRoute, 'component')?.body.arguments[0].value,
    '@/view/Home/index.vue'
  )
})

test('navigation ends with one homepage entry pointing to the retained page', () => {
  const script = parse(readSource('../components/BlogHeader.vue')).descriptor
    .scriptSetup
  const scriptNodes = [...nodes(parseScript(script.content))]
  const entries = scriptNodes.filter(
    (node) => node.type === 'ObjectExpression' && property(node, 'to')
  )
  const homeEntries = entries.filter(
    (node) => property(node, 'label')?.value === '\u4e3b\u9875'
  )
  assert.equal(homeEntries.length, 1)
  assert.equal(property(homeEntries[0], 'to')?.value, '/about')
  assert.equal(
    entries.some((node) => property(node, 'to')?.value === '/home'),
    false
  )
  const navItems = scriptNodes.find(
    (node) => node.type === 'VariableDeclarator' && node.id.name === 'navItems'
  )
  const statements = navItems.init.arguments[0].body.body
  const initialItems = statements[0].declarations[0].init.elements
  assert.equal(
    initialItems.some(
      (node) => property(node, 'label')?.value === '\u4e3b\u9875'
    ),
    false
  )
  const finalPush = statements.findLast(
    (node) =>
      node.type === 'ExpressionStatement' &&
      node.expression.callee?.property?.name === 'push'
  )
  assert.equal(
    property(finalPush.expression.arguments[0], 'label')?.value,
    '\u4e3b\u9875'
  )
})
