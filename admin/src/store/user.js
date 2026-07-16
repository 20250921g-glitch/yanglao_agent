import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
  const menuIds = ref(JSON.parse(localStorage.getItem('menuIds') || '[]'))
  const perms = ref(JSON.parse(localStorage.getItem('perms') || '[]'))

  const isLoggedIn = computed(() => !!token.value)

  const isSuperAdmin = computed(() => roles.value.includes('ROLE_1'))

  // 超级管理员拥有全部权限；否则按 perms 集合判定
  function hasPerm(code) {
    if (!code) return true
    if (isSuperAdmin.value) return true
    return perms.value.includes(code)
  }

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function setRoles(newRoles) {
    roles.value = newRoles
    localStorage.setItem('roles', JSON.stringify(newRoles))
  }

  function setMenuIds(newMenuIds) {
    menuIds.value = newMenuIds
    localStorage.setItem('menuIds', JSON.stringify(newMenuIds))
  }

  function setPerms(newPerms) {
    perms.value = newPerms || []
    localStorage.setItem('perms', JSON.stringify(perms.value))
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    roles.value = []
    menuIds.value = []
    perms.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('roles')
    localStorage.removeItem('menuIds')
    localStorage.removeItem('perms')
  }

  return { token, userInfo, roles, menuIds, perms, isLoggedIn, isSuperAdmin, hasPerm, setToken, setUserInfo, setRoles, setMenuIds, setPerms, logout }
})
