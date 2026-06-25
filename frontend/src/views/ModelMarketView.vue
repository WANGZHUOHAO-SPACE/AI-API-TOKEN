<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDownBold, ArrowUpBold, Collection, CopyDocument, EditPen, Link, Promotion, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { modelPriceApi, publicPricingApi } from '@/api'
import { externalPricingCatalog, modelCatalog, providerSources, type CatalogModel, type ModelType } from '@/data/modelCatalog'
import { useAuthStore } from '@/stores/auth'
import { modelDescription, translateText, useLanguage } from '@/i18n'

const router = useRouter()
const auth = useAuthStore()
const { locale } = useLanguage()
const keyword = ref('')
const selectedProvider = ref('全部供应商')
const selectedType = ref<'全部类型' | ModelType>('全部类型')
const featuredOnly = ref(false)
const currentPage = ref(1)
const pageSize = 30
const pricingLoading = ref(false)
const onlinePricing = ref(false)
const pricingSyncedAt = ref('')
const providerListRef = ref<HTMLElement>()
const models = ref<CatalogModel[]>(modelCatalog.map(item => ({ ...item })))
const overriddenCodes = ref(new Set<string>())
const priceDialogVisible = ref(false)
const savingPrice = ref(false)
const editingModel = ref<CatalogModel>()
const priceForm = reactive({ inputPrice: '', cachedPrice: '', outputPrice: '', extraPrice: '' })

const providers = computed(() => ['全部供应商', ...Array.from(new Set(models.value.map(item => item.provider)))])
const modelTypes: Array<'全部类型' | ModelType> = ['全部类型', '对话', '代码', '图像', '音频', '视频', '检索', '翻译']

const providerColors: Record<string, string> = {
  OpenAI: '#151a24',
  'OpenAI Official': '#151a24',
  'Azure OpenAI': '#2563eb',
  Anthropic: '#d97745',
  Google: '#4285f4',
  xAI: '#5965ef',
  DeepSeek: '#4169e1',
  阿里云百炼: '#6f5bf6',
  Bailian: '#6f5bf6',
  BytePlus: '#0ea5e9',
  DouBaoSeed: '#ef4444',
  'Zhipu GLM': '#7c3aed',
  'Zhipu GLM en': '#8b5cf6',
  Kimi: '#111827',
  StepFun: '#f97316',
  'StepFun en': '#fb923c',
  MiniMax: '#f59e0b',
  'MiniMax en': '#fbbf24',
  SiliconFlow: '#10b981',
  'SiliconFlow en': '#14b8a6',
  OpenRouter: '#6366f1',
  TheRouter: '#64748b',
  自定义配置: '#0ea5e9',
}

const filteredModels = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  return models.value.filter((item) => {
    const matchesProvider = selectedProvider.value === '全部供应商' || item.provider === selectedProvider.value
    const matchesType = selectedType.value === '全部类型' || item.type === selectedType.value
    const matchesFeatured = !featuredOnly.value || item.featured
    const searchable = `${item.name} ${item.provider} ${displayText(item.provider)} ${item.description} ${displayDescription(item)} ${item.tags.join(' ')} ${item.tags.map(tag => displayText(tag)).join(' ')}`.toLowerCase()
    return matchesProvider && matchesType && matchesFeatured && (!query || searchable.includes(query))
  })
})

const pagedModels = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredModels.value.slice(start, start + pageSize)
})

const syncTimeLabel = computed(() => {
  if (!pricingSyncedAt.value) return ''
  const date = new Date(pricingSyncedAt.value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString(locale.value === 'zh' ? 'zh-CN' : 'en-US', { hour12: false })
})

watch([keyword, selectedProvider, selectedType, featuredOnly], () => {
  currentPage.value = 1
})

function countProvider(provider: string) {
  return provider === '全部供应商' ? models.value.length : models.value.filter(item => item.provider === provider).length
}

function countType(type: string) {
  return type === '全部类型' ? models.value.length : models.value.filter(item => item.type === type).length
}

function scrollProviders(direction: 'up' | 'down') {
  providerListRef.value?.scrollBy({
    top: direction === 'up' ? -220 : 220,
    behavior: 'smooth',
  })
}

async function loadPrices() {
  pricingLoading.value = true
  try {
    const [pricingResult, overrideResult] = await Promise.allSettled([
      publicPricingApi.list(),
      modelPriceApi.list(),
    ])
    let baseModels = modelCatalog
    if (pricingResult.status === 'fulfilled') {
      const externalModels = externalPricingCatalog(pricingResult.value)
      if (externalModels.length) {
        baseModels = externalModels
        onlinePricing.value = true
        pricingSyncedAt.value = pricingResult.value.syncedAt || ''
      }
    } else {
      onlinePricing.value = false
      pricingSyncedAt.value = ''
    }

    const overrides = overrideResult.status === 'fulfilled' ? overrideResult.value : []
    const overrideMap = new Map(overrides.map(item => [item.modelCode, item]))
    overriddenCodes.value = new Set(overrides.map(item => item.modelCode))
    models.value = baseModels.map((item) => {
      const override = overrideMap.get(item.id)
      return override ? {
        ...item,
        input: override.inputPrice?.trim() || item.input,
        cached: override.cachedPrice?.trim() || item.cached,
        output: override.outputPrice?.trim() || item.output,
        extra: override.extraPrice?.trim() || item.extra,
      } : { ...item }
    })
    currentPage.value = 1
  } finally {
    pricingLoading.value = false
  }
}

function editPrice(item: CatalogModel) {
  editingModel.value = item
  priceForm.inputPrice = item.input || ''
  priceForm.cachedPrice = item.cached || ''
  priceForm.outputPrice = item.output || ''
  priceForm.extraPrice = item.extra || ''
  priceDialogVisible.value = true
}

async function savePrice() {
  if (!editingModel.value) return
  savingPrice.value = true
  try {
    await modelPriceApi.update(editingModel.value.id, { ...priceForm })
    await loadPrices()
    priceDialogVisible.value = false
    ElMessage.success('模型价格已更新，普通用户将看到最新价格')
  } finally {
    savingPrice.value = false
  }
}

async function resetPrice() {
  if (!editingModel.value) return
  await ElMessageBox.confirm(`确定恢复 ${editingModel.value.name} 的官方默认价格吗？`, '恢复默认价格', { type: 'warning' })
  savingPrice.value = true
  try {
    await modelPriceApi.reset(editingModel.value.id)
    await loadPrices()
    priceDialogVisible.value = false
    ElMessage.success('已恢复官方默认价格')
  } finally {
    savingPrice.value = false
  }
}

async function copyModel(modelName: string) {
  await navigator.clipboard.writeText(modelName)
  ElMessage.success(`已复制模型名称：${modelName}`)
}

function tryModel(item: CatalogModel) {
  if (!auth.isLoggedIn) {
    ElMessage.info(locale.value === 'zh' ? '登录后即可体验模型调用' : 'Sign in to try model calls')
    router.push('/console-login')
    return
  }
  router.push({ path: '/playground', query: { provider: item.providerCode, model: item.id } })
}

function openSource(provider: CatalogModel['provider']) {
  const source = onlinePricing.value ? 'https://jeniya.chat/pricing' : providerSources[provider] || 'https://api-jeniya-top.apifox.cn/'
  window.open(source, '_blank', 'noopener,noreferrer')
}

function displayDescription(item: CatalogModel) {
  return locale.value === 'en' ? modelDescription(item.name, item.provider, item.type, item.tags) : item.description
}

function displayText(value?: string) {
  return value ? translateText(value) : value
}

function providerColor(provider: string) {
  if (providerColors[provider]) return providerColors[provider]
  let hash = 0
  for (const char of provider) hash = (hash * 31 + char.charCodeAt(0)) % 360
  return `hsl(${hash} 72% 52%)`
}

function providerInitial(provider: string) {
  if (provider === 'Anthropic') return 'A'
  if (provider === 'Google') return 'G'
  if (provider === 'xAI') return 'x'
  if (provider === 'DeepSeek') return 'D'
  if (provider === '阿里云百炼' || provider === 'Bailian') return 'Q'
  if (provider === 'OpenAI' || provider === 'OpenAI Official' || provider === 'Azure OpenAI') return 'O'
  return provider.replace(/[^\p{L}\p{N}]/gu, '').slice(0, 1).toUpperCase() || 'AI'
}

onMounted(loadPrices)
</script>

<template>
  <div class="market-page">
    <div class="market-heading">
      <div>
        <div class="eyebrow"><el-icon><Collection /></el-icon> MODEL MARKET</div>
        <h1>模型广场</h1>
        <p>{{ auth.isAdmin ? '管理平台模型价格，调整后将同步展示给所有普通用户。' : '浏览主流 AI 服务商公开 API 模型，快速比较能力与平台价格。' }}</p>
      </div>
      <div class="price-note">
        <strong>{{ auth.isAdmin ? '管理员价格管理模式' : onlinePricing ? `联网价格 · ${models.length} 个模型` : '本地价格快照' }}</strong>
        <span v-if="auth.isAdmin">联网基础价可由管理员覆盖，修改后保存到数据库并同步展示给所有用户</span>
        <span v-else-if="onlinePricing">来自 jeniya.chat，按最低可用分组倍率计算；每 10 分钟更新<span v-if="syncTimeLabel"> · {{ syncTimeLabel }}</span></span>
        <span v-else>联网价格源暂时不可用，当前展示项目内置价格</span>
      </div>
    </div>

    <div class="market-layout">
      <aside class="filter-panel">
        <div class="filter-block">
          <div class="filter-title">供应商</div>
          <div class="provider-scroll-shell">
            <button type="button" class="provider-scroll-button" :aria-label="displayText('向上浏览供应商')" :title="displayText('向上浏览供应商')" @click="scrollProviders('up')">
              <el-icon><ArrowUpBold /></el-icon><span>{{ displayText('向上') }}</span>
            </button>
            <div ref="providerListRef" class="provider-list">
              <button
                v-for="provider in providers"
                :key="provider"
                class="filter-item"
                :class="{ active: selectedProvider === provider }"
                @click="selectedProvider = provider"
              >
                <span v-if="provider !== '全部供应商'" class="provider-dot" :style="{ background: providerColor(provider) }"></span>
                <span v-else class="all-dot">AI</span>
                <span>{{ provider }}</span>
                <b>{{ countProvider(provider) }}</b>
              </button>
            </div>
            <button type="button" class="provider-scroll-button" :aria-label="displayText('向下浏览供应商')" :title="displayText('向下浏览供应商')" @click="scrollProviders('down')">
              <el-icon><ArrowDownBold /></el-icon><span>{{ displayText('向下') }}</span>
            </button>
          </div>
        </div>

        <div class="filter-block">
          <div class="filter-title">模型类型</div>
          <button
            v-for="type in modelTypes"
            :key="type"
            class="filter-item type-item"
            :class="{ active: selectedType === type }"
            @click="selectedType = type"
          >
            <span>{{ type }}</span><b>{{ countType(type) }}</b>
          </button>
        </div>

        <div class="featured-filter">
          <div><strong>只看精选</strong><span>推荐演示模型</span></div>
          <el-switch v-model="featuredOnly" />
        </div>

        <div class="source-box">
          <strong>价格来源</strong>
          <p>{{ onlinePricing ? '实时同步 jeniya.chat 价格数据，点击卡片右上角可查看原页面。' : '来自项目内置价格快照，点击卡片右上角可查看供应商文档。' }}</p>
        </div>
      </aside>

      <main v-loading="pricingLoading" class="models-area">
        <div class="market-toolbar">
          <el-input v-model="keyword" :prefix-icon="Search" clearable placeholder="搜索模型名称、供应商或能力标签" />
          <div class="result-count"><strong>{{ filteredModels.length }}</strong> 个模型</div>
        </div>

        <div v-if="filteredModels.length" class="model-grid">
          <article v-for="item in pagedModels" :key="item.id" class="model-card">
            <div class="card-top">
              <div class="provider-logo" :style="{ '--provider-color': providerColor(item.provider) }">
                {{ providerInitial(item.provider) }}
              </div>
              <el-tag size="small" effect="plain" round>{{ displayText(item.type) }}</el-tag>
              <button class="source-link" title="查看官方价格" @click="openSource(item.provider)"><el-icon><Link /></el-icon></button>
            </div>

            <h2>{{ item.name }}</h2>
            <div class="provider-name">{{ displayText(item.provider) }}</div>
            <p class="description">{{ displayDescription(item) }}</p>

            <div class="pricing">
              <div class="pricing-head"><span>{{ displayText('计费明细') }}</span><small>{{ displayText('M = 100 万 Token') }}</small></div>
              <div class="price-row"><span>{{ displayText('输入价格') }}</span><strong>{{ displayText(item.input) }}</strong></div>
              <div class="price-row"><span>{{ displayText('缓存价格') }}</span><strong>{{ displayText(item.cached) }}</strong></div>
              <div class="price-row"><span>{{ displayText('输出价格') }}</span><strong>{{ displayText(item.output) }}</strong></div>
              <div class="price-row extra-price"><span>{{ displayText('其他计费') }}</span><strong :title="item.extra">{{ displayText(item.extra) }}</strong></div>
            </div>

            <div class="model-tags">
              <span v-for="tag in item.tags" :key="tag">{{ displayText(tag) }}</span>
              <b v-if="item.featured">精选</b>
              <b v-if="overriddenCodes.has(item.id)" class="adjusted-tag">已调整</b>
            </div>

            <div class="card-actions" :class="{ admin: auth.isAdmin }">
              <el-button :icon="CopyDocument" title="复制模型名称" @click="copyModel(item.name)" />
              <el-button type="primary" plain :icon="Promotion" @click="tryModel(item)">立即体验</el-button>
              <el-button v-if="auth.isAdmin" type="warning" plain :icon="EditPen" title="调整价格" @click="editPrice(item)" />
            </div>
          </article>
        </div>
        <el-empty v-else description="没有找到符合条件的模型" />
        <el-pagination
          v-if="filteredModels.length > pageSize"
          v-model:current-page="currentPage"
          class="market-pagination"
          background
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="filteredModels.length"
        />
      </main>
    </div>

    <el-dialog v-model="priceDialogVisible" width="520px" class="price-dialog" destroy-on-close>
      <template #header>
        <div class="dialog-heading">
          <div class="dialog-icon"><el-icon><EditPen /></el-icon></div>
          <div><strong>调整模型价格</strong><span>{{ editingModel?.name }} · {{ editingModel?.provider }}</span></div>
        </div>
      </template>
      <el-alert title="价格字段支持直接填写展示内容，例如 $2.50/M、¥12.00/M 或 $0.10/秒。留空则不展示该项。" type="info" :closable="false" show-icon />
      <el-form label-position="top" class="price-form">
        <div class="price-form-grid">
          <el-form-item label="输入价格"><el-input v-model="priceForm.inputPrice" maxlength="120" placeholder="例如 $2.50/M" /></el-form-item>
          <el-form-item label="缓存价格"><el-input v-model="priceForm.cachedPrice" maxlength="120" placeholder="例如 $0.25/M" /></el-form-item>
          <el-form-item label="输出价格"><el-input v-model="priceForm.outputPrice" maxlength="120" placeholder="例如 $15.00/M" /></el-form-item>
          <el-form-item label="其他计费"><el-input v-model="priceForm.extraPrice" maxlength="500" placeholder="例如 图片 $0.07/张" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="editingModel && overriddenCodes.has(editingModel.id)" type="danger" plain :loading="savingPrice" @click="resetPrice">恢复默认</el-button>
          <span></span>
          <el-button @click="priceDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingPrice" @click="savePrice">保存价格</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.market-page { min-height: calc(100vh - 134px); }
.market-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; margin-bottom: 20px; }
.eyebrow { display: flex; align-items: center; gap: 7px; margin-bottom: 8px; color: var(--brand); font-size: 11px; font-weight: 800; letter-spacing: 1.7px; }
.market-heading h1 { margin: 0; font-size: 28px; letter-spacing: -.6px; }
.market-heading p { margin: 8px 0 0; color: var(--muted); font-size: 14px; }
.price-note { display: flex; max-width: 430px; flex-direction: column; gap: 5px; padding: 11px 15px; border: 1px solid #dfe5ff; border-radius: 11px; background: #f5f7ff; color: #5965e9; font-size: 12px; text-align: right; }
.price-note span { color: #79839a; font-size: 11px; }
.market-layout { display: grid; grid-template-columns: 205px minmax(0, 1fr); gap: 18px; }
.filter-panel { position: sticky; top: 94px; height: fit-content; padding: 17px 12px; border: 1px solid var(--border); border-radius: 15px; background: #fff; box-shadow: 0 7px 24px rgba(26,39,72,.04); }
.filter-block + .filter-block { margin-top: 22px; }
.filter-title { margin-bottom: 8px; padding: 0 8px; color: #707a8f; font-size: 12px; font-weight: 700; }
.provider-scroll-shell { display: grid; grid-template-rows: 28px minmax(0, 1fr) 28px; gap: 5px; }
.provider-list { max-height: 260px; overflow-x: hidden; overflow-y: auto; padding-right: 3px; scroll-behavior: smooth; scrollbar-color: #b9c4f3 #f0f3fb; scrollbar-width: thin; }
.provider-list::-webkit-scrollbar { width: 5px; }.provider-list::-webkit-scrollbar-track { border-radius: 99px; background: #f0f3fb; }.provider-list::-webkit-scrollbar-thumb { border-radius: 99px; background: #b9c4f3; }
.provider-scroll-button { display: flex; width: 100%; align-items: center; justify-content: center; gap: 5px; border: 1px solid #e4e9fb; border-radius: 8px; background: linear-gradient(180deg, #fbfcff 0%, #f2f5ff 100%); color: #6573ce; font-size: 10px; font-weight: 700; cursor: pointer; transition: border-color .18s ease, background .18s ease, color .18s ease, transform .18s ease; }
.provider-scroll-button:hover { transform: translateY(-1px); border-color: #bfc9ff; background: #eaf0ff; color: #4054d8; }.provider-scroll-button:active { transform: translateY(0); }.provider-scroll-button:focus-visible { outline: 2px solid #7b89ff; outline-offset: 2px; }
.filter-item { display: grid; width: 100%; min-height: 37px; align-items: center; padding: 0 9px; border: 0; border-radius: 8px; background: transparent; color: #4c576d; cursor: pointer; grid-template-columns: 21px 1fr auto; gap: 7px; text-align: left; }
.filter-item:hover { background: #f5f7fb; }.filter-item.active { background: #eaf3ff; color: #287fea; font-weight: 700; }
.filter-item b { font-size: 11px; font-weight: 650; }.provider-dot { width: 10px; height: 10px; border-radius: 50%; }.all-dot { display: grid; width: 19px; height: 19px; place-items: center; border-radius: 6px; background: #5b67f1; color: #fff; font-size: 8px; font-weight: 800; }
.type-item { grid-template-columns: 1fr auto; padding-left: 10px; }
.featured-filter { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-top: 22px; padding: 14px 9px; border-top: 1px solid #eef0f5; border-bottom: 1px solid #eef0f5; }
.featured-filter div { display: flex; flex-direction: column; gap: 3px; }.featured-filter strong { color: #445067; font-size: 12px; }.featured-filter span { color: #98a0af; font-size: 10px; }
.source-box { margin-top: 15px; padding: 12px; border-radius: 10px; background: #f7f8fb; }.source-box strong { color: #586176; font-size: 12px; }.source-box p { margin: 6px 0 0; color: #929aaa; font-size: 10px; line-height: 1.6; }
.models-area { min-width: 0; }
.market-toolbar { display: flex; align-items: center; gap: 14px; margin-bottom: 15px; }.market-toolbar :deep(.el-input) { flex: 1; }.market-toolbar :deep(.el-input__wrapper) { min-height: 43px; border-radius: 11px; box-shadow: 0 0 0 1px #e5e9f0 inset; }
.result-count { flex: 0 0 auto; color: #8a93a6; font-size: 12px; }.result-count strong { color: var(--brand); font-size: 15px; }
.model-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
.model-card { position: relative; display: flex; min-height: 465px; flex-direction: column; padding: 17px; border: 1px solid #e6eaf1; border-radius: 15px; background: #fff; box-shadow: 0 5px 18px rgba(31,45,77,.035); transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease; }
.model-card:hover { transform: translateY(-3px); border-color: #cfd6ff; box-shadow: 0 15px 30px rgba(31,45,77,.09); }
.card-top { display: flex; align-items: flex-start; gap: 8px; }.card-top :deep(.el-tag) { margin-left: auto; color: #68738a; }
.provider-logo { display: grid; width: 48px; height: 48px; place-items: center; border: 1px solid #edf0f4; border-radius: 13px; background: color-mix(in srgb, var(--provider-color) 8%, white); color: var(--provider-color); font-size: 22px; font-weight: 850; }
.source-link { display: grid; width: 25px; height: 25px; place-items: center; border: 0; background: transparent; color: #9da5b3; cursor: pointer; }.source-link:hover { color: var(--brand); }
.model-card h2 { min-height: 43px; margin: 15px 0 3px; overflow-wrap: anywhere; color: #273046; font-size: 16px; line-height: 1.35; }
.provider-name { color: #8490a7; font-size: 11px; }.description { display: -webkit-box; min-height: 58px; margin: 12px 0 13px; overflow: hidden; color: #79849a; font-size: 12px; line-height: 1.65; -webkit-box-orient: vertical; -webkit-line-clamp: 3; }
.pricing { display: flex; min-height: 142px; flex-direction: column; gap: 7px; padding: 10px 12px 11px; border: 1px solid #edf0f5; border-radius: 10px; background: #f8f9fc; }
.pricing-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; padding-bottom: 7px; border-bottom: 1px solid #e9edf4; color: #536078; font-size: 10px; font-weight: 750; }.pricing-head small { color: #a0a8b7; font-size: 9px; font-weight: 500; }.pricing .price-row { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; color: #8992a4; font-size: 10px; }.pricing strong { max-width: 65%; color: #43506a; font-size: 11px; text-align: right; overflow-wrap: anywhere; }.pricing .extra-price strong { color: #d47824; }
.model-tags { display: flex; min-height: 45px; flex-wrap: wrap; align-content: flex-start; gap: 5px; padding: 12px 0 9px; }.model-tags span, .model-tags b { height: fit-content; padding: 3px 7px; border-radius: 999px; background: #f2f4f7; color: #778197; font-size: 9px; font-weight: 500; }.model-tags b { background: #eafaf4; color: #1da879; }.model-tags .adjusted-tag { background: #fff2dc; color: #d98518; }
.card-actions { display: grid; margin-top: auto; grid-template-columns: 40px 1fr; gap: 8px; }.card-actions :deep(.el-button) { width: 100%; height: 36px; margin: 0; border-radius: 9px; }
.card-actions.admin { grid-template-columns: 40px 1fr 40px; }
.market-pagination { justify-content: center; margin-top: 22px; }
.dialog-heading { display: flex; align-items: center; gap: 11px; }.dialog-heading > div:last-child { display: flex; flex-direction: column; gap: 4px; }.dialog-heading strong { color: #263148; font-size: 17px; }.dialog-heading span { color: #8992a5; font-size: 11px; }.dialog-icon { display: grid; width: 40px; height: 40px; place-items: center; border-radius: 11px; background: #fff1dc; color: #e58a16; font-size: 20px; }.price-form { margin-top: 18px; }.price-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 2px 14px; }.dialog-footer { display: flex; align-items: center; }.dialog-footer span { flex: 1; }
@media (max-width: 1280px) { .model-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 900px) { .market-heading { align-items: flex-start; flex-direction: column; }.price-note { text-align: left; }.market-layout { grid-template-columns: 1fr; }.filter-panel { position: static; display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }.filter-block + .filter-block { margin-top: 0; }.featured-filter, .source-box { display: none; } }
@media (max-width: 620px) { .model-grid { grid-template-columns: 1fr; }.filter-panel { grid-template-columns: 1fr; }.model-card { min-height: 390px; }.price-form-grid { grid-template-columns: 1fr; } }
</style>
