<template>
  <template v-for="menu in menus" :key="menu.id">
    <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.id.toString()">
      <template #title>
        <el-icon :size="20"><component :is="getIconComponent(menu.icon)" /></el-icon>
        <span>{{ menu.name }}</span>
      </template>
      <MenuTree :menus="menu.children" />
    </el-sub-menu>
    <el-menu-item v-else :index="(menu.path && String(menu.path).trim()) || menu.id.toString()">
      <el-icon :size="20"><component :is="getIconComponent(menu.icon)" /></el-icon>
      <span>{{ menu.name }}</span>
    </el-menu-item>
  </template>
</template>

<script setup>
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { Monitor } from '@element-plus/icons-vue'

const props = defineProps({
  menus: { type: Array, default: () => [] }
})

const getIconComponent = (iconName) => {
  if (!iconName) return Monitor
  return ElementPlusIconsVue[iconName] || Monitor
}
</script>
