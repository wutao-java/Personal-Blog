export const demoTracks = [
  {
    id: 'demo-daybreak',
    title: '晨光',
    artist: 'FeiTwnd',
    album: '原创示例',
    duration: 41,
    musicUrl: '/music/daybreak.mp3'
  },
  {
    id: 'demo-rain-window',
    title: '雨窗',
    artist: 'FeiTwnd',
    album: '原创示例',
    duration: 36,
    musicUrl: '/music/rain-window.mp3'
  },
  {
    id: 'demo-night-walk',
    title: '夜行',
    artist: 'FeiTwnd',
    album: '原创示例',
    duration: 34,
    musicUrl: '/music/night-walk.mp3'
  }
]

export const musicTracksOrDemo = (tracks) =>
  Array.isArray(tracks) && tracks.length ? tracks : demoTracks
