import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api'
import type { User } from '@/types'

const TOKEN_KEY = 'keybridge_token'
const USER_KEY = 'keybridge_user'

function loadUser(): User | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<User | null>(loadUser())
  const isLoggedIn = computed(() => Boolean(token.value))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(username: string, password: string) {
    const result = await authApi.login({ username, password })
    token.value = result.accessToken
    user.value = result.user
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  async function fetchProfile() {
    user.value = await authApi.profile()
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }

  return { token, user, isLoggedIn, isAdmin, login, fetchProfile, logout }
})
