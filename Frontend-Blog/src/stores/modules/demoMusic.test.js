import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'
import { demoTracks, musicTracksOrDemo } from './demoMusic.js'

test('local demo tracks have playable audio files and playlist metadata', () => {
  assert.ok(demoTracks.length >= 3)
  assert.equal(
    new Set(demoTracks.map((track) => track.id)).size,
    demoTracks.length
  )

  for (const track of demoTracks) {
    assert.ok(track.title && track.artist && track.album)
    assert.ok(track.duration > 0)
    assert.match(track.musicUrl, /^\/music\/[\w-]+\.mp3$/)
    const audio = readFileSync(
      new URL(`../../../public${track.musicUrl}`, import.meta.url)
    )
    assert.ok(audio.length > 10_000)
    assert.equal(audio.toString('ascii', 0, 3), 'ID3')
  }
})

test('published tracks take priority over demo tracks', () => {
  const published = [{ id: 1, title: 'Published' }]
  assert.equal(musicTracksOrDemo(published), published)
  assert.equal(musicTracksOrDemo([]), demoTracks)
  assert.equal(musicTracksOrDemo(null), demoTracks)
})
