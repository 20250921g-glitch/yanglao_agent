<template>
  <el-card class="placeholder-card" shadow="never">
    <el-empty :description="''">
      <div class="ph-title">{{ title }}</div>
      <div class="ph-tip">该功能正在开发中，敬请期待</div>
      <el-button type="primary" plain @click="$router.push('/')">返回首页</el-button>
    </el-empty>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta && route.meta.title ? route.meta.title : '功能')
</script>
