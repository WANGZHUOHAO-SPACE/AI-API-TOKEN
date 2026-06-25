import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure(proxy) {
          proxy.on('error', (_error, _request, response) => {
            if (!response.headersSent) {
              response.writeHead(503, { 'Content-Type': 'application/json; charset=utf-8' })
            }
            response.end(JSON.stringify({
              code: 503,
              message: '后端服务未启动，请先启动 MySQL、Redis 和 Spring Boot',
              data: null,
            }))
          })
        },
      },
    },
  },
})
