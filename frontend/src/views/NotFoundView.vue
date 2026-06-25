<template>
  <div class="not-found"><div><strong>404</strong><h1>页面不存在</h1><p>你访问的页面可能已移动或删除。</p><el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button></div></div>
</template>
<style scoped>.not-found{display:grid;min-height:100vh;place-items:center;text-align:center;background:#f4f7fb}.not-found strong{color:var(--brand);font-size:82px;line-height:1}.not-found h1{margin:12px 0 8px}.not-found p{margin:0 0 22px;color:var(--muted)}</style>
