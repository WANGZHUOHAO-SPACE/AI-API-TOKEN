<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { walletApi } from '@/api'
import type { RechargeOrder } from '@/types'

const loading = ref(false)
const records = ref<RechargeOrder[]>([])
const total = ref(0)
const filters = reactive({ page: 1, size: 20, status: '' })

const methodNames: Record<string, string> = { ALIPAY: '支付宝', WECHAT: '微信支付', USDT: 'USDT' }
const statusMap: Record<string, { label: string; type: 'warning' | 'success' | 'danger' }> = {
  PENDING: { label: '待审核', type: 'warning' },
  SUCCESS: { label: '已到账', type: 'success' },
  REJECTED: { label: '已拒绝', type: 'danger' },
}

async function loadOrders() {
  loading.value = true
  try {
    const result = await walletApi.adminOrders({ page: filters.page, size: filters.size, status: filters.status || undefined })
    records.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function confirmOrder(row: RechargeOrder | any) {
  await ElMessageBox.confirm(`确认订单 ${row.orderNo} 已到账，并为用户增加 $${Number(row.amountUsd).toFixed(2)} 余额吗？`, '确认充值到账', { type: 'warning' })
  await walletApi.confirm(row.id)
  ElMessage.success('充值已确认，余额已入账')
  loadOrders()
}

async function rejectOrder(row: RechargeOrder | any) {
  await ElMessageBox.confirm(`确定拒绝订单 ${row.orderNo} 吗？`, '拒绝充值', { type: 'warning' })
  await walletApi.reject(row.id)
  ElMessage.success('订单已拒绝')
  loadOrders()
}

onMounted(loadOrders)
</script>

<template>
  <div class="page-shell">
    <div class="page-heading">
      <div><h1>充值订单</h1><p>核对支付宝、微信和 USDT 到账记录，确认后系统自动增加用户美元余额。</p></div>
      <el-button :icon="Refresh" @click="loadOrders">刷新</el-button>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div class="filter-bar">
          <el-select v-model="filters.status" style="width:150px" @change="filters.page = 1; loadOrders()">
            <el-option label="全部状态" value="" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已到账" value="SUCCESS" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </div>
      </div>
      <div class="panel-body">
        <el-table v-loading="loading" :data="records">
          <el-table-column prop="orderNo" label="订单号" min-width="185"><template #default="{ row }"><span class="mono">{{ row.orderNo }}</span></template></el-table-column>
          <el-table-column prop="userId" label="用户 ID" width="90" />
          <el-table-column label="支付方式" width="110"><template #default="{ row }">{{ methodNames[row.paymentMethod] || row.paymentMethod }}</template></el-table-column>
          <el-table-column label="美元入账" width="115"><template #default="{ row }"><strong>${{ Number(row.amountUsd).toFixed(2) }}</strong></template></el-table-column>
          <el-table-column label="应付金额" min-width="135"><template #default="{ row }">{{ row.payCurrency === 'CNY' ? '¥' : '' }}{{ Number(row.payAmount).toFixed(2) }} {{ row.payCurrency }}</template></el-table-column>
          <el-table-column label="汇率" width="105"><template #default="{ row }">{{ Number(row.exchangeRate).toFixed(4) }}</template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="statusMap[row.status]?.type" effect="light">{{ statusMap[row.status]?.label || row.status }}</el-tag></template></el-table-column>
          <el-table-column prop="createdAt" label="提交时间" min-width="165" />
          <el-table-column label="操作" fixed="right" width="155">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button type="success" link @click="confirmOrder(row)">确认到账</el-button>
                <el-button type="danger" link @click="rejectOrder(row)">拒绝</el-button>
              </template>
              <span v-else class="muted">已处理</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination"><el-pagination v-model:current-page="filters.page" v-model:page-size="filters.size" layout="total, prev, pager, next" :total="total" @current-change="loadOrders" /></div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.pagination { display: flex; justify-content: flex-end; margin-top: 18px; }
.mono { color: #5360d8; font-size: 11px; }
</style>
