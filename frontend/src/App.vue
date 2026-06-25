<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted } from 'vue'
import en from 'element-plus/es/locale/lang/en'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { installDomTranslation, useLanguage } from '@/i18n'

const { locale } = useLanguage()
const elementLocale = computed(() => locale.value === 'en' ? en : zhCn)
let stopTranslation: (() => void) | undefined

onMounted(() => { stopTranslation = installDomTranslation() })
onBeforeUnmount(() => stopTranslation?.())
</script>

<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>
