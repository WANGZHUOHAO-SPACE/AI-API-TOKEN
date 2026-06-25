import type { ExternalPricingModel, ExternalPricingPayload, ExternalPricingStep } from '@/types'

export type ModelType = '对话' | '代码' | '图像' | '音频' | '视频' | '检索' | '翻译'

export interface CatalogModel {
  id: string
  name: string
  provider: string
  providerCode: string
  type: ModelType
  description: string
  input?: string
  cached?: string
  output?: string
  extra?: string
  tags: string[]
  featured?: boolean
}

const model = (
  id: string,
  provider: CatalogModel['provider'],
  type: ModelType,
  description: string,
  prices: Pick<CatalogModel, 'input' | 'cached' | 'output' | 'extra'>,
  tags: string[],
  featured = false,
): CatalogModel => ({
  id,
  name: id,
  provider,
  providerCode: providerCode(provider),
  type,
  description,
  input: prices.input?.trim() || '不适用',
  cached: prices.cached?.trim() || '暂不提供缓存计费',
  output: prices.output?.trim() || '不适用',
  extra: prices.extra?.trim() || '无额外费用',
  tags,
  featured,
})

const providerCodeMap: Record<string, string> = {
  OpenAI: 'openai',
  'OpenAI Official': 'openai',
  'Azure OpenAI': 'azure-openai',
  Anthropic: 'anthropic',
  Google: 'google',
  xAI: 'xai',
  DeepSeek: 'deepseek',
  阿里云百炼: 'qwen',
  Bailian: 'qwen',
  BytePlus: 'byteplus',
  DouBaoSeed: 'doubao',
  'Zhipu GLM': 'zhipu',
  'Zhipu GLM en': 'zhipu',
  Kimi: 'kimi',
  StepFun: 'stepfun',
  'StepFun en': 'stepfun',
  ModelScope: 'modelscope',
  Longcat: 'longcat',
  MiniMax: 'minimax',
  'MiniMax en': 'minimax',
  SiliconFlow: 'siliconflow',
  'SiliconFlow en': 'siliconflow',
  'OpenRouter': 'openrouter',
  TheRouter: 'therouter',
}

function providerCode(provider: string) {
  if (providerCodeMap[provider]) return providerCodeMap[provider]
  const asciiCode = provider.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '')
  if (asciiCode) return asciiCode
  return `provider-${Array.from(provider).map(char => char.charCodeAt(0).toString(16)).join('-')}`
}

export const providerSources: Record<string, string> = {
  OpenAI: 'https://developers.openai.com/api/docs/pricing',
  'OpenAI Official': 'https://developers.openai.com/api/docs/pricing',
  'Azure OpenAI': 'https://azure.microsoft.com/en-us/pricing/details/cognitive-services/openai-service/',
  Anthropic: 'https://docs.anthropic.com/en/docs/about-claude/pricing',
  Google: 'https://ai.google.dev/gemini-api/docs/pricing',
  xAI: 'https://docs.x.ai/developers/models',
  DeepSeek: 'https://api-docs.deepseek.com/quick_start/pricing',
  阿里云百炼: 'https://help.aliyun.com/zh/model-studio/model-pricing',
  Bailian: 'https://help.aliyun.com/zh/model-studio/model-pricing',
  BytePlus: 'https://www.byteplus.com/en/product/modelark',
  DouBaoSeed: 'https://www.volcengine.com/product/doubao',
  Kimi: 'https://platform.moonshot.cn/docs/pricing',
  MiniMax: 'https://www.minimax.io/platform',
  SiliconFlow: 'https://siliconflow.cn/pricing',
  OpenRouter: 'https://openrouter.ai/models',
  TheRouter: 'https://therouter.co/models',
}

const providerPreset = (
  id: string,
  provider: string,
  type: ModelType,
  description: string,
  tags: string[],
  featured = false,
) => model(
  `provider-${id}`,
  provider,
  type,
  description,
  { input: '按所选模型计费', cached: '按通道规则计费', output: '按所选模型计费', extra: '支持在 API Key 管理中配置该供应商通道' },
  tags,
  featured,
)

const presetProviderCatalog: CatalogModel[] = [
  providerPreset('custom', '自定义配置', '对话', '自定义 OpenAI Compatible 供应商，适合接入课程演示或个人代理接口。', ['自定义', 'OpenAI Compatible'], true),
  providerPreset('openai-official', 'OpenAI Official', '对话', 'OpenAI 官方模型通道，适合 GPT、Realtime、Image 与 Sora 系列能力接入。', ['官方通道', '对话', '多模态'], true),
  providerPreset('shengsuanyun', '胜算云', '对话', '聚合式模型供应商通道，适合统一管理多模型 API 调用。', ['聚合通道', '对话']),
  providerPreset('patewayai', 'PatewayAI', '对话', '面向开发者的 AI API 聚合通道，支持多模型统一转发。', ['聚合通道', '低成本']),
  providerPreset('volcano-agentplan', '火山Agentplan', '对话', '火山引擎 Agent 方案通道，适合智能体和企业应用演示。', ['Agent', '国产']),
  providerPreset('byteplus', 'BytePlus', '对话', 'BytePlus 海外模型服务通道，适合国际化 API 接入场景。', ['海外通道', '多模型']),
  providerPreset('doubaoseed', 'DouBaoSeed', '对话', '豆包 Seed 系列模型通道，适合中文对话、知识问答和内容生成。', ['豆包', '国产'], true),
  providerPreset('ccsub', 'CCSub', '代码', '面向 Claude Code 与开发工具场景的订阅通道。', ['代码', '开发工具']),
  providerPreset('azure-openai', 'Azure OpenAI', '对话', 'Azure OpenAI 企业云服务通道，适合合规部署和企业账号接入。', ['企业云', '官方通道'], true),
  providerPreset('zhipu-glm', 'Zhipu GLM', '对话', '智谱 GLM 中文模型通道，适合文本生成、问答和工具调用。', ['国产', '对话']),
  providerPreset('zhipu-glm-en', 'Zhipu GLM en', '对话', '智谱 GLM 国际化通道，适合英文与海外 API 接入。', ['海外通道', '对话']),
  providerPreset('baidu-qianfan-coding-plan', 'Baidu Qianfan Coding Plan', '代码', '百度千帆代码套餐通道，适合编程辅助和工程演示。', ['代码', '国产']),
  providerPreset('bailian', 'Bailian', '对话', '阿里云百炼模型服务通道，适合 Qwen 系列模型接入。', ['国产', 'Qwen'], true),
  providerPreset('kimi', 'Kimi', '对话', 'Moonshot Kimi 模型通道，适合长文本、知识问答和中文写作。', ['长文本', '国产'], true),
  providerPreset('stepfun', 'StepFun', '对话', '阶跃星辰 StepFun 模型通道，适合通用对话与多模态应用。', ['国产', '多模态']),
  providerPreset('stepfun-en', 'StepFun en', '对话', 'StepFun 英文/海外通道，适合国际化模型调用演示。', ['海外通道', '多模态']),
  providerPreset('modelscope', 'ModelScope', '对话', '魔搭社区模型通道，适合开源模型与国产模型展示。', ['开源模型', '国产']),
  providerPreset('longcat', 'Longcat', '对话', 'LongCat 模型通道，适合长文本和通用对话场景。', ['长文本', '对话']),
  providerPreset('minimax', 'MiniMax', '对话', 'MiniMax 模型服务通道，适合对话、语音和多模态应用。', ['国产', '多模态'], true),
  providerPreset('minimax-en', 'MiniMax en', '对话', 'MiniMax 海外通道，适合英文场景和国际化部署。', ['海外通道', '多模态']),
  providerPreset('bailing', 'BaiLing', '对话', 'BaiLing 模型供应商通道，适合统一代理和课程演示。', ['聚合通道', '对话']),
  providerPreset('xiaomi-mimo', 'Xiaomi MiMo', '对话', '小米 MiMo 模型通道，适合国产模型能力展示。', ['国产', '对话']),
  providerPreset('xiaomi-mimo-token-plan', 'Xiaomi MiMo Token Plan (China)', '对话', '小米 MiMo 国内 Token 套餐通道，适合中文演示和成本控制。', ['国产', 'Token 套餐']),
  providerPreset('siliconflow', 'SiliconFlow', '对话', '硅基流动模型平台通道，适合开源模型与国产模型聚合调用。', ['开源模型', '聚合通道'], true),
  providerPreset('siliconflow-en', 'SiliconFlow en', '对话', 'SiliconFlow 海外通道，适合国际化模型代理接入。', ['海外通道', '聚合通道']),
  providerPreset('novita-ai', 'Novita AI', '图像', 'Novita AI 多模态模型通道，适合图像、视频和生成式应用。', ['图像', '多模态']),
  providerPreset('nvidia', 'Nvidia', '对话', 'NVIDIA AI 模型服务通道，适合高性能推理和开源模型部署。', ['高性能', '开源模型']),
  providerPreset('aihubmix', 'AiHubMix', '对话', 'AiHubMix 聚合通道，适合多供应商模型统一转发。', ['聚合通道', '多模型']),
  providerPreset('cherryin', 'CherryIN', '对话', 'CherryIN 模型供应商通道，适合轻量级模型调用演示。', ['聚合通道', '低成本']),
  providerPreset('dmxapi', 'DMXAPI', '对话', 'DMXAPI 聚合服务通道，适合多模型 API 统一接入。', ['聚合通道', '对话'], true),
  providerPreset('packycode', 'PackyCode', '代码', 'PackyCode 代码模型通道，适合编程辅助、代码生成和课程答辩演示。', ['代码', '开发工具']),
  providerPreset('apikey-fun', 'APIKEY.FUN', '对话', 'APIKEY.FUN 聚合通道，适合统一 Key 管理和快速调用验证。', ['聚合通道', '低成本']),
  providerPreset('apinebula', 'APINebula', '对话', 'APINebula 模型网关通道，适合多模型统一转发。', ['网关', '多模型']),
  providerPreset('atlascloud', 'AtlasCloud', '对话', 'AtlasCloud 云模型通道，适合企业级模型访问和统一计费。', ['企业云', '对话']),
  providerPreset('sudocode', 'SudoCode', '代码', 'SudoCode 代码模型通道，适合开发助手和工程代码生成。', ['代码', '开发工具']),
  providerPreset('claudecn', 'ClaudeCN', '对话', 'ClaudeCN Claude 模型通道，适合长文本、代码和 Agent 调用。', ['Claude', '长文本'], true),
  providerPreset('runapi', 'RunAPI', '对话', 'RunAPI 聚合通道，适合快速接入多家模型供应商。', ['聚合通道', '多模型']),
  providerPreset('relaxycode', 'RelaxyCode', '代码', 'RelaxyCode 代码服务通道，适合编程辅助和代码模型体验。', ['代码', '低成本']),
  providerPreset('cubence', 'Cubence', '对话', 'Cubence 模型供应商通道，适合模型代理和统一转发。', ['聚合通道', '对话']),
  providerPreset('aigocode', 'AIGoCode', '代码', 'AIGoCode 代码模型通道，适合开发者工具和代码生成。', ['代码', '开发工具']),
  providerPreset('rightcode', 'RightCode', '代码', 'RightCode 编程模型通道，适合软件工程课程演示。', ['代码', '课程演示']),
  providerPreset('aicodemirror', 'AICodeMirror', '代码', 'AICodeMirror 代码镜像通道，适合备用模型接入和代码演示。', ['代码', '备用通道']),
  providerPreset('crazyrouter', 'CrazyRouter', '对话', 'CrazyRouter 模型路由通道，适合多模型自动切换和聚合转发。', ['路由', '多模型'], true),
  providerPreset('sssaicode', 'SSSAiCode', '代码', 'SSSAiCode 代码模型通道，适合编程辅助和代码生成。', ['代码', '开发工具']),
  providerPreset('youyun-zhisuan', '优云智算', '对话', '优云智算模型通道，适合国产算力和中文模型调用。', ['国产', '算力']),
  providerPreset('youyun-zhisuan-coding', '优云智算Coding Plan', '代码', '优云智算代码套餐通道，适合代码生成和开发工具演示。', ['代码', '国产']),
  providerPreset('micu', 'Micu', '对话', 'Micu 模型供应商通道，适合轻量聚合和快速验证。', ['聚合通道', '低成本']),
  providerPreset('ctok-ai', 'CTok.ai', '对话', 'CTok.ai Token 计费模型通道，适合按量调用和成本展示。', ['Token 套餐', '聚合通道']),
  providerPreset('eflowcode', 'E-FlowCode', '代码', 'E-FlowCode 编程模型通道，适合代码生成和工程任务。', ['代码', '工作流']),
  providerPreset('lemondata', 'LemonData', '对话', 'LemonData 模型数据服务通道，适合企业数据问答和 API 调用。', ['数据问答', '企业']),
  providerPreset('pipellm', 'PIPELLM', '对话', 'PIPELLM 模型管道通道，适合多模型流水线和统一代理。', ['工作流', '多模型']),
  providerPreset('openrouter', 'OpenRouter', '对话', 'OpenRouter 聚合通道，适合访问全球主流大模型。', ['全球模型', '聚合通道'], true),
  providerPreset('therouter', 'TheRouter', '对话', 'TheRouter 模型路由通道，适合统一入口和多模型路由。', ['路由', '聚合通道']),
]

export const modelCatalog: CatalogModel[] = [
  ...presetProviderCatalog,
  model('gpt-5.5', 'OpenAI', '对话', 'OpenAI 旗舰通用模型，适合复杂推理、Agent 与生产级任务。', { input: '$2.50/M', cached: '$0.25/M', output: '$15.00/M' }, ['推理', '工具', 'Agent'], true),
  model('gpt-5.5-pro', 'OpenAI', '对话', '面向最高质量任务的专业版本，适合高难度推理。', { input: '$30.00/M', output: '$180.00/M' }, ['推理', '高性能']),
  model('gpt-5.4', 'OpenAI', '对话', '高性能通用模型，支持长上下文和工具调用。', { input: '$2.50/M', cached: '$0.25/M', output: '$15.00/M' }, ['对话', '工具'], true),
  model('gpt-5.4-pro', 'OpenAI', '对话', 'GPT-5.4 的高计算版本，侧重复杂专业任务。', { input: '$30.00/M', output: '$180.00/M' }, ['推理', '专业']),
  model('gpt-5.4-mini', 'OpenAI', '对话', '兼顾能力、延迟和成本的小型通用模型。', { input: '$0.75/M', cached: '$0.075/M', output: '$4.50/M' }, ['低成本', '快速'], true),
  model('gpt-5.4-nano', 'OpenAI', '对话', '轻量级高速模型，适合分类、抽取和批处理。', { input: '$0.20/M', cached: '$0.02/M', output: '$1.25/M' }, ['超低成本', '批处理']),
  model('gpt-realtime-2', 'OpenAI', '音频', '实时语音交互模型，可处理文本和双向音频。', { input: '文本 $5/M', cached: '文本 $0.50/M', output: '文本 $20/M', extra: '音频输入 $36/M；输出 $72/M' }, ['实时', '语音']),
  model('gpt-4o-transcribe', 'OpenAI', '音频', '高质量语音转文字模型。', { input: '$6.00/M', output: '$12.00/M' }, ['转写', '语音']),
  model('gpt-4o-mini-transcribe', 'OpenAI', '音频', '更经济的语音转写模型。', { input: '$1.25/M', output: '$5.00/M' }, ['转写', '低成本']),
  model('whisper', 'OpenAI', '音频', '经典多语言语音识别模型。', { extra: '$0.006/分钟' }, ['转写', '多语言']),
  model('translate', 'OpenAI', '翻译', '音频翻译服务，按音频时长计费。', { extra: '$0.003/分钟' }, ['翻译', '音频']),
  model('gpt-image-2', 'OpenAI', '图像', '高质量图像生成与编辑模型。', { input: '文本 $5/M', cached: '$0.50/M', output: '文本 $10/M', extra: '图像输入 $8/M；输出 $32/M' }, ['生图', '编辑'], true),
  model('gpt-image-1.5', 'OpenAI', '图像', '稳定的图像生成模型，支持图像输入和编辑。', { input: '文本 $5/M', cached: '$0.50/M', output: '文本 $10/M', extra: '图像输入 $8/M；输出 $32/M' }, ['生图', '编辑']),
  model('gpt-image-1-mini', 'OpenAI', '图像', '经济型图像生成模型。', { input: '文本 $2/M', cached: '$0.20/M', extra: '图像输入 $2/M；输出 $8/M' }, ['生图', '低成本']),
  model('sora-2', 'OpenAI', '视频', '文本或图片生成视频模型。', { extra: '$0.10/秒' }, ['视频生成']),
  model('sora-2-pro', 'OpenAI', '视频', '更高质量和分辨率的视频生成模型。', { extra: '720p $0.30/秒；1080p $0.50/秒' }, ['视频生成', '高清']),

  model('claude-fable-5', 'Anthropic', '对话', 'Anthropic 最新旗舰模型，面向 Agent、代码与长任务。', { input: '$5.00/M', cached: '$0.50/M', output: '$25.00/M', extra: '缓存写入 $6.25/M' }, ['Agent', '代码', '长上下文'], true),
  model('claude-mythos-5', 'Anthropic', '对话', '面向复杂推理和知识工作的高能力模型。', { input: '$5.00/M', cached: '$0.50/M', output: '$25.00/M', extra: '缓存写入 $6.25/M' }, ['推理', '知识工作']),
  model('claude-opus-4-8', 'Anthropic', '对话', 'Opus 4.8 高能力模型，适合复杂代码与 Agent。', { input: '$5.00/M', cached: '$0.50/M', output: '$25.00/M' }, ['推理', '代码'], true),
  model('claude-opus-4-7', 'Anthropic', '对话', 'Opus 4.7 旗舰推理模型。', { input: '$5.00/M', cached: '$0.50/M', output: '$25.00/M' }, ['推理']),
  model('claude-opus-4-6', 'Anthropic', '对话', 'Opus 系列高性能通用模型。', { input: '$5.00/M', cached: '$0.50/M', output: '$25.00/M' }, ['推理']),
  model('claude-opus-4-5', 'Anthropic', '对话', '适合复杂分析、代码和长文本处理。', { input: '$5.00/M', cached: '$0.50/M', output: '$25.00/M' }, ['代码', '长文本']),
  model('claude-sonnet-4-6', 'Anthropic', '对话', '能力、速度与价格平衡的 Sonnet 模型。', { input: '$3.00/M', cached: '$0.30/M', output: '$15.00/M' }, ['均衡', '代码'], true),
  model('claude-sonnet-4-5', 'Anthropic', '对话', '成熟稳定的通用 Claude 模型。', { input: '$3.00/M', cached: '$0.30/M', output: '$15.00/M' }, ['对话', '代码']),
  model('claude-haiku-4-5', 'Anthropic', '对话', '低延迟、低成本的轻量模型。', { input: '$1.00/M', cached: '$0.10/M', output: '$5.00/M' }, ['快速', '低成本']),

  model('gemini-3.1-pro-preview', 'Google', '对话', 'Google 高级推理模型，适合复杂多模态和长上下文任务。', { input: '$2.00/M', cached: '$0.20/M', output: '$12.00/M', extra: '>200K 上下文：输入 $4/M，输出 $18/M' }, ['推理', '多模态'], true),
  model('gemini-3-flash-preview', 'Google', '对话', '高性能低延迟 Flash 模型。', { input: '$0.50/M', cached: '$0.05/M', output: '$3.00/M' }, ['快速', '多模态'], true),
  model('gemini-3.1-flash-lite-preview', 'Google', '对话', '成本优先的轻量模型，适合大规模调用。', { input: '$0.25/M', cached: '$0.025/M', output: '$1.50/M' }, ['低成本', '批处理']),
  model('gemini-3.1-flash-live-preview', 'Google', '音频', '实时原生音频对话模型。', { input: '文本 $0.50/M', output: '文本 $2.00/M', extra: '音频输入 $0.005/秒；输出 $0.018/秒' }, ['实时', '语音']),
  model('gemini-3.1-flash-image-preview', 'Google', '图像', '快速图像生成和编辑模型。', { input: '文本/音频 $0.50/M', output: '文本 $3/M', extra: '图像输出约 $0.067/张' }, ['生图', '快速'], true),
  model('gemini-3.1-flash-preview-tts', 'Google', '音频', 'Flash 文本转语音预览模型。', { input: '$0.50/M', output: '音频 $10.00/M' }, ['TTS', '语音']),
  model('gemini-3-pro-image-preview', 'Google', '图像', '高质量专业图像生成模型。', { input: '$2.00/M', output: '文本 $12/M', extra: '图像输出约 $0.134/张' }, ['生图', '高质量']),
  model('gemini-2.5-pro', 'Google', '对话', '稳定版高级推理与多模态模型。', { input: '$1.25/M', cached: '$0.125/M', output: '$10.00/M', extra: '>200K 上下文：输入 $2.50/M，输出 $15/M' }, ['推理', '多模态']),
  model('gemini-2.5-flash', 'Google', '对话', '稳定、高性价比的多模态模型。', { input: '$0.30/M', cached: '$0.03/M', output: '$2.50/M' }, ['快速', '多模态']),
  model('gemini-2.5-flash-lite', 'Google', '对话', '适合高并发简单任务的经济型模型。', { input: '$0.10/M', cached: '$0.01/M', output: '$0.40/M' }, ['超低成本', '高并发']),
  model('gemini-2.5-flash-image', 'Google', '图像', '面向快速图像生成和编辑的稳定模型。', { input: '$0.30/M', output: '$2.50/M', extra: '图像输出约 $0.039/张' }, ['生图', '编辑']),
  model('gemini-2.5-flash-native-audio', 'Google', '音频', '原生音频实时会话模型。', { extra: '音频输入 $0.003/秒；输出 $0.012/秒' }, ['实时', '原生音频']),
  model('gemini-2.5-flash-preview-tts', 'Google', '音频', '经济型文本转语音模型。', { input: '$0.50/M', output: '音频 $10.00/M' }, ['TTS']),
  model('gemini-2.5-pro-preview-tts', 'Google', '音频', '高质量文本转语音模型。', { input: '$1.00/M', output: '音频 $20.00/M' }, ['TTS', '高质量']),

  model('grok-4.3', 'xAI', '对话', 'xAI 高级通用推理模型。', { input: '$2.00/M', output: '$6.00/M' }, ['推理', '工具'], true),
  model('grok-code-fast-1', 'xAI', '代码', '面向代码生成和快速迭代的专用模型。', { input: '$0.20/M', cached: '$0.02/M', output: '$1.50/M' }, ['代码', '快速'], true),
  model('grok-4.20-multi-agent', 'xAI', '对话', '支持多 Agent 和工具协同的模型。', { input: '$2.00/M', output: '$6.00/M', extra: '内置工具 $0.10/次' }, ['多Agent', '工具']),
  model('grok-4.20-reasoning', 'xAI', '对话', '启用推理模式的 Grok 4.20。', { input: '$2.00/M', output: '$6.00/M' }, ['推理']),
  model('grok-4.20-non-reasoning', 'xAI', '对话', '低延迟非推理版本。', { input: '$2.00/M', output: '$6.00/M' }, ['快速', '对话']),
  model('grok-imagine-image', 'xAI', '图像', '经济型图片生成模型。', { extra: '$0.02/张' }, ['生图', '低成本']),
  model('grok-imagine-image-pro', 'xAI', '图像', '高质量图片生成模型。', { extra: '$0.07/张' }, ['生图', '高质量'], true),
  model('grok-imagine-video', 'xAI', '视频', 'Grok 视频生成模型。', { extra: '$0.05/秒' }, ['视频生成']),
  model('grok-imagine-video-1.5', 'xAI', '视频', '新一代 Grok 视频生成模型。', { extra: '$0.05/秒' }, ['视频生成']),
  model('grok-voice-agent', 'xAI', '音频', '实时语音 Agent 模型。', { input: '文本 $0.20/M', cached: '$0.05/M', extra: '音频输入 $3/M；输出 $8/M' }, ['实时', '语音Agent']),
  model('grok-tts', 'xAI', '音频', '文本转语音服务。', { extra: '$0.50/百万字符' }, ['TTS']),
  model('grok-stt', 'xAI', '音频', '语音转文字服务。', { extra: '$0.04/分钟' }, ['STT', '转写']),

  model('deepseek-v4-flash', 'DeepSeek', '对话', 'DeepSeek V4 高并发经济型模型，支持思考与非思考模式。', { input: '$0.14/M', cached: '$0.0028/M', output: '$0.28/M' }, ['推理', '1M上下文', '国产'], true),
  model('deepseek-v4-pro', 'DeepSeek', '对话', 'DeepSeek V4 高能力版本，适合复杂推理、代码和 Agent。', { input: '$0.435/M', cached: '$0.003625/M', output: '$0.87/M' }, ['推理', '代码', '国产'], true),

  model('qwen3.7-max', '阿里云百炼', '对话', '千问 3.7 旗舰模型，支持思考与非思考模式及百万上下文。', { input: '¥12.00/M', output: '¥36.00/M' }, ['推理', '1M上下文', '国产'], true),
  model('qwen3-max', '阿里云百炼', '对话', '千问 3 系列高能力通用模型。', { input: '¥2.50/M', output: '¥10.00/M', extra: '32K 以上按阶梯计费' }, ['推理', '工具', '国产']),
  model('qwen3.6-flash', '阿里云百炼', '对话', '兼顾长上下文、速度与成本的 Flash 模型。', { input: '¥1.20/M', output: '¥7.20/M', extra: '256K 以上输入 ¥4.80/M，输出 ¥28.80/M' }, ['快速', '长上下文', '国产']),
  model('qwen3.5-flash', '阿里云百炼', '对话', '适合高并发通用任务的低成本千问模型。', { input: '¥0.20/M', output: '¥2.00/M', extra: '128K 以上按阶梯计费' }, ['低成本', '高并发', '国产']),
  model('qwen3-coder-plus', '阿里云百炼', '代码', '面向复杂软件工程和 Agent 编程的代码模型。', { input: '¥7.339/M', output: '¥36.696/M', extra: '国际区 32K 内价格' }, ['代码', 'Agent', '国产'], true),
  model('qwen3-coder-flash', '阿里云百炼', '代码', '低延迟、高性价比的代码生成模型。', { input: '¥2.202/M', output: '¥11.009/M', extra: '国际区 32K 内价格' }, ['代码', '快速', '国产']),
  model('qwen-image-2.0', '阿里云百炼', '图像', '千问图像生成与编辑模型。', { extra: '中国内地 ¥0.20/张' }, ['生图', '编辑', '国产']),
  model('qwen-image-2.0-pro', '阿里云百炼', '图像', '更高质量的千问专业图像生成模型。', { extra: '中国内地 ¥0.50/张' }, ['生图', '高质量', '国产'], true),
]

function effectiveGroupRatio(item: ExternalPricingModel, payload: ExternalPricingPayload) {
  const ratios = (item.enable_groups || []).map((group) => {
    const modelRatio = payload.group_model_ratio?.[group]?.[item.model_name]
    return modelRatio ?? payload.group_ratio?.[group]
  }).filter((ratio): ratio is number => Number.isFinite(ratio) && ratio > 0)
  return ratios.length ? Math.min(...ratios) : 1
}

function money(value: number) {
  const digits = Math.abs(value) > 0 && Math.abs(value) < 0.0001 ? 8 : 4
  return `$${value.toFixed(digits)}`
}

function tokenPrice(value: number) {
  return `${money(value)}/M`
}

function tokenSize(value: number) {
  if (value >= 1_000_000) return `${Number((value / 1_000_000).toFixed(2))}M`
  if (value >= 1_000) return `${Number((value / 1_000).toFixed(2))}K`
  return String(value)
}

function perRequestMultiplier(item: ExternalPricingModel) {
  const name = item.model_name
  const fixed: Record<string, number> = {
    'aigc-image': 20,
    'aigc-video': 23,
    'aigc-image-gem': 30,
    'aigc-image-qwen': 30,
    'aigc-image-hunyuan': 20,
    'aigc-video-vidu': 25,
    'aigc-template-effect-vidu': 40,
    'aigc-video-kling': 30,
    'aigc-video-hailuo': 23,
    'kling-image': 2.5,
    'kling-omni-image': 20,
    'kling-video': 100,
    'kling-omni-video': 100,
    'kling-avatar-image2video': 100,
    'kling-audio': 5,
    'suno_music_open': 9,
    'kling-custom-voices': 5,
    'kling-effects': 100,
    'kling-multi-elements': 100,
    'kling-video-extend': 100,
    'kling-advanced-lip-sync': 50,
    'kling-image-recognize': 10,
    viduq2: 18.75,
    viduq1: 62.5,
    'viduq2-turbo': 18.75,
    'viduq2-pro': 25,
    'viduq3-pro': 218.75,
    'viduq3-turbo': 125,
    viduq3: 156.25,
    'viduq3-mix': 390.625,
    'viduq1-classic': 250,
    'vidu2.0': 62.5,
    'audio1.0': 31.25,
    'vidu-tts': 31.25,
    'MiniMax-Hailuo-02': 200,
    'MiniMax-Hailuo-2.3': 200,
    'MiniMax-Hailuo-2.3-Fast': 135,
    'S2V-01': 200,
    'MiniMax-Voice-Clone': 990,
    'MiniMax-Voice-Design': 200,
    'speech-02-hd': 350,
    'speech-02-turbo': 200,
    'speech-2.6-hd': 350,
    'speech-2.6-turbo': 200,
    'speech-2.8-hd': 350,
    'speech-2.8-turbo': 200,
  }
  if (fixed[name]) return fixed[name]
  if (name.startsWith('grok-imagine-video')) return item.model_ratio && item.model_ratio > 0 ? item.model_ratio : 7
  if (name.startsWith('doubao-seedance-2-0')) return name.includes('fast') ? 3700 : 4600
  if (name.startsWith('pixverse-') && name !== 'pixverse-upload') return 2.93
  return 1
}

function perSecondMultiplier(item: ExternalPricingModel) {
  if (item.model_name.startsWith('happyhorse-1.0')) return 90
  if (item.model_name.startsWith('grok-imagine-video')) return item.model_ratio && item.model_ratio > 0 ? item.model_ratio : 7
  if (item.model_name === 'kling-motion-control') return 50
  return 1
}

function tierPricing(input: number, output: number, steps?: ExternalPricingStep[]) {
  if (!steps?.length) return []
  return steps.slice(0, 4).map((step) => {
    const promptRatio = step.prompt_step_ratio ?? 1
    const completionRatio = step.completion_step_ratio ?? 1
    return `≤${tokenSize(step.step_size)}：输入 ${tokenPrice(input * promptRatio)}，输出 ${tokenPrice(output * completionRatio)}`
  })
}

function pricesFor(item: ExternalPricingModel, payload: ExternalPricingPayload) {
  const groupRatio = effectiveGroupRatio(item, payload)
  if (item.quota_type === 0) {
    const input = 2 * (item.model_ratio ?? 0) * groupRatio
    const output = 2 * (item.model_ratio ?? 0) * (item.completion_ratio ?? 0) * groupRatio
    const extras = tierPricing(input, output, item.step_ratios)
    if (item.cache_creation_ratio) extras.push(`缓存写入 ${tokenPrice(input * item.cache_creation_ratio)}`)
    if (item.cache_creation_5m_ratio) extras.push(`5 分钟缓存写入 ${tokenPrice(input * item.cache_creation_5m_ratio)}`)
    if (item.cache_creation_1h_ratio) extras.push(`1 小时缓存写入 ${tokenPrice(input * item.cache_creation_1h_ratio)}`)
    if (item.audio_ratio) extras.push(`音频输入 ${tokenPrice(item.audio_ratio * groupRatio)}`)
    if (item.audio_completion_ratio) extras.push(`音频输出 ${tokenPrice(item.audio_completion_ratio * groupRatio)}`)
    return {
      input: tokenPrice(input),
      cached: item.cache_ratio ? tokenPrice(input * item.cache_ratio) : '未提供缓存折扣',
      output: tokenPrice(output),
      extra: extras.length ? extras.join('；') : '无额外费用',
    }
  }

  if (item.quota_type === 4) {
    const price = (item.model_price ?? 0) * groupRatio * perSecondMultiplier(item)
    return {
      input: '按生成时长计费',
      cached: '不适用',
      output: '已含在每秒价格',
      extra: `${money(price)}/秒`,
    }
  }

  if (item.quota_type === 3) {
    const price = (item.model_price ?? 0) * groupRatio
    return {
      input: `${money(price)}/M 视频 Token`,
      cached: '未提供缓存折扣',
      output: '按视频 Token 统一计费',
      extra: `视频生成 ${money(price)}/M Token`,
    }
  }

  const price = (item.model_price ?? 0) * groupRatio * perRequestMultiplier(item)
  return {
    input: '按单次请求计费',
    cached: '不适用',
    output: '已含在单次价格',
    extra: `${money(price)}/次`,
  }
}

function modelType(item: ExternalPricingModel): ModelType {
  const text = `${item.model_name} ${item.model_type || ''} ${item.tags || ''}`.toLowerCase()
  if (text.includes('检索') || text.includes('search') || text.includes('rerank')) return '检索'
  if (text.includes('翻译') || text.includes('translate')) return '翻译'
  if (text.includes('视频') || text.includes('video') || text.includes('i2v') || text.includes('t2v')) return '视频'
  if (text.includes('音频') || text.includes('语音') || text.includes('audio') || text.includes('speech') || text.includes('voice') || text.includes('tts') || text.includes('stt')) return '音频'
  if (text.includes('图像') || text.includes('绘画') || text.includes('image')) return '图像'
  if (text.includes('代码') || text.includes('coder') || text.includes('code')) return '代码'
  return '对话'
}

export function externalPricingCatalog(payload: ExternalPricingPayload): CatalogModel[] {
  const vendors = new Map((payload.vendors || []).map(vendor => [vendor.id, vendor.name]))
  return (payload.data || []).map((item) => {
    const provider = vendors.get(item.vendor_id ?? -1) || item.owner_by || '其他供应商'
    const tags = (item.tags || '').split(/[,，;；|]/).map(tag => tag.trim()).filter(Boolean).slice(0, 5)
    const type = modelType(item)
    return model(
      item.model_name,
      provider,
      type,
      item.description || `${provider} 提供的 ${item.model_name} 模型。`,
      pricesFor(item, payload),
      tags.length ? tags : [type],
      (item.sort_order ?? 0) >= 500,
    )
  })
}
