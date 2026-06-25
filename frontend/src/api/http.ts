import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiEnvelope } from '@/types'

interface RequestConfig extends AxiosRequestConfig {
  silent?: boolean
  ignoreUnauthorized?: boolean
  skipAuth?: boolean
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
})

function showError(message: string) {
  ElMessage.closeAll()
  ElMessage({
    message,
    type: 'error',
    grouping: true,
    duration: 3500,
  })
}

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('keybridge_token')
  if (token && !(config as RequestConfig).skipAuth) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '请求失败'
    const silent = Boolean((error.config as RequestConfig | undefined)?.silent)
    const ignoreUnauthorized = Boolean((error.config as RequestConfig | undefined)?.ignoreUnauthorized)
    if (status === 401 && !ignoreUnauthorized) {
      localStorage.removeItem('keybridge_token')
      localStorage.removeItem('keybridge_user')
      if (!location.pathname.startsWith('/console-login')) location.href = '/console-login'
    }
    if (!silent) showError(message)
    return Promise.reject(error)
  },
)

export async function request<T>(config: RequestConfig): Promise<T> {
  const response = await http.request<ApiEnvelope<T>>(config)
  const envelope = response.data
  if (envelope.code < 200 || envelope.code >= 300) {
    if (!config.silent) showError(envelope.message || '请求失败')
    throw new Error(envelope.message)
  }
  return envelope.data
}
