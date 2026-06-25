<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Coin,
  DataAnalysis,
  Document,
  Expand,
  Fold,
  Grid,
  Key,
  Service,
  SwitchButton,
  User,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useLanguage } from '@/i18n'
import RoleAvatar from '@/components/RoleAvatar.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { locale, toggleLocale } = useLanguage()
const collapsed = ref(false)
const title = computed(() => String(route.meta.title || 'KeyBridge AI'))
const isPublicVisitor = computed(() => !auth.isLoggedIn)

async function handleLogout() {
  await ElMessageBox.confirm('确定退出当前账号吗？', '退出登录', { type: 'warning' })
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-layout" :class="{ collapsed }">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark"><img src="/keybridge-logo.png" alt="KeyBridge AI Logo" /></div>
        <div v-show="!collapsed" class="brand-copy">
          <strong>KeyBridge AI</strong>
          <span>Unified API Console</span>
        </div>
      </div>

      <el-menu :default-active="route.path" router :collapse="collapsed" class="side-menu">
        <template v-if="auth.isLoggedIn">
          <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><template #title>首页仪表盘</template></el-menu-item>
          <el-menu-item index="/keys"><el-icon><Key /></el-icon><template #title>API Key 管理</template></el-menu-item>
          <el-menu-item index="/playground"><el-icon><ChatDotRound /></el-icon><template #title>AI 调用测试</template></el-menu-item>
          <el-menu-item index="/logs"><el-icon><Document /></el-icon><template #title>调用日志</template></el-menu-item>
        </template>
        <el-menu-item v-if="!auth.isAdmin" index="/models"><el-icon><Grid /></el-icon><template #title>模型广场</template></el-menu-item>
        <el-menu-item v-if="auth.isLoggedIn && !auth.isAdmin" index="/contact"><el-icon><Service /></el-icon><template #title>联系我们</template></el-menu-item>
        <template v-if="auth.isAdmin">
          <div v-show="!collapsed" class="menu-caption">管理员</div>
          <el-menu-item index="/models"><el-icon><Grid /></el-icon><template #title>模型与价格</template></el-menu-item>
          <el-menu-item index="/admin/recharges"><el-icon><Coin /></el-icon><template #title>充值订单</template></el-menu-item>
          <el-menu-item index="/admin/users"><el-icon><User /></el-icon><template #title>用户管理</template></el-menu-item>
          <el-menu-item index="/admin/logs"><el-icon><Document /></el-icon><template #title>全站日志</template></el-menu-item>
        </template>
      </el-menu>

      <div v-if="isPublicVisitor && !collapsed" class="public-tip">
        <strong>公开浏览模式</strong>
        <span>登录后可管理 API Key 并发起模型调用。</span>
      </div>

      <button class="collapse-button" @click="collapsed = !collapsed">
        <el-icon><Expand v-if="collapsed" /><Fold v-else /></el-icon>
        <span v-show="!collapsed">收起菜单</span>
      </button>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <div>
          <div class="topbar-title">{{ title }}</div>
          <div class="topbar-subtitle">AI API 密钥管理与统一转发平台</div>
        </div>
        <div class="topbar-actions">
          <button class="language-switch" type="button" :title="locale === 'zh' ? '切换为英文' : 'Switch to Chinese'" @click="toggleLocale">
            <span class="language-symbol">文</span>
            <strong>{{ locale === 'zh' ? '中 / EN' : 'EN / 中' }}</strong>
          </button>
          <button v-if="isPublicVisitor" class="login-link" type="button" @click="router.push('/console-login')">登录</button>
          <el-dropdown v-else trigger="click">
            <div class="user-chip">
              <RoleAvatar :role="auth.isAdmin ? 'ADMIN' : 'USER'" :size="38" :label="`${auth.user?.nickname || ''} ${auth.isAdmin ? '管理员头像' : '普通用户头像'}`" />
              <div class="user-meta">
                <strong>{{ auth.user?.nickname }}</strong>
                <span>{{ auth.isAdmin ? '管理员' : '普通用户' }}</span>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ auth.user?.username }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="content"><router-view /></main>
    </div>
  </div>
</template>

<style scoped>
.app-layout { min-height: 100vh; --side-width: 238px; }
.app-layout.collapsed { --side-width: 72px; }
.sidebar { position: fixed; inset: 0 auto 0 0; z-index: 20; display: flex; width: var(--side-width); flex-direction: column; overflow: hidden; background: var(--sidebar); color: #fff; transition: width .2s ease; }
.brand { display: flex; height: 74px; align-items: center; gap: 11px; padding: 0 16px; border-bottom: 1px solid rgba(255,255,255,.08); }
.brand-mark { display: grid; width: 44px; height: 44px; flex: 0 0 44px; overflow: hidden; place-items: center; border: 1px solid rgba(255,255,255,.14); border-radius: 12px; background: #fff; box-shadow: 0 8px 22px rgba(45,73,180,.24); }
.brand-mark img { display: block; width: 100%; height: 100%; object-fit: cover; }
.brand-copy { display: flex; min-width: 150px; flex-direction: column; gap: 3px; }
.brand-copy strong { font-size: 15px; }
.brand-copy span { color: #8993a8; font-size: 10px; letter-spacing: .4px; }
.side-menu { flex: 1; padding: 14px 10px; border-right: 0; background: transparent; }
.side-menu:not(.el-menu--collapse) { width: 238px; }
.side-menu :deep(.el-menu-item) { height: 46px; margin: 3px 0; border-radius: 9px; color: #aab2c3; }
.side-menu :deep(.el-menu-item:hover) { background: rgba(255,255,255,.06); color: #fff; }
.side-menu :deep(.el-menu-item.is-active) { background: rgba(99,112,245,.18); color: #aeb5ff; }
.menu-caption { padding: 19px 12px 6px; color: #657087; font-size: 11px; letter-spacing: 1px; }
.public-tip { margin: 0 14px 14px; padding: 13px; border: 1px solid rgba(255,255,255,.09); border-radius: 12px; background: rgba(255,255,255,.05); }
.public-tip strong { display: block; color: #dbe3ff; font-size: 12px; }
.public-tip span { display: block; margin-top: 6px; color: #8993a8; font-size: 10px; line-height: 1.6; }
.collapse-button { display: flex; height: 52px; align-items: center; justify-content: center; gap: 8px; border: 0; border-top: 1px solid rgba(255,255,255,.08); background: transparent; color: #929caf; cursor: pointer; }
.main-area { min-height: 100vh; margin-left: var(--side-width); transition: margin-left .2s ease; }
.topbar { position: sticky; top: 0; z-index: 10; display: flex; height: 74px; align-items: center; justify-content: space-between; padding: 0 28px; border-bottom: 1px solid var(--border); background: rgba(255,255,255,.93); backdrop-filter: blur(12px); }
.topbar-title { font-size: 17px; font-weight: 700; }
.topbar-subtitle { margin-top: 3px; color: var(--muted); font-size: 12px; }
.topbar-actions { display: flex; align-items: center; gap: 10px; }
.language-switch { display: flex; height: 38px; align-items: center; gap: 7px; padding: 0 11px; border: 1px solid #e0e5ef; border-radius: 10px; background: #fff; color: #5360df; cursor: pointer; box-shadow: 0 5px 16px rgba(44,57,96,.05); transition: border-color .2s ease, background .2s ease, transform .2s ease; }
.language-switch:hover { border-color: #aeb7ff; background: #f5f7ff; transform: translateY(-1px); }
.language-symbol { display: grid; width: 22px; height: 22px; place-items: center; border-radius: 7px; background: #eef0ff; font-size: 12px; font-weight: 800; }
.language-switch strong { font-size: 11px; white-space: nowrap; }
.login-link { height: 38px; padding: 0 18px; border: 0; border-radius: 10px; background: linear-gradient(135deg, #5b67f1, #4f8df7); box-shadow: 0 8px 18px rgba(77,102,235,.2); color: #fff; cursor: pointer; font-size: 13px; font-weight: 700; }
.login-link:hover { filter: brightness(1.04); transform: translateY(-1px); }
.user-chip { display: flex; align-items: center; gap: 10px; padding: 6px 9px; border-radius: 10px; cursor: pointer; }
.user-chip:hover { background: #f4f6fa; }
.user-meta { display: flex; flex-direction: column; min-width: 72px; }
.user-meta strong { font-size: 13px; }
.user-meta span { margin-top: 2px; color: var(--muted); font-size: 11px; }
.content { padding: 24px 28px 36px; }
@media (max-width: 820px) {
  .app-layout, .app-layout.collapsed { --side-width: 72px; }
  .brand-copy, .collapse-button span, .menu-caption { display: none; }
  .side-menu:not(.el-menu--collapse) { width: 72px; }
  .side-menu :deep(.el-menu-item span) { display: none; }
  .topbar { padding: 0 16px; }
  .content { padding: 18px 14px 28px; }
  .topbar-subtitle, .user-meta { display: none; }
  .language-switch { padding: 0 8px; }
  .language-switch strong { display: none; }
}
</style>
