import { request } from './http'
import type {
  ChatResult,
  Credential,
  CredentialPayload,
  LoginResult,
  ModelPriceOverride,
  PageResult,
  Provider,
  PaymentConfig,
  RechargeOrder,
  RequestLog,
  StatisticsBreakdown,
  StatisticsSummary,
  TrendPoint,
  User,
  UserOverview,
  Wallet,
  ExchangeRate,
  ExternalPricingPayload,
  SystemHealth,
} from '@/types'

export const systemApi = {
  health: () => request<SystemHealth>({
    url: '/api/health',
    skipAuth: true,
    ignoreUnauthorized: true,
    silent: true,
  }),
}

export const authApi = {
  login: (data: { username: string; password: string }) =>
    request<LoginResult>({ url: '/api/auth/login', method: 'POST', data }),
  register: (data: { username: string; password: string; nickname: string }) =>
    request<User>({ url: '/api/auth/register', method: 'POST', data }),
  profile: () => request<User>({ url: '/api/auth/profile' }),
  logout: () => request<void>({ url: '/api/auth/logout', method: 'POST' }),
}

export const providerApi = {
  list: () => request<PageResult<Provider>>({ url: '/api/providers', params: { page: 1, size: 100 } }),
}

export const modelPriceApi = {
  list: () => request<ModelPriceOverride[]>({
    url: '/api/model-prices',
    silent: true,
    skipAuth: true,
    ignoreUnauthorized: true,
  }),
  update: (modelCode: string, data: Pick<ModelPriceOverride, 'inputPrice' | 'cachedPrice' | 'outputPrice' | 'extraPrice'>) =>
    request<ModelPriceOverride>({ url: `/api/model-prices/${encodeURIComponent(modelCode)}`, method: 'PUT', data }),
  reset: (modelCode: string) =>
    request<void>({ url: `/api/model-prices/${encodeURIComponent(modelCode)}`, method: 'DELETE' }),
}

export const publicPricingApi = {
  list: () => request<ExternalPricingPayload>({
    url: '/api/public/pricing',
    skipAuth: true,
    ignoreUnauthorized: true,
    silent: true,
    timeout: 20000,
  }),
}

export const credentialApi = {
  list: (params: { page: number; size: number; providerId?: number }) =>
    request<PageResult<Credential>>({ url: '/api/credentials', params }),
  create: (data: CredentialPayload) => request<Credential>({ url: '/api/credentials', method: 'POST', data }),
  update: (id: number, data: CredentialPayload) =>
    request<Credential>({ url: `/api/credentials/${id}`, method: 'PUT', data }),
  remove: (id: number) => request<void>({ url: `/api/credentials/${id}`, method: 'DELETE' }),
}

export const proxyApi = {
  chat: (data: { provider: string; model: string; keyId: number; prompt: string }) =>
    request<ChatResult>({ url: '/api/proxy/chat', method: 'POST', data, timeout: 120000 }),
}

export const logApi = {
  list: (params: Record<string, unknown>) =>
    request<PageResult<RequestLog>>({ url: '/api/request-logs', params }),
}

export const statisticsApi = {
  summary: (days = 7) => request<StatisticsSummary>({ url: '/api/statistics/summary', params: { days } }),
  trend: (days = 7) => request<TrendPoint[]>({ url: '/api/statistics/trend', params: { days } }),
  providers: (days = 7) => request<StatisticsBreakdown[]>({ url: '/api/statistics/providers', params: { days } }),
  models: (days = 7) => request<StatisticsBreakdown[]>({ url: '/api/statistics/models', params: { days } }),
}

export const walletApi = {
  wallet: () => request<Wallet>({ url: '/api/wallet' }),
  exchangeRate: () => request<ExchangeRate>({ url: '/api/wallet/exchange-rate' }),
  paymentConfig: () => request<PaymentConfig>({ url: '/api/wallet/payment-config' }),
  createRecharge: (data: { amountUsd: number; paymentMethod: string }) =>
    request<RechargeOrder>({ url: '/api/wallet/recharge-orders', method: 'POST', data }),
  orders: (params: { page: number; size: number }) =>
    request<PageResult<RechargeOrder>>({ url: '/api/wallet/recharge-orders', params }),
  adminOrders: (params: { page: number; size: number; status?: string }) =>
    request<PageResult<RechargeOrder>>({ url: '/api/wallet/admin/recharge-orders', params }),
  confirm: (id: number) => request<void>({ url: `/api/wallet/admin/recharge-orders/${id}/confirm`, method: 'PATCH' }),
  reject: (id: number) => request<void>({ url: `/api/wallet/admin/recharge-orders/${id}/reject`, method: 'PATCH' }),
}

export const userApi = {
  overview: () => request<UserOverview>({ url: '/api/users/overview', silent: true }),
  list: (params: { page: number; size: number; username?: string }) =>
    request<PageResult<User>>({ url: '/api/users', params }),
  updateStatus: (id: number, status: number) =>
    request<void>({ url: `/api/users/${id}/status`, method: 'PATCH', params: { status } }),
  promoteToAdmin: (id: number) =>
    request<void>({ url: `/api/users/${id}/role`, method: 'PATCH', params: { role: 'ADMIN' } }),
}
