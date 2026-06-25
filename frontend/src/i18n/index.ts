import { nextTick, readonly, ref, watch } from 'vue'

export type AppLocale = 'zh' | 'en'

const STORAGE_KEY = 'keybridge_language'
const savedLocale = localStorage.getItem(STORAGE_KEY)
const locale = ref<AppLocale>(savedLocale === 'en' ? 'en' : 'zh')

const translations: Record<string, string> = {
  '首页': 'Home',
  '控制台': 'Console',
  '模型广场': 'Model Market',
  '联系我们': 'Contact Us',
  '文章资讯': 'News',
  'API 文档': 'API Docs',
  '登录': 'Sign In',
  '浅色': 'Light',
  '深色': 'Dark',
  '跟随系统': 'System',
  '主题模式': 'Theme Mode',
  '切换为英文': 'Switch to English',
  '主导航': 'Main Navigation',
  '统一 AI API 平台': 'Unified AI API Platform',
  '解锁未来': 'Unlock the Future',
  'AI 生产力': 'AI Productivity',
  '聚合全球顶级模型，提供最稳定的': 'Connect leading AI models through a reliable',
  '企业级 API 访问通道。': 'enterprise-grade API gateway.',
  '全球用户': 'Global Users',
  'API 调用': 'API Calls',
  'AI 模型': 'AI Models',
  '全球服务': 'Global Service',
  '覆盖 8 个核心区域，20 万+客户信赖，全年稳定运行': 'Serving 8 core regions with reliable year-round availability.',
  '企业级安全': 'Enterprise Security',
  'AES 加密存储、JWT 身份鉴权，保障每一次 API 调用': 'AES encryption and JWT authentication protect every API call.',
  '统一转发': 'Unified Proxy',
  '统一封装多家模型接口，降低课程演示和二次开发成本': 'Wrap multiple model APIs behind one interface for demos and development.',
  '控制台入口': 'Console Entry',
  '点击进入独立登录页面，登录后可管理 API Key、测试模型调用、查看调用日志与余额。': 'Open the standalone sign-in page to manage API keys, test models, review logs and balance.',
  '进入控制台': 'Enter Console',
  '先看模型广场': 'View Model Market First',
  '普通用户：demo_user / demo123456': 'User: demo_user / demo123456',
  '管理员：admin / admin123456': 'Admin: admin / admin123456',
  '周一至周日 10:00-21:00': 'Monday to Sunday 10:00-21:00',
  '欢迎回来': 'Welcome Back',
  '登录 KeyBridge AI 控制台': 'Sign in to the KeyBridge AI console',
  '用户名': 'Username',
  '密码': 'Password',
  '请输入用户名': 'Please enter your username',
  '请输入密码': 'Please enter your password',
  '登录控制台': 'Sign In',
  '还没有账号？': "Don't have an account?",
  '立即注册': 'Create Account',
  '演示账号快捷登录': 'Demo Quick Login',
  '正在登录...': 'Signing in...',
  '普通用户': 'Regular User',
  '管理员': 'Administrator',
  '系统管理员': 'System Administrator',
  '管理个人 Key 与调用记录': 'Manage personal keys and usage logs',
  '查看全站数据与用户管理': 'View platform data and manage users',
  '快捷入口需在后端启用演示数据后使用。': 'Demo data must be enabled on the backend for quick login.',
  '登录成功': 'Signed in successfully',
  '请先登录后进入控制台': 'Please sign in to open the console',
  '简易-API中转站-专业的AI大模型API接口中转站平台API供应商': 'Simple API Relay Platform for Professional AI Model API Access',
  '更强模型': 'Stronger Models',
  '更低价格': 'Lower Prices',
  '更易落地': 'Easier Deployment',
  '致力于为开发者提供快速、便捷的 Web API 接口调用方案，打造稳定且易于使用的 API 接口平台，一站式集成几乎所有 AI 大模型。': 'Built for developers who need fast and convenient Web API access, with a stable and easy-to-use platform that integrates nearly all major AI models.',
  '安全 AES 加密': 'Secure AES Encryption',
  '统一转发链路': 'Unified Proxy Flow',
  '多模型接入': 'Multi-Model Access',
  '用户登录': 'User Login',
  '密码登录': 'Password Login',
  '用户名或邮箱': 'Username or Email',
  '请输入您的用户名或邮箱地址': 'Enter your username or email address',
  '请输入您的密码': 'Enter your password',
  '我已阅读并同意': 'I have read and agree to',
  '用户协议': 'User Agreement',
  '《用户协议》': 'User Agreement',
  '忘记密码?': 'Forgot Password?',
  '没有账户？': "Don't have an account?",
  '注册': 'Register',
  '用户协议页面正在建设中': 'The user agreement page is coming soon',
  '忘记密码功能正在建设中': 'Password recovery is coming soon',
  '请先阅读并同意用户协议': 'Please agree to the user agreement first',
  '模型广场正在建设中': 'The model market is coming soon',
  '文章资讯正在建设中': 'The news section is coming soon',
  '该功能正在建设中': 'This section is coming soon',
  '公开浏览模式': 'Public Browsing Mode',
  '登录后可管理 API Key 并发起模型调用。': 'Sign in to manage API keys and make model calls.',
  '登录后即可体验模型调用': 'Sign in to try model calls',
  '项目咨询、接口接入和演示支持，请通过以下方式联系。': 'Contact us for project, API integration and demo support.',
  '联系邮箱': 'Email',
  '服务时间': 'Service Hours',
  '周一至周五 09:00-18:00': 'Mon-Fri 09:00-18:00',

  '创建账号': 'Create Account',
  '创建安全账号，开始管理你的 AI API 密钥与调用记录': 'Create a secure account to manage your AI API keys and invocation records',
  '开始管理你的 AI API 密钥与调用记录': 'Start managing your AI API keys and invocation records',
  '昵称': 'Display Name',
  '确认密码': 'Confirm Password',
  '注册账号': 'Create Account',
  '已有账号？': 'Already have an account?',
  '返回登录': 'Back to Sign In',
  '请输入昵称': 'Please enter a display name',
  '请再次输入密码': 'Please confirm your password',
  '两次输入的密码不一致': 'The passwords do not match',
  '注册成功，请登录': 'Account created. Please sign in.',
  '注册成功，已自动登录': 'Account created and signed in successfully',
  '登录账号（用户名）': 'Login Account (Username)',
  '例如 student2026': 'Example: student2026',
  '用于登录且注册后不可随意修改，请牢记该账号。': 'Used for sign-in and cannot be freely changed after registration.',
  '例如 软件工程小王': 'Example: Software Student',
  '仅用于控制台展示，可以使用中文，但不能填写纯数字。': 'Shown only in the console. It cannot contain numbers only.',
  '安全密码': 'Secure Password',
  '请输入高强度密码': 'Enter a strong password',
  '长度达到 10-64 位': '10-64 characters long',
  '包含大写和小写字母': 'Contains uppercase and lowercase letters',
  '至少包含一个数字': 'Contains at least one number',
  '至少包含一个特殊字符': 'Contains at least one special character',
  '不含用户名和常见弱密码': 'Does not contain the username or common weak passwords',
  '未设置': 'Not Set',
  '很弱': 'Very Weak',
  '较弱': 'Weak',
  '一般': 'Fair',
  '较强': 'Strong',
  '很强': 'Very Strong',
  '请再次输入安全密码': 'Enter the secure password again',
  '安全提示': 'Security Tip',
  '不要使用姓名、手机号、生日或与其他网站相同的密码。': 'Do not reuse names, phone numbers, birthdays or passwords from other websites.',
  '请输入登录账号': 'Please enter a login account',
  '6-20 位，字母开头，至少包含一个数字': 'Use 6-20 characters, start with a letter and include at least one number',
  '不能使用系统保留名称作为账号开头': 'The account cannot start with a reserved system name',
  '2-20 位中英文、数字、_ 或 -，不能为纯数字': 'Use 2-20 letters, numbers, _ or -. Numbers-only names are not allowed',
  '密码必须同时包含大小写字母、数字和特殊字符': 'Password must include uppercase, lowercase, numbers and special characters',
  '密码不能包含用户名、弱密码片段或三个连续相同字符': 'Password cannot contain the username, weak password fragments or three repeated characters',
  '两次密码不一致': 'The passwords do not match',
  '用户名用于登录，注册后不可修改': 'Your username is used to sign in and cannot be changed later',
  '昵称将显示在控制台右上角': 'Your display name appears in the console header',
  '密码强度': 'Password Strength',
  '弱': 'Weak',
  '中': 'Medium',
  '强': 'Strong',
  '至少 8 位，包含大小写字母、数字和特殊字符': 'Use at least 8 characters with uppercase, lowercase, numbers and symbols',
  '我已阅读并同意平台使用规范与隐私说明': 'I agree to the platform terms and privacy notice',
  '请阅读并同意平台使用规范': 'Please agree to the platform terms',

  '首页仪表盘': 'Dashboard',
  'API Key 管理': 'API Key Management',
  'AI 调用测试': 'AI Playground',
  '调用日志': 'Invocation Logs',
  '模型与价格': 'Models & Pricing',
  '充值订单': 'Recharge Orders',
  '用户管理': 'User Management',
  '全站日志': 'Global Logs',
  '收起菜单': 'Collapse Menu',
  'AI API 密钥管理与统一转发平台': 'AI API Key Management and Unified Proxy Platform',
  '退出登录': 'Sign Out',
  '确定退出当前账号吗？': 'Are you sure you want to sign out?',
  '警告': 'Warning',
  '确定': 'Confirm',
  '取消': 'Cancel',
  '关闭': 'Close',
  '保存': 'Save',
  '删除': 'Delete',
  '编辑': 'Edit',
  '详情': 'Details',
  '查询': 'Search',
  '重置': 'Reset',
  '刷新': 'Refresh',
  '操作': 'Actions',
  '状态': 'Status',
  '创建时间': 'Created At',
  '注册时间': 'Registered At',
  '开始时间': 'Start Time',
  '结束时间': 'End Time',
  '至': 'to',
  '成功': 'Success',
  '失败': 'Failed',
  '正常': 'Active',
  '已禁用': 'Disabled',
  '启用': 'Enabled',
  '停用': 'Disabled',
  '禁用': 'Disable',
  '无': 'None',
  '请求失败': 'Request failed',
  '服务器内部错误': 'Internal server error',
  '请先登录': 'Please sign in first',
  '请先登录或重新登录': 'Please sign in or sign in again',
  '登录状态无效': 'Invalid login session',
  '用户名或密码错误': 'Incorrect username or password',
  '账号已被禁用': 'This account has been disabled',
  '用户名已存在': 'Username already exists',
  '用户不存在': 'User not found',
  '无权执行此操作': 'You do not have permission to perform this action',
  '请求参数错误': 'Invalid request parameters',
  '请求过于频繁': 'Too many requests',
  'API Key 不存在': 'API key not found',
  'API Key 已被禁用': 'API key is disabled',
  'API Key 已过期': 'API key has expired',
  'API Key 与指定服务商不匹配': 'The API key does not match the selected provider',
  '服务商不存在': 'Provider not found',
  '服务商已停用': 'Provider is disabled',
  '当前仅支持 OpenAI Compatible 协议': 'Only the OpenAI Compatible protocol is currently supported',
  '统一转发内部错误': 'Unified proxy internal error',
  '限流服务暂不可用': 'Rate limiting service is temporarily unavailable',
  '限流服务返回异常结果': 'Rate limiting service returned an invalid result',
  '充值订单不存在': 'Recharge order not found',
  '该订单已处理，请勿重复操作': 'This order has already been processed',
  '余额入账失败': 'Failed to credit the balance',
  '不支持的充值方式': 'Unsupported recharge method',
  '原密码错误': 'Current password is incorrect',
  '指定的模型不存在': 'The selected model was not found',
  '平台令牌不存在': 'Platform token not found',
  '请求日志不存在': 'Invocation log not found',
  '上游密钥加密失败': 'Failed to encrypt the upstream key',
  '上游密钥解密失败': 'Failed to decrypt the upstream key',
  '无法调用 OpenAI Compatible API': 'Unable to call the OpenAI Compatible API',
  '上游调用被中断': 'The upstream call was interrupted',
  '用户名须为6到20位，以字母开头，至少包含一个数字，只能使用字母、数字和下划线': 'Username must be 6-20 characters, start with a letter, include a number and use only letters, numbers or underscores',
  '昵称须为2到20位，可使用中英文、数字、下划线或短横线，且不能为纯数字': 'Display name must be 2-20 characters and cannot contain numbers only',
  '密码须为10到64位，并同时包含大写字母、小写字母、数字和特殊字符': 'Password must be 10-64 characters and include uppercase, lowercase, numbers and special characters',
  '该用户名属于系统保留名称，请更换用户名': 'This username is reserved by the system. Choose another username.',
  '密码包含常见弱密码片段，请使用更复杂的密码': 'Password contains a common weak fragment. Use a stronger password.',
  '密码不能包含用户名': 'Password cannot contain the username',
  '密码不能连续出现三个相同字符': 'Password cannot contain three identical consecutive characters',
  '创建密钥时 apiKey 不能为空': 'apiKey is required when creating a key',
  '过期时间必须晚于当前时间': 'Expiration time must be later than the current time',
  '分页参数不合法，size 最大为 100': 'Invalid pagination parameters. Maximum page size is 100.',
  'days 必须在 1 到 90 之间': 'days must be between 1 and 90',
  'startTime 不能晚于 endTime': 'startTime cannot be later than endTime',
  '请选择供应商': 'Please select a provider',
  '请输入 Key 名称': 'Please enter a key name',
  '请输入 API Key': 'Please enter an API key',
  'API Key 已更新': 'API key updated',
  'API Key 已安全保存': 'API key saved securely',
  '删除 API Key': 'Delete API Key',
  '删除成功': 'Deleted successfully',

  '运行概览': 'Operations Overview',
  '查看平台调用质量、用量趋势与热门服务。': 'Review platform quality, usage trends and popular services.',
  '最近 7 天': 'Last 7 Days',
  '最近 30 天': 'Last 30 Days',
  '最近 90 天': 'Last 90 Days',
  '账户余额': 'Account Balance',
  '平台余额统一以美元 USD 结算': 'Platform balance is settled in USD',
  '累计充值': 'Total Recharged',
  'USD / CNY 实时参考汇率': 'USD / CNY Reference Rate',
  '正在更新': 'Updating',
  '每 5 分钟刷新': 'Refreshes every 5 minutes',
  '· 每 5 分钟刷新': '· Refreshes every 5 minutes',
  '充值余额': 'Recharge',
  '调用总数': 'Total Calls',
  '所选周期内全部请求': 'All requests in the selected period',
  '成功率': 'Success Rate',
  '成功请求占比': 'Share of successful requests',
  'Token 用量': 'Token Usage',
  '输入与输出总计': 'Total input and output',
  '平均耗时': 'Average Latency',
  '端到端请求耗时': 'End-to-end request latency',
  '调用趋势': 'Invocation Trend',
  '按天聚合': 'Grouped by day',
  '请求数': 'Requests',
  '成功数': 'Successful',
  '运行状态': 'Service Status',
  '服务正常': 'Operational',
  '成功请求': 'Successful Requests',
  '失败请求': 'Failed Requests',
  '每日用户限额': 'Daily User Limit',
  '单 Key 限速': 'Per-Key Rate Limit',
  '供应商排行': 'Provider Ranking',
  '模型排行': 'Model Ranking',
  '暂无调用数据': 'No invocation data',
  '充值美元余额': 'Recharge USD Balance',
  '选择支付方式，人民币金额按当前汇率自动换算': 'Choose a payment method. CNY is calculated using the current rate.',
  '充值金额': 'Recharge Amount',
  '到账币种：USD': 'Credit Currency: USD',
  '支付宝': 'Alipay',
  '微信支付': 'WeChat Pay',
  '支付宝扫码支付': 'Alipay QR Payment',
  '微信扫码支付': 'WeChat QR Payment',
  'USDT 转账': 'USDT Transfer',
  'USDT 按 1:1 计入美元余额': 'USDT is credited to USD balance at 1:1',
  '请扫码支付指定金额': 'Scan the QR code to pay the specified amount',
  '付款备注建议填写登录用户名': 'Use your login username as the payment note',
  'USDT 收款地址': 'USDT Address',
  '复制': 'Copy',
  '管理员尚未配置 USDT 地址，请先设置环境变量 USDT_ADDRESS。': 'The administrator has not configured a USDT address. Set USDT_ADDRESS first.',
  '请确保转账网络与上方网络完全一致，否则资产可能无法找回。': 'Make sure the transfer network matches exactly or funds may be lost.',
  '充值说明': 'Recharge Instructions',
  '支付完成后提交订单，由管理员核对到账后增加美元余额。汇率来自 Frankfurter / ECB 参考汇率，实际到账以订单创建时汇率为准。': 'Submit the order after payment. An administrator will verify it and credit your USD balance. The order exchange rate is final.',
  '我已付款，提交审核': 'Paid, Submit for Review',
  'USDT 地址已复制': 'USDT address copied',
  '充值金额不能少于 1 美元': 'Recharge amount must be at least USD 1',

  '安全加密存储': 'Encrypted Storage',
  '密钥以 AES 加密保存，平台仅展示脱敏后的末四位。': 'Keys are stored with AES encryption. Only the masked last four characters are displayed.',
  '密钥已安全保护': 'Keys Are Protected',
  '完整密钥仅在保存时传输，之后不会在任何页面或接口中返回。': 'The full key is transmitted only when saved and is never returned by any page or API.',
  '筛选': 'Filter',
  'API Key 使用 AES 加密保存，页面永远只展示脱敏信息。': 'API keys are stored with AES encryption and only masked values are displayed.',
  '添加 API Key': 'Add API Key',
  '编辑 API Key': 'Edit API Key',
  '密钥名称': 'Key Name',
  '所属用户': 'Owner',
  '供应商': 'Provider',
  '密钥': 'Key',
  '失败次数': 'Failures',
  '名称': 'Name',
  '例如：课程演示 OpenAI Key': 'Example: Course Demo OpenAI Key',
  '替换密钥（留空则不修改）': 'Replace Key (leave blank to keep current)',
  '密钥保存后仅显示末四位。': 'Only the last four characters are shown after saving.',
  '优先级': 'Priority',
  '权重': 'Weight',
  '过期时间（可选）': 'Expiration (Optional)',
  '不设置则长期有效': 'Leave empty for no expiration',
  '保存修改': 'Save Changes',
  '加密保存': 'Encrypt and Save',

  '选择供应商和自己的 API Key，快速验证统一转发链路。': 'Choose a provider and your API key to test the unified proxy flow.',
  '支持 Mock / OpenAI Compatible': 'Supports Mock / OpenAI Compatible',
  '请求配置': 'Request Configuration',
  '清空': 'Clear',
  '选择供应商': 'Select Provider',
  '模型': 'Model',
  '常用模型': 'Popular Models',
  '选择可用 Key': 'Select an Available Key',
  '该供应商暂无可用 Key，请先前往 API Key 管理添加。': 'No available key for this provider. Add one in API Key Management first.',
  '输入要发送给 AI 的问题...': 'Enter a question for the AI...',
  '发送请求': 'Send Request',
  '模型响应': 'Model Response',
  '正在请求模型': 'Requesting Model',
  '后端正在校验 Key、限流并转发请求...': 'The backend is validating the key, applying limits and forwarding the request...',
  '耗时': 'Latency',
  '等待发送请求': 'Waiting for Request',
  '配置请求参数后，模型回答会显示在这里。': 'Configure the request and the model response will appear here.',

  '追踪自己的每次 AI 请求、响应状态和资源用量。': 'Track every AI request, response status and resource usage.',
  '仅展示本人数据': 'Personal Data Only',
  '供应商编码': 'Provider Code',
  '模型名称': 'Model Name',
  '调用结果': 'Result',
  '状态码': 'Status Code',
  '用户 ID': 'User ID',
  '结果': 'Result',
  '调用时间': 'Invoked At',
  '调用详情': 'Invocation Details',
  '输入 Token': 'Input Tokens',
  '输出 Token': 'Output Tokens',
  '错误信息': 'Error Message',

  '管理平台模型价格，调整后将同步展示给所有普通用户。': 'Manage platform model prices. Changes are shown to all regular users.',
  '浏览主流 AI 服务商公开 API 模型，快速比较能力与平台价格。': 'Browse public API models from leading AI providers and compare capabilities and prices.',
  '管理员价格管理模式': 'Administrator Pricing Mode',
  '价格快照：2026-06-14': 'Price Snapshot: 2026-06-14',
  '点击模型卡片下方的编辑按钮即可调整，修改后保存到数据库': 'Use the edit button on a model card to update prices in the database.',
  '默认单位为美元 / 百万 Token；¥ 表示人民币，实际价格以平台展示为准': 'Default unit: USD per million tokens. ¥ indicates CNY. Platform prices apply.',
  '全部供应商': 'All Providers',
  '向上': 'Up',
  '向下': 'Down',
  '向上浏览供应商': 'Browse Providers Up',
  '向下浏览供应商': 'Browse Providers Down',
  '模型类型': 'Model Type',
  '全部类型': 'All Types',
  '对话': 'Chat',
  '代码': 'Code',
  '图像': 'Image',
  '音频': 'Audio',
  '视频': 'Video',
  '检索': 'Search',
  '翻译': 'Translation',
  '只看精选': 'Featured Only',
  '推荐演示模型': 'Recommended demo models',
  '价格来源': 'Pricing Sources',
  '来自各供应商官方定价文档，点击卡片右上角可查看。': 'Prices come from official provider documentation. Open the link on each card for details.',
  '搜索模型名称、供应商或能力标签': 'Search model, provider or capability',
  '个模型': 'models',
  '阿里云百炼': 'Alibaba Model Studio',
  '查看官方价格': 'View Official Pricing',
  '输入价格': 'Input Price',
  '缓存价格': 'Cached Price',
  '输出价格': 'Output Price',
  '其他计费': 'Other Pricing',
  '计费明细': 'Pricing Details',
  'M = 100 万 Token': 'M = 1 Million Tokens',
  '不适用': 'Not Applicable',
  '未提供缓存折扣': 'No Cached-input Discount',
  '按生成时长计费': 'Billed by Generated Duration',
  '已含在每秒价格': 'Included in Per-second Price',
  '按单次请求计费': 'Billed per Request',
  '已含在单次价格': 'Included in Per-request Price',
  '按视频 Token 统一计费': 'Unified Video-token Pricing',
  '暂不提供缓存计费': 'Cached Pricing Unavailable',
  '无额外费用': 'No Additional Charge',
  '精选': 'Featured',
  '已调整': 'Adjusted',
  '复制模型名称': 'Copy Model Name',
  '立即体验': 'Try Now',
  '调整价格': 'Edit Price',
  '没有找到符合条件的模型': 'No matching models found',
  '调整模型价格': 'Edit Model Pricing',
  '价格字段支持直接填写展示内容，例如 $2.50/M、¥12.00/M 或 $0.10/秒。留空则不展示该项。': 'Enter display prices directly, such as $2.50/M, ¥12.00/M or $0.10/sec. Leave blank to hide an item.',
  '例如 图片 $0.07/张': 'Example: Image $0.07/image',
  '恢复默认': 'Restore Default',
  '保存价格': 'Save Pricing',
  '模型价格已更新，普通用户将看到最新价格': 'Model pricing updated. Regular users will see the latest prices.',
  '恢复默认价格': 'Restore Default Pricing',
  '已恢复官方默认价格': 'Official default pricing restored',
  '推理': 'Reasoning',
  '工具': 'Tools',
  '高性能': 'High Performance',
  '专业': 'Professional',
  '低成本': 'Low Cost',
  '快速': 'Fast',
  '超低成本': 'Ultra-low Cost',
  '批处理': 'Batch',
  '实时': 'Realtime',
  '语音': 'Voice',
  '转写': 'Transcription',
  '多语言': 'Multilingual',
  '生图': 'Image Generation',
  '视频生成': 'Video Generation',
  '高清': 'HD',
  '长上下文': 'Long Context',
  '知识工作': 'Knowledge Work',
  '均衡': 'Balanced',
  '多模态': 'Multimodal',
  '高并发': 'High Concurrency',
  '原生音频': 'Native Audio',
  '高质量': 'High Quality',
  '多Agent': 'Multi-Agent',
  '语音Agent': 'Voice Agent',
  '国产': 'Domestic',
  '长文本': 'Long Text',
  '1M上下文': '1M Context',

  '查看平台用户，并控制账号启用状态。': 'View platform users and control account availability.',
  '管理员权限': 'Administrator Access',
  '搜索用户名': 'Search Username',
  '用户': 'User',
  '角色': 'Role',
  '禁用账号': 'Disable Account',
  '启用账号': 'Enable Account',
  '账号状态': 'Account Status',
  '用户状态已更新': 'User status updated',
  '核对支付宝、微信和 USDT 到账记录，确认后系统自动增加用户美元余额。': 'Verify Alipay, WeChat and USDT payments. Confirmed orders automatically credit the user balance.',
  '全部状态': 'All Statuses',
  '待审核': 'Pending',
  '已到账': 'Credited',
  '已拒绝': 'Rejected',
  '订单号': 'Order No.',
  '支付方式': 'Payment Method',
  '美元入账': 'USD Credit',
  '应付金额': 'Amount Due',
  '汇率': 'Exchange Rate',
  '提交时间': 'Submitted At',
  '确认到账': 'Confirm Payment',
  '拒绝': 'Reject',
  '已处理': 'Processed',
  '确认充值到账': 'Confirm Recharge',
  '拒绝充值': 'Reject Recharge',
  '充值已确认，余额已入账': 'Recharge confirmed and balance credited',
  '订单已拒绝': 'Order rejected',

  '全站调用日志': 'Global Invocation Logs',
  '管理员视角查看所有用户的调用记录与异常请求。': 'Review all user invocation records and failed requests from an administrator view.',

  '客户支持': 'Customer Support',
  '需要帮助？': 'Need Help?',
  '我们的客服团队随时准备为您提供专业的支持服务': 'Our support team is ready to provide professional assistance.',
  '我们随时为您提供帮助。请选择以下任一方式与我们联系。': 'We are ready to help. Contact us through any of the following methods.',
  '我们随时为您提供帮助，请选择以下任一方式与我们联系。': 'We are ready to help. Contact us through any of the following methods.',
  '邮件支持': 'Email Support',
  '发送您的问题，我们会尽快回复': 'Send us your question and we will reply as soon as possible',
  '电话支持': 'Phone Support',
  '在线时间': 'Online Hours',
  '周一至周日': 'Monday to Sunday',
  '当前服务时间': 'Current Service Hours',
  '工作时间内可直接致电咨询': 'Call us directly during service hours',
  '发送邮件': 'Send Email',
  '页面不存在': 'Page Not Found',
  '你访问的页面可能已移动或删除。': 'The page may have moved or been deleted.',
  '返回首页': 'Back to Dashboard',
  '请配置': 'Not Configured',
  '请配置 USDT_ADDRESS': 'Configure USDT_ADDRESS',
  '系统备用汇率': 'System Fallback Rate',
}

const phraseTranslations: Array<[string, string]> = [
  ['阶梯价', 'Tiered pricing'],
  ['视频生成', 'Video generation'],
  ['视频 Token', 'video tokens'],
  ['音频输出', 'Audio output'],
  ['小时', 'hour'],
  ['缓存写入', 'Cache write'],
  ['中国内地', 'Mainland China'],
  ['国际区', 'International region'],
  ['以上按阶梯计费', 'and above use tiered pricing'],
  ['音频输入', 'Audio input'],
  ['图像输入', 'Image input'],
  ['图像输出约', 'Image output about'],
  ['文本/音频', 'Text/audio'],
  ['文本', 'Text'],
  ['输出', 'Output'],
  ['输入', 'Input'],
  ['上下文', 'context'],
  ['内置工具', 'Built-in tools'],
  ['百万字符', 'million characters'],
  ['分钟', 'min'],
  ['秒', 'sec'],
  ['张', 'image'],
  ['次', 'times'],
]

const modelTypeNames: Record<string, string> = {
  对话: 'chat', 代码: 'coding', 图像: 'image', 音频: 'audio', 视频: 'video', 检索: 'search', 翻译: 'translation',
}

export function translateText(value: string): string {
  if (!value || locale.value === 'zh') return value
  const trimmed = value.trim()
  if (!trimmed) return value
  const exact = translations[trimmed]
  if (exact) return value.replace(trimmed, exact)

  const modelCount = trimmed.match(/^(\d+)\s*个模型$/)
  if (modelCount) return value.replace(trimmed, `${modelCount[1]} models`)
  const rechargeOrder = trimmed.match(/^充值订单\s+(.+)\s+已提交，请等待管理员确认到账$/)
  if (rechargeOrder) return value.replace(trimmed, `Recharge order ${rechargeOrder[1]} submitted. Awaiting administrator confirmation.`)
  const copiedModel = trimmed.match(/^已复制模型名称：(.+)$/)
  if (copiedModel) return value.replace(trimmed, `Model name copied: ${copiedModel[1]}`)
  const restoreModel = trimmed.match(/^确定恢复\s+(.+)\s+的官方默认价格吗？$/)
  if (restoreModel) return value.replace(trimmed, `Restore official default pricing for ${restoreModel[1]}?`)
  const userStatus = trimmed.match(/^确定(启用|禁用)用户“(.+)”吗？$/)
  if (userStatus) return value.replace(trimmed, `${userStatus[1] === '启用' ? 'Enable' : 'Disable'} user "${userStatus[2]}"?`)
  const viewLabel = trimmed.match(/^查看(.+)$/)
  if (viewLabel) return value.replace(trimmed, `View ${translations[viewLabel[1]] || viewLabel[1]}`)
  const deleteKey = trimmed.match(/^确定删除“(.+)”吗？删除后无法恢复。$/)
  if (deleteKey) return value.replace(trimmed, `Delete "${deleteKey[1]}"? This action cannot be undone.`)
  const confirmRecharge = trimmed.match(/^确认订单\s+(.+)\s+已到账，并为用户增加\s+(.+)\s+余额吗？$/)
  if (confirmRecharge) return value.replace(trimmed, `Confirm order ${confirmRecharge[1]} and credit ${confirmRecharge[2]} to the user balance?`)
  const rejectRecharge = trimmed.match(/^确定拒绝订单\s+(.+)\s+吗？$/)
  if (rejectRecharge) return value.replace(trimmed, `Reject order ${rejectRecharge[1]}?`)
  const keyLimit = trimmed.match(/^该 API Key 每分钟最多调用\s+(\d+)\s+次$/)
  if (keyLimit) return value.replace(trimmed, `This API key allows up to ${keyLimit[1]} calls per minute`)
  const dailyLimit = trimmed.match(/^今日调用次数已达到\s+(\d+)\s+次上限$/)
  if (dailyLimit) return value.replace(trimmed, `Today's limit of ${dailyLimit[1]} calls has been reached`)
  const upstreamHttp = trimmed.match(/^上游服务返回 HTTP\s+(.+)$/)
  if (upstreamHttp) return value.replace(trimmed, `Upstream service returned HTTP ${upstreamHttp[1]}`)
  const proxyMode = trimmed.match(/^不支持的转发模式:\s*(.+)$/)
  if (proxyMode) return value.replace(trimmed, `Unsupported proxy mode: ${proxyMode[1]}`)
  const missingClient = trimmed.match(/^转发客户端未配置:\s*(.+)$/)
  if (missingClient) return value.replace(trimmed, `Proxy client is not configured: ${missingClient[1]}`)
  const upstreamFailure = trimmed.match(/^上游 AI 服务调用失败:\s*(.+)$/)
  if (upstreamFailure) return value.replace(trimmed, `Upstream AI service call failed: ${upstreamFailure[1]}`)

  let translated = trimmed
  for (const [source, target] of phraseTranslations) translated = translated.split(source).join(target)
  return translated === trimmed ? value : value.replace(trimmed, translated)
}

export function modelDescription(name: string, provider: string, type: string, tags: string[]): string {
  if (locale.value === 'zh') return ''
  const providerName = translations[provider] || provider
  const capability = modelTypeNames[type] || 'AI'
  const tagText = tags.map(tag => translations[tag] || tag).join(', ')
  return `${name} is a ${capability} model from ${providerName}${tagText ? `, designed for ${tagText}` : ''}.`
}

export function setLocale(value: AppLocale) {
  locale.value = value
  localStorage.setItem(STORAGE_KEY, value)
}

export function toggleLocale() {
  setLocale(locale.value === 'zh' ? 'en' : 'zh')
}

export function useLanguage() {
  return {
    locale: readonly(locale),
    isEnglish: () => locale.value === 'en',
    setLocale,
    toggleLocale,
    t: (value: string) => locale.value === 'en' ? translations[value] || translateText(value) : value,
  }
}

const originalText = new WeakMap<Text, string>()
const renderedText = new WeakMap<Text, string>()
const originalAttributes = new WeakMap<Element, Map<string, string>>()
const renderedAttributes = new WeakMap<Element, Map<string, string>>()
const translatedAttributes = ['placeholder', 'title', 'aria-label']

function updateTextNode(node: Text) {
  if (node.parentElement?.closest('code, script, style, [data-no-translate]')) return
  const current = node.nodeValue || ''
  const lastRendered = renderedText.get(node)
  if (!originalText.has(node) || (lastRendered !== undefined && current !== lastRendered)) originalText.set(node, current)
  const original = originalText.get(node) || current
  const target = locale.value === 'en' ? translateText(original) : original
  if (current !== target) node.nodeValue = target
  renderedText.set(node, target)
}

function updateElementAttributes(element: Element) {
  if (element.closest('[data-no-translate]')) return
  let originals = originalAttributes.get(element)
  let rendered = renderedAttributes.get(element)
  if (!originals) {
    originals = new Map()
    originalAttributes.set(element, originals)
  }
  if (!rendered) {
    rendered = new Map()
    renderedAttributes.set(element, rendered)
  }

  for (const attribute of translatedAttributes) {
    const current = element.getAttribute(attribute)
    if (current === null) continue
    const lastRendered = rendered.get(attribute)
    if (!originals.has(attribute) || (lastRendered !== undefined && current !== lastRendered)) originals.set(attribute, current)
    const original = originals.get(attribute) || current
    const target = locale.value === 'en' ? translateText(original) : original
    if (current !== target) element.setAttribute(attribute, target)
    rendered.set(attribute, target)
  }
}

function translateNode(node: Node) {
  if (node.nodeType === Node.TEXT_NODE) {
    updateTextNode(node as Text)
    return
  }
  if (!(node instanceof Element) || node.matches('script, style, code, [data-no-translate]')) return
  updateElementAttributes(node)
  const walker = document.createTreeWalker(node, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT)
  let current = walker.nextNode()
  while (current) {
    if (current.nodeType === Node.TEXT_NODE) updateTextNode(current as Text)
    else if (current instanceof Element && !current.matches('script, style, code, [data-no-translate]')) updateElementAttributes(current)
    current = walker.nextNode()
  }
}

export function installDomTranslation() {
  document.documentElement.lang = locale.value === 'en' ? 'en' : 'zh-CN'
  nextTick(() => translateNode(document.body))

  const observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      if (mutation.type === 'characterData') translateNode(mutation.target)
      if (mutation.type === 'attributes' && mutation.target instanceof Element) updateElementAttributes(mutation.target)
      mutation.addedNodes.forEach(translateNode)
    }
  })
  observer.observe(document.body, { childList: true, subtree: true, characterData: true, attributes: true, attributeFilter: translatedAttributes })

  const stop = watch(locale, async (value) => {
    document.documentElement.lang = value === 'en' ? 'en' : 'zh-CN'
    await nextTick()
    translateNode(document.body)
  })

  return () => {
    stop()
    observer.disconnect()
  }
}
