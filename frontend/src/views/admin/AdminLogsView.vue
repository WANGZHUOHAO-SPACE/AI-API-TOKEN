<script setup lang="ts">
import LogTable from '@/components/LogTable.vue'
</script>
<template><div class="page-shell"><div class="page-heading"><div><h1>全站调用日志</h1><p>管理员视角查看所有用户的调用记录与异常请求。</p></div><el-tag type="danger" effect="light">管理员权限</el-tag></div><LogTable show-user /></div></template>
