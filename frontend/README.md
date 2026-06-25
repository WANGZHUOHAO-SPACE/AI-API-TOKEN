# KeyBridge AI Frontend

Vue 3 + TypeScript + Vite + Element Plus 管理后台。

## 页面

- 登录与注册
- 首页仪表盘
- API Key 管理
- AI 调用测试
- 用户调用日志
- 管理员用户管理
- 管理员全站日志

## 启动

需要 Node.js 20 或更高版本：

```bash
cd frontend
npm install
npm run dev
```

开发服务器运行在 `http://localhost:5173`，Vite 会将 `/api` 请求代理到 `http://localhost:8080`。

生产环境可以设置：

```text
VITE_API_BASE_URL=https://your-api.example.com
```

然后执行：

```bash
npm run build
```
