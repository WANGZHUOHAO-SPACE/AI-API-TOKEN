<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  role?: 'ADMIN' | 'USER'
  size?: number
  label?: string
}>(), {
  role: 'USER',
  size: 38,
  label: '',
})

const isAdmin = computed(() => props.role === 'ADMIN')
const accessibleLabel = computed(() => props.label || (isAdmin.value ? '管理员头像' : '普通用户头像'))
</script>

<template>
  <span
    class="role-avatar"
    :class="isAdmin ? 'role-admin' : 'role-user'"
    :style="{ '--avatar-size': `${size}px` }"
    role="img"
    :aria-label="accessibleLabel"
    :title="accessibleLabel"
  >
    <svg v-if="isAdmin" viewBox="0 0 64 64" aria-hidden="true">
      <path class="avatar-shape" d="M32 8 50 15v13c0 13-7.2 22.2-18 28-10.8-5.8-18-15-18-28V15L32 8Z" />
      <path class="avatar-detail" d="m23 28 5.7 5.7L41.5 21l3.5 3.5-16.3 16.3L19.5 31.5 23 28Z" />
      <path class="avatar-glint" d="M20 16.5 32 12l12 4.5" />
    </svg>
    <svg v-else viewBox="0 0 64 64" aria-hidden="true">
      <circle class="avatar-shape" cx="32" cy="24" r="11" />
      <path class="avatar-shape" d="M13 54c1.4-12 8.5-19 19-19s17.6 7 19 19H13Z" />
      <path class="avatar-glint" d="M21 47c3-4 6.7-6 11-6s8 2 11 6" />
    </svg>
    <span class="role-badge">{{ isAdmin ? 'A' : 'U' }}</span>
  </span>
</template>

<style scoped>
.role-avatar {
  position: relative;
  display: inline-grid;
  width: var(--avatar-size);
  height: var(--avatar-size);
  flex: 0 0 var(--avatar-size);
  place-items: center;
  border: 2px solid rgba(255,255,255,.92);
  border-radius: 32%;
  color: #fff;
  box-shadow: 0 8px 18px rgba(45,62,130,.2), 0 0 0 1px rgba(103,117,190,.1);
  isolation: isolate;
}
.role-avatar::before { position: absolute; inset: 2px; z-index: -1; border: 1px solid rgba(255,255,255,.2); border-radius: 28%; content: ''; }
.role-admin { background: linear-gradient(145deg, #7c4dff 0%, #a43be7 52%, #e348bb 100%); }
.role-user { background: linear-gradient(145deg, #3e63f4 0%, #477ff8 52%, #22b8e6 100%); }
.role-avatar svg { width: 72%; height: 72%; filter: drop-shadow(0 3px 5px rgba(17,24,68,.22)); }
.avatar-shape { fill: currentColor; }
.avatar-detail { fill: #6840dc; }
.avatar-glint { fill: none; stroke: rgba(255,255,255,.54); stroke-linecap: round; stroke-width: 2; }
.role-badge { position: absolute; right: -4px; bottom: -4px; display: grid; width: 16px; height: 16px; place-items: center; border: 2px solid #fff; border-radius: 50%; background: #17233d; color: #fff; font-size: 7px; font-weight: 900; line-height: 1; }
.role-admin .role-badge { background: #7c3aed; }.role-user .role-badge { background: #147bd1; }
@media (prefers-reduced-motion: no-preference) { .role-avatar { transition: transform .2s ease, box-shadow .2s ease; }.role-avatar:hover { transform: translateY(-1px) scale(1.03); box-shadow: 0 11px 22px rgba(45,62,130,.25), 0 0 0 1px rgba(103,117,190,.12); } }
</style>
