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
                    class="text-left p-3 rounded-2xl bg-gray-50 hover:bg-indigo-50 hover:text-indigo-600 transition text-sm font-bold text-gray-700"
                >
                  {{ item }}
                </button>
              </div>
            </div>

            <!-- 对话展示 -->
            <div class="p-5 space-y-4 min-h-[360px]">
              <div class="flex gap-3">
                <div class="w-9 h-9 rounded-full bg-indigo-500 text-white flex items-center justify-center shrink-0">
                  <Bot class="w-5 h-5" />
                </div>

                <div class="bg-gray-50 rounded-2xl p-4 text-sm text-gray-700 leading-7">
                  你好，我是渤大校园事务 Agent。你可以问我：
                  <br />
                  <span class="font-bold text-gray-900">“缓考怎么申请？”</span>
                  <br />
                  <span class="font-bold text-gray-900">“这条通知和我有关吗？”</span>
                  <br />
                  <span class="font-bold text-gray-900">“帮我把通知总结成三句话。”</span>
                </div>
              </div>

              <div
                  v-if="demoAnswer"
                  class="flex gap-3"
              >
                <div class="w-9 h-9 rounded-full bg-indigo-500 text-white flex items-center justify-center shrink-0">
                  <Bot class="w-5 h-5" />
                </div>

                <div class="flex-1">
                  <div class="bg-indigo-50 rounded-2xl p-4 text-sm text-gray-800 leading-7">
                    <div class="font-black text-gray-900 mb-2">
                      {{ demoAnswer.title }}
                    </div>

                    <div class="whitespace-pre-line">
                      {{ demoAnswer.answer }}
                    </div>
                  </div>

                  <div v-if="demoAnswer.sources?.length" class="mt-3 space-y-2">
                    <div
                        v-for="source in demoAnswer.sources"
                        :key="source.title"
                        class="px-3 py-2 rounded-xl bg-green-50 text-green-700 text-xs"
                    >
                      来源：{{ source.sourceName }} / {{ source.title }} / 可信度：{{ source.trustLevel }}
                    </div>
                  </div>
                  <div v-if="demoAnswer.todos?.length" class="mt-3 space-y-2">
                    <div class="font-bold text-sm text-gray-900">待办建议：</div>
                    <div
                        v-for="todo in demoAnswer.todos"
                        :key="todo.title"
                        class="px-3 py-2 rounded-xl bg-indigo-50 text-indigo-700 text-xs"
                    >
                      □ {{ todo.title }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 输入区 -->
            <div class="p-5 border-t border-gray-100 bg-gray-50">
              <div class="flex gap-3">
                <input
                    v-model="question"
                    class="flex-1 bg-white rounded-2xl px-4 py-3 outline-none border border-gray-100 focus:ring-2 focus:ring-indigo-200 text-sm"
                    placeholder="例如：缓考怎么申请？成绩证明去哪办？"
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
import { ref, computed } from 'vue'
import { chatWithCampusAgent } from '@/api/campusAgent'
const question = ref('')
const demoAnswer = ref(null)

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
  '缓考怎么申请？',
  '成绩证明去哪办？',
  '这条通知和我有关吗？',
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
  '回答附带来源可信度'
]

const fillQuestion = (text) => {
  question.value = text
  sendQuestion()
}
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
const loading = ref(false)

const sendQuestion = async () => {
  if (!question.value.trim()) {
    return
  }

  loading.value = true

  try {
    const res = await chatWithCampusAgent({
      question: question.value
    })

    const data = res.data || res

    demoAnswer.value = {
      title: '校园事务 Agent 回答：',
      answer: data.answer,
      intent: data.intent,
      confidence: data.confidence,
      sources: data.sources || [],
      todos: data.todos || [],
      deadlines: data.deadlines || []
    }
  } catch (e) {
    console.error(e)
    demoAnswer.value = {
      title: '请求失败',
      answer: '校园 Agent 暂时无法回答，请稍后再试。',
      sources: [],
      todos: [],
      deadlines: []
    }
  } finally {
    loading.value = false
  }
}
</script>