<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { logApi } from '@/api'
import type { RequestLog } from '@/types'

defineProps<{ showUser?: boolean }>()
const loading = ref(false)
const logs = ref<RequestLog[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const dateRange = ref<string[]>([])
const detailVisible = ref(false)
const selected = ref<RequestLog>()
const filters = reactive<{ provider: string; model: string; success?: boolean; statusCode?: number }>({ provider: '', model: '' })

async function loadLogs() {
  loading.value = true
  try {
    const result = await logApi.list({
      page: page.value, size: size.value,
      provider: filters.provider || undefined, model: filters.model || undefined,
      success: filters.success, statusCode: filters.statusCode,
      startTime: dateRange.value?.[0], endTime: dateRange.value?.[1],
    })
    logs.value = result.records; total.value = result.total
  } finally { loading.value = false }
}
function search() { page.value = 1; loadLogs() }
function reset() { Object.assign(filters, { provider: '', model: '', success: undefined, statusCode: undefined }); dateRange.value = []; search() }
function showDetail(row: unknown) { selected.value = row as RequestLog; detailVisible.value = true }
function statusTag(status: number): 'success' | 'warning' | 'danger' { if (status >= 200 && status < 300) return 'success'; if (status === 429) return 'warning'; return 'danger' }
function changePageSize() { page.value = 1; loadLogs() }
onMounted(loadLogs)
</script>

<template>
  <section class="panel">
    <div class="panel-body filter-area">
      <div class="filter-bar">
        <el-input v-model="filters.provider" placeholder="供应商编码" clearable style="width:150px" />
        <el-input v-model="filters.model" placeholder="模型名称" clearable style="width:170px" />
        <el-select v-model="filters.success" placeholder="调用结果" clearable style="width:130px"><el-option label="成功" :value="true" /><el-option label="失败" :value="false" /></el-select>
        <el-select v-model="filters.statusCode" placeholder="状态码" clearable style="width:120px"><el-option label="200" :value="200" /><el-option label="400" :value="400" /><el-option label="429" :value="429" /><el-option label="500" :value="500" /><el-option label="502" :value="502" /><el-option label="503" :value="503" /></el-select>
        <el-date-picker v-model="dateRange" type="datetimerange" value-format="YYYY-MM-DDTHH:mm:ss" start-placeholder="开始时间" end-placeholder="结束时间" range-separator="至" style="width:340px" />
        <el-button type="primary" :icon="Search" @click="search">查询</el-button>
        <el-button :icon="Refresh" @click="reset">重置</el-button>
      </div>
    </div>
    <div class="table-area">
      <el-table v-loading="loading" :data="logs" row-key="id">
        <el-table-column prop="requestId" label="Request ID" min-width="185"><template #default="{ row }"><code class="request-id">{{ row.requestId }}</code></template></el-table-column>
        <el-table-column v-if="showUser" prop="userId" label="用户 ID" width="90" align="center" />
        <el-table-column prop="providerCode" label="供应商" width="120" />
        <el-table-column prop="modelName" label="模型" min-width="140" show-overflow-tooltip />
        <el-table-column prop="credentialId" label="Key ID" width="80" align="center" />
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="statusTag(row.statusCode)" effect="light">{{ row.statusCode }}</el-tag></template></el-table-column>
        <el-table-column label="结果" width="80"><template #default="{ row }"><span :class="row.success === 1 ? 'success-dot' : 'failure-dot'"></span>{{ row.success === 1 ? '成功' : '失败' }}</template></el-table-column>
        <el-table-column label="耗时" width="95"><template #default="{ row }">{{ row.durationMs }} ms</template></el-table-column>
        <el-table-column label="Token" width="80" align="right"><template #default="{ row }">{{ row.totalTokens || 0 }}</template></el-table-column>
        <el-table-column prop="createdAt" label="调用时间" min-width="165" />
        <el-table-column label="操作" width="75" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="showDetail(row)">详情</el-button></template></el-table-column>
      </el-table>
      <div class="pagination"><el-pagination v-model:current-page="page" v-model:page-size="size" layout="total, sizes, prev, pager, next" :page-sizes="[10,20,50]" :total="total" @current-change="loadLogs" @size-change="changePageSize" /></div>
    </div>
  </section>

  <el-dialog v-model="detailVisible" title="调用详情" width="min(620px, 92vw)">
    <el-descriptions v-if="selected" :column="2" border>
      <el-descriptions-item label="Request ID" :span="2"><code>{{ selected.requestId }}</code></el-descriptions-item>
      <el-descriptions-item label="用户 ID">{{ selected.userId }}</el-descriptions-item><el-descriptions-item label="Key ID">{{ selected.credentialId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="供应商">{{ selected.providerCode }}</el-descriptions-item><el-descriptions-item label="模型">{{ selected.modelName }}</el-descriptions-item>
      <el-descriptions-item label="状态码">{{ selected.statusCode }}</el-descriptions-item><el-descriptions-item label="耗时">{{ selected.durationMs }} ms</el-descriptions-item>
      <el-descriptions-item label="输入 Token">{{ selected.inputTokens }}</el-descriptions-item><el-descriptions-item label="输出 Token">{{ selected.outputTokens }}</el-descriptions-item>
      <el-descriptions-item label="调用时间" :span="2">{{ selected.createdAt }}</el-descriptions-item>
      <el-descriptions-item label="错误信息" :span="2"><span :class="selected.errorMessage ? 'error-text' : 'muted'">{{ selected.errorMessage || '无' }}</span></el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<style scoped>
.filter-area{padding-bottom:14px}.table-area{padding:0 20px 20px;overflow:auto}.request-id{color:#5963d9;font-size:11px}.success-dot,.failure-dot{display:inline-block;width:7px;height:7px;margin-right:6px;border-radius:50%;background:#25a979}.failure-dot{background:#ef5b68}.pagination{display:flex;justify-content:flex-end;margin-top:18px}.error-text{color:#d84655;line-height:1.6}code{word-break:break-all}@media(max-width:720px){.table-area{padding:0 12px 14px}.pagination{justify-content:flex-start}}
</style>
