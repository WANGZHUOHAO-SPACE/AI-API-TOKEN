<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { credentialApi, providerApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { Credential, CredentialPayload, Provider } from '@/types'

const auth = useAuthStore()
const loading = ref(false)
const providers = ref<Provider[]>([])
const keys = ref<Credential[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const providerFilter = ref<number>()
const dialogVisible = ref(false)
const editingId = ref<number>()
const formRef = ref<FormInstance>()
const form = reactive<CredentialPayload>({ providerId: 0, name: '', apiKey: '', priority: 0, weight: 1, status: 1, expiresAt: undefined })
const rules: FormRules = {
  providerId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  name: [{ required: true, message: '请输入 Key 名称', trigger: 'blur' }],
  apiKey: [{ validator: (_r, value, callback) => !editingId.value && !value ? callback(new Error('请输入 API Key')) : callback(), trigger: 'blur' }],
}
const providerMap = computed(() => new Map(providers.value.map(item => [item.id, item])))

async function loadProviders() { providers.value = (await providerApi.list()).records.filter(item => item.status === 1) }
async function loadKeys() {
  loading.value = true
  try {
    const result = await credentialApi.list({ page: page.value, size: size.value, providerId: providerFilter.value })
    keys.value = result.records; total.value = result.total
  } finally { loading.value = false }
}
function resetForm() { Object.assign(form, { providerId: providers.value[0]?.id || 0, name: '', apiKey: '', priority: 0, weight: 1, status: 1, expiresAt: undefined }) }
function openCreate() { editingId.value = undefined; resetForm(); dialogVisible.value = true }
function openEdit(value: unknown) {
  const row = value as Credential
  editingId.value = row.id
  Object.assign(form, { providerId: row.providerId, name: row.name, apiKey: '', priority: row.priority, weight: row.weight, status: row.status, expiresAt: row.expiresAt })
  dialogVisible.value = true
}
async function submit() {
  await formRef.value?.validate()
  const payload = { ...form }
  if (!payload.apiKey) delete payload.apiKey
  if (editingId.value) await credentialApi.update(editingId.value, payload)
  else await credentialApi.create(payload)
  ElMessage.success(editingId.value ? 'API Key 已更新' : 'API Key 已安全保存')
  dialogVisible.value = false
  loadKeys()
}
async function remove(value: unknown) {
  const row = value as Credential
  await ElMessageBox.confirm(`确定删除“${row.name}”吗？删除后无法恢复。`, '删除 API Key', { type: 'warning' })
  await credentialApi.remove(row.id)
  ElMessage.success('删除成功')
  loadKeys()
}
function statusType(status: number): 'success' | 'info' { return status === 1 ? 'success' : 'info' }
function filterByProvider() { page.value = 1; loadKeys() }
onMounted(async () => { await loadProviders(); await loadKeys() })
</script>

<template>
  <div class="page-shell">
    <div class="page-heading">
      <div><h1>API Key 管理</h1><p>密钥以 AES 加密保存，平台仅展示脱敏后的末四位。</p></div>
      <el-button type="primary" :icon="Plus" @click="openCreate">添加 API Key</el-button>
    </div>

    <div class="security-tip">
      <div class="shield">✓</div><div><strong>密钥已安全保护</strong><p>完整密钥仅在保存时传输，之后不会在任何页面或接口中返回。</p></div>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div class="filter-bar"><el-select v-model="providerFilter" clearable placeholder="全部供应商" style="width: 180px" @change="filterByProvider"><el-option v-for="item in providers" :key="item.id" :label="item.name" :value="item.id" /></el-select><el-button :icon="Search" @click="loadKeys">筛选</el-button></div>
        <el-button text :icon="Refresh" @click="loadKeys">刷新</el-button>
      </div>
      <div class="panel-body table-wrap">
        <el-table v-loading="loading" :data="keys" row-key="id">
          <el-table-column label="名称" min-width="150"><template #default="{ row }"><div class="key-name"><span class="key-icon">K</span><div><strong>{{ row.name }}</strong><small>#{{ row.id }}</small></div></div></template></el-table-column>
          <el-table-column v-if="auth.isAdmin" prop="ownerUsername" label="所属用户" width="130" />
          <el-table-column label="供应商" width="150"><template #default="{ row }">{{ providerMap.get(row.providerId)?.name || `#${row.providerId}` }}</template></el-table-column>
          <el-table-column label="密钥" min-width="170"><template #default="{ row }"><code>{{ row.maskedKey }}</code></template></el-table-column>
          <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="statusType(row.status)" effect="light">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template></el-table-column>
          <el-table-column prop="failureCount" label="失败次数" width="90" align="center" />
          <el-table-column label="创建时间" min-width="165"><template #default="{ row }">{{ row.createdAt || '-' }}</template></el-table-column>
          <el-table-column label="操作" width="130" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
        </el-table>
        <div class="pagination"><el-pagination v-model:current-page="page" v-model:page-size="size" layout="total, prev, pager, next" :total="total" @current-change="loadKeys" /></div>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑 API Key' : '添加 API Key'" width="min(520px, 92vw)" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="供应商" prop="providerId"><el-select v-model="form.providerId" style="width:100%"><el-option v-for="item in providers" :key="item.id" :label="`${item.name} (${item.code})`" :value="item.id" /></el-select></el-form-item>
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="例如：课程演示 OpenAI Key" maxlength="100" /></el-form-item>
        <el-form-item :label="editingId ? '替换密钥（留空则不修改）' : 'API Key'" prop="apiKey"><el-input v-model="form.apiKey" type="password" show-password autocomplete="new-password" placeholder="sk-..." /><div class="field-tip">密钥保存后仅显示末四位。</div></el-form-item>
        <div class="form-row"><el-form-item label="优先级"><el-input-number v-model="form.priority" :min="0" :max="100" /></el-form-item><el-form-item label="权重"><el-input-number v-model="form.weight" :min="1" :max="100" /></el-form-item><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" /></el-form-item></div>
        <el-form-item label="过期时间（可选）"><el-date-picker v-model="form.expiresAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="不设置则长期有效" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">{{ editingId ? '保存修改' : '加密保存' }}</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.security-tip { display: flex; align-items: center; gap: 13px; padding: 15px 18px; border: 1px solid #dcefe8; border-radius: 12px; background: #f0faf6; }.shield { display:grid;width:36px;height:36px;place-items:center;border-radius:10px;background:#22a977;color:#fff;font-weight:800}.security-tip strong{font-size:14px}.security-tip p{margin:4px 0 0;color:#678377;font-size:12px}.table-wrap{padding-top:14px}.key-name{display:flex;align-items:center;gap:10px}.key-name>div{display:flex;flex-direction:column}.key-name small{margin-top:3px;color:#9aa2b4}.key-icon{display:grid;width:32px;height:32px;place-items:center;border-radius:8px;background:#eef0ff;color:var(--brand);font-weight:800}.pagination{display:flex;justify-content:flex-end;margin-top:18px}.form-row{display:grid;grid-template-columns:1fr 1fr 1fr;gap:14px}.field-tip{margin-top:5px;color:#929aad;font-size:11px}code{padding:5px 8px;border-radius:6px;background:#f3f5f9;color:#525c71}@media(max-width:650px){.form-row{grid-template-columns:1fr}.table-wrap{overflow:auto}}
</style>
