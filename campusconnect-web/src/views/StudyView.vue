<template>
  <div class="min-h-screen bg-gray-50 pt-20 pb-10">
    <div class="max-w-7xl mx-auto px-6">
      <!-- 顶部介绍 -->
      <div class="mb-6">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-50 text-indigo-600 text-xs font-bold mb-3">
          <Sparkles class="w-4 h-4" />
          渤大校园事务 Agent
        </div>

        <h1 class="text-3xl font-black text-gray-900">
          问通知、查流程、生成待办
        </h1>

        <p class="text-gray-500 mt-2">
          导入渤海大学官网、教务处、就业网、学院通知等公开信息，帮你把复杂通知变成看得懂的行动清单。
        </p>
      </div>

      <div class="grid grid-cols-12 gap-6">
        <!-- 左侧：知识来源 -->
        <div class="col-span-12 lg:col-span-3 space-y-4">
          <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-5">
            <div class="flex items-center justify-between mb-4">
              <h2 class="font-bold text-gray-900">知识来源</h2>
              <span class="text-xs text-green-600 bg-green-50 px-2 py-1 rounded-full">
                可扩展
              </span>
            </div>

            <div class="space-y-3">
              <div
                  v-for="source in sources"
                  :key="source.name"
                  class="p-3 rounded-2xl bg-gray-50 border border-gray-100"
              >
                <div class="flex items-center gap-2">
                  <component :is="source.icon" class="w-4 h-4 text-indigo-500" />
                  <span class="font-bold text-sm text-gray-800">{{ source.name }}</span>
                </div>

                <p class="text-xs text-gray-500 mt-1">
                  {{ source.desc }}
                </p>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-5">
            <h2 class="font-bold text-gray-900 mb-3">Agent 能力</h2>

            <div class="space-y-2 text-sm text-gray-600">
              <div class="flex items-center gap-2">
                <ShieldCheck class="w-4 h-4 text-green-500" />
                来源可信度标注
              </div>

              <div class="flex items-center gap-2">
                <FileText class="w-4 h-4 text-blue-500" />
                通知三句话总结
              </div>

              <div class="flex items-center gap-2">
                <CalendarClock class="w-4 h-4 text-orange-500" />
                截止时间提取
              </div>

              <div class="flex items-center gap-2">
                <CheckSquare class="w-4 h-4 text-purple-500" />
                一键生成待办
              </div>

              <div class="flex items-center gap-2">
                <Sparkles class="w-4 h-4 text-indigo-500" />
                多专家协作与反思
              </div>
            </div>
          </div>
        </div>

        <!-- 中间：Agent 对话 -->
        <div class="col-span-12 lg:col-span-6">
          <div class="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden">
            <div class="p-5 border-b border-gray-100 bg-gradient-to-r from-indigo-50 to-sky-50">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-2xl bg-indigo-500 text-white flex items-center justify-center shadow-lg">
                  <Bot class="w-6 h-6" />
                </div>

                <div>
                  <h2 class="font-black text-gray-900">校园事务 Agent</h2>
                  <p class="text-sm text-gray-500">
                    你可以问校历、通知、办事流程、材料清单和截止时间
                  </p>
                </div>
              </div>
            </div>

            <!-- 快捷问题 -->
            <div class="p-5 border-b border-gray-100">
              <div class="grid grid-cols-2 gap-3">
                <button
                    v-for="item in quickQuestions"
                    :key="item"
                    @click="fillQuestion(item)"
                    :disabled="loading"
                    class="text-left p-3 rounded-2xl bg-gray-50 hover:bg-indigo-50 hover:text-indigo-600 transition text-sm font-bold text-gray-700 disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {{ item }}
                </button>
              </div>
            </div>

            <!-- 对话展示 -->
            <div class="p-5 space-y-4 min-h-[360px]">
              <!-- 开场白 -->
              <div class="flex gap-3">
                <div class="w-9 h-9 rounded-full bg-indigo-500 text-white flex items-center justify-center shrink-0">
                  <Bot class="w-5 h-5" />
                </div>

                <div class="bg-gray-50 rounded-2xl p-4 text-sm text-gray-700 leading-7">
                  你好，我是渤大校园事务 Agent。你可以问我：
                  <br />
                  <span class="font-bold text-gray-900">“最近学校有什么通知？”</span>
                  <br />
                  <span class="font-bold text-gray-900">“缓考怎么申请？”</span>
                  <br />
                  <span class="font-bold text-gray-900">“帮我把通知总结成三句话。”</span>
                </div>
              </div>

              <!-- 多轮消息 -->
              <div
                  v-for="message in chatMessages"
                  :key="message.id"
                  class="space-y-4"
              >
                <!-- 用户问题 -->
                <div
                    v-if="message.role === 'user'"
                    class="flex justify-end"
                >
                  <div class="max-w-[80%] bg-indigo-500 text-white rounded-2xl px-4 py-3 text-sm leading-7">
                    {{ message.content }}
                  </div>
                </div>

                <!-- Agent 回答 -->
                <div
                    v-else
                    class="flex gap-3"
                >
                  <div class="w-9 h-9 rounded-full bg-indigo-500 text-white flex items-center justify-center shrink-0">
                    <Bot class="w-5 h-5" />
                  </div>

                  <div class="flex-1">
                    <!-- 回答主体 -->
                    <div class="bg-indigo-50 rounded-2xl p-4 text-sm text-gray-800 leading-7">
                      <div class="flex items-center justify-between gap-3 mb-2">
                        <div class="font-black text-gray-900">
                          校园事务 Agent 回答：
                        </div>

                        <div
                            v-if="message.data.intent"
                            class="text-[11px] px-2 py-1 rounded-full bg-white text-indigo-600 font-bold"
                        >
                          {{ formatIntent(message.data.intent) }}
                        </div>
                      </div>

                      <div
                          v-if="message.data.confidence !== null && message.data.confidence !== undefined"
                          class="text-xs text-indigo-500 mb-2"
                      >
                        置信度：{{ formatConfidence(message.data.confidence) }}
                      </div>

                      <div class="agent-answer-text">
                        {{ message.data.answer }}
                      </div>
                    </div>

                    <!-- 来源卡片，只展示前 3 条，避免页面过长 -->
                    <div
                        v-if="message.data.sources && message.data.sources.length"
                        class="source-list"
                    >
                      <div
                          v-for="(source, index) in message.data.sources.slice(0, 3)"
                          :key="`${source.title || 'source'}-${index}`"
                          class="source-card"
                      >
                        <div class="source-title">
                          {{ index + 1 }}. {{ source.title || '未命名资料' }}
                        </div>

                        <div class="source-meta">
                          来源：{{ source.sourceName || '未知来源' }}
                          <span v-if="source.sourceType"> / {{ source.sourceType }}</span>
                          <span v-if="source.trustLevel"> / 可信度：{{ source.trustLevel }}</span>
                        </div>

                        <a
                            v-if="source.url"
                            class="source-link"
                            :href="source.url"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                          查看原通知
                        </a>
                      </div>
                    </div>

                    <!-- 待办建议 -->
                    <div
                        v-if="message.data.todos && message.data.todos.length"
                        class="todo-suggestion"
                    >
                      <div class="todo-title">
                        待办建议：
                      </div>

                      <div
                          v-for="(todo, index) in message.data.todos"
                          :key="`${todo.title || 'todo'}-${index}`"
                          class="todo-item"
                      >
                        <input
                            type="checkbox"
                            :checked="todo.done"
                            disabled
                        />
                        <span>{{ todo.title }}</span>
                      </div>
                    </div>

                    <!-- Agent 执行过程 -->
                    <div
                        v-if="message.data.expertTrace && message.data.expertTrace.length"
                        class="agent-trace-card"
                    >
                      <div class="trace-header">
                        <span>Agent 执行过程</span>

                        <span
                            class="risk-badge"
                            :class="getRiskBadgeClass(message.data.riskLevel)"
                        >
                          {{ message.data.riskLevel || 'LOW' }}
                        </span>
                      </div>

                      <div
                          v-if="message.data.reflectionSuggestion"
                          class="reflection-text"
                      >
                        反思结果：{{ message.data.reflectionSuggestion }}
                      </div>

                      <div class="trace-list">
                        <div
                            v-for="(trace, index) in message.data.expertTrace"
                            :key="`${trace}-${index}`"
                            class="trace-item"
                        >
                          {{ index + 1 }}. {{ formatTrace(trace) }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div ref="chatEndRef"></div>
            </div>

            <!-- 输入区 -->
            <div class="p-5 border-t border-gray-100 bg-gray-50">
              <div class="flex gap-3">
                <input
                    v-model="question"
                    class="flex-1 bg-white rounded-2xl px-4 py-3 outline-none border border-gray-100 focus:ring-2 focus:ring-indigo-200 text-sm"
                    placeholder="例如：最近学校有什么通知？缓考怎么申请？"
                    @keyup.enter="sendQuestion"
                />

                <button
                    @click="sendQuestion"
                    :disabled="loading"
                    class="px-5 py-3 rounded-2xl bg-indigo-500 text-white font-bold hover:bg-indigo-600 transition disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {{ loading ? '思考中...' : '发送' }}
                </button>
              </div>

              <p class="text-xs text-gray-400 mt-2">
                已接入校园知识库 RAG，回答会结合检索到的学校资料生成。
              </p>
            </div>
          </div>
        </div>

        <!-- 右侧：截止雷达 + 待办 -->
        <div class="col-span-12 lg:col-span-3 space-y-4">
          <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-5">
            <div class="flex items-center justify-between mb-4">
              <h2 class="font-bold text-gray-900">截止时间雷达</h2>
              <CalendarClock class="w-5 h-5 text-orange-500" />
            </div>

            <div class="space-y-3">
              <div
                  v-for="deadline in displayDeadlines"
                  :key="deadline.title"
                  class="p-3 rounded-2xl bg-orange-50 border border-orange-100"
              >
                <div class="text-xs text-orange-600 font-bold">
                  {{ deadline.time }}
                </div>

                <div class="font-bold text-sm text-gray-900 mt-1">
                  {{ deadline.title }}
                </div>

                <div class="text-xs text-gray-500 mt-1">
                  {{ deadline.source }}
                </div>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-5">
            <div class="flex items-center justify-between mb-4">
              <h2 class="font-bold text-gray-900">我的校园待办</h2>
              <CheckSquare class="w-5 h-5 text-indigo-500" />
            </div>

            <div class="space-y-3">
              <label
                  v-for="todo in displayTodos"
                  :key="todo"
                  class="flex items-center gap-3 p-3 rounded-2xl bg-gray-50 text-sm text-gray-700"
              >
                <input type="checkbox" class="rounded text-indigo-500" />
                <span>{{ todo }}</span>
              </label>
            </div>

            <button class="mt-4 w-full py-3 rounded-2xl bg-indigo-500 text-white text-sm font-bold hover:bg-indigo-600 transition">
              生成新的待办
            </button>
          </div>

          <div class="bg-white rounded-3xl border border-gray-100 shadow-sm p-5">
            <h2 class="font-bold text-gray-900 mb-3">小巧思</h2>

            <div class="space-y-2">
              <div
                  v-for="idea in ideas"
                  :key="idea"
                  class="text-xs px-3 py-2 rounded-xl bg-gray-50 text-gray-600"
              >
                {{ idea }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  Bot,
  BookOpen,
  Briefcase,
  Building2,
  CalendarClock,
  CheckSquare,
  FileText,
  Globe2,
  ShieldCheck,
  Sparkles
} from 'lucide-vue-next'
import { ref, computed, nextTick } from 'vue'
import { chatWithCampusAgent } from '@/api/campusAgent'

const question = ref('')
const demoAnswer = ref(null)
const loading = ref(false)
const chatMessages = ref([])
const chatEndRef = ref(null)

const sources = [
  {
    name: '教务处',
    desc: '校历、缓考、成绩证明、规章制度、下载表格',
    icon: BookOpen
  },
  {
    name: '就业网',
    desc: '招聘公告、宣讲信息、就业通知、实习信息',
    icon: Briefcase
  },
  {
    name: '学校官网',
    desc: '学校简介、教学单位、校园概况、官方公告',
    icon: Building2
  },
  {
    name: '学院通知',
    desc: '学院事务、活动通知、材料提交、竞赛报名',
    icon: Globe2
  }
]

const quickQuestions = [
  '最近学校有什么通知？',
  '缓考怎么申请？',
  '成绩证明去哪办？',
  '帮我把通知总结成三句话'
]

const defaultDeadlines = [
  {
    time: '3 天后',
    title: '实习备案材料提交',
    source: '来源：学院通知'
  },
  {
    time: '5 天后',
    title: '创新创业项目报名截止',
    source: '来源：就业 / 双创通知'
  },
  {
    time: '7 天后',
    title: '研究生材料汇总',
    source: '来源：研究生事务'
  }
]

const defaultTodos = [
  '查看缓考申请条件',
  '下载申请表',
  '准备学院审核材料'
]

const ideas = [
  '三句话看懂通知',
  '判断通知是否与我有关',
  '自动提取截止时间',
  '一键生成待办清单',
  '回答附带来源可信度',
  '展示 Agent 执行过程'
]

const displayTodos = computed(() => {
  if (demoAnswer.value?.todos?.length) {
    return demoAnswer.value.todos.map(todo => todo.title)
  }

  return defaultTodos
})

const displayDeadlines = computed(() => {
  if (demoAnswer.value?.deadlines?.length) {
    return demoAnswer.value.deadlines.map(item => ({
      time: item.deadline || '时间待确认',
      title: item.title || '未命名截止事项',
      source: `重要程度：${item.importance || '中'}`
    }))
  }

  return defaultDeadlines
})

const scrollToBottom = async () => {
  await nextTick()
  chatEndRef.value?.scrollIntoView({
    behavior: 'smooth',
    block: 'end'
  })
}

const formatIntent = (intent) => {
  const intentMap = {
    NOTICE_QUERY: '通知查询',
    SERVICE_GUIDE: '办事流程',
    DEADLINE_QUERY: '时间截止',
    RELATED_CHECK: '相关性判断',
    NOTICE_SUMMARY: '通知总结',
    GENERAL_QA: '通用问答',
    ERROR: '请求异常'
  }

  return intentMap[intent] || intent || '未知意图'
}

const formatTrace = (trace) => {
  if (!trace) {
    return ''
  }

  return trace
      .replaceAll('RouterExpert', '意图识别专家')
      .replaceAll('KnowledgeExpert', '知识检索专家')
      .replaceAll('NoticeExpert', '通知查询专家')
      .replaceAll('ReflectionExpert', '反思校验专家')
      .replaceAll('Orchestrator', '智能体编排器')
      .replaceAll('MemoryService', '记忆服务')
}

const formatConfidence = (value) => {
  if (value === null || value === undefined || value === '') {
    return '未知'
  }

  if (typeof value === 'number') {
    return `${Math.round(value * 100)}%`
  }

  return value
}

const getRiskBadgeClass = (riskLevel) => {
  const risk = riskLevel || 'LOW'

  if (risk === 'HIGH') {
    return 'risk-high'
  }

  if (risk === 'MEDIUM') {
    return 'risk-medium'
  }

  return 'risk-low'
}

const fillQuestion = (text) => {
  question.value = text
  sendQuestion()
}

const normalizeAgentData = (res) => {
  if (res?.answer) {
    return res
  }

  if (res?.data?.answer) {
    return res.data
  }

  if (res?.data?.data?.answer) {
    return res.data.data
  }

  return res?.data || res
}

const sendQuestion = async () => {
  const currentQuestion = question.value.trim()

  if (!currentQuestion || loading.value) {
    return
  }

  loading.value = true
  question.value = ''

  chatMessages.value.push({
    id: Date.now() + '-user',
    role: 'user',
    content: currentQuestion
  })

  await scrollToBottom()

  try {
    const res = await chatWithCampusAgent({
      question: currentQuestion
    })

    const data = normalizeAgentData(res)

    const answerData = {
      answer: data.answer || '暂时没有生成回答。',
      intent: data.intent,
      confidence: data.confidence,
      sources: data.sources || [],
      todos: data.todos || [],
      deadlines: data.deadlines || [],
      reflectionPassed: data.reflectionPassed,
      riskLevel: data.riskLevel,
      reflectionSuggestion: data.reflectionSuggestion,
      expertTrace: data.expertTrace || []
    }

    demoAnswer.value = {
      title: '校园事务 Agent 回答：',
      ...answerData
    }

    chatMessages.value.push({
      id: Date.now() + '-assistant',
      role: 'assistant',
      data: answerData
    })
  } catch (e) {
    console.error(e)

    const errorData = {
      answer: '校园 Agent 暂时无法回答，请稍后再试。',
      intent: 'ERROR',
      confidence: null,
      sources: [],
      todos: [],
      deadlines: [],
      reflectionPassed: false,
      riskLevel: 'UNKNOWN',
      reflectionSuggestion: '接口请求失败',
      expertTrace: []
    }

    demoAnswer.value = {
      title: '请求失败',
      ...errorData
    }

    chatMessages.value.push({
      id: Date.now() + '-assistant-error',
      role: 'assistant',
      data: errorData
    })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}
</script>

<style scoped>
.agent-answer-text {
  white-space: pre-line;
  line-height: 1.8;
  font-size: 14px;
  color: #1f2937;
}

.source-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.source-card {
  padding: 10px 12px;
  border-radius: 14px;
  background: #ecfdf5;
  border: 1px solid #bbf7d0;
}

.source-title {
  font-size: 13px;
  font-weight: 700;
  color: #065f46;
  margin-bottom: 5px;
  line-height: 1.45;
}

.source-meta {
  font-size: 12px;
  color: #047857;
  margin-bottom: 4px;
}

.source-link {
  display: inline-block;
  margin-top: 4px;
  font-size: 12px;
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
}

.source-link:hover {
  text-decoration: underline;
}

.todo-suggestion {
  margin-top: 14px;
}

.todo-title {
  font-size: 13px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 9px 10px;
  border-radius: 12px;
  background: #eef2ff;
  color: #3730a3;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 6px;
}

.todo-item input {
  margin-top: 3px;
}

.agent-trace-card {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.trace-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.risk-badge {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.risk-low {
  background: #dcfce7;
  color: #166534;
}

.risk-medium {
  background: #fef3c7;
  color: #92400e;
}

.risk-high {
  background: #fee2e2;
  color: #991b1b;
}

.reflection-text {
  font-size: 12px;
  color: #4b5563;
  margin-bottom: 8px;
  line-height: 1.6;
}

.trace-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.trace-item {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
}
</style>