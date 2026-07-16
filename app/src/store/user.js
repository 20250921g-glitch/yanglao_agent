import { ref } from 'vue'
import { appInfo } from '@/api'

// 当前登录用户（全局响应式，组件间共享）
export const currentUser = ref(null)

// 启动时从 localStorage 恢复，避免刷新后闪烁
export function loadUserFromCache() {
  try {
    const raw = localStorage.getItem('app_user')
    if (raw) currentUser.value = JSON.parse(raw)
  } catch (e) {
    // 忽略损坏的缓存
  }
}
loadUserFromCache()

// 拉取最新用户信息（force=true 时忽略缓存强制刷新）
export async function fetchUser(force = false) {
  if (currentUser.value && !force) return currentUser.value
  const res = await appInfo()
  currentUser.value = res.data
  localStorage.setItem('app_user', JSON.stringify(res.data))
  return currentUser.value
}

export function clearUser() {
  currentUser.value = null
  localStorage.removeItem('app_user')
  localStorage.removeItem('app_token')
}
