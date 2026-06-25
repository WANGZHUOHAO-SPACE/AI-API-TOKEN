<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Connection, Lock, Moon, Service, Sunny } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useLanguage } from '@/i18n'
import RotatingGlobe from '@/components/RotatingGlobe.vue'

type ThemeMode = 'light' | 'dark' | 'system'

const router = useRouter()
const auth = useAuthStore()
const { locale: language, toggleLocale: toggleLanguage } = useLanguage()
const contactVisible = ref(false)
const savedTheme = localStorage.getItem('keybridge_theme') as ThemeMode | null
const themeMode = ref<ThemeMode>(['light', 'dark', 'system'].includes(savedTheme || '') ? savedTheme as ThemeMode : 'system')
const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
const systemDark = ref(mediaQuery.matches)
const isDark = computed(() => themeMode.value === 'dark' || (themeMode.value === 'system' && systemDark.value))
const featureIndex = ref(0)

const stats = reactive([
  { label: '全球用户', value: 6735, step: 7 },
  { label: 'API 调用', value: 99475, step: 168 },
  { label: 'AI 模型', value: 205, step: 1 },
])

const features = [
  {
    title: '全球服务',
    description: '覆盖 8 个核心区域，20 万+客户信赖，全年稳定运行',
    icon: Connection,
    tone: 'purple',
  },
  {
    title: '企业级安全',
    description: 'AES 加密存储、JWT 身份鉴权，保障每一次 API 调用',
    icon: Lock,
    tone: 'blue',
  },
  {
    title: '统一转发',
    description: '统一封装多家模型接口，降低课程演示和二次开发成本',
    icon: Service,
    tone: 'cyan',
  },
]

const navigation = [
  { key: 'home', label: '首页' },
  { key: 'console', label: '控制台' },
  { key: 'models', label: '模型广场' },
  { key: 'contact', label: '联系我们' },
  { key: 'news', label: '文章资讯' },
  { key: 'docs', label: 'API 文档' },
]

const currentFeature = computed(() => features[featureIndex.value])

let statsTimer: number | undefined
let featureTimer: number | undefined

function formatNumber(value: number) {
  return `${Math.round(value).toLocaleString('en-US')}+`
}

function setThemeMode(mode: ThemeMode) {
  themeMode.value = mode
  localStorage.setItem('keybridge_theme', mode)
}

function openConsole() {
  if (auth.isLoggedIn) router.push('/dashboard')
  else router.push('/console-login')
}

function handleNavigation(key: string) {
  if (key === 'home') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }
  if (key === 'console') {
    openConsole()
    return
  }
  if (key === 'models') {
    router.push('/models')
    return
  }
  if (key === 'contact') {
    contactVisible.value = true
    return
  }
  if (key === 'news') {
    window.open('https://jeniya.chat/news', '_blank', 'noopener,noreferrer')
    return
  }
  if (key === 'docs') {
    window.open('https://api-jeniya-top.apifox.cn/', '_blank', 'noopener,noreferrer')
  }
}

function handleSystemThemeChange(event: MediaQueryListEvent) {
  systemDark.value = event.matches
}

onMounted(() => {
  mediaQuery.addEventListener('change', handleSystemThemeChange)
  statsTimer = window.setInterval(() => {
    stats.forEach((item) => {
      item.value += Math.max(1, Math.round(Math.random() * item.step))
    })
  }, 1700)
  featureTimer = window.setInterval(() => {
    featureIndex.value = (featureIndex.value + 1) % features.length
  }, 3600)
})

onBeforeUnmount(() => {
  mediaQuery.removeEventListener('change', handleSystemThemeChange)
  window.clearInterval(statsTimer)
  window.clearInterval(featureTimer)
})
</script>

<template>
  <div class="landing-shell" :class="{ 'theme-dark': isDark }">
    <header class="landing-header">
      <button class="brand" type="button" @click="handleNavigation('home')">
        <img src="/keybridge-logo.png" alt="KeyBridge AI Logo" />
        <span>
          <strong>KeyBridge AI</strong>
          <small>统一 AI API 平台</small>
        </span>
      </button>

      <nav class="nav-links" aria-label="主导航">
        <button v-for="item in navigation" :key="item.key" type="button" :class="{ active: item.key === 'home' }" @click="handleNavigation(item.key)">
          {{ item.label }}
        </button>
      </nav>

      <div class="header-actions">
        <el-dropdown trigger="click">
          <button class="icon-action" type="button" title="主题模式">
            <el-icon><Moon v-if="isDark" /><Sunny v-else /></el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="setThemeMode('light')">浅色</el-dropdown-item>
              <el-dropdown-item @click="setThemeMode('dark')">深色</el-dropdown-item>
              <el-dropdown-item @click="setThemeMode('system')">跟随系统</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <button class="language-action" type="button" @click="toggleLanguage">{{ language === 'zh' ? '中 / EN' : 'EN / 中' }}</button>
        <button class="login-action" type="button" @click="openConsole">登录</button>
      </div>
    </header>

    <main class="landing-main">
      <section class="hero-panel">
        <RotatingGlobe class="hero-globe" />
        <div class="hero-content">
          <div class="eyebrow">KEYBRIDGE AI</div>
          <h1>
            解锁未来<br />
            <span>AI 生产力</span>
          </h1>
          <div class="hero-line"></div>
          <p>聚合全球顶级模型，提供最稳定的<br />企业级 API 访问通道。</p>

          <div class="stats-grid">
            <div v-for="item in stats" :key="item.label" class="stat-item">
              <strong>{{ formatNumber(item.value) }}</strong>
              <span>{{ item.label }}</span>
            </div>
          </div>

          <article class="feature-card" :class="`tone-${currentFeature.tone}`">
            <div class="feature-icon"><component :is="currentFeature.icon" /></div>
            <div>
              <h2>{{ currentFeature.title }}</h2>
              <p>{{ currentFeature.description }}</p>
            </div>
            <div class="feature-progress"></div>
          </article>
          <div class="feature-dots">
            <button v-for="(_, index) in features" :key="index" type="button" :class="{ active: index === featureIndex }" @click="featureIndex = index"></button>
          </div>
        </div>
      </section>

      <section class="entry-panel">
        <div class="entry-card">
          <div class="entry-logo"><img src="/keybridge-logo.png" alt="KeyBridge AI Logo" /></div>
          <p class="entry-kicker">Console Portal</p>
          <h2>控制台入口</h2>
          <p class="entry-text">点击进入独立登录页面，登录后可管理 API Key、测试模型调用、查看调用日志与余额。</p>
          <button class="primary-entry" type="button" @click="openConsole">进入控制台</button>
          <button class="secondary-entry" type="button" @click="router.push('/models')">先看模型广场</button>
          <div class="entry-note">
            <strong>演示账号</strong>
            <span>普通用户：demo_user / demo123456</span>
            <span>管理员：admin / admin123456</span>
          </div>
        </div>
      </section>
    </main>

    <el-dialog v-model="contactVisible" title="联系我们" width="520px">
      <div class="contact-dialog">
        <p>项目咨询、接口接入和演示支持，请通过以下方式联系。</p>
        <div><strong>联系邮箱</strong><span>19834339457@163.com</span></div>
        <div><strong>电话支持</strong><span>19834339457</span></div>
        <div><strong>在线时间</strong><span>周一至周日 10:00-21:00</span></div>
      </div>
      <template #footer>
        <el-button type="primary" @click="contactVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.landing-shell { min-height: 100vh; background: #f7faff; color: #101827; }
.landing-shell.theme-dark { background: #0b1220; color: #f8fbff; }
.landing-header { position: sticky; top: 0; z-index: 20; display: grid; height: 78px; align-items: center; padding: 0 30px; border-bottom: 1px solid rgba(224,230,241,.75); background: rgba(255,255,255,.9); box-shadow: 0 14px 38px rgba(37,55,96,.08); backdrop-filter: blur(18px); grid-template-columns: minmax(240px, 1fr) auto minmax(240px, 1fr); }
.theme-dark .landing-header { border-color: rgba(255,255,255,.08); background: rgba(11,18,32,.86); box-shadow: 0 14px 38px rgba(0,0,0,.25); }
.brand { display: flex; align-items: center; gap: 12px; padding: 0; border: 0; background: transparent; color: inherit; cursor: pointer; text-align: left; }
.brand img { width: 48px; height: 48px; border-radius: 14px; object-fit: cover; box-shadow: 0 10px 24px rgba(74,103,242,.18); }
.brand span { display: flex; flex-direction: column; gap: 2px; }
.brand strong { font-size: 17px; font-weight: 850; }
.brand small { color: #7c879b; font-size: 11px; }
.theme-dark .brand small { color: #9ca7bd; }
.nav-links { display: flex; align-items: center; justify-content: center; gap: 34px; }
.nav-links button { position: relative; padding: 26px 0 24px; border: 0; background: transparent; color: #303a4f; cursor: pointer; font-size: 15px; font-weight: 750; }
.theme-dark .nav-links button { color: #dbe4ff; }
.nav-links button::after { position: absolute; right: 0; bottom: 12px; left: 0; height: 3px; border-radius: 999px; background: linear-gradient(90deg, #2f85ff, #6c63ff); content: ''; opacity: 0; transform: scaleX(.5); transition: opacity .2s ease, transform .2s ease; }
.nav-links button:hover::after, .nav-links button.active::after { opacity: 1; transform: scaleX(1); }
.header-actions { display: flex; align-items: center; justify-content: flex-end; gap: 12px; }
.icon-action, .language-action, .login-action { border: 0; cursor: pointer; font-weight: 800; }
.icon-action { display: grid; width: 42px; height: 42px; place-items: center; border-radius: 999px; background: #f0f5ff; color: #2f85ff; box-shadow: 0 8px 24px rgba(50,88,154,.08); }
.theme-dark .icon-action { background: rgba(255,255,255,.08); color: #aebcff; }
.language-action { height: 42px; padding: 0 12px; border-radius: 999px; background: transparent; color: #2f85ff; }
.login-action { height: 48px; padding: 0 25px; border-radius: 18px; background: linear-gradient(135deg, #36a9ff, #2877ff); color: #fff; box-shadow: 0 14px 28px rgba(47,132,255,.24); }
.landing-main { display: grid; min-height: calc(100vh - 78px); grid-template-columns: minmax(0, 1.58fr) minmax(410px, .92fr); }
.hero-panel { position: relative; min-height: calc(100vh - 78px); overflow: hidden; background: radial-gradient(circle at 56% 26%, rgba(88,99,220,.35), transparent 28%), linear-gradient(135deg, #0e1726 0%, #111936 52%, #081121 100%); color: #fff; }
.theme-dark .hero-panel { background: radial-gradient(circle at 56% 26%, rgba(102,112,255,.32), transparent 28%), linear-gradient(135deg, #070d18 0%, #111936 55%, #050914 100%); }
.hero-panel::before { position: absolute; inset: 0; background-image: radial-gradient(rgba(185,201,255,.14) 1px, transparent 1px); background-size: 28px 28px; content: ''; opacity: .45; }
.hero-content { position: relative; z-index: 2; display: flex; min-height: calc(100vh - 78px); width: min(760px, 78%); margin: 0 auto; flex-direction: column; justify-content: center; padding: 48px 0; text-align: center; }
.hero-globe { position: absolute; inset: 2% 0 0; z-index: 1; opacity: .52; }
.eyebrow { width: fit-content; margin: 0 auto 24px; padding: 8px 17px; border: 1px solid rgba(176,189,255,.25); border-radius: 999px; background: rgba(114,126,255,.16); color: #c9d3ff; font-size: 12px; font-weight: 900; letter-spacing: 1.8px; }
.hero-content h1 { margin: 0; background: linear-gradient(135deg, #ffffff 0%, #d6ddff 38%, #c283ff 72%, #ff67d8 100%); color: transparent; font-size: clamp(54px, 6.4vw, 86px); line-height: .98; letter-spacing: -4px; text-shadow: 0 18px 50px rgba(114,89,255,.18); -webkit-background-clip: text; background-clip: text; }
.hero-content h1 span { display: inline-block; }
.hero-line { width: 130px; height: 5px; margin: 24px auto 22px; border-radius: 999px; background: linear-gradient(90deg, #4b88ff, #f04ee8); box-shadow: 0 0 30px rgba(129,101,255,.6); }
.hero-content > p { margin: 0 auto; color: rgba(221,228,255,.82); font-size: 17px; font-weight: 650; line-height: 1.85; }
.stats-grid { display: grid; gap: 32px; margin: 34px auto 26px; grid-template-columns: repeat(3, minmax(120px, 1fr)); }
.stat-item strong { display: block; color: #fff; font-size: clamp(29px, 3.1vw, 40px); font-weight: 900; letter-spacing: -.8px; }
.stat-item span { display: block; margin-top: 6px; color: #8792ad; font-size: 12px; font-weight: 800; letter-spacing: .8px; }
.feature-card { position: relative; display: grid; width: min(760px, 100%); min-height: 118px; align-items: center; margin: 0 auto; padding: 22px 34px; overflow: hidden; border: 1px solid rgba(210,220,255,.1); border-radius: 22px; background: rgba(8,13,33,.78); box-shadow: 0 20px 60px rgba(0,0,0,.24); backdrop-filter: blur(12px); grid-template-columns: 88px 1fr; gap: 18px; text-align: left; }
.feature-card::before { position: absolute; inset: -1px; border-radius: inherit; background: linear-gradient(90deg, rgba(55,133,255,.9), rgba(233,70,219,.9)); content: ''; opacity: .2; pointer-events: none; }
.feature-icon { display: grid; width: 62px; height: 62px; place-items: center; border-radius: 17px; background: linear-gradient(135deg, #7f5cff, #eb49d5); color: #fff; font-size: 30px; box-shadow: 0 12px 30px rgba(169,77,247,.32); }
.tone-blue .feature-icon { background: linear-gradient(135deg, #4f7cff, #21c4ff); }
.tone-cyan .feature-icon { background: linear-gradient(135deg, #21c4ff, #43e6ad); }
.feature-card h2 { margin: 0 0 8px; color: #fff; font-size: 24px; }
.feature-card p { margin: 0; color: #98a5c1; font-size: 13px; font-weight: 650; line-height: 1.7; }
.feature-progress { position: absolute; right: 30%; bottom: 0; left: 0; height: 3px; background: linear-gradient(90deg, #3d85ff, #ec4eda); animation: progress-run 3.6s linear infinite; transform-origin: left; }
.feature-dots { display: flex; justify-content: center; gap: 8px; margin-top: 14px; }
.feature-dots button { width: 8px; height: 8px; padding: 0; border: 0; border-radius: 999px; background: rgba(158,169,200,.34); cursor: pointer; }
.feature-dots button.active { width: 27px; background: #6d7bff; }
.entry-panel { position: relative; display: grid; min-height: calc(100vh - 78px); place-items: center; overflow: hidden; padding: 52px 54px; background: radial-gradient(circle at 86% 18%, rgba(125,160,255,.2), transparent 22%), radial-gradient(circle at 10% 72%, rgba(85,211,216,.12), transparent 22%), #fbfdff; }
.theme-dark .entry-panel { background: radial-gradient(circle at 86% 18%, rgba(125,160,255,.16), transparent 22%), #0f1727; }
.entry-panel::before { position: absolute; width: 560px; height: 560px; border: 1px solid rgba(129,153,220,.16); border-radius: 50%; content: ''; transform: translate(30%, -12%); }
.entry-card { position: relative; z-index: 2; width: min(430px, 100%); padding: 43px 38px; border: 1px solid rgba(255,255,255,.78); border-radius: 28px; background: rgba(255,255,255,.82); box-shadow: 0 34px 80px rgba(52,75,122,.14); backdrop-filter: blur(22px); }
.theme-dark .entry-card { border-color: rgba(255,255,255,.1); background: rgba(17,24,39,.78); box-shadow: 0 34px 80px rgba(0,0,0,.32); }
.entry-logo { display: grid; width: 58px; height: 58px; place-items: center; overflow: hidden; border-radius: 17px; background: #fff; box-shadow: 0 14px 28px rgba(78,103,238,.18); }
.entry-logo img { width: 100%; height: 100%; object-fit: cover; }
.entry-kicker { margin: 24px 0 10px; color: #6d78ff; font-size: 12px; font-weight: 900; letter-spacing: 1.5px; text-transform: uppercase; }
.entry-card h2 { margin: 0; color: #111827; font-size: 30px; letter-spacing: -.7px; }
.theme-dark .entry-card h2 { color: #fff; }
.entry-text { margin: 14px 0 26px; color: #7e8aa0; font-size: 14px; line-height: 1.85; }
.primary-entry, .secondary-entry { width: 100%; height: 48px; border-radius: 14px; cursor: pointer; font-size: 14px; font-weight: 850; }
.primary-entry { border: 0; background: linear-gradient(135deg, #5965ef, #338dff); color: #fff; box-shadow: 0 16px 32px rgba(67,102,241,.22); }
.secondary-entry { margin-top: 12px; border: 1px solid #dfe6ff; background: rgba(255,255,255,.58); color: #5360df; }
.theme-dark .secondary-entry { border-color: rgba(255,255,255,.1); background: rgba(255,255,255,.05); color: #bcc7ff; }
.entry-note { display: grid; gap: 6px; margin-top: 22px; padding: 13px 15px; border-radius: 14px; background: #f5f7ff; color: #8a95aa; font-size: 12px; }
.theme-dark .entry-note { background: rgba(255,255,255,.06); color: #9ca7bd; }
.entry-note strong { color: #4d5edc; font-size: 13px; }
.contact-dialog { display: grid; gap: 14px; color: #69758b; }
.contact-dialog p { margin: 0 0 4px; line-height: 1.7; }
.contact-dialog div { display: flex; justify-content: space-between; gap: 18px; padding: 13px 15px; border-radius: 12px; background: #f6f8fc; }
.contact-dialog strong { color: #1f2937; }
.contact-dialog span { color: #2f80ff; font-weight: 750; }
@keyframes progress-run { from { transform: scaleX(0); } to { transform: scaleX(1); } }
@media (prefers-reduced-motion: reduce) { .feature-progress { animation: none; transform: none; } }
@media (max-width: 1180px) { .landing-header { grid-template-columns: 1fr auto; } .nav-links { display: none; } .landing-main { grid-template-columns: 1fr; } .hero-panel, .entry-panel, .hero-content { min-height: auto; } .hero-content { padding: 80px 0 48px; } .entry-panel { padding: 46px 24px 70px; } }
@media (max-width: 680px) { .landing-header { height: auto; min-height: 72px; padding: 12px 16px; } .brand small { display: none; } .brand img { width: 42px; height: 42px; } .header-actions { gap: 6px; } .language-action { padding: 0 6px; font-size: 12px; } .login-action { height: 42px; padding: 0 17px; border-radius: 14px; } .hero-content { width: calc(100% - 32px); } .hero-content h1 { font-size: 46px; letter-spacing: -2px; } .stats-grid { grid-template-columns: 1fr; gap: 14px; } .feature-card { grid-template-columns: 1fr; text-align: center; } .feature-icon { margin: 0 auto; } .entry-card { padding: 32px 24px; } }
</style>
