import { onScopeDispose, ref } from 'vue'

export const useMobile = () => {
  const media = window.matchMedia('(max-width: 767px)')
  const isMobile = ref(media.matches)
  const update = (event) => {
    isMobile.value = event.matches
  }

  media.addEventListener('change', update)
  onScopeDispose(() => media.removeEventListener('change', update))

  return { isMobile }
}
