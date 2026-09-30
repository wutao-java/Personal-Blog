<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores'
import { getConfigByKey } from '@/api/settings'
import { Menu } from '@element-plus/icons-vue'
import { useMobile } from '@/composables/useMobile'

const route = useRoute()
const userStore = useUserStore()
const { isMobile } = useMobile()
const mobileMenuOpen = ref(false)

watch(isMobile, (mobile) => {
  if (!mobile) mobileMenuOpen.value = false
})

// 页面刷新时恢复用户信息（含role，用于游客检测）
const footprintEnabled = ref(false)

const fetchFootprintEnabled = async () => {
  try {
    const res = await getConfigByKey('use-footprint')
    footprintEnabled.value = res?.data?.configValue === 'true'
  } catch {
    /* keep false */
  }
}

onMounted(async () => {
  if (userStore.token) userStore.fetchUserInfo()
  fetchFootprintEnabled()
})

watch(
  () => route.path,
  () => {
    mobileMenuOpen.value = false
    fetchFootprintEnabled()
  }
)

/** 侧边栏是否收起 */
const collapsed = ref(false)

const activeMenu = computed(() => route.path)

/** 文章编辑页需要全高无内边距布局 */
const isEditorPage = computed(() => route.path.startsWith('/article/edit'))

const navItems = computed(() => {
  const items = [
    { path: '/dashboard', icon: 'icon-yibiaopan', label: '仪表盘' },
    {
      path: '/article/list',
      icon: 'icon-bianjiwenzhang_huaban',
      label: '文章管理'
    },
    { path: '/category', icon: 'icon-folder', label: '分类 / 标签' },
    { path: '/comment', icon: 'icon-comment', label: '评论管理' },
    { path: '/message', icon: 'icon-liuyan', label: '留言管理' },
    { path: '/friend-link', icon: 'icon-link', label: '友链管理' },
    { path: '/music', icon: 'icon-music', label: '音乐管理' },
    { path: '/visitor', icon: 'icon-user', label: '访客管理' },
    { path: '/view-record', icon: 'icon-eye', label: '浏览记录' }
  ]
  if (footprintEnabled.value) {
    items.push({ path: '/footprint', icon: 'icon-zuji', label: '足迹管理' })
  }
  items.push(
    { path: '/operation-log', icon: 'icon-wj-rz', label: '操作日志' },
    { path: '/profile', icon: 'icon-iconfontprofile', label: '个人资料' },
    { path: '/settings', icon: 'icon-setting', label: '系统设置' }
  )
  return items
})

const handleLogout = () => {
  ElMessageBox.confirm('确认退出登录？', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logoutAction()
  })
}
</script>

<template>
  <div class="admin-shell">
    <!-- 左侧菜单 -->
    <aside :class="['sidebar', { collapsed }]">
      <div class="sidebar-logo">
        <span v-if="!collapsed" class="logo-text">管理控制台</span>
        <span v-else class="logo-icon">
          <span class="iconfont icon-guanliduan" />
        </span>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          :class="['nav-item', { active: activeMenu.startsWith(item.path) }]"
        >
          <span :class="['iconfont', item.icon]" />
          <span v-if="!collapsed" class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <button
        class="collapse-btn"
        :aria-label="collapsed ? '展开侧栏' : '收起侧栏'"
        :title="collapsed ? '展开侧栏' : '收起侧栏'"
        @click="collapsed = !collapsed"
      >
        <span
          :class="[
            'iconfont',
            collapsed ? 'icon-arrow-right-bold' : 'icon-arrow-left-bold'
          ]"
        />
      </button>
    </aside>

    <el-drawer
      v-model="mobileMenuOpen"
      title="管理控制台"
      direction="ltr"
      size="min(280px, 85vw)"
      class="mobile-nav-drawer"
    >
      <nav id="mobile-navigation" class="sidebar-nav mobile-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          :class="['nav-item', { active: activeMenu.startsWith(item.path) }]"
          @click="mobileMenuOpen = false"
        >
          <span :class="['iconfont', item.icon]" />
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>
    </el-drawer>

    <!-- 右侧主区域 -->
    <div class="main-wrapper">
      <!-- 顶部栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <button
            class="mobile-menu-btn"
            aria-label="打开导航菜单"
            title="打开导航菜单"
            aria-controls="mobile-navigation"
            :aria-expanded="mobileMenuOpen"
            @click="mobileMenuOpen = true"
          >
            <el-icon :size="22"><Menu /></el-icon>
          </button>
          <div class="topbar-breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }"
                >首页</el-breadcrumb-item
              >
              <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <div class="topbar-right">
          <span class="user-name">
            <span class="iconfont icon-user" />
            {{ userStore.userInfo?.nickname || '管理员' }}
          </span>
          <button
            class="logout-btn"
            aria-label="退出登录"
            title="退出登录"
            @click="handleLogout"
          >
            <span class="iconfont icon-logout" />
            <span class="logout-label">退出</span>
          </button>
        </div>
      </header>

      <!-- 页面内容 -->
      <main :class="['page-main', { 'editor-page': isEditorPage }]">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" :key="$route.fullPath" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  display: flex;
  height: 100vh;
  height: 100dvh;
  background-color: #f5f7fa;
}

/* ---- 侧边栏 ---- */
.sidebar {
  width: 220px;
  background-color: #ffffff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.25s ease;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e4e7ed;
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 1px;
  white-space: nowrap;
  overflow: hidden;
}

.logo-icon .iconfont {
  font-size: 22px;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 0;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition:
    background 0.15s,
    color 0.15s;
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
}

.nav-item:hover {
  background-color: #f5f7fa;
  color: #303133;
}

.nav-item.active {
  background-color: #f5f7fa;
  color: #000000;
  font-weight: 600;
}

.nav-item .iconfont {
  font-size: 18px;
  flex-shrink: 0;
}

.collapse-btn {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-top: 1px solid #e4e7ed;
  background: transparent;
  cursor: pointer;
  color: #909399;
  transition: color 0.15s;
}

.collapse-btn:hover {
  color: #303133;
}

/* ---- 顶部栏 ---- */
.main-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.mobile-menu-btn {
  display: none;
}

.mobile-nav {
  padding: 0;
}

.mobile-nav-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.mobile-nav-drawer :deep(.el-drawer__body) {
  padding: 12px 0;
  padding-bottom: max(12px, env(safe-area-inset-bottom));
}

.user-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.user-name .iconfont {
  font-size: 16px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.15s;
}

.logout-btn:hover {
  border-color: #000000;
  color: #000000;
}

/* ---- 主内容 ---- */
.page-main {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 24px;
}

.page-main.editor-page {
  padding: 0;
  overflow: hidden;
}

/* ---- 路由切换过渡 ---- */
.page-fade-enter-active,
.page-fade-leave-active {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@media (max-width: 767px) {
  .sidebar {
    display: none;
  }

  .topbar {
    height: 56px;
    gap: 8px;
    padding: 0 12px;
  }

  .topbar-left {
    flex: 1;
  }

  .mobile-menu-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    flex-shrink: 0;
    border: none;
    border-radius: 6px;
    background: transparent;
    color: #303133;
    cursor: pointer;
  }

  .topbar-breadcrumb {
    min-width: 0;
  }

  .topbar-breadcrumb :deep(.el-breadcrumb__item:first-child) {
    display: none;
  }

  .topbar-breadcrumb :deep(.el-breadcrumb__inner) {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .topbar-right {
    flex-shrink: 0;
    gap: 8px;
  }

  .user-name {
    display: block;
    max-width: 72px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
  }

  .user-name .iconfont,
  .logout-label {
    display: none;
  }

  .logout-btn {
    justify-content: center;
    width: 40px;
    height: 40px;
    padding: 0;
    flex-shrink: 0;
    font-size: 18px;
  }

  .page-main {
    padding: 12px;
    padding-bottom: max(12px, env(safe-area-inset-bottom));
  }

  .page-main.editor-page {
    overflow-y: auto;
  }
}
</style>
