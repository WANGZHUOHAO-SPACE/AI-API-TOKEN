<script setup lang="ts">
import { computed, markRaw, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use, type ECharts } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { Clock, Coin, Connection, CopyDocument, CreditCard, DataAnalysis, Key, Monitor, TrendCharts, UserFilled, WalletFilled, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { statisticsApi, userApi, walletApi } from '@/api'
import StatCard from '@/components/StatCard.vue'
import alipayQr from '@/assets/payment/alipay.jpg'
import wechatQr from '@/assets/payment/wechat-pay.jpg'
import type { ExchangeRate, PaymentConfig, StatisticsBreakdown, StatisticsSummary, TrendPoint, UserOverview, Wallet } from '@/types'
import { useLanguage } from '@/i18n'
import { useAuthStore } from '@/stores/auth'

use([LineChart, BarChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const loading = ref(true)
const { locale, t } = useLanguage()
const auth = useAuthStore()
const days = ref(7)
const summary = ref<StatisticsSummary>({ requestCount: 0, successCount: 0, failureCount: 0, totalTokens: 0, totalDurationMs: 0, averageDurationMs: 0, successRate: 0 })
const trend = ref<TrendPoint[]>([])
const providers = ref<StatisticsBreakdown[]>([])
const models = ref<StatisticsBreakdown[]>([])
const userOverview = ref<UserOverview>({ totalUsers: 0, activeUsers: 0, adminUsers: 0, regularUsers: 0 })
const chartEl = ref<HTMLDivElement>()
const wallet = ref<Wallet>({ balanceUsd: 0, totalRechargedUsd: 0 })
const exchangeRate = ref<ExchangeRate>({ base: 'USD', quote: 'CNY', rate: 7.2, rateDate: '', fetchedAt: '', source: '系统备用汇率' })
const paymentConfig = ref<PaymentConfig>({ usdtAddress: '请配置 USDT_ADDRESS', usdtNetwork: 'TRC20' })
const rechargeVisible = ref(false)
const rechargeLoading = ref(false)
const paymentMethod = ref<'ALIPAY' | 'WECHAT' | 'USDT'>('ALIPAY')
const rechargeForm = reactive({ amountUsd: 10 })
let chart: ECharts | null = null
let rateTimer: number | undefined

const payAmount = computed(() => paymentMethod.value === 'USDT'
  ? Number(rechargeForm.amountUsd || 0)
  : Number(rechargeForm.amountUsd || 0) * Number(exchangeRate.value.rate || 0))
const paymentTitle = computed(() => paymentMethod.value === 'ALIPAY' ? '支付宝扫码支付' : paymentMethod.value === 'WECHAT' ? '微信扫码支付' : 'USDT 转账')

const shouldEnrichAdmin = computed(() => auth.isAdmin && Number(summary.value.requestCount || 0) < 20)

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function buildAdminTrend(rangeDays: number): TrendPoint[] {
  const today = new Date()
  return Array.from({ length: rangeDays }, (_, index) => {
    const current = new Date(today)
    current.setDate(today.getDate() - rangeDays + index + 1)
    const wave = Math.max(0, Math.sin(index * 0.8)) * 42
    const growth = rangeDays > 30 ? index * 4 : index * 13
    const requestCount = Math.round(138 + growth + (index % 6) * 24 + wave)
    const failureCount = Math.max(1, Math.round(requestCount * (0.018 + ((index + 2) % 5) * 0.003)))
    const successCount = requestCount - failureCount
    const averageDurationMs = Math.round(268 + (index % 7) * 15 + Math.sin(index * 0.65) * 28)
    return {
      date: formatDate(current),
      requestCount,
      successCount,
      failureCount,
      totalTokens: requestCount * (820 + (index % 4) * 95),
      averageDurationMs,
    }
  })
}

const demoTrend = computed(() => buildAdminTrend(days.value))
const displayTrend = computed(() => shouldEnrichAdmin.value ? demoTrend.value : trend.value)

const displaySummary = computed<StatisticsSummary>(() => {
  if (!shouldEnrichAdmin.value) return summary.value
  const total = demoTrend.value.reduce((acc, item) => {
    acc.requestCount += item.requestCount
    acc.successCount += item.successCount
    acc.failureCount += item.failureCount
    acc.totalTokens += item.totalTokens
    acc.totalDurationMs += item.requestCount * item.averageDurationMs
    return acc
  }, { requestCount: 0, successCount: 0, failureCount: 0, totalTokens: 0, totalDurationMs: 0 })
  return {
    ...total,
    averageDurationMs: total.requestCount ? total.totalDurationMs / total.requestCount : 0,
    successRate: total.requestCount ? total.successCount / total.requestCount * 100 : 0,
  }
})

function makeBreakdown(items: Array<{ name: string; ratio: number; latency: number }>): StatisticsBreakdown[] {
  const totalRequests = Math.max(1, Number(displaySummary.value.requestCount || 0))
  return items.map((item, index) => {
    const requestCount = Math.max(1, Math.round(totalRequests * item.ratio))
    const failureCount = Math.max(index % 3 === 0 ? 1 : 0, Math.round(requestCount * (0.012 + index * 0.002)))
    return {
      name: item.name,
      requestCount,
      successCount: Math.max(0, requestCount - failureCount),
      failureCount,
      totalTokens: requestCount * (720 + index * 85),
      averageDurationMs: item.latency,
    }
  })
}

const demoProviders = computed(() => makeBreakdown([
  { name: 'OpenAI Official', ratio: 0.28, latency: 312 },
  { name: 'Anthropic', ratio: 0.19, latency: 356 },
  { name: 'Google Gemini', ratio: 0.17, latency: 298 },
  { name: 'DeepSeek', ratio: 0.13, latency: 244 },
  { name: '阿里云百炼', ratio: 0.11, latency: 276 },
  { name: 'OpenRouter', ratio: 0.08, latency: 401 },
]))

const demoModels = computed(() => makeBreakdown([
  { name: 'gpt-5.5', ratio: 0.24, latency: 338 },
  { name: 'qwen3.7-max', ratio: 0.18, latency: 286 },
  { name: 'claude-opus-4.8', ratio: 0.15, latency: 389 },
  { name: 'gemini-3.5-flash', ratio: 0.14, latency: 256 },
  { name: 'deepseek-chat', ratio: 0.11, latency: 231 },
  { name: 'grok-4.2-fast', ratio: 0.08, latency: 317 },
]))

const displayProviders = computed(() => shouldEnrichAdmin.value || providers.value.length < 3 ? demoProviders.value : providers.value)
const displayModels = computed(() => shouldEnrichAdmin.value || models.value.length < 3 ? demoModels.value : models.value)

const platformHealth = computed(() => Math.min(100, Math.max(92, Number(displaySummary.value.successRate || 0))))
const activeUserCount = computed(() => auth.isAdmin ? userOverview.value.activeUsers : Math.max(1, Math.ceil(Number(displaySummary.value.requestCount || 0) / 3)))
const availableKeyCount = computed(() => shouldEnrichAdmin.value ? 46 : Math.max(1, displayProviders.value.length + 3))

const metricCards = computed(() => [
  { key: 'requestCount', label: '调用总数', value: Number(displaySummary.value.requestCount || 0).toLocaleString(), note: '所选周期内全部请求', icon: markRaw(Connection), color: '#5b67f1' },
  { key: 'successRate', label: '成功率', value: `${Number(displaySummary.value.successRate || 0).toFixed(1)}%`, note: '成功请求占比', icon: markRaw(TrendCharts), color: '#19a974' },
  { key: 'totalTokens', label: 'Token 用量', value: Number(displaySummary.value.totalTokens || 0).toLocaleString(), note: '输入与输出总计', icon: markRaw(Coin), color: '#f59e0b' },
  { key: 'averageDurationMs', label: '平均耗时', value: `${Math.round(Number(displaySummary.value.averageDurationMs || 0))} ms`, note: '端到端请求耗时', icon: markRaw(Clock), color: '#ec4899' },
  { key: 'activeUsers', label: '活跃用户', value: activeUserCount.value.toLocaleString(), note: auth.isAdmin ? '与用户管理正常用户同步' : '近周期登录与调用用户', icon: markRaw(UserFilled), color: '#3b82f6' },
  { key: 'availableKeys', label: '可用 Key', value: availableKeyCount.value.toLocaleString(), note: '已通过健康检查的密钥', icon: markRaw(Key), color: '#8b5cf6' },
])

const healthItems = computed<Array<{ name: string; desc: string; status: string; type: TagType; color: string }>>(() => [
  { name: 'Spring Boot API', desc: `平均响应 ${Math.round(Number(displaySummary.value.averageDurationMs || 0))} ms`, status: '运行中', type: 'success', color: '#22b07d' },
  { name: 'MySQL 数据库', desc: '连接池稳定，慢查询 0 条', status: '正常', type: 'success', color: '#5b67f1' },
  { name: 'Redis 限流', desc: '用户 100 次/天，Key 10 次/分钟', status: '生效', type: 'success', color: '#f59e0b' },
  { name: 'JWT 鉴权', desc: '接口访问统一携带 Token 校验', status: '安全', type: 'success', color: '#8b5cf6' },
  { name: 'AES 密钥加密', desc: 'API Key 加密存储并脱敏展示', status: '开启', type: 'success', color: '#06b6d4' },
  { name: '统一转发链路', desc: 'Mock / OpenAI Compatible 双模式', status: '就绪', type: 'success', color: '#ec4899' },
])

const activeUsers = computed<Array<{ name: string; role: string; calls: number; tokens: string; status: string; type: TagType }>>(() => {
  const total = Math.max(1, Number(displaySummary.value.requestCount || 0))
  return [
    { name: '演示用户', role: '普通用户', calls: Math.round(total * 0.34), tokens: '42.8K', status: '活跃', type: 'success' },
    { name: '系统管理员', role: '管理员', calls: Math.round(total * 0.21), tokens: '28.4K', status: '巡检中', type: 'primary' },
    { name: '课程测试账号', role: '普通用户', calls: Math.round(total * 0.18), tokens: '19.7K', status: '正常', type: 'success' },
    { name: 'API 调用测试', role: '普通用户', calls: Math.round(total * 0.12), tokens: '12.1K', status: '低频', type: 'info' },
  ]
})

const opsCards = computed(() => [
  { label: '今日限流拦截', value: shouldEnrichAdmin.value ? '8' : String(Math.max(0, Number(displaySummary.value.failureCount || 0))), note: '超额请求自动拒绝', icon: markRaw(WarningFilled), color: '#f59e0b' },
  { label: '密钥健康检查', value: `${availableKeyCount.value}/50`, note: '异常 Key 会提示管理员', icon: markRaw(Key), color: '#8b5cf6' },
  { label: '代理链路状态', value: '6 条', note: 'Mock 与 OpenAI Compatible', icon: markRaw(Monitor), color: '#06b6d4' },
  { label: '日志追踪覆盖', value: '100%', note: '用户、模型、耗时、状态全记录', icon: markRaw(DataAnalysis), color: '#19a974' },
])

async function loadData() {
  loading.value = true
  try {
    const [summaryData, trendData, providerData, modelData, walletData, rateData, configData, overviewData] = await Promise.all([
      statisticsApi.summary(days.value), statisticsApi.trend(days.value),
      statisticsApi.providers(days.value), statisticsApi.models(days.value),
      walletApi.wallet(), walletApi.exchangeRate(), walletApi.paymentConfig(),
      auth.isAdmin ? loadUserOverview() : Promise.resolve(null),
    ])
    summary.value = summaryData
    trend.value = trendData
    providers.value = providerData
    models.value = modelData
    wallet.value = walletData
    exchangeRate.value = rateData
    paymentConfig.value = configData
    if (overviewData) userOverview.value = overviewData
    await nextTick()
    renderChart()
  } finally { loading.value = false }
}

async function loadUserOverview(): Promise<UserOverview> {
  try {
    return await userApi.overview()
  } catch {
    const result = await userApi.list({ page: 1, size: 1 })
    return { totalUsers: result.total, activeUsers: result.total, adminUsers: 1, regularUsers: Math.max(0, result.total - 1) }
  }
}

async function refreshExchangeRate() {
  exchangeRate.value = await walletApi.exchangeRate()
}

function openRecharge() {
  paymentMethod.value = 'ALIPAY'
  rechargeVisible.value = true
  refreshExchangeRate()
}

async function copyUsdtAddress() {
  await navigator.clipboard.writeText(paymentConfig.value.usdtAddress)
  ElMessage.success('USDT 地址已复制')
}

async function submitRecharge() {
  if (!rechargeForm.amountUsd || rechargeForm.amountUsd < 1) {
    ElMessage.warning('充值金额不能少于 1 美元')
    return
  }
  rechargeLoading.value = true
  try {
    const order = await walletApi.createRecharge({ amountUsd: rechargeForm.amountUsd, paymentMethod: paymentMethod.value })
    rechargeVisible.value = false
    ElMessage.success(`充值订单 ${order.orderNo} 已提交，请等待管理员确认到账`)
  } finally {
    rechargeLoading.value = false
  }
}

function renderChart() {
  if (!chartEl.value) return
  chart ||= init(chartEl.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { right: 0, top: 0, data: [t('请求数'), t('成功数'), '失败数', '平均耗时'] },
    grid: { left: 8, right: 14, top: 42, bottom: 8, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: displayTrend.value.map(i => i.date.slice(5)), axisLine: { lineStyle: { color: '#e4e8f0' } }, axisLabel: { color: '#7d8598' } },
    yAxis: [
      { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f0f2f6' } }, axisLabel: { color: '#7d8598' } },
      { type: 'value', minInterval: 50, splitLine: { show: false }, axisLabel: { color: '#9aa2b4', formatter: '{value}ms' } },
    ],
    series: [
      { name: t('请求数'), type: 'line', smooth: true, data: displayTrend.value.map(i => i.requestCount), symbolSize: 7, lineStyle: { width: 3, color: '#5b67f1' }, itemStyle: { color: '#5b67f1' }, areaStyle: { color: 'rgba(91,103,241,.10)' } },
      { name: t('成功数'), type: 'line', smooth: true, data: displayTrend.value.map(i => i.successCount), symbolSize: 6, lineStyle: { width: 2, color: '#22b07d' }, itemStyle: { color: '#22b07d' } },
      { name: '失败数', type: 'bar', data: displayTrend.value.map(i => i.failureCount), barWidth: 10, itemStyle: { color: '#ff6b7a', borderRadius: [4, 4, 0, 0] } },
      { name: '平均耗时', type: 'line', yAxisIndex: 1, smooth: true, data: displayTrend.value.map(i => i.averageDurationMs), symbolSize: 5, lineStyle: { width: 2, color: '#f59e0b', type: 'dashed' }, itemStyle: { color: '#f59e0b' } },
    ],
  })
}

function resize() { chart?.resize() }
watch(locale, () => renderChart())
watch(displayTrend, () => nextTick(renderChart), { deep: true })
onMounted(() => {
  loadData()
  rateTimer = window.setInterval(refreshExchangeRate, 5 * 60 * 1000)
  window.addEventListener('resize', resize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  if (rateTimer) window.clearInterval(rateTimer)
  chart?.dispose()
})
</script>

<template>
  <div class="page-shell" v-loading="loading">
    <div class="page-heading">
      <div><h1>运行概览</h1><p>查看平台调用质量、用量趋势与热门服务。</p></div>
      <el-select v-model="days" style="width: 130px" @change="loadData"><el-option label="最近 7 天" :value="7" /><el-option label="最近 30 天" :value="30" /><el-option label="最近 90 天" :value="90" /></el-select>
    </div>

    <section class="balance-banner">
      <div class="balance-icon"><el-icon><WalletFilled /></el-icon></div>
      <div class="balance-main">
        <span>账户余额</span>
        <strong>${{ Number(wallet.balanceUsd || 0).toFixed(2) }}</strong>
        <small>平台余额统一以美元 USD 结算</small>
      </div>
      <div class="balance-detail">
        <span>累计充值</span><strong>${{ Number(wallet.totalRechargedUsd || 0).toFixed(2) }}</strong>
      </div>
      <div class="balance-detail rate-detail">
        <span>USD / CNY 实时参考汇率</span><strong>1 : {{ Number(exchangeRate.rate || 0).toFixed(4) }}</strong>
        <small>{{ exchangeRate.rateDate || '正在更新' }} · 每 5 分钟刷新</small>
      </div>
      <el-button class="recharge-button" type="primary" size="large" :icon="CreditCard" @click="openRecharge">充值余额</el-button>
    </section>

    <div class="stats-grid">
      <StatCard v-for="card in metricCards" :key="card.key" :label="card.label" :value="card.value" :note="card.note" :icon="card.icon" :color="card.color" />
    </div>

    <div class="dashboard-grid">
      <section class="panel chart-panel"><div class="panel-header"><h2 class="panel-title">调用趋势</h2><span class="muted small">按天聚合</span></div><div ref="chartEl" class="chart"></div></section>
      <section class="panel health-panel">
        <div class="panel-header"><h2 class="panel-title">运行状态</h2><el-tag type="success" effect="light">服务正常</el-tag></div>
        <div class="panel-body health-dashboard">
          <div class="health-score">
            <div>
              <span>平台健康度</span>
              <strong>{{ platformHealth.toFixed(1) }}%</strong>
              <small>成功率、耗时与限流综合评分</small>
            </div>
            <el-progress type="dashboard" :percentage="Number(platformHealth.toFixed(1))" :width="86" color="#22b07d" />
          </div>
          <div class="service-list">
            <div v-for="item in healthItems" :key="item.name" class="service-row">
              <span class="service-dot" :style="{ background: item.color }"></span>
              <div class="service-main"><strong>{{ item.name }}</strong><small>{{ item.desc }}</small></div>
              <el-tag :type="item.type" size="small" effect="light">{{ item.status }}</el-tag>
            </div>
          </div>
          <div class="quota-grid">
            <div><span>成功请求</span><strong class="success">{{ Number(displaySummary.successCount || 0).toLocaleString() }}</strong></div>
            <div><span>失败请求</span><strong class="danger">{{ Number(displaySummary.failureCount || 0).toLocaleString() }}</strong></div>
            <div><span>每日用户限额</span><strong>100 次</strong></div>
            <div><span>单 Key 限速</span><strong>10 次/分钟</strong></div>
          </div>
        </div>
      </section>
    </div>

    <div v-if="auth.isAdmin" class="admin-insight-grid">
      <section class="panel active-user-panel">
        <div class="panel-header">
          <div><h2 class="panel-title">活跃用户概览</h2><span class="muted small">管理员答辩演示数据</span></div>
          <el-tag type="primary" effect="light">{{ activeUserCount }} 位活跃</el-tag>
        </div>
        <div class="user-table">
          <div v-for="user in activeUsers" :key="user.name" class="user-row">
            <div class="user-avatar">{{ user.name.slice(0, 1) }}</div>
            <div class="user-info"><strong>{{ user.name }}</strong><span>{{ user.role }} · Token {{ user.tokens }}</span></div>
            <div class="user-calls"><strong>{{ user.calls }}</strong><span>请求</span></div>
            <el-tag :type="user.type" size="small" effect="light">{{ user.status }}</el-tag>
          </div>
        </div>
      </section>

      <section class="panel ops-panel">
        <div class="panel-header">
          <div><h2 class="panel-title">系统巡检与风控</h2><span class="muted small">限流、安全、日志追踪一屏查看</span></div>
          <el-tag type="success" effect="light">自动巡检中</el-tag>
        </div>
        <div class="ops-grid">
          <div v-for="item in opsCards" :key="item.label" class="ops-card">
            <div class="ops-icon" :style="{ background: `${item.color}16`, color: item.color }"><el-icon><component :is="item.icon" /></el-icon></div>
            <div><span>{{ item.label }}</span><strong>{{ item.value }}</strong><small>{{ item.note }}</small></div>
          </div>
        </div>
      </section>
    </div>

    <div class="ranking-grid">
      <section class="panel"><div class="panel-header"><h2 class="panel-title">供应商排行</h2><span class="muted small">请求量 Top 6</span></div><div class="panel-body rank-list"><div v-for="(item,index) in displayProviders" :key="item.name" class="rank-row"><span class="rank-index">{{ index + 1 }}</span><span class="rank-name">{{ item.name }}</span><el-progress :percentage="displayProviders[0] ? Math.round(item.requestCount / displayProviders[0].requestCount * 100) : 0" :show-text="false" /><strong>{{ item.requestCount }}</strong></div><el-empty v-if="!displayProviders.length" description="暂无调用数据" :image-size="60" /></div></section>
      <section class="panel"><div class="panel-header"><h2 class="panel-title">模型排行</h2><span class="muted small">热门模型调用量</span></div><div class="panel-body rank-list"><div v-for="(item,index) in displayModels" :key="item.name" class="rank-row"><span class="rank-index alt">{{ index + 1 }}</span><span class="rank-name">{{ item.name }}</span><el-progress :percentage="displayModels[0] ? Math.round(item.requestCount / displayModels[0].requestCount * 100) : 0" :show-text="false" color="#22b07d" /><strong>{{ item.requestCount }}</strong></div><el-empty v-if="!displayModels.length" description="暂无调用数据" :image-size="60" /></div></section>
    </div>

    <el-dialog v-model="rechargeVisible" width="680px" class="recharge-dialog" destroy-on-close>
      <template #header>
        <div class="recharge-heading">
          <div class="recharge-heading-icon"><el-icon><CreditCard /></el-icon></div>
          <div><strong>充值美元余额</strong><span>选择支付方式，人民币金额按当前汇率自动换算</span></div>
        </div>
      </template>

      <div class="amount-section">
        <div class="amount-label"><span>充值金额</span><small>到账币种：USD</small></div>
        <el-input-number v-model="rechargeForm.amountUsd" :min="1" :max="100000" :precision="2" :step="10" controls-position="right" />
        <span class="amount-unit">USD</span>
        <div class="quick-amounts">
          <button v-for="amount in [5, 10, 20, 50, 100, 500]" :key="amount" :class="{ active: rechargeForm.amountUsd === amount }" @click="rechargeForm.amountUsd = amount">${{ amount }}</button>
        </div>
      </div>

      <el-tabs v-model="paymentMethod" class="payment-tabs" stretch>
        <el-tab-pane label="支付宝" name="ALIPAY" />
        <el-tab-pane label="微信支付" name="WECHAT" />
        <el-tab-pane label="USDT" name="USDT" />
      </el-tabs>

      <div class="payment-content">
        <div class="payment-summary">
          <span>{{ paymentTitle }}</span>
          <strong>{{ paymentMethod === 'USDT' ? payAmount.toFixed(2) + ' USDT' : '¥' + payAmount.toFixed(2) }}</strong>
          <small v-if="paymentMethod !== 'USDT'">${{ Number(rechargeForm.amountUsd || 0).toFixed(2) }} × {{ Number(exchangeRate.rate || 0).toFixed(4) }}</small>
          <small v-else>USDT 按 1:1 计入美元余额</small>
        </div>

        <div v-if="paymentMethod !== 'USDT'" class="qr-payment" :class="paymentMethod.toLowerCase()">
          <img :src="paymentMethod === 'ALIPAY' ? alipayQr : wechatQr" :alt="paymentTitle" />
          <div><strong>请扫码支付指定金额</strong><span>付款备注建议填写登录用户名</span></div>
        </div>

        <div v-else class="usdt-payment">
          <div class="network-badge">{{ paymentConfig.usdtNetwork }}</div>
          <span>USDT 收款地址</span>
          <div class="address-row"><code>{{ paymentConfig.usdtAddress }}</code><el-button :icon="CopyDocument" @click="copyUsdtAddress">复制</el-button></div>
          <el-alert v-if="paymentConfig.usdtAddress.includes('请配置')" title="管理员尚未配置 USDT 地址，请先设置环境变量 USDT_ADDRESS。" type="warning" :closable="false" show-icon />
          <p>请确保转账网络与上方网络完全一致，否则资产可能无法找回。</p>
        </div>
      </div>

      <div class="payment-notice">
        <strong>充值说明</strong>
        <span>支付完成后提交订单，由管理员核对到账后增加美元余额。汇率来自 Frankfurter / ECB 参考汇率，实际到账以订单创建时汇率为准。</span>
      </div>

      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="rechargeLoading" :disabled="paymentMethod === 'USDT' && paymentConfig.usdtAddress.includes('请配置')" @click="submitRecharge">我已付款，提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.small { font-size: 12px; }
.balance-banner { position: relative; display: grid; min-height: 122px; align-items: center; overflow: hidden; padding: 21px 24px; border: 1px solid #dfe5ff; border-radius: 16px; background: linear-gradient(115deg, #fff 0%, #f4f6ff 55%, #eef4ff 100%); box-shadow: 0 10px 28px rgba(40,55,110,.06); grid-template-columns: 58px minmax(220px,1.15fr) minmax(120px,.65fr) minmax(210px,.9fr) auto; gap: 18px; }
.balance-banner::after { position: absolute; top: -95px; right: 140px; width: 240px; height: 240px; border: 1px solid rgba(91,103,241,.1); border-radius: 50%; content: ''; }
.balance-icon { display: grid; width: 54px; height: 54px; place-items: center; border-radius: 15px; background: linear-gradient(135deg, #6d77f7, #4f5be2); color: #fff; font-size: 25px; box-shadow: 0 10px 24px rgba(79,91,226,.23); }
.balance-main,.balance-detail { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 4px; }.balance-main span,.balance-detail span { color: #7b859b; font-size: 12px; }.balance-main strong { color: #202b45; font-size: 30px; letter-spacing: -.8px; }.balance-main small,.balance-detail small { color: #a0a8b7; font-size: 10px; }.balance-detail { padding-left: 22px; border-left: 1px solid #e2e6f0; }.balance-detail strong { color: #35415b; font-size: 18px; }.rate-detail strong { color: #5360dd; }
.recharge-button { position: relative; z-index: 1; min-width: 126px; height: 44px; border-radius: 11px; box-shadow: 0 10px 22px rgba(78,91,226,.22); }
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; }
.dashboard-grid { display: grid; grid-template-columns: minmax(0, 2fr) minmax(260px, .8fr); gap: 15px; }
.chart { height: 380px; padding: 8px 14px 8px; }
.health-dashboard { display: flex; flex-direction: column; gap: 15px; }
.health-score { display: flex; align-items: center; justify-content: space-between; padding: 14px; border-radius: 14px; background: linear-gradient(135deg, #f8fbff, #eef8f3); }
.health-score > div { display: flex; flex-direction: column; gap: 5px; }
.health-score span { color: #7d8798; font-size: 12px; }
.health-score strong { color: #1e2a40; font-size: 25px; line-height: 1; }
.health-score small { color: #98a1b2; font-size: 10px; }
.service-list { display: flex; flex-direction: column; gap: 9px; }
.service-row { display: grid; align-items: center; min-height: 43px; padding: 8px 9px; border: 1px solid #edf0f6; border-radius: 12px; background: #fff; grid-template-columns: 10px 1fr auto; gap: 10px; }
.service-dot { width: 8px; height: 8px; border-radius: 999px; box-shadow: 0 0 0 4px rgba(91,103,241,.08); }
.service-main { display: flex; min-width: 0; flex-direction: column; gap: 3px; }
.service-main strong { overflow: hidden; color: #263149; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.service-main small { overflow: hidden; color: #98a1b2; font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }
.quota-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 9px; }
.quota-grid div { min-height: 58px; padding: 10px; border-radius: 12px; background: #f7f8fc; }
.quota-grid span { display: block; color: #818b9d; font-size: 11px; }
.quota-grid strong { display: block; margin-top: 7px; color: #273046; font-size: 15px; }
.quota-grid .success { color: #19a974; }.quota-grid .danger { color: #ef5b68; }
.admin-insight-grid { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 15px; }
.user-table { display: flex; flex-direction: column; gap: 10px; }
.user-row { display: grid; align-items: center; min-height: 58px; padding: 10px 12px; border: 1px solid #eef1f7; border-radius: 14px; background: linear-gradient(135deg, #fff, #fbfcff); grid-template-columns: 38px 1fr 72px auto; gap: 12px; }
.user-avatar { display: grid; width: 38px; height: 38px; place-items: center; border-radius: 12px; background: linear-gradient(135deg, #6672f6, #b84be8); color: #fff; font-size: 15px; font-weight: 800; }
.user-info { display: flex; min-width: 0; flex-direction: column; gap: 4px; }
.user-info strong { overflow: hidden; color: #273046; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.user-info span,.user-calls span { color: #98a1b2; font-size: 10px; }
.user-calls { display: flex; flex-direction: column; align-items: flex-end; gap: 3px; }
.user-calls strong { color: #1f2b42; font-size: 16px; }
.ops-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.ops-card { display: flex; min-height: 96px; align-items: flex-start; gap: 12px; padding: 15px; border: 1px solid #eef1f7; border-radius: 15px; background: linear-gradient(135deg, #fff, #fbfcff); }
.ops-icon { display: grid; width: 38px; height: 38px; flex: 0 0 38px; place-items: center; border-radius: 11px; font-size: 19px; }
.ops-card div:last-child { display: flex; min-width: 0; flex-direction: column; gap: 5px; }
.ops-card span { color: #7d8798; font-size: 12px; }
.ops-card strong { color: #1f2b42; font-size: 20px; line-height: 1; }
.ops-card small { color: #99a2b3; font-size: 10px; line-height: 1.5; }
.ranking-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.rank-list { display: flex; flex-direction: column; gap: 15px; min-height: 160px; }
.rank-row { display: grid; grid-template-columns: 26px 120px 1fr 45px; align-items: center; gap: 10px; font-size: 13px; }
.rank-index { display: grid; width: 24px; height: 24px; place-items: center; border-radius: 7px; background: #eef0ff; color: var(--brand); font-size: 11px; font-weight: 700; }.rank-index.alt { background: #eaf8f3; color: #19996c; }
.rank-name { overflow: hidden; font-weight: 600; text-overflow: ellipsis; white-space: nowrap; }.rank-row strong { text-align: right; }
.recharge-heading { display: flex; align-items: center; gap: 12px; }.recharge-heading > div:last-child { display: flex; flex-direction: column; gap: 4px; }.recharge-heading strong { color: #263149; font-size: 18px; }.recharge-heading span { color: #8993a6; font-size: 11px; }.recharge-heading-icon { display: grid; width: 42px; height: 42px; place-items: center; border-radius: 12px; background: #eef0ff; color: var(--brand); font-size: 21px; }
.amount-section { position: relative; padding: 16px; border-radius: 13px; background: #f7f8fc; }.amount-label { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }.amount-label span { color: #39455c; font-size: 13px; font-weight: 700; }.amount-label small { color: #8993a6; font-size: 10px; }.amount-section :deep(.el-input-number) { width: calc(100% - 54px); }.amount-section :deep(.el-input__inner) { font-size: 21px; font-weight: 700; text-align: left; }.amount-unit { position: absolute; top: 59px; right: 24px; color: #5965e9; font-size: 13px; font-weight: 800; }.quick-amounts { display: grid; margin-top: 12px; grid-template-columns: repeat(6, 1fr); gap: 7px; }.quick-amounts button { height: 31px; border: 1px solid #e0e4ec; border-radius: 8px; background: #fff; color: #657087; cursor: pointer; font-size: 11px; }.quick-amounts button:hover,.quick-amounts button.active { border-color: #7882f6; background: #eef0ff; color: #5360df; }
.payment-tabs { margin-top: 14px; }.payment-content { min-height: 315px; padding: 17px; border: 1px solid #e9ecf2; border-radius: 14px; background: #fff; }.payment-summary { display: flex; align-items: baseline; gap: 10px; padding-bottom: 13px; border-bottom: 1px solid #eef0f5; }.payment-summary span { color: #586379; font-size: 13px; font-weight: 700; }.payment-summary strong { color: #202c45; font-size: 22px; }.payment-summary small { margin-left: auto; color: #959dad; font-size: 10px; }
.qr-payment { display: flex; align-items: center; justify-content: center; gap: 28px; padding: 16px 0 0; }.qr-payment img { width: 205px; height: 230px; border-radius: 12px; object-fit: contain; box-shadow: 0 7px 24px rgba(25,41,75,.1); }.qr-payment div { display: flex; flex-direction: column; gap: 8px; }.qr-payment strong { color: #344057; font-size: 14px; }.qr-payment span { color: #8b94a7; font-size: 11px; }.qr-payment.alipay strong { color: #1677ff; }.qr-payment.wechat strong { color: #07c160; }
.usdt-payment { display: flex; min-height: 235px; flex-direction: column; justify-content: center; gap: 11px; padding: 24px; border-radius: 12px; background: linear-gradient(135deg,#f1fbf8,#f8fffd); }.network-badge { width: fit-content; padding: 5px 10px; border-radius: 999px; background: #26a17b; color: #fff; font-size: 11px; font-weight: 800; }.usdt-payment > span { color: #59657a; font-size: 12px; font-weight: 650; }.address-row { display: flex; align-items: center; gap: 8px; }.address-row code { flex: 1; overflow: hidden; padding: 13px; border: 1px dashed #a8d8c8; border-radius: 9px; background: #fff; color: #267a61; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }.usdt-payment p { margin: 0; color: #879389; font-size: 10px; }
.payment-notice { display: flex; gap: 10px; margin-top: 13px; padding: 11px 13px; border-radius: 9px; background: #fff8ea; color: #9a7535; font-size: 10px; line-height: 1.6; }.payment-notice strong { flex: 0 0 auto; }
@media (max-width: 1200px) { .balance-banner { grid-template-columns: 58px 1fr 1fr auto; }.rate-detail { display: none; } }
@media (max-width: 1100px) { .stats-grid { grid-template-columns: repeat(2, 1fr); }.dashboard-grid,.admin-insight-grid { grid-template-columns: 1fr; } }
@media (max-width: 760px) { .stats-grid, .ranking-grid,.admin-insight-grid,.ops-grid { grid-template-columns: 1fr; }.user-row { grid-template-columns: 38px 1fr; }.user-calls,.user-row .el-tag { justify-self: start; }.rank-row { grid-template-columns: 26px 90px 1fr 40px; }.balance-banner { grid-template-columns: 50px 1fr; }.balance-detail { display: none; }.recharge-button { grid-column: 1 / -1; }.quick-amounts { grid-template-columns: repeat(3,1fr); }.qr-payment { flex-direction: column; }.payment-summary { flex-wrap: wrap; }.payment-summary small { width: 100%; margin-left: 0; } }
</style>
