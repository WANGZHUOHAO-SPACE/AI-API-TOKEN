<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Promotion, RefreshRight } from '@element-plus/icons-vue'
import { credentialApi, providerApi, proxyApi } from '@/api'
import type { ChatResult, Credential, Provider } from '@/types'

const loading = ref(false)
const route = useRoute()
const providers = ref<Provider[]>([])
const keys = ref<Credential[]>([])
const result = ref<ChatResult>()
const form = reactive({ provider: '', model: 'gpt-4.1-mini', keyId: undefined as number | undefined, prompt: '' })
const providerId = computed(() => providers.value.find(item => item.code === form.provider)?.id)
const availableKeys = computed(() => keys.value.filter(item => item.providerId === providerId.value && item.status === 1))

watch(() => form.provider, () => { form.keyId = availableKeys.value[0]?.id })
async function loadOptions() {
  const [providerPage, keyPage] = await Promise.all([providerApi.list(), credentialApi.list({ page: 1, size: 100 })])
  providers.value = providerPage.records.filter(item => item.status === 1)
  keys.value = keyPage.records
  const requestedProvider = String(route.query.provider || '')
  form.provider = providers.value.some(item => item.code === requestedProvider) ? requestedProvider : (providers.value[0]?.code || '')
  form.model = String(route.query.model || form.model)
  form.keyId = availableKeys.value[0]?.id
}
async function submit() {
  if (!form.provider || !form.model || !form.keyId || !form.prompt.trim()) return
  loading.value = true; result.value = undefined
  try { result.value = await proxyApi.chat({ provider: form.provider, model: form.model, keyId: form.keyId, prompt: form.prompt }) }
  finally { loading.value = false }
}
function reset() { form.prompt = ''; result.value = undefined }
function selectModel(value: string | number | object) { form.model = String(value) }
onMounted(loadOptions)
</script>

<template>
  <div class="page-shell">
    <div class="page-heading"><div><h1>AI 调用测试</h1><p>选择供应商和自己的 API Key，快速验证统一转发链路。</p></div><el-tag type="success" effect="light">支持 Mock / OpenAI Compatible</el-tag></div>
    <div class="playground-grid">
      <section class="panel config-panel">
        <div class="panel-header"><h2 class="panel-title">请求配置</h2><el-button text :icon="RefreshRight" @click="reset">清空</el-button></div>
        <div class="panel-body">
          <el-form label-position="top">
            <el-form-item label="供应商"><el-select v-model="form.provider" style="width:100%" placeholder="选择供应商"><el-option v-for="item in providers" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.code" /></el-select></el-form-item>
            <el-form-item label="模型"><el-input v-model="form.model" placeholder="例如 gpt-4.1-mini"><template #append><el-dropdown @command="selectModel"><span>常用模型</span><template #dropdown><el-dropdown-menu><el-dropdown-item command="gpt-4.1-mini">gpt-4.1-mini</el-dropdown-item><el-dropdown-item command="gpt-4o-mini">gpt-4o-mini</el-dropdown-item><el-dropdown-item command="deepseek-chat">deepseek-chat</el-dropdown-item></el-dropdown-menu></template></el-dropdown></template></el-input></el-form-item>
            <el-form-item label="API Key"><el-select v-model="form.keyId" style="width:100%" placeholder="选择可用 Key" :empty-values="[null, undefined]"><el-option v-for="item in availableKeys" :key="item.id" :label="`${item.name} · ${item.maskedKey}`" :value="item.id" /></el-select><div v-if="form.provider && !availableKeys.length" class="warning-tip">该供应商暂无可用 Key，请先前往 API Key 管理添加。</div></el-form-item>
            <el-form-item label="Prompt"><el-input v-model="form.prompt" type="textarea" :rows="9" maxlength="32000" show-word-limit placeholder="输入要发送给 AI 的问题..." /></el-form-item>
            <el-button class="send-button" type="primary" :icon="Promotion" :loading="loading" :disabled="!form.keyId || !form.prompt.trim()" @click="submit">发送请求</el-button>
          </el-form>
        </div>
      </section>
      <section class="panel response-panel">
        <div class="panel-header"><h2 class="panel-title">模型响应</h2><el-tag v-if="result" :type="result.mode === 'MOCK' ? 'warning' : 'success'" effect="light">{{ result.mode }}</el-tag></div>
        <div class="response-body">
          <div v-if="loading" class="response-empty"><div class="thinking"></div><strong>正在请求模型</strong><span>后端正在校验 Key、限流并转发请求...</span></div>
          <div v-else-if="result" class="response-content">
            <div class="answer" data-no-translate>{{ result.content }}</div>
            <div class="response-meta"><span>Request ID <code>{{ result.requestId }}</code></span><span>模型 <b>{{ result.model }}</b></span><span>耗时 <b>{{ result.durationMs }} ms</b></span><span>Token <b>{{ result.usage.totalTokens }}</b></span></div>
          </div>
          <div v-else class="response-empty"><div class="empty-icon">AI</div><strong>等待发送请求</strong><span>配置请求参数后，模型回答会显示在这里。</span></div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.playground-grid{display:grid;grid-template-columns:minmax(340px,.8fr) minmax(420px,1.2fr);gap:16px}.config-panel,.response-panel{min-height:610px}.send-button{width:100%;height:42px}.warning-tip{margin-top:6px;color:#d88a12;font-size:11px}.response-body{display:flex;min-height:540px;padding:20px}.response-empty{display:flex;flex:1;align-items:center;justify-content:center;flex-direction:column;color:var(--muted);text-align:center}.response-empty strong{margin:14px 0 7px;color:#3e475b}.response-empty span{font-size:13px}.empty-icon{display:grid;width:58px;height:58px;place-items:center;border-radius:18px;background:#eef0ff;color:var(--brand);font-size:18px;font-weight:800}.thinking{width:42px;height:42px;border:4px solid #e4e7ff;border-top-color:var(--brand);border-radius:50%;animation:spin .8s linear infinite}.response-content{display:flex;width:100%;flex-direction:column}.answer{flex:1;padding:20px;border-radius:12px;background:#f8f9fc;color:#283146;line-height:1.9;white-space:pre-wrap}.response-meta{display:flex;flex-wrap:wrap;gap:10px;margin-top:15px}.response-meta span{padding:7px 10px;border-radius:7px;background:#f3f5f8;color:#7a8396;font-size:11px}.response-meta b{color:#3b4458}.response-meta code{font-size:10px;color:#4e59d9}@keyframes spin{to{transform:rotate(360deg)}}@media(max-width:1000px){.playground-grid{grid-template-columns:1fr}.config-panel,.response-panel{min-height:auto}.response-body{min-height:350px}}
</style>
