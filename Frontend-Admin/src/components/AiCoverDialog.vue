<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { Loading, Refresh, Check } from '@element-plus/icons-vue'
import { generateAiCover, discardAiCover } from '@/api/ai'

const visible = defineModel({ type: Boolean, default: false })
const props = defineProps({
  title: { type: String, required: true },
  content: { type: String, required: true }
})
const emit = defineEmits(['adopted'])
const candidate = ref(null)
const generating = ref(false)
const imageReady = ref(false)
const imageError = ref(false)
const error = ref('')
let disposed = false

const handleImageError = () => {
  imageError.value = true
  imageReady.value = false
}

const discard = (asset) => {
  if (asset) discardAiCover(asset.id).catch(() => {})
}

const generate = async () => {
  if (generating.value) return
  generating.value = true
  error.value = ''
  try {
    const result = await generateAiCover(props.title, props.content)
    if (disposed) {
      discard(result)
      return
    }
    const previous = candidate.value
    candidate.value = result
    imageReady.value = false
    imageError.value = false
    discard(previous)
  } catch (e) {
    error.value = e?.response?.data?.msg || e?.msg || '封面生成失败，请重试'
  } finally {
    generating.value = false
  }
}

const adopt = () => {
  if (!candidate.value || !imageReady.value || generating.value) return
  emit('adopted', candidate.value)
  candidate.value = null
  visible.value = false
}

const close = (done) => {
  if (!generating.value) done()
}

watch(visible, (open) => {
  if (open) {
    generate()
  } else {
    discard(candidate.value)
    candidate.value = null
  }
})

onBeforeUnmount(() => {
  disposed = true
  discard(candidate.value)
})
</script>

<template>
  <el-dialog
    v-model="visible"
    title="AI 封面"
    width="640px"
    :style="{ maxWidth: 'calc(100% - 32px)' }"
    :close-on-click-modal="false"
    :close-on-press-escape="!generating"
    :show-close="!generating"
    :before-close="close"
  >
    <div class="cover-stage" :aria-busy="generating">
      <img
        v-if="candidate"
        :key="candidate.id"
        :src="candidate.previewUrl"
        alt="AI 候选封面"
        @load="imageReady = true"
        @error="handleImageError"
      />
      <div v-if="generating" class="stage-status" role="status">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
        <span>生成中...</span>
      </div>
      <div v-else-if="!candidate" class="stage-status">暂无候选封面</div>
    </div>
    <el-alert
      v-if="error || imageError"
      :title="imageError ? '封面预览加载失败，请重新生成' : error"
      type="error"
      :closable="false"
      show-icon
      class="cover-error"
    />
    <template #footer>
      <div class="cover-actions">
        <el-button :disabled="generating" @click="visible = false">
          取消
        </el-button>
        <el-button :icon="Refresh" :loading="generating" @click="generate">
          重新生成
        </el-button>
        <el-button
          type="primary"
          :icon="Check"
          :disabled="!candidate || !imageReady || generating"
          @click="adopt"
        >
          采用封面
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.cover-stage {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 2;
  background: #f5f7fa;
  overflow: hidden;
  border-radius: 6px;
}
.cover-stage img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.stage-status {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #606266;
  background: rgb(245 247 250 / 90%);
}
.cover-error {
  margin-top: 12px;
}
.cover-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}
.cover-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}
</style>
