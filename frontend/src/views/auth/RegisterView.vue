<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useLanguage } from '@/i18n'

const router = useRouter()
const auth = useAuthStore()
const { locale, toggleLocale } = useLanguage()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })
const reservedPrefixes = ['admin', 'administrator', 'root', 'system', 'support', 'operator', 'keybridge']
const weakFragments = ['password', 'qwerty', '123456', 'abcdef', 'admin', 'letmein', 'welcome']

const passwordChecks = computed(() => [
  { label: '长度达到 10-64 位', met: form.password.length >= 10 && form.password.length <= 64 },
  { label: '包含大写和小写字母', met: /[a-z]/.test(form.password) && /[A-Z]/.test(form.password) },
  { label: '至少包含一个数字', met: /\d/.test(form.password) },
  { label: '至少包含一个特殊字符', met: /[^A-Za-z0-9\s]/.test(form.password) },
  { label: '不含用户名和常见弱密码', met: isPasswordContentSafe() },
])
const passwordScore = computed(() => passwordChecks.value.filter(item => item.met).length)
const strengthLabel = computed(() => ['未设置', '很弱', '较弱', '一般', '较强', '很强'][passwordScore.value])
const strengthColor = computed(() => ['#d9dde5', '#f56c6c', '#e6a23c', '#d4a72c', '#67c23a', '#19a974'][passwordScore.value])

function isPasswordContentSafe() {
  const password = form.password.toLowerCase()
  const username = form.username.trim().toLowerCase()
  return Boolean(form.password)
    && (!username || !password.includes(username))
    && !weakFragments.some(fragment => password.includes(fragment))
    && !/(.)\1\1/.test(form.password)
    && !/\s/.test(form.password)
}

function validateUsername(_rule: unknown, value: string, callback: (error?: Error) => void) {
  const username = String(value || '').trim()
  if (!username) return callback(new Error('请输入登录账号'))
  if (!/^(?=[A-Za-z])(?=.*\d)[A-Za-z][A-Za-z0-9_]{5,19}$/.test(username)) {
    return callback(new Error('6-20 位，字母开头，至少包含一个数字'))
  }
  if (reservedPrefixes.some(prefix => username.toLowerCase().startsWith(prefix))) {
    return callback(new Error('不能使用系统保留名称作为账号开头'))
  }
  callback()
}

function validateNickname(_rule: unknown, value: string, callback: (error?: Error) => void) {
  const nickname = String(value || '').trim()
  if (!nickname) return callback(new Error('请输入昵称'))
  if (!/^(?!\d+$)[\p{L}\p{N}_-]{2,20}$/u.test(nickname)) {
    return callback(new Error('2-20 位中英文、数字、_ 或 -，不能为纯数字'))
  }
  callback()
}

function validatePassword(_rule: unknown, value: string, callback: (error?: Error) => void) {
  if (!value) return callback(new Error('请输入密码'))
  if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9\s])\S{10,64}$/.test(value)) {
    return callback(new Error('密码必须同时包含大小写字母、数字和特殊字符'))
  }
  if (!isPasswordContentSafe()) return callback(new Error('密码不能包含用户名、弱密码片段或三个连续相同字符'))
  callback()
}

const rules: FormRules = {
  username: [{ validator: validateUsername, trigger: ['blur', 'change'] }],
  nickname: [{ validator: validateNickname, trigger: ['blur', 'change'] }],
  password: [{ validator: validatePassword, trigger: ['blur', 'change'] }],
  confirmPassword: [{ validator: (_rule, value, callback) => value && value === form.password ? callback() : callback(new Error('两次密码不一致')), trigger: ['blur', 'change'] }],
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const username = form.username.trim()
    await authApi.register({ username, password: form.password, nickname: form.nickname.trim() })
    await auth.login(username, form.password)
    ElMessage.success('注册成功，已自动登录')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <button class="register-language" type="button" @click="toggleLocale">
      <span>文</span>{{ locale === 'zh' ? '中 / EN' : 'EN / 中' }}
    </button>
    <div class="register-card panel">
      <div class="logo"><img src="/keybridge-logo.png" alt="KeyBridge AI Logo" /></div>
      <h1>创建账号</h1>
      <p>创建安全账号，开始管理你的 AI API 密钥与调用记录</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="登录账号（用户名）" prop="username">
          <el-input v-model="form.username" maxlength="20" show-word-limit placeholder="例如 student2026" />
          <div class="field-help">用于登录且注册后不可随意修改，请牢记该账号。</div>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="20" show-word-limit placeholder="例如 软件工程小王" />
          <div class="field-help">仅用于控制台展示，可以使用中文，但不能填写纯数字。</div>
        </el-form-item>
        <el-form-item label="安全密码" prop="password">
          <el-input v-model="form.password" type="password" maxlength="64" show-password placeholder="请输入高强度密码" />
          <div class="strength-panel">
            <div class="strength-head"><span>密码强度</span><strong :style="{ color: strengthColor }">{{ strengthLabel }}</strong></div>
            <div class="strength-track"><i v-for="index in 5" :key="index" :class="{ active: passwordScore >= index }" :style="passwordScore >= index ? { background: strengthColor } : {}"></i></div>
            <div class="password-checks">
              <span v-for="item in passwordChecks" :key="item.label" :class="{ met: item.met }"><b>{{ item.met ? '✓' : '○' }}</b>{{ item.label }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" maxlength="64" show-password placeholder="请再次输入安全密码" /></el-form-item>
        <div class="security-tip"><strong>安全提示</strong><span>不要使用姓名、手机号、生日或与其他网站相同的密码。</span></div>
        <el-button class="register-button" type="primary" :loading="loading" @click="submit">注册账号</el-button>
      </el-form>
      <div class="back-login">已有账号？<router-link to="/console-login">返回登录</router-link></div>
    </div>
  </div>
</template>

<style scoped>
.register-page { display: grid; min-height: 100vh; place-items: center; padding: 36px 18px; background: radial-gradient(circle at top, #eef0ff, #f5f7fb 42%); }
.register-language { position: fixed; top: 22px; right: 25px; z-index: 3; display: flex; height: 39px; align-items: center; gap: 7px; padding: 0 12px; border: 1px solid #dce2ee; border-radius: 11px; background: rgba(255,255,255,.9); color: #5360df; font-size: 11px; font-weight: 750; cursor: pointer; box-shadow: 0 8px 24px rgba(43,55,91,.08); backdrop-filter: blur(10px); }
.register-language span { display: grid; width: 22px; height: 22px; place-items: center; border-radius: 7px; background: #eef0ff; }
.register-card { width: min(570px, 100%); padding: 34px 40px; }
.logo { display: grid; width: 54px; height: 54px; overflow: hidden; place-items: center; border: 1px solid #e2e7f1; border-radius: 14px; background: #fff; box-shadow: 0 9px 24px rgba(68,83,187,.15); }
.logo img { display: block; width: 100%; height: 100%; object-fit: cover; }
h1 { margin: 22px 0 8px; font-size: 28px; }
.register-card > p { margin: 0 0 25px; color: var(--muted); }
.field-help { width: 100%; margin-top: 6px; color: #929bad; font-size: 11px; line-height: 1.5; }
.strength-panel { width: 100%; margin-top: 9px; padding: 11px 13px; border-radius: 10px; background: #f7f8fc; }
.strength-head { display: flex; align-items: center; justify-content: space-between; color: #737d92; font-size: 11px; }.strength-head strong { font-size: 11px; }
.strength-track { display: grid; margin: 8px 0 10px; grid-template-columns: repeat(5,1fr); gap: 5px; }.strength-track i { height: 4px; border-radius: 999px; background: #dfe3eb; transition: background .2s ease; }
.password-checks { display: grid; grid-template-columns: 1fr 1fr; gap: 6px 10px; }.password-checks span { display: flex; align-items: center; gap: 5px; color: #8e97a8; font-size: 10px; }.password-checks span.met { color: #26a875; }.password-checks b { width: 12px; font-size: 11px; }
.security-tip { display: flex; gap: 9px; margin: -2px 0 18px; padding: 10px 12px; border: 1px solid #ffe4b5; border-radius: 9px; background: #fff9ed; color: #94713b; font-size: 10px; line-height: 1.5; }.security-tip strong { flex: 0 0 auto; }
.register-button { width: 100%; height: 45px; }
.back-login { margin-top: 20px; color: var(--muted); text-align: center; font-size: 14px; }
.back-login a { color: var(--brand); font-weight: 650; text-decoration: none; }
@media (max-width: 560px) { .register-card { padding: 28px 22px; }.password-checks { grid-template-columns: 1fr; } }
</style>
