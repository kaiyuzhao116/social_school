<template>
  <div class="flex flex-col h-full max-h-[85vh]">
    <!-- Header -->
    <div class="flex justify-between items-center px-5 py-4 border-b border-gray-100 dark:border-gray-700 bg-gradient-to-r from-brand-purple/5 to-blue-500/5 dark:from-brand-purple/10 dark:to-blue-500/10 shrink-0">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-brand-purple to-blue-500 flex items-center justify-center text-white">
          <PenSquare class="w-5 h-5" />
        </div>
        <div>
          <h2 class="text-lg font-bold text-gray-900 dark:text-white">{{ isEditing ? '编辑帖子' : '创建新帖子' }}</h2>
          <p class="text-xs text-gray-400">分享你的精彩瞬间</p>
        </div>
      </div>
      <button @click="uiStore.closeModal()" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full text-gray-500"><X class="w-5 h-5" /></button>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto p-5 space-y-4 custom-scrollbar">
      <!-- User Info -->
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <img :src="getAvatarUrl(userStore.currentUser?.avatar, userStore.currentUser?.name)" @error="handleAvatarError" class="w-11 h-11 rounded-full object-cover ring-2 ring-brand-purple/20" />
          <div>
            <p class="font-bold text-gray-900 dark:text-white text-sm">{{ userStore.currentUser?.name }}</p>
            <div class="flex items-center gap-1 text-xs text-gray-400"><Globe class="w-3 h-3" /> 公开发布</div>
          </div>
        </div>
        <!-- AI Assistant Button -->
        <button @click="showAIPanel = !showAIPanel" :class="['flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium transition-all', showAIPanel ? 'bg-gradient-to-r from-brand-purple to-blue-500 text-white shadow-lg shadow-brand-purple/25' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600']">
          <Sparkles class="w-4 h-4" /> AI 助手
        </button>
      </div>

      <!-- Draft Banner -->
      <div v-if="hasDraft" class="flex items-center justify-between p-3 bg-amber-50 dark:bg-amber-900/20 rounded-xl border border-amber-200 dark:border-amber-800/30">
        <div class="flex items-center gap-2 text-amber-700 dark:text-amber-400">
          <FileText class="w-4 h-4" />
          <span class="text-sm font-medium">检测到未完成的草稿</span>
        </div>
        <div class="flex items-center gap-2">
          <button @click="loadDraft" class="px-3 py-1 text-xs font-medium bg-amber-500 text-white rounded-full hover:bg-amber-600 transition-colors">恢复草稿</button>
          <button @click="discardDraft" class="px-3 py-1 text-xs font-medium text-amber-600 dark:text-amber-400 hover:underline">丢弃</button>
        </div>
      </div>

      <!-- AI Panel -->
      <div v-if="showAIPanel" class="bg-gradient-to-br from-brand-purple/5 to-blue-500/5 dark:from-brand-purple/10 dark:to-blue-500/10 rounded-2xl p-4 border border-brand-purple/20">
        <div class="flex items-center gap-2 mb-3">
          <Sparkles class="w-4 h-4 text-brand-purple" />
          <span class="text-sm font-bold text-gray-800 dark:text-white">AI 写作助手</span>
          <span class="text-[10px] px-2 py-0.5 bg-brand-purple/20 text-brand-purple rounded-full">Beta</span>
        </div>
        <div class="grid grid-cols-2 gap-2 mb-3">
          <button @click="aiAction('polish')" :disabled="aiLoading || !content.trim()" class="flex items-center justify-center gap-2 px-3 py-2.5 bg-white dark:bg-gray-800 rounded-xl text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 border border-gray-200 dark:border-gray-600 disabled:opacity-50 transition-all">
            <Wand2 class="w-4 h-4 text-purple-500" /> 润色优化
          </button>
          <button @click="aiAction('expand')" :disabled="aiLoading || !content.trim()" class="flex items-center justify-center gap-2 px-3 py-2.5 bg-white dark:bg-gray-800 rounded-xl text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 border border-gray-200 dark:border-gray-600 disabled:opacity-50 transition-all">
            <ArrowUpRight class="w-4 h-4 text-blue-500" /> 扩写内容
          </button>
          <button @click="aiAction('summarize')" :disabled="aiLoading || !content.trim()" class="flex items-center justify-center gap-2 px-3 py-2.5 bg-white dark:bg-gray-800 rounded-xl text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 border border-gray-200 dark:border-gray-600 disabled:opacity-50 transition-all">
            <AlignLeft class="w-4 h-4 text-green-500" /> 精简总结
          </button>
          <button @click="aiAction('rewrite')" :disabled="aiLoading || !content.trim()" class="flex items-center justify-center gap-2 px-3 py-2.5 bg-white dark:bg-gray-800 rounded-xl text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 border border-gray-200 dark:border-gray-600 disabled:opacity-50 transition-all">
            <RotateCcw class="w-4 h-4 text-orange-500" /> 换种说法
          </button>
        </div>
        <!-- AI Generate -->
        <div class="flex gap-2">
          <input v-model="aiPrompt" type="text" placeholder="描述你想写的内容，让AI帮你生成..." class="flex-1 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-600 rounded-xl px-3 py-2 text-sm focus:ring-2 focus:ring-brand-purple outline-none" @keydown.enter="aiAction('generate')" />
          <button @click="aiAction('generate')" :disabled="aiLoading || !aiPrompt.trim()" class="px-4 py-2 bg-gradient-to-r from-brand-purple to-blue-500 text-white rounded-xl text-sm font-medium hover:shadow-lg hover:shadow-brand-purple/25 disabled:opacity-50 transition-all">
            <Send class="w-4 h-4" />
          </button>
        </div>
        <!-- Loading -->
        <div v-if="aiLoading" class="mt-3 flex items-center gap-2 text-sm text-brand-purple">
          <Loader2 class="w-4 h-4 animate-spin" /> AI 正在思考中...
        </div>
        <!-- Error -->
        <div v-if="aiError" class="mt-3 text-sm text-red-500">{{ aiError }}</div>
      </div>

      <!-- Title -->
      <div class="relative">
        <input v-model="title" type="text" placeholder="添加一个吸引人的标题..." class="w-full bg-gray-50 dark:bg-gray-700/50 border-0 rounded-xl px-4 py-3.5 text-base font-medium focus:ring-2 focus:ring-brand-purple outline-none transition-all placeholder:text-gray-400" />
        <span class="absolute right-3 top-3.5 text-xs text-gray-400">{{ title.length }}/50</span>
      </div>

      <!-- Rich Text Toolbar -->
      <div class="flex items-center gap-1 p-2 bg-gray-50 dark:bg-gray-700/50 rounded-xl">
        <button @click="formatText('bold')" class="p-2 hover:bg-white dark:hover:bg-gray-600 rounded-lg text-gray-500 hover:text-gray-700 dark:hover:text-white transition-colors" title="加粗"><Bold class="w-4 h-4" /></button>
        <button @click="formatText('italic')" class="p-2 hover:bg-white dark:hover:bg-gray-600 rounded-lg text-gray-500 hover:text-gray-700 dark:hover:text-white transition-colors" title="斜体"><Italic class="w-4 h-4" /></button>
        <button @click="formatText('underline')" class="p-2 hover:bg-white dark:hover:bg-gray-600 rounded-lg text-gray-500 hover:text-gray-700 dark:hover:text-white transition-colors" title="下划线"><Underline class="w-4 h-4" /></button>
        <div class="w-px h-5 bg-gray-300 dark:bg-gray-600 mx-1"></div>
        <button @click="formatText('list')" class="p-2 hover:bg-white dark:hover:bg-gray-600 rounded-lg text-gray-500 hover:text-gray-700 dark:hover:text-white transition-colors" title="列表"><List class="w-4 h-4" /></button>
        <button @click="formatText('quote')" class="p-2 hover:bg-white dark:hover:bg-gray-600 rounded-lg text-gray-500 hover:text-gray-700 dark:hover:text-white transition-colors" title="引用"><Quote class="w-4 h-4" /></button>
        <div class="w-px h-5 bg-gray-300 dark:bg-gray-600 mx-1"></div>
        <div class="relative group/emoji">
          <button class="p-2 hover:bg-white dark:hover:bg-gray-600 rounded-lg text-gray-500 hover:text-gray-700 dark:hover:text-white transition-colors" title="表情"><Smile class="w-4 h-4" /></button>
          <div class="absolute left-0 top-full mt-1 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 p-2 invisible group-hover/emoji:visible opacity-0 group-hover/emoji:opacity-100 transition-all z-20 w-[240px]">
            <div class="grid grid-cols-8 gap-1">
              <button v-for="emoji in emojiList" :key="emoji" @click="insertEmoji(emoji)" class="w-7 h-7 flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-700 rounded text-lg transition-colors">{{ emoji }}</button>
            </div>
          </div>
        </div>
        <button @click="triggerImageUpload" class="p-2 hover:bg-white dark:hover:bg-gray-600 rounded-lg text-gray-500 hover:text-gray-700 dark:hover:text-white transition-colors" title="图片"><ImageIcon class="w-4 h-4" /></button>
        <div class="flex-1"></div>
        <span class="text-xs text-gray-400">{{ content.length }} 字</span>
      </div>

      <!-- Content Editor -->
      <div ref="editorRef" contenteditable="true" @input="handleEditorInput" @paste="handlePaste" class="w-full min-h-[180px] max-h-[300px] overflow-y-auto bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-brand-purple focus:border-transparent outline-none transition-all leading-relaxed custom-scrollbar" :data-placeholder="'分享你的想法、经历或见闻...\n\n支持富文本格式，可使用工具栏进行排版'"></div>

      <!-- Tags -->
      <div class="space-y-2">
        <div class="flex items-center gap-2">
          <Hash class="w-4 h-4 text-gray-400" />
          <span class="text-sm font-medium text-gray-600 dark:text-gray-400">话题标签</span>
        </div>
        <div class="flex flex-wrap gap-2">
          <span v-for="tag in tags" :key="tag" class="px-3 py-1.5 rounded-full text-xs font-medium bg-gradient-to-r from-brand-purple/10 to-blue-500/10 text-brand-purple flex items-center gap-1.5 group">
            #{{ tag }}
            <button @click="removeTag(tag)" class="opacity-50 group-hover:opacity-100 hover:text-red-500 transition-opacity"><X class="w-3 h-3" /></button>
          </span>
          <div class="relative">
            <input v-model="tagInput" @keydown.enter.prevent="addTag" type="text" placeholder="+ 添加标签" class="w-24 bg-transparent border-0 text-sm text-gray-500 focus:outline-none focus:w-32 transition-all placeholder:text-gray-400" />
          </div>
        </div>
        <!-- Quick Tags -->
        <div class="flex flex-wrap gap-1.5">
          <button v-for="qt in quickTags" :key="qt" @click="addQuickTag(qt)" :class="['px-2 py-1 rounded-full text-[10px] transition-all', tags.includes(qt) ? 'bg-brand-purple text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-500 hover:bg-gray-200 dark:hover:bg-gray-600']">{{ qt }}</button>
        </div>
      </div>

      <!-- Image Preview -->
      <div v-if="unifiedImages.length > 0" class="space-y-2">
        <div class="flex items-center gap-2">
          <ImageIcon class="w-4 h-4 text-gray-400" />
          <span class="text-sm font-medium text-gray-600 dark:text-gray-400">图片 ({{ unifiedImages.length }}/9)</span>
        </div>
        <div class="grid grid-cols-3 gap-2">
          <div v-for="(img, index) in unifiedImages" :key="index" class="relative rounded-xl overflow-hidden aspect-square group">
            <img :src="img.url" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
              <button @click="removeImage(index)" class="p-2 bg-white/20 backdrop-blur rounded-full text-white hover:bg-red-500 transition-colors"><Trash2 class="w-4 h-4" /></button>
            </div>
          </div>
          <label v-if="unifiedImages.length < 9" class="aspect-square rounded-xl border-2 border-dashed border-gray-200 dark:border-gray-600 flex flex-col items-center justify-center cursor-pointer hover:border-brand-purple hover:bg-brand-purple/5 transition-all">
            <Plus class="w-6 h-6 text-gray-400" />
            <span class="text-xs text-gray-400 mt-1">添加</span>
            <input type="file" accept="image/*" multiple class="hidden" @change="handleImageUpload" />
          </label>
        </div>
      </div>

      <!-- Add Image (when empty) -->
      <label v-if="unifiedImages.length === 0" class="block border-2 border-dashed border-gray-200 dark:border-gray-600 rounded-xl p-6 text-center cursor-pointer hover:border-brand-purple hover:bg-brand-purple/5 transition-all group">
        <div class="w-12 h-12 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center mx-auto mb-3 group-hover:bg-brand-purple/10 transition-colors">
          <ImageIcon class="w-6 h-6 text-gray-400 group-hover:text-brand-purple transition-colors" />
        </div>
        <p class="text-sm font-medium text-gray-600 dark:text-gray-400">点击或拖拽添加图片</p>
        <p class="text-xs text-gray-400 mt-1">支持 JPG、PNG，最多9张</p>
        <input ref="fileInput" type="file" accept="image/*" multiple class="hidden" @change="handleImageUpload" />
      </label>
    </div>

    <!-- Footer -->
    <div class="p-4 border-t border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 shrink-0">
      <div class="flex items-center gap-3">
        <button @click="saveDraft" :disabled="draftSaved || (!title.trim() && !content.trim() && tags.length === 0 && unifiedImages.length === 0)" :class="['px-4 py-2.5 rounded-xl text-sm font-medium transition-all flex items-center gap-2', draftSaved ? 'bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50']">
          <Check v-if="draftSaved" class="w-4 h-4" />
          {{ draftSaved ? '已保存' : '保存草稿' }}
        </button>
        <button @click="handleSubmit" :disabled="!content.trim()" class="flex-1 bg-gradient-to-r from-brand-purple to-blue-500 text-white py-2.5 rounded-xl font-bold hover:shadow-lg hover:shadow-brand-purple/25 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none">
          {{ isEditing ? '保存修改' : '发布帖子' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useUserStore, useUIStore, usePostStore } from '../../stores'
import { X, Image as ImageIcon, PenSquare, Globe, Sparkles, Wand2, ArrowUpRight, AlignLeft, RotateCcw, Send, Loader2, Bold, Italic, Underline, List, Quote, Smile, Hash, Plus, Trash2, FileText, Check } from 'lucide-vue-next'

const userStore = useUserStore()
const uiStore = useUIStore()
const postStore = usePostStore()

const title = ref('')
const content = ref('')
const tags = ref([])
const tagInput = ref('')
const editorRef = ref(null)

const showAIPanel = ref(false)
const aiPrompt = ref('')
const aiLoading = ref(false)
const aiError = ref('')

const quickTags = ['校园生活', '学习交流', '活动分享', '美食推荐', '失物招领', '二手交易', '组队', '提问']

const emojiList = [
  '😊', '😄', '😁', '🤣', '😂', '😅', '😉', '😍',
  '🥰', '😘', '😋', '😎', '🤔', '😏', '😴', '😭',
  '😤', '😱', '🤗', '🤭', '🤫', '🤨', '😐', '😑',
  '👍', '👎', '👏', '🙌', '🤝', '✌️', '🤞', '💪',
  '❤️', '🧡', '💛', '💚', '💜', '🖤', '💔',
  '🔥', '✨', '⭐', '🌟', '💯', '💢', '💥', '💫',
  '🎉', '🎊', '🎁', '🏆', '🎯', '📚', '✏️', '💻'
]

// SiliconFlow API - 从环境变量读取
const SILICON_API_KEY = import.meta.env.VITE_SILICON_API_KEY
const SILICON_API_URL = import.meta.env.VITE_AI_API_URL || 'https://api.siliconflow.cn/v1/chat/completions'
const SILICON_MODEL = import.meta.env.VITE_AI_MODEL || 'THUDM/GLM-4.1V-9B-Thinking'

const postToEdit = computed(() => uiStore.modalData.data)
const isEditing = computed(() => !!postToEdit.value?.id)
const hasDraft = ref(false)
const draftSaved = ref(false)

const isSubmitting = ref(false)
const fileInput = ref(null)
import { uploadApi } from '../../api'

// ... 这里需要重构 images 的数据结构，为了稳健性，我将重写 script 部分的关键逻辑

// ======================= 新的逻辑开始 =======================
// 重构：使用 unifiedList 存储图片，包含 { url, file }
const unifiedImages = ref([]) // { url: string, file?: File }[]

// 计算属性，兼容旧代码的 images
const imagesComp = computed(() => unifiedImages.value.map(item => item.url))

onMounted(() => {
  if (postToEdit.value) {
    title.value = postToEdit.value.title || ''
    content.value = postToEdit.value.content || ''
    tags.value = [...(postToEdit.value.tags || [])]
    // 编辑模式下的图片只有 URL
    unifiedImages.value = (postToEdit.value.images || []).map(url => ({ url }))
  } else {
    // Check for saved draft
    const savedDraft = localStorage.getItem('postDraft')
    if (savedDraft) {
      try {
        const draft = JSON.parse(savedDraft)
        if (draft.title?.trim() || draft.content?.trim() || draft.tags?.length > 0 || draft.images?.length > 0) {
          hasDraft.value = true
        }
      } catch (e) {
        localStorage.removeItem('postDraft')
      }
    }
  }
  nextTick(() => {
    if (editorRef.value && content.value) {
      editorRef.value.innerHTML = content.value
    }
  })
})

function loadDraft() {
  const savedDraft = localStorage.getItem('postDraft')
  if (savedDraft) {
    try {
      const draft = JSON.parse(savedDraft)
      title.value = draft.title || ''
      content.value = draft.content || ''
      tags.value = draft.tags || []
      // 草稿里的图片只有 URL (blob URL 可能已过期，这里假设草稿不存 blob)
      // 如果草稿存了 blob URL，那是没办法恢复的。所以通常草稿不存未上传的图片，或者忽略图片。
      // 这里简单处理：忽略图片或只恢复 URL
      unifiedImages.value = (draft.images || []).map(url => ({ url }))
      
      if (editorRef.value) editorRef.value.innerText = content.value
      hasDraft.value = false
    } catch (e) {
      console.error('Failed to load draft', e)
    }
  }
}

function discardDraft() {
  localStorage.removeItem('postDraft')
  hasDraft.value = false
}

function handleEditorInput(e) {
  content.value = e.target.innerText
}

function handlePaste(e) {
  e.preventDefault()
  const text = e.clipboardData.getData('text/plain')
  document.execCommand('insertText', false, text)
}

function formatText(format) {
  switch (format) {
    case 'bold': document.execCommand('bold'); break
    case 'italic': document.execCommand('italic'); break
    case 'underline': document.execCommand('underline'); break
    case 'list': document.execCommand('insertUnorderedList'); break
    case 'quote':
      const selection = window.getSelection()
      if (selection.rangeCount > 0) {
        const text = selection.toString()
        document.execCommand('insertHTML', false, `<blockquote style="border-left: 3px solid #6366f1; padding-left: 12px; color: #6b7280; margin: 8px 0;">${text || '引用内容'}</blockquote>`)
      }
      break
  }
  editorRef.value?.focus()
}

function insertEmoji(emoji) {
  editorRef.value?.focus()
  document.execCommand('insertText', false, emoji)
}

async function aiAction(action) {
  aiLoading.value = true
  aiError.value = ''
  
  let prompt = ''
  const currentContent = content.value.trim()
  
  switch (action) {
    case 'polish':
      prompt = `请帮我润色优化以下文字，使其更加生动有趣、表达更流畅，保持原意不变，直接返回优化后的内容：\n\n${currentContent}`
      break
    case 'expand':
      prompt = `请帮我扩写以下内容，增加更多细节和描述，使文章更加丰富完整，直接返回扩写后的内容：\n\n${currentContent}`
      break
    case 'summarize':
      prompt = `请帮我精简以下内容，保留核心信息，使表达更加简洁明了，直接返回精简后的内容：\n\n${currentContent}`
      break
    case 'rewrite':
      prompt = `请用不同的表达方式重写以下内容，保持原意但换一种说法，直接返回重写后的内容：\n\n${currentContent}`
      break
    case 'generate':
      prompt = `请根据以下描述，帮我写一段适合在校园社交平台发布的帖子内容，要求生动有趣、贴近学生生活，直接返回生成的内容：\n\n${aiPrompt.value}`
      break
  }

  try {
    const response = await fetch(SILICON_API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${SILICON_API_KEY}`
      },
      body: JSON.stringify({
        model: SILICON_MODEL,
        messages: [
          { role: 'system', content: '你是一个专业的写作助手，帮助用户优化和创作文字内容。请直接返回处理后的内容，不要添加额外的解释或标记。' },
          { role: 'user', content: prompt }
        ],
        max_tokens: 1024,
        temperature: 0.7
      })
    })

    if (!response.ok) throw new Error('API请求失败')
    
    const data = await response.json()
    const result = data.choices?.[0]?.message?.content?.trim()
    
    if (result) {
      content.value = result
      if (editorRef.value) editorRef.value.innerText = result
      if (action === 'generate') aiPrompt.value = ''
    }
  } catch (err) {
    aiError.value = '生成失败，请稍后重试'
    console.error('AI Error:', err)
  } finally {
    aiLoading.value = false
  }
}

function addTag() {
  const tag = tagInput.value.trim()
  if (tag && !tags.value.includes(tag) && tags.value.length < 5) {
    tags.value.push(tag)
    tagInput.value = ''
  }
}

function addQuickTag(tag) {
  if (tags.value.includes(tag)) {
    tags.value = tags.value.filter(t => t !== tag)
  } else if (tags.value.length < 5) {
    tags.value.push(tag)
  }
}

function removeTag(tag) {
  tags.value = tags.value.filter(t => t !== tag)
}

function triggerImageUpload() {
  fileInput.value?.click()
}

function handleImageUpload(e) {
  const files = e.target.files
  for (const file of files) {
    if (unifiedImages.value.length >= 9) break
    const url = URL.createObjectURL(file)
    unifiedImages.value.push({ url, file })
  }
  e.target.value = ''
}

function removeImage(index) {
  unifiedImages.value.splice(index, 1)
}

// 替换原来的 images.value 为 imagesComp
// 由于 computed 是只读的，所以模板里的 images 需要改为统一使用 unifiedImages 并修改模板访问方式

async function handleSubmit() {
  if (isSubmitting.value) return
  isSubmitting.value = true

  try {
    const finalImageUrls = []
    
    // 1. 分离出需要上传的文件和已有的 URL
    const filesToUpload = []
    const existingUrls = []
    
    // 保持顺序
    const placeholderIndices = []
    
    unifiedImages.value.forEach((item, index) => {
      if (item.file) {
        filesToUpload.push(item.file)
        placeholderIndices.push(index)
      } else {
        existingUrls[index] = item.url
      }
    })
    
    // 2. 批量上传新图片
    if (filesToUpload.length > 0) {
      // 假设 uploadImages 返回 URL 数组
      const res = await uploadApi.uploadImages(filesToUpload)
      const uploadedUrls = res.data?.urls || [] // 修正：后端返回的是 { data: { urls: [...] } }
      
      // 填充回对应的位置
      uploadedUrls.forEach((url, i) => {
        existingUrls[placeholderIndices[i]] = url
      })
    }
    
    // 3. 构建最终 URL 列表 (去除空位)
    const finalImages = []
    for (let i = 0; i < unifiedImages.value.length; i++) {
      finalImages.push(existingUrls[i])
    }

    // 4. 提交或更新帖子
    const postData = {
      title: title.value,
      content: content.value,
      tags: tags.value,
      images: finalImages
    }

    if (isEditing.value) {
      await postStore.updatePost(postToEdit.value.id, postData)
    } else {
      await postStore.createPost(postData, userStore.currentUser)
    }
    
    localStorage.removeItem('postDraft')
    uiStore.closeModal()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    isSubmitting.value = false
  }
}


// 使用公共头像工具函数
import { getAvatarUrl, handleAvatarError } from '../../utils/avatar'
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 2px; }

[contenteditable]:empty:before {
  content: attr(data-placeholder);
  color: #9ca3af;
  white-space: pre-wrap;
  pointer-events: none;
}

[contenteditable] blockquote {
  border-left: 3px solid #6366f1;
  padding-left: 12px;
  color: #6b7280;
  margin: 8px 0;
}
</style>
