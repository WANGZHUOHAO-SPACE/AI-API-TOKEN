<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  label: string
  value: string | number
  note: string
  icon: Component
  color: string
}>()
</script>

<template>
  <div class="stat-card panel">
    <div class="stat-icon" :style="{ background: `${color}18`, color }"><el-icon :size="21"><component :is="icon" /></el-icon></div>
    <div class="stat-main">
      <span>{{ label }}</span>
      <strong>{{ value }}</strong>
      <small>{{ note }}</small>
    </div>
  </div>
</template>

<style scoped>
.stat-card { display: flex; min-height: 126px; align-items: flex-start; gap: 15px; padding: 20px; }
.stat-icon { display: grid; width: 43px; height: 43px; flex: 0 0 43px; place-items: center; border-radius: 11px; }
.stat-main { display: flex; flex-direction: column; }
.stat-main span { color: var(--muted); font-size: 13px; }
.stat-main strong { margin-top: 8px; color: #192136; font-size: 26px; line-height: 1; letter-spacing: -.5px; }
.stat-main small { margin-top: 9px; color: #9aa2b4; font-size: 11px; }
</style>
