import { useUserStore } from '@/store/user'

/**
 * 按钮级权限指令：v-has="'user:add'"
 * 当当前用户不具备该权限码时，移除挂载的元素（及其子节点）。
 * 超级管理员(ROLE_1)默认拥有全部权限。
 */
export const hasPerm = (code) => {
  const userStore = useUserStore()
  return userStore.hasPerm(code)
}

export default {
  mounted(el, binding) {
    const code = binding.value
    if (code && !hasPerm(code)) {
      // 无权限：移除元素
      if (el.parentNode) el.parentNode.removeChild(el)
      else el.style.display = 'none'
    }
  }
}
