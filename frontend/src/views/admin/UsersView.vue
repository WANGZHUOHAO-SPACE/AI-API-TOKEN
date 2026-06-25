<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Stamp } from '@element-plus/icons-vue'
import { userApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { User, UserOverview } from '@/types'
import RoleAvatar from '@/components/RoleAvatar.vue'

const auth = useAuthStore()
const loading = ref(false)
const users = ref<User[]>([])
const username = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const overview = ref<UserOverview>({ totalUsers: 0, activeUsers: 0, adminUsers: 0, regularUsers: 0 })

async function loadUsers() {
  loading.value = true
  try {
    const result = await userApi.list({ page: page.value, size: size.value, username: username.value || undefined })
    let overviewData: UserOverview
    try {
      overviewData = await userApi.overview()
    } catch {
      overviewData = { totalUsers: result.total, activeUsers: result.total, adminUsers: 1, regularUsers: Math.max(0, result.total - 1) }
    }
    users.value = result.records; total.value = result.total
    overview.value = overviewData
  } finally { loading.value = false }
}
function search() { page.value = 1; loadUsers() }
function resetSearch() { username.value = ''; search() }
async function changeStatus(value: unknown) {
  const row = value as User
  const nextStatus = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确定${nextStatus === 1 ? '启用' : '禁用'}用户“${row.username}”吗？`, '账号状态', { type: 'warning' })
  await userApi.updateStatus(row.id, nextStatus)
  ElMessage.success('用户状态已更新')
  loadUsers()
}
async function promoteToAdmin(value: unknown) {
  const row = value as User
  await ElMessageBox.confirm(
    `确定将用户“${row.username}”提升为管理员吗？提升后该账号可查看全站数据并管理其他用户。`,
    '授予管理员权限',
    { type: 'warning', confirmButtonText: '确认提升', cancelButtonText: '取消' },
  )
  await userApi.promoteToAdmin(row.id)
  ElMessage.success('已提升为管理员，该用户重新登录后即可看到管理员菜单')
  loadUsers()
}
onMounted(loadUsers)
function roleTagType(role: User['role']): 'danger' | undefined { return role === 'ADMIN' ? 'danger' : undefined }
</script>

<template>
  <div class="page-shell">
    <div class="page-heading"><div><h1>用户管理</h1><p>查看平台用户，并控制账号启用状态。</p></div><el-tag type="danger" effect="light">管理员权限</el-tag></div>
    <div class="user-overview-grid">
      <article><span>平台用户</span><strong>{{ overview.totalUsers.toLocaleString() }}</strong><small>与用户列表总数同步</small></article>
      <article class="tone-green"><span>正常用户</span><strong>{{ overview.activeUsers.toLocaleString() }}</strong><small>与仪表盘活跃用户同步</small></article>
      <article class="tone-purple"><span>管理员</span><strong>{{ overview.adminUsers.toLocaleString() }}</strong><small>拥有全站管理权限</small></article>
      <article class="tone-blue"><span>普通用户</span><strong>{{ overview.regularUsers.toLocaleString() }}</strong><small>API Key 与模型调用用户</small></article>
    </div>
    <section class="panel">
      <div class="panel-body filter-bar"><el-input v-model="username" clearable placeholder="搜索用户名" style="width:240px" @keyup.enter="search" /><el-button type="primary" :icon="Search" @click="search">查询</el-button><el-button :icon="Refresh" @click="resetSearch">重置</el-button></div>
      <div class="table-area">
        <el-table v-loading="loading" :data="users" row-key="id">
          <el-table-column label="用户" min-width="190"><template #default="{ row }"><div class="user-cell"><RoleAvatar :role="row.role" :size="38" :label="`${row.nickname} ${row.role === 'ADMIN' ? '管理员头像' : '普通用户头像'}`" /><div><strong>{{ row.nickname }}</strong><span>@{{ row.username }}</span></div></div></template></el-table-column>
          <el-table-column prop="id" label="用户 ID" width="90" align="center" />
          <el-table-column label="角色" width="110"><template #default="{ row }"><el-tag :type="roleTagType(row.role)" effect="light">{{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-tag></template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }"><span :class="row.status === 1 ? 'online' : 'offline'"></span>{{ row.status === 1 ? '正常' : '已禁用' }}</template></el-table-column>
          <el-table-column prop="createdAt" label="注册时间" min-width="170" />
          <el-table-column label="操作" width="210" fixed="right"><template #default="{ row }"><div class="row-actions"><el-button v-if="row.role === 'USER'" link type="warning" :icon="Stamp" @click="promoteToAdmin(row)">设为管理员</el-button><el-button link :type="row.status === 1 ? 'danger' : 'success'" :disabled="row.id === auth.user?.id" @click="changeStatus(row)">{{ row.status === 1 ? '禁用账号' : '启用账号' }}</el-button></div></template></el-table-column>
        </el-table>
        <div class="pagination"><el-pagination v-model:current-page="page" :page-size="size" layout="total, prev, pager, next" :total="total" @current-change="loadUsers" /></div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.user-overview-grid{display:grid;margin-bottom:18px;grid-template-columns:repeat(4,minmax(0,1fr));gap:14px}.user-overview-grid article{position:relative;padding:17px 18px;overflow:hidden;border:1px solid #e7eaf2;border-radius:14px;background:#fff;box-shadow:0 7px 22px rgba(31,45,77,.04)}.user-overview-grid article::after{position:absolute;right:-20px;bottom:-35px;width:90px;height:90px;border-radius:50%;background:#fff1ef;content:''}.user-overview-grid span,.user-overview-grid small{position:relative;z-index:1;display:block;color:#8791a5;font-size:11px}.user-overview-grid strong{position:relative;z-index:1;display:block;margin:7px 0 5px;color:#ef6461;font-size:27px;line-height:1}.user-overview-grid .tone-green::after{background:#e7f8f1}.user-overview-grid .tone-green strong{color:#20a878}.user-overview-grid .tone-purple::after{background:#f1ebff}.user-overview-grid .tone-purple strong{color:#7c4dff}.user-overview-grid .tone-blue::after{background:#e8f2ff}.user-overview-grid .tone-blue strong{color:#3483ed}.table-area{padding:0 20px 20px;overflow:auto}.user-cell{display:flex;align-items:center;gap:11px}.user-cell>div{display:flex;flex-direction:column}.user-cell span{margin-top:3px;color:#9098aa;font-size:11px}.row-actions{display:flex;align-items:center;gap:4px}.row-actions :deep(.el-button){margin-left:0}.online,.offline{display:inline-block;width:8px;height:8px;margin-right:7px;border-radius:50%;background:#22a977}.offline{background:#a7afbd}.pagination{display:flex;justify-content:flex-end;margin-top:18px}@media(max-width:980px){.user-overview-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:580px){.user-overview-grid{grid-template-columns:1fr}}
</style>
