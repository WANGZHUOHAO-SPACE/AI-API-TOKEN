<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { House, Lock, Moon, Sunny, User } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useLanguage } from '@/i18n'
import RotatingGlobe from '@/components/RotatingGlobe.vue'
import { systemApi } from '@/api'
import RoleAvatar from '@/components/RoleAvatar.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { locale: language, toggleLocale: toggleLanguage } = useLanguage()

type ThemeMode = 'light' | 'dark'
const savedTheme = localStorage.getItem('keybridge_theme') as ThemeMode | null
const themeMode = ref<ThemeMode>(savedTheme === 'dark' ? 'dark' : 'light')
const isDark = computed(() => themeMode.value === 'dark')
const formRef = ref<FormInstance>()
const loading = ref(false)
const quickLoginRole = ref<'user' | 'admin'>()
const agreement = ref(false)
const agreementDialogVisible = ref(false)
const forgotDialogVisible = ref(false)
const forgotFormRef = ref<FormInstance>()
const submittingReset = ref(false)
const form = reactive({ username: '', password: '' })
const resetForm = reactive({
  account: '',
  contact: '',
  reason: '无法登录控制台账号，请协助重置密码。',
})

const agreementSections = [
  {
    title: '1. 服务说明',
    paragraphs: [
      'KeyBridge AI 是一个面向学习、课程演示和开发测试的 AI API 密钥管理与统一转发平台。平台提供 API Key 加密保存、统一转发、调用日志、限流、余额展示和模型广场等功能。',
      '您在使用本平台前，应当仔细阅读并理解本协议全部内容。点击“同意并继续”或继续使用本服务，即表示您已充分阅读、理解并接受本协议。',
    ],
  },
  {
    title: '2. 账号与安全',
    paragraphs: [
      '您应妥善保管账号、密码、API Key、访问令牌等敏感信息，不得将账号借用、出租、出售或转让给他人使用。',
      '因您主动泄露、保管不当、在非可信环境登录等原因造成的损失，由您自行承担。平台会尽力通过 AES 加密、JWT 鉴权等方式保护数据安全。',
    ],
  },
  {
    title: '3. API Key 与调用规范',
    paragraphs: [
      '您上传或保存的第三方 API Key 仅用于您主动发起的模型调用和课程演示场景。平台页面不会展示完整密钥，仅展示脱敏后的密钥信息。',
      '您不得利用本平台从事违法违规、侵害他人权益、绕过第三方服务限制、攻击网络系统、批量滥用接口或生成不当内容等行为。',
    ],
  },
  {
    title: '4. 费用、余额与限流',
    paragraphs: [
      '平台可能展示账户余额、充值记录、参考汇率和模型价格。演示环境中的余额、价格和汇率可用于课程展示，实际结算以管理员确认记录为准。',
      '为保障系统稳定，平台可设置每日用户调用次数、单 Key 每分钟调用次数等限流规则。当达到限制时，请稍后再试或联系管理员处理。',
    ],
  },
  {
    title: '5. 日志与数据',
    paragraphs: [
      '为便于排查问题、统计用量和完成课程答辩展示，平台会记录调用用户、供应商、模型、耗时、状态、错误信息等必要日志。',
      '平台不会主动公开您的完整 API Key。管理员可基于系统管理需要查看用户、脱敏密钥、调用日志和统计数据。',
    ],
  },
  {
    title: '6. 服务变更与中断',
    paragraphs: [
      '由于网络故障、上游模型服务异常、数据库或 Redis 不可用、系统升级、配置错误、不可抗力等原因，服务可能出现中断、延迟或失败。',
      '本平台作为课程项目和统一转发系统，会尽力保持稳定，但不保证任何模型或第三方接口始终可用。',
    ],
  },
  {
    title: '7. 免责声明',
    paragraphs: [
      'AI 模型输出内容由模型服务生成，可能存在不准确、不完整或不适当的情况。您应自行判断输出内容的真实性、合法性和适用性。',
      '因您违反法律法规、本协议或第三方平台规则导致的任何责任，由您自行承担。',
    ],
  },
  {
    title: '8. 联系方式',
    paragraphs: [
      '如果您对本协议或平台功能有任何疑问，可通过“联系我们”页面联系管理员。',
      '邮箱：19834339457@163.com；手机号：19834339457；在线时间：周一至周日 10:00-21:00。',
    ],
  },
]

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const resetRules: FormRules = {
  account: [{ required: true, message: '请输入需要找回的账号', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入邮箱或手机号，方便管理员联系你', trigger: 'blur' }],
}

function toggleTheme() {
  themeMode.value = themeMode.value === 'dark' ? 'light' : 'dark'
  localStorage.setItem('keybridge_theme', themeMode.value)
}

function openUserAgreement() {
  agreementDialogVisible.value = true
}

function acceptUserAgreement() {
  agreement.value = true
  agreementDialogVisible.value = false
  ElMessage.success(language.value === 'zh' ? '已同意用户协议' : 'User agreement accepted')
}

function forgotPassword() {
  resetForm.account = form.username.trim()
  resetForm.contact = ''
  resetForm.reason = '无法登录控制台账号，请协助重置密码。'
  forgotDialogVisible.value = true
}

async function submitResetRequest() {
  await forgotFormRef.value?.validate()
  submittingReset.value = true
  try {
    forgotDialogVisible.value = false
    ElMessage.success(language.value === 'zh' ? '找回申请已提交，请等待管理员核验' : 'Recovery request submitted')
  } finally {
    submittingReset.value = false
  }
}

async function login() {
  if (!agreement.value) {
    ElMessage.warning(language.value === 'zh' ? '请先阅读并同意用户协议' : 'Please agree to the user agreement first')
    return
  }
  loading.value = true
  try {
    await auth.login(form.username.trim(), form.password)
    ElMessage.success(language.value === 'zh' ? '登录成功' : 'Signed in successfully')
    router.push(String(route.query.redirect || '/dashboard'))
  } finally {
    loading.value = false
    quickLoginRole.value = undefined
  }
}

async function submit() {
  await formRef.value?.validate()
  await login()
}

async function quickLogin(role: 'user' | 'admin') {
  quickLoginRole.value = role
  loading.value = true
  try {
    const health = await systemApi.health()
    if (health.status !== 'UP') {
      ElMessage.error(`服务未就绪：MySQL ${health.database}，Redis ${health.redis}`)
      return
    }
    if (!health.demoDataEnabled) {
      ElMessage.warning('后端未启用演示数据，请使用 DEMO_DATA_ENABLED=true 重启后端')
      return
    }
  } catch {
    ElMessage.error('后端未就绪，请先启动 MySQL、Redis 和 Spring Boot')
    return
  } finally {
    loading.value = false
    quickLoginRole.value = undefined
  }

  quickLoginRole.value = role
  agreement.value = true
  form.username = role === 'admin' ? 'admin' : 'demo_user'
  form.password = role === 'admin' ? 'admin123456' : 'demo123456'
  formRef.value?.clearValidate()
  await login()
}
</script>

<template>
  <div class="console-login" :class="{ 'theme-dark': isDark }">
    <div class="login-actions">
      <button type="button" title="首页" @click="router.push('/login')"><el-icon><House /></el-icon></button>
      <button type="button" :title="isDark ? '浅色' : '深色'" @click="toggleTheme">
        <el-icon><Sunny v-if="!isDark" /><Moon v-else /></el-icon>
      </button>
      <button class="language-action" type="button" @click="toggleLanguage">{{ language === 'zh' ? '中 / EN' : 'EN / 中' }}</button>
    </div>

    <section class="login-hero">
      <RotatingGlobe class="hero-globe" />
      <div class="hero-copy">
        <div class="hero-brand">
          <img src="/keybridge-logo.png" alt="KeyBridge AI Logo" />
          <span>
            <strong>KeyBridge AI</strong>
            <small>Console Gateway</small>
          </span>
        </div>
        <p class="hero-kicker">统一 AI API 控制台</p>
        <h1>一处管理密钥，统一转发模型调用。</h1>
        <h2>安全保存 Key，稳定接入 OpenAI Compatible 与多家模型服务。</h2>
        <div class="hero-tags">
          <span>安全 AES 加密</span>
          <span>统一转发链路</span>
          <span>多模型接入</span>
        </div>
        <div class="hero-metrics">
          <div><strong>100</strong><span>每日用户限额</span></div>
          <div><strong>10/min</strong><span>单 Key 限速</span></div>
          <div><strong>Mock</strong><span>演示模式</span></div>
        </div>
      </div>
      <div class="security-card">
        <el-icon><Lock /></el-icon>
        <div>
          <strong>企业级安全</strong>
          <span>AES 加密存储 · JWT 身份鉴权 · 调用日志追踪</span>
        </div>
      </div>
    </section>

    <section class="login-card-wrap">
      <div class="login-card">
        <div class="login-card-header">
          <div>
            <span class="login-badge">Console Login</span>
            <h2>欢迎回来</h2>
            <p>登录后进入 KeyBridge AI 控制台</p>
          </div>
        </div>
        <div class="login-tab">密码登录</div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @keyup.enter="submit">
          <el-form-item label="用户名或邮箱" prop="username">
            <el-input v-model="form.username" placeholder="请输入您的用户名或邮箱地址" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入您的密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          <div class="login-options">
            <el-checkbox v-model="agreement">
              我已阅读并同意
              <button class="text-link" type="button" @click.stop="openUserAgreement">《用户协议》</button>
            </el-checkbox>
            <button class="text-link" type="button" @click="forgotPassword">忘记密码?</button>
          </div>
          <el-button class="submit-button" type="primary" :loading="loading" @click="submit">登录</el-button>
        </el-form>

        <div class="register-line">没有账户？<router-link to="/register">注册</router-link></div>

        <div class="quick-divider"><span>演示账号快捷登录</span></div>
        <div class="quick-login-grid">
          <button type="button" :disabled="loading" @click="quickLogin('user')">
            <RoleAvatar role="USER" :size="42" label="普通用户头像" />
            <span class="quick-role-copy">
              <strong>{{ quickLoginRole === 'user' ? '正在登录...' : '普通用户' }}</strong>
              <small>个人密钥与调用</small>
            </span>
          </button>
          <button type="button" :disabled="loading" @click="quickLogin('admin')">
            <RoleAvatar role="ADMIN" :size="42" label="管理员头像" />
            <span class="quick-role-copy">
              <strong>{{ quickLoginRole === 'admin' ? '正在登录...' : '管理员' }}</strong>
              <small>全站管理与审核</small>
            </span>
          </button>
        </div>
      </div>
    </section>

    <el-dialog
      v-model="agreementDialogVisible"
      class="agreement-dialog"
      width="760px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="agreement-header">
          <div>
            <span>KeyBridge AI</span>
            <h2>用户协议</h2>
          </div>
        </div>
      </template>

      <div class="agreement-body">
        <div class="agreement-notice">
          <strong>请在使用平台前认真阅读以下条款。</strong>
          <span>点击底部“同意并继续”后，系统会自动勾选登录页的用户协议选项。</span>
        </div>
        <section v-for="section in agreementSections" :key="section.title" class="agreement-section">
          <h3>{{ section.title }}</h3>
          <p v-for="paragraph in section.paragraphs" :key="paragraph">{{ paragraph }}</p>
        </section>
      </div>

      <template #footer>
        <div class="agreement-footer">
          <el-button @click="agreementDialogVisible = false">暂不同意</el-button>
          <el-button type="primary" class="agree-button" @click="acceptUserAgreement">同意并继续</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="forgotDialogVisible"
      class="forgot-dialog"
      width="600px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="forgot-header">
          <span>Account Recovery</span>
          <h2>找回密码</h2>
          <p>提交账号信息后，管理员会根据联系方式协助你重置密码。</p>
        </div>
      </template>

      <div class="forgot-body">
        <div class="recovery-flow">
          <div><strong>1</strong><span>填写账号</span></div>
          <i></i>
          <div><strong>2</strong><span>管理员核验</span></div>
          <i></i>
          <div><strong>3</strong><span>重置密码</span></div>
        </div>

        <el-form ref="forgotFormRef" :model="resetForm" :rules="resetRules" label-position="top" class="forgot-form">
          <el-form-item label="需要找回的账号或邮箱" prop="account">
            <el-input v-model="resetForm.account" placeholder="请输入用户名或邮箱" />
          </el-form-item>
          <el-form-item label="联系方式" prop="contact">
            <el-input v-model="resetForm.contact" placeholder="请输入手机号或邮箱，便于管理员联系" />
          </el-form-item>
          <el-form-item label="问题说明">
            <el-input v-model="resetForm.reason" type="textarea" :rows="3" maxlength="120" show-word-limit />
          </el-form-item>
        </el-form>

        <div class="admin-contact">
          <strong>管理员联系方式</strong>
          <span>邮箱：19834339457@163.com</span>
          <span>手机：19834339457</span>
          <span>在线时间：周一至周日 10:00-21:00</span>
        </div>
      </div>

      <template #footer>
        <div class="forgot-footer">
          <el-button @click="forgotDialogVisible = false">取消</el-button>
          <el-button type="primary" class="recovery-button" :loading="submittingReset" @click="submitResetRequest">提交找回申请</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.console-login {
  position: relative;
  display: grid;
  min-height: 100vh;
  overflow: hidden;
  padding: 86px 88px 64px;
  background:
    radial-gradient(circle at 16% 14%, rgba(68, 146, 255, .16), transparent 28%),
    radial-gradient(circle at 88% 18%, rgba(194, 103, 255, .18), transparent 24%),
    linear-gradient(135deg, #f8fbff 0%, #eef5ff 52%, #fbf8ff 100%);
  color: #101827;
  grid-template-columns: minmax(650px, 1fr) 500px;
  gap: 56px;
}
.console-login::before {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(69, 120, 255, .12) 1px, transparent 1px);
  background-size: 25px 25px;
  content: '';
  opacity: .42;
  pointer-events: none;
}
.console-login::after {
  position: absolute;
  top: 18%;
  right: 10%;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  background: rgba(94, 113, 255, .12);
  content: '';
  filter: blur(58px);
  pointer-events: none;
}
.theme-dark {
  background:
    radial-gradient(circle at 18% 18%, rgba(59, 130, 246, .24), transparent 30%),
    radial-gradient(circle at 84% 16%, rgba(168, 85, 247, .2), transparent 28%),
    linear-gradient(135deg, #060b16 0%, #101827 54%, #151124 100%);
  color: #f8fbff;
}
.login-actions {
  position: fixed;
  top: 24px;
  right: 28px;
  z-index: 5;
  display: flex;
  align-items: center;
  gap: 10px;
}
.login-actions button {
  display: grid;
  height: 42px;
  min-width: 42px;
  place-items: center;
  padding: 0 14px;
  border: 1px solid rgba(255, 255, 255, .72);
  border-radius: 999px;
  background: rgba(255, 255, 255, .8);
  color: #2f80ff;
  cursor: pointer;
  box-shadow: 0 12px 26px rgba(64, 88, 142, .12);
  backdrop-filter: blur(14px);
  transition: transform .18s ease, box-shadow .18s ease;
}
.login-actions button:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 30px rgba(64, 88, 142, .17);
}
.theme-dark .login-actions button {
  border-color: rgba(255, 255, 255, .1);
  background: rgba(17, 24, 39, .72);
  color: #bac6ff;
}
.language-action {
  font-size: 12px;
  font-weight: 850;
}
.login-hero {
  position: relative;
  z-index: 1;
  min-height: 660px;
  overflow: hidden;
  padding: 48px 54px;
  border: 1px solid rgba(255, 255, 255, .14);
  border-radius: 36px;
  background:
    linear-gradient(135deg, rgba(12, 20, 39, .94), rgba(17, 28, 56, .88)),
    radial-gradient(circle at 80% 20%, rgba(92, 91, 255, .45), transparent 35%);
  box-shadow: 0 34px 90px rgba(26, 46, 94, .18);
}
.theme-dark .login-hero {
  border-color: rgba(255, 255, 255, .08);
  box-shadow: 0 34px 90px rgba(0, 0, 0, .32);
}
.login-hero::before {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, .045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, .045) 1px, transparent 1px);
  background-size: 56px 56px;
  content: '';
  mask-image: linear-gradient(120deg, #000 0%, transparent 74%);
  pointer-events: none;
}
.hero-copy {
  position: relative;
  z-index: 2;
  display: flex;
  max-width: 560px;
  min-height: 100%;
  flex-direction: column;
}
.hero-brand {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  gap: 12px;
  margin-bottom: 58px;
  padding: 9px 15px 9px 10px;
  border: 1px solid rgba(255, 255, 255, .12);
  border-radius: 999px;
  background: rgba(255, 255, 255, .09);
  color: #eaf0ff;
  box-shadow: 0 18px 46px rgba(0, 0, 0, .14);
  backdrop-filter: blur(12px);
}
.hero-brand img {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  object-fit: cover;
  background: #fff;
}
.hero-brand span {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.hero-brand strong {
  font-size: 14px;
  font-weight: 900;
}
.hero-brand small {
  color: #8fa2c7;
  font-size: 10px;
  letter-spacing: .8px;
  text-transform: uppercase;
}
.hero-kicker {
  margin: 0 0 12px;
  color: #80a7ff;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 2px;
  text-transform: uppercase;
}
.hero-copy h1 {
  margin: 0;
  color: #fff;
  font-size: clamp(44px, 4.6vw, 66px);
  font-weight: 950;
  line-height: 1.06;
  letter-spacing: -2.7px;
}
.hero-copy h1::after {
  display: block;
  width: 104px;
  height: 5px;
  margin-top: 24px;
  border-radius: 999px;
  background: linear-gradient(90deg, #4e8cff, #ed52d7);
  box-shadow: 0 0 28px rgba(126, 98, 255, .6);
  content: '';
}
.hero-copy h2 {
  max-width: 480px;
  margin: 24px 0 0;
  color: rgba(227, 233, 255, .74);
  font-size: 17px;
  font-weight: 650;
  line-height: 1.8;
}
.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 28px;
}
.hero-tags span {
  padding: 9px 13px;
  border: 1px solid rgba(255, 255, 255, .13);
  border-radius: 999px;
  background: rgba(255, 255, 255, .1);
  color: #dce7ff;
  font-size: 12px;
  font-weight: 800;
  backdrop-filter: blur(10px);
}
.hero-metrics {
  display: grid;
  max-width: 520px;
  margin-top: auto;
  padding-top: 56px;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.hero-metrics div {
  padding: 16px 14px;
  border: 1px solid rgba(255, 255, 255, .1);
  border-radius: 18px;
  background: rgba(255, 255, 255, .08);
  backdrop-filter: blur(12px);
}
.hero-metrics strong {
  display: block;
  color: #fff;
  font-size: 22px;
  font-weight: 950;
  letter-spacing: -.5px;
}
.hero-metrics span {
  display: block;
  margin-top: 5px;
  color: #8fa0bd;
  font-size: 11px;
  font-weight: 750;
}
.hero-globe {
  position: absolute;
  right: -210px;
  bottom: -180px;
  z-index: 1;
  width: 760px;
  height: 760px;
  opacity: .46;
  mix-blend-mode: screen;
  filter: drop-shadow(0 28px 80px rgba(76, 113, 255, .28));
}
.security-card {
  position: absolute;
  right: 34px;
  bottom: 34px;
  z-index: 3;
  display: flex;
  width: 330px;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, .12);
  border-radius: 20px;
  background: rgba(4, 10, 24, .58);
  color: #fff;
  box-shadow: 0 20px 50px rgba(0, 0, 0, .18);
  backdrop-filter: blur(16px);
}
.security-card .el-icon {
  display: grid;
  width: 46px;
  height: 46px;
  flex: 0 0 46px;
  place-items: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #2c8dff, #1fc7e8);
  font-size: 23px;
}
.security-card strong {
  display: block;
  margin-bottom: 3px;
  font-size: 15px;
}
.security-card span {
  color: #93a3c1;
  font-size: 11px;
  line-height: 1.6;
}
.login-card-wrap {
  position: relative;
  z-index: 2;
  display: grid;
  min-height: 660px;
  align-items: center;
}
.login-card {
  position: relative;
  width: min(468px, 100%);
  margin: 0 auto;
  padding: 42px 40px 34px;
  border: 1px solid rgba(255, 255, 255, .82);
  border-radius: 32px;
  background: linear-gradient(160deg, rgba(255, 255, 255, .94), rgba(247, 250, 255, .82));
  box-shadow: 0 34px 90px rgba(65, 78, 113, .16);
  backdrop-filter: blur(24px);
}
.login-card::before {
  position: absolute;
  inset: -1px;
  z-index: -1;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(75, 135, 255, .32), rgba(242, 91, 218, .18));
  content: '';
}
.theme-dark .login-card {
  border-color: rgba(255, 255, 255, .1);
  background: linear-gradient(160deg, rgba(16, 24, 39, .9), rgba(28, 24, 52, .78));
  box-shadow: 0 32px 90px rgba(0, 0, 0, .35);
}
.login-card-header {
  display: flex;
  justify-content: center;
  margin-bottom: 26px;
  text-align: center;
}
.login-badge {
  display: inline-flex;
  margin-bottom: 12px;
  padding: 7px 12px;
  border-radius: 999px;
  background: #eef3ff;
  color: #4e65f2;
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 1px;
  text-transform: uppercase;
}
.theme-dark .login-badge {
  background: rgba(255, 255, 255, .08);
  color: #bdc7ff;
}
.login-card h2 {
  margin: 0;
  color: #111827;
  font-size: 30px;
  font-weight: 950;
  letter-spacing: -.8px;
}
.theme-dark .login-card h2 {
  color: #fff;
}
.login-card-header p {
  margin: 8px 0 0;
  color: #7b879b;
  font-size: 14px;
}
.login-tab {
  width: fit-content;
  margin-bottom: 22px;
  padding-bottom: 11px;
  border-bottom: 3px solid #2d87ff;
  color: #162033;
  font-size: 14px;
  font-weight: 850;
}
.theme-dark .login-tab {
  color: #f4f7ff;
}
.login-card :deep(.el-form-item) {
  margin-bottom: 21px;
}
.login-card :deep(.el-form-item__label) {
  color: #536074;
  font-weight: 800;
}
.theme-dark .login-card :deep(.el-form-item__label) {
  color: #d6def2;
}
.login-card :deep(.el-input__wrapper) {
  min-height: 54px;
  border-radius: 16px;
  background: rgba(255, 255, 255, .84);
  box-shadow: 0 0 0 1px #e4ebf7 inset;
}
.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #6da6ff inset, 0 12px 30px rgba(79, 133, 255, .13);
}
.theme-dark .login-card :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, .08);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, .1) inset;
}
.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: -2px 0 28px;
  color: #8a95a8;
  font-size: 13px;
}
.login-options :deep(.el-checkbox__label) {
  color: #8a95a8;
  font-size: 13px;
}
.text-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: #2488ff;
  cursor: pointer;
  font-weight: 800;
}
.submit-button {
  width: 100%;
  height: 54px;
  border: 0;
  border-radius: 17px;
  background: linear-gradient(100deg, #238cff, #3caeff 52%, #5577ff);
  font-size: 16px;
  font-weight: 900;
  box-shadow: 0 18px 34px rgba(37, 138, 255, .25);
}
.register-line {
  margin-top: 22px;
  color: #9aa3b5;
  text-align: center;
  font-size: 14px;
}
.register-line a {
  color: #2488ff;
  font-weight: 850;
  text-decoration: none;
}
.quick-divider {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 29px 0 15px;
  color: #a0a8b8;
  font-size: 12px;
}
.quick-divider::before,
.quick-divider::after {
  flex: 1;
  height: 1px;
  background: #e5e9f1;
  content: '';
}
.theme-dark .quick-divider::before,
.theme-dark .quick-divider::after {
  background: rgba(255, 255, 255, .1);
}
.quick-login-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.quick-login-grid button {
  display: flex;
  min-height: 70px;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid #dfe6ff;
  border-radius: 16px;
  background: rgba(255, 255, 255, .72);
  color: #28324a;
  cursor: pointer;
  font-weight: 850;
  transition: transform .18s ease, border-color .18s ease, background .18s ease;
}
.quick-login-grid button:disabled { cursor: wait; opacity: .72; }
.quick-role-copy { display: flex; min-width: 0; flex-direction: column; align-items: flex-start; gap: 4px; text-align: left; }
.quick-role-copy strong { font-size: 14px; }.quick-role-copy small { overflow: hidden; color: #8b95aa; font-size: 10px; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }
.quick-login-grid button:hover {
  transform: translateY(-2px);
  border-color: #9bb8ff;
  background: #f4f7ff;
}
.theme-dark .quick-login-grid button {
  border-color: rgba(255, 255, 255, .11);
  background: rgba(255, 255, 255, .06);
  color: #f4f7ff;
}
.theme-dark .quick-role-copy small { color: #9ca8c2; }
:global(.agreement-dialog) {
  border-radius: 22px;
  overflow: hidden;
}
:global(.agreement-dialog .el-dialog__header) {
  padding: 24px 28px 14px;
  margin: 0;
  border-bottom: 1px solid #edf1f7;
}
:global(.agreement-dialog .el-dialog__body) {
  padding: 0;
}
:global(.agreement-dialog .el-dialog__footer) {
  padding: 16px 28px 22px;
  border-top: 1px solid #edf1f7;
  background: #fff;
}
.agreement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.agreement-header span {
  display: inline-flex;
  margin-bottom: 6px;
  padding: 5px 10px;
  border-radius: 999px;
  background: #eef3ff;
  color: #5360df;
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 1px;
  text-transform: uppercase;
}
.agreement-header h2 {
  margin: 0;
  color: #111827;
  font-size: 24px;
  font-weight: 950;
}
.agreement-body {
  max-height: min(64vh, 560px);
  overflow-y: auto;
  padding: 24px 30px 28px;
  color: #39465c;
  line-height: 1.85;
  scrollbar-color: #9aa6ba #eef2f8;
  scrollbar-width: thin;
}
.agreement-body::-webkit-scrollbar {
  width: 9px;
}
.agreement-body::-webkit-scrollbar-track {
  border-radius: 999px;
  background: #eef2f8;
}
.agreement-body::-webkit-scrollbar-thumb {
  border: 2px solid #eef2f8;
  border-radius: 999px;
  background: #9aa6ba;
}
.agreement-notice {
  display: grid;
  gap: 5px;
  margin-bottom: 18px;
  padding: 15px 17px;
  border: 1px solid #dce8ff;
  border-radius: 14px;
  background: linear-gradient(135deg, #f5f8ff, #f9fbff);
}
.agreement-notice strong {
  color: #1f2b44;
  font-size: 14px;
}
.agreement-notice span {
  color: #748099;
  font-size: 12px;
}
.agreement-section {
  padding: 16px 0;
  border-bottom: 1px solid #eef2f7;
}
.agreement-section:first-of-type {
  padding-top: 6px;
}
.agreement-section:last-child {
  border-bottom: 0;
}
.agreement-section h3 {
  margin: 0 0 8px;
  color: #172033;
  font-size: 16px;
  font-weight: 900;
}
.agreement-section p {
  margin: 8px 0 0;
  color: #4c5870;
  font-size: 14px;
}
.agreement-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}
.agree-button {
  min-width: 128px;
  border: 0;
  background: linear-gradient(100deg, #238cff, #5577ff);
  font-weight: 850;
}
:global(.forgot-dialog) {
  overflow: hidden;
  border-radius: 24px;
}
:global(.forgot-dialog .el-dialog__header) {
  padding: 28px 30px 16px;
  margin: 0;
  border-bottom: 1px solid #edf1f7;
}
:global(.forgot-dialog .el-dialog__body) {
  padding: 0;
}
:global(.forgot-dialog .el-dialog__footer) {
  padding: 16px 30px 24px;
  border-top: 1px solid #edf1f7;
  background: #fff;
}
.forgot-header span {
  display: inline-flex;
  margin-bottom: 10px;
  padding: 6px 11px;
  border-radius: 999px;
  background: #eef3ff;
  color: #5360df;
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 1px;
  text-transform: uppercase;
}
.forgot-header h2 {
  margin: 0;
  color: #111827;
  font-size: 25px;
  font-weight: 950;
}
.forgot-header p {
  margin: 8px 0 0;
  color: #7c879a;
  font-size: 13px;
}
.forgot-body {
  padding: 24px 30px 26px;
}
.recovery-flow {
  display: grid;
  align-items: center;
  margin-bottom: 22px;
  grid-template-columns: 1fr 28px 1fr 28px 1fr;
  gap: 10px;
}
.recovery-flow div {
  display: grid;
  min-height: 68px;
  place-items: center;
  padding: 10px;
  border: 1px solid #dde8ff;
  border-radius: 16px;
  background: linear-gradient(135deg, #f6f9ff, #fbfdff);
  text-align: center;
}
.recovery-flow strong {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  border-radius: 999px;
  background: linear-gradient(135deg, #238cff, #5577ff);
  color: #fff;
  font-size: 13px;
}
.recovery-flow span {
  margin-top: 6px;
  color: #47536a;
  font-size: 12px;
  font-weight: 800;
}
.recovery-flow i {
  height: 2px;
  border-radius: 999px;
  background: #cfe0ff;
}
.forgot-form :deep(.el-form-item__label) {
  color: #445067;
  font-weight: 800;
}
.forgot-form :deep(.el-input__wrapper),
.forgot-form :deep(.el-textarea__inner) {
  border-radius: 13px;
  box-shadow: 0 0 0 1px #e1e8f3 inset;
}
.forgot-form :deep(.el-input__wrapper) {
  min-height: 44px;
}
.admin-contact {
  display: grid;
  gap: 6px;
  margin-top: 5px;
  padding: 14px 16px;
  border: 1px solid #dce8ff;
  border-radius: 14px;
  background: #f6f9ff;
}
.admin-contact strong {
  color: #1f2b44;
  font-size: 13px;
}
.admin-contact span {
  color: #69758c;
  font-size: 12px;
}
.forgot-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.recovery-button {
  min-width: 136px;
  border: 0;
  background: linear-gradient(100deg, #238cff, #5577ff);
  font-weight: 850;
}
@media (max-width: 1280px) {
  .console-login {
    padding: 78px 36px 48px;
    grid-template-columns: 1fr;
  }
  .login-hero,
  .login-card-wrap {
    min-height: auto;
  }
  .login-card-wrap {
    align-items: start;
  }
  .hero-copy {
    min-height: 580px;
  }
}
@media (max-width: 720px) {
  .console-login {
    padding: 76px 16px 34px;
  }
  .login-actions {
    top: 14px;
    right: 14px;
  }
  .login-hero {
    padding: 32px 24px;
    border-radius: 28px;
  }
  .hero-brand {
    margin-bottom: 40px;
  }
  .hero-copy h1 {
    font-size: 38px;
  }
  .hero-copy h2 {
    font-size: 15px;
  }
  .hero-metrics {
    grid-template-columns: 1fr;
    padding-top: 34px;
  }
  .hero-globe {
    right: -310px;
    bottom: -260px;
    width: 680px;
    height: 680px;
  }
  .security-card {
    position: relative;
    right: auto;
    bottom: auto;
    width: auto;
    margin-top: 22px;
  }
  .login-card {
    padding: 34px 24px 28px;
    border-radius: 26px;
  }
  .login-options {
    align-items: flex-start;
    flex-direction: column;
  }
  .quick-login-grid {
    grid-template-columns: 1fr;
  }
  :global(.agreement-dialog) {
    width: calc(100vw - 28px) !important;
  }
  :global(.forgot-dialog) {
    width: calc(100vw - 28px) !important;
  }
  .agreement-body {
    max-height: 62vh;
    padding: 20px 18px 22px;
  }
  .forgot-body {
    padding: 20px 18px 22px;
  }
  .recovery-flow {
    grid-template-columns: 1fr;
  }
  .recovery-flow i {
    display: none;
  }
}
</style>
