export interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
}

export interface SystemHealth {
  status: 'UP' | 'DEGRADED'
  service: string
  database: 'UP' | 'DOWN'
  redis: 'UP' | 'DOWN'
  proxyMode: string
  demoDataEnabled: boolean
  timestamp: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface User {
  id: number
  username: string
  nickname: string
  role: 'ADMIN' | 'USER'
  status: number
  createdAt: string
  updatedAt: string
}

export interface UserOverview {
  totalUsers: number
  activeUsers: number
  adminUsers: number
  regularUsers: number
}

export interface LoginResult {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface Wallet {
  balanceUsd: number
  totalRechargedUsd: number
}

export interface ExchangeRate {
  base: string
  quote: string
  rate: number
  rateDate: string
  fetchedAt: string
  source: string
}

export interface PaymentConfig {
  usdtAddress: string
  usdtNetwork: string
}

export interface RechargeOrder {
  id: number
  orderNo: string
  userId: number
  paymentMethod: 'ALIPAY' | 'WECHAT' | 'USDT'
  amountUsd: number
  payAmount: number
  payCurrency: string
  exchangeRate: number
  paymentNetwork?: string
  paymentAddress?: string
  status: 'PENDING' | 'SUCCESS' | 'REJECTED'
  reviewedBy?: number
  reviewedAt?: string
  createdAt: string
  updatedAt: string
}

export interface Provider {
  id: number
  name: string
  code: string
  baseUrl: string
  protocolType: string
  status: number
  timeoutSeconds: number
  description?: string
}

export interface ModelPriceOverride {
  id: number
  modelCode: string
  inputPrice: string
  cachedPrice: string
  outputPrice: string
  extraPrice: string
  updatedBy: number
  createdAt: string
  updatedAt: string
}

export interface ExternalPricingStep {
  step_size: number
  completion_step_size?: number
  prompt_step_ratio?: number
  completion_step_ratio?: number
  cache_step_ratio?: number
  prompt_thinking_step_ratio?: number
  completion_thinking_step_ratio?: number
}

export interface ExternalPricingModel {
  model_name: string
  description?: string
  cover_image?: string
  tags?: string
  model_type?: string
  vendor_id?: number
  owner_by?: string
  quota_type: number
  model_ratio?: number
  model_price?: number
  completion_ratio?: number
  cache_ratio?: number
  cache_creation_ratio?: number
  cache_creation_5m_ratio?: number
  cache_creation_1h_ratio?: number
  audio_ratio?: number
  audio_completion_ratio?: number
  enable_groups?: string[]
  supported_endpoint_types?: string[]
  step_ratios?: ExternalPricingStep[]
  sort_order?: number
}

export interface ExternalPricingVendor {
  id: number
  name: string
  icon?: string
  description?: string
}

export interface ExternalPricingPayload {
  success: boolean
  data: ExternalPricingModel[]
  vendors: ExternalPricingVendor[]
  group_ratio: Record<string, number>
  group_model_ratio?: Record<string, Record<string, number>>
  usable_group?: Record<string, string>
  source?: string
  syncedAt?: string
}

export interface Credential {
  id: number
  userId: number
  ownerUsername?: string
  providerId: number
  name: string
  maskedKey: string
  priority: number
  weight: number
  status: number
  failureCount: number
  lastCheckedAt?: string
  expiresAt?: string
  createdAt: string
  updatedAt: string
}

export interface CredentialPayload {
  providerId: number
  name: string
  apiKey?: string
  priority: number
  weight: number
  status: number
  expiresAt?: string
}

export interface ChatResult {
  requestId: string
  mode: string
  provider: string
  model: string
  content: string
  finishReason: string
  durationMs: number
  usage: {
    inputTokens: number
    outputTokens: number
    totalTokens: number
  }
}

export interface RequestLog {
  id: number
  requestId: string
  userId: number
  providerId?: number
  providerCode: string
  modelName: string
  credentialId?: number
  inputTokens: number
  outputTokens: number
  totalTokens: number
  durationMs: number
  statusCode: number
  success: number
  errorMessage?: string
  createdAt: string
}

export interface StatisticsSummary {
  requestCount: number
  successCount: number
  failureCount: number
  totalTokens: number
  totalDurationMs: number
  averageDurationMs: number
  successRate: number
}

export interface TrendPoint {
  date: string
  requestCount: number
  successCount: number
  failureCount: number
  totalTokens: number
  averageDurationMs: number
}

export interface StatisticsBreakdown {
  name: string
  requestCount: number
  successCount: number
  failureCount: number
  totalTokens: number
  averageDurationMs: number
}
