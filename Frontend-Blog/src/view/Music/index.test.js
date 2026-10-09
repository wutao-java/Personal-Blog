import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import test from 'node:test'
import { parse as parseVue } from 'vue/compiler-sfc'

const require = createRequire(import.meta.url)
const { parse: parseCss } = require(
  require.resolve('postcss', { paths: [require.resolve('vite/package.json')] })
)
const source = readFileSync(new URL('./index.vue', import.meta.url), 'utf8')
const { template, styles: vueStyles } = parseVue(source).descriptor
const styles = parseCss(vueStyles[0].content)
const rule = (selector) =>
  styles.nodes.find(
    (node) => node.type === 'rule' && node.selector === selector
  )
const value = (selector, property) =>
  rule(selector)?.nodes.find((node) => node.prop === property)?.value
const findByClass = (node, className) => {
  if (
    node.type === 1 &&
    node.props.some(
      (prop) =>
        prop.name === 'class' &&
        prop.value?.content.split(/\s+/).includes(className)
    )
  ) {
    return node
  }
  for (const child of node.children ?? []) {
    const found = findByClass(child, className)
    if (found) return found
  }
}

test('music list shares one rounded surface instead of alternating row colors', () => {
  assert.equal(value('.playlist', 'background'), 'var(--blog-card)')
  assert.equal(value('.playlist', 'border-radius'), 'var(--blog-radius)')
  assert.equal(value('.track-row', 'background'), 'transparent')
  assert.equal(value('.track-list', 'gap'), '0')
  assert.equal(rule('.track-row:nth-child(odd)'), undefined)
  assert.equal(value('.track-row.active', 'background'), undefined)
  assert.equal(
    value('.track-row.active', 'box-shadow'),
    'inset 3px 0 0 var(--music-accent)'
  )
})

test('player stays inside the taller playlist instead of docking to the viewport', () => {
  const playlist = findByClass(template.ast, 'playlist')
  assert.ok(findByClass(playlist, 'player-dock'))
  assert.equal(value('.playlist', 'min-height'), '80dvh')
  assert.equal(value('.player-dock', 'position'), undefined)
})

test('player controls are centered with balanced side columns and vertical padding', () => {
  assert.equal(
    value('.player-dock', 'grid-template-columns'),
    'minmax(0, 1fr) minmax(220px, 1.7fr) minmax(0, 1fr)'
  )
  assert.equal(value('.player-dock', 'grid-template-rows'), '52px 16px')
  assert.equal(
    value('.player-dock', 'grid-template-areas').replace(/\s+/g, ' '),
    "'track controls volume' '. progress .'"
  )
  assert.equal(value('.player-dock', 'padding'), '8px')
  assert.equal(value('.dock-center', 'display'), 'contents')
  assert.equal(value('.dock-controls', 'grid-area'), 'controls')
  assert.equal(value('.progress-row', 'grid-area'), 'progress')

  const mobile = styles.nodes.find(
    (node) => node.type === 'atrule' && node.params === '(max-width: 760px)'
  )
  const mobileDock = mobile.nodes.find(
    (node) => node.type === 'rule' && node.selector === '.player-dock'
  )
  assert.equal(
    mobileDock.nodes.find((node) => node.prop === 'grid-template-areas').value,
    "'track controls'"
  )
})
