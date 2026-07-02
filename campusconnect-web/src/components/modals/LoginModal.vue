<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6" role="dialog" aria-modal="true">
    <!-- Backdrop -->
    <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" @click="uiStore.closeModal()">
    </div>

    <!-- Modal Container -->
    <div
      class="relative w-full max-w-5xl bg-white rounded-3xl shadow-2xl overflow-hidden flex flex-col md:flex-row min-h-[640px] animate-in fade-in zoom-in-95 duration-300">

      <!-- Close Button -->
      <button @click="uiStore.closeModal()"
        class="absolute top-5 right-5 z-20 p-2 rounded-full bg-white/50 hover:bg-white text-slate-500 hover:text-slate-800 backdrop-blur-md transition-all duration-200 shadow-sm">
        <X class="w-5 h-5" />
      </button>

      <!-- Left Panel: Visual Branding (Hidden on mobile) -->
      <div
        class="hidden md:flex md:w-5/12 relative bg-slate-900 text-white flex-col justify-end p-10 overflow-hidden group">
        <!-- Background Image -->
        <div class="absolute inset-0">
          <img src="/src/assets/campus-background.jpg" alt="Campus Life"
            class="w-full h-full object-cover opacity-90 transition-transform duration-700 group-hover:scale-105" />
          <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/40 to-transparent"></div>
        </div>

        <!-- Content -->
        <div class="relative z-10 space-y-4">
          <div
            class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 backdrop-blur-md border border-white/20 text-xs font-medium tracking-wide">
            <span class="w-1.5 h-1.5 rounded-full bg-indigo-400 animate-pulse"></span>
            CampusConnect 2.0
          </div>
          <h2 class="text-3xl font-bold tracking-tight text-white leading-tight">
            连接校园生活的<br />每一个精彩瞬间
          </h2>
          <p class="text-slate-300 text-sm leading-relaxed max-w-xs">
            加入我们，探索丰富多彩的校园社群，发现志同道合的伙伴，记录你的青春足迹。
          </p>
        </div>
      </div>

      <!-- Right Panel: Forms -->
      <div class="w-full md:w-7/12 bg-white flex flex-col justify-center p-8 sm:p-12 lg:p-16 relative">
        <div class="max-w-sm mx-auto w-full space-y-8">

          <!-- Header & Tabs -->
          <div class="text-center space-y-6">
            <div class="flex items-center justify-center p-1 bg-slate-100/80 rounded-2xl w-fit mx-auto">
              <button v-for="tab in ['login', 'register']" :key="tab" @click="switchView(tab)" :class="[
                'px-6 py-2.5 rounded-xl text-sm font-medium transition-all duration-300',
                view === tab
                  ? 'bg-white text-indigo-600 shadow-lg shadow-black/5 ring-1 ring-black/5'
                  : 'text-slate-500 hover:text-slate-700'
              ]">
                {{ tab === 'login' ? '快速登录' : '注册账号' }}
              </button>
            </div>

            <div class="space-y-1">
              <h1 class="text-2xl font-bold text-slate-900 tracking-tight">
                {{ viewTitle }}
              </h1>
              <p class="text-sm text-slate-500">{{ viewSubtitle }}</p>
            </div>
          </div>

          <!-- LOGIN FORM -->
          <form v-if="view === 'login'" @submit.prevent="handleLogin"
            class="space-y-5 animate-in slide-in-from-right-4 duration-300">
            <div class="space-y-4">
              <div class="space-y-2">
                <label class="text-xs font-semibold text-slate-500 uppercase tracking-widest ml-1">邮箱</label>
                <div class="relative group">
                  <Mail
                    class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                  <input type="email" v-model="loginForm.email"
                    class="w-full pl-12 pr-4 py-3.5 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium"
                    placeholder="请输入邮箱地址" />
                </div>
              </div>

              <div class="space-y-2">
                <label class="text-xs font-semibold text-slate-500 uppercase tracking-widest ml-1">密码</label>
                <div class="relative group">
                  <Lock
                    class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                  <input :type="showPassword ? 'text' : 'password'" v-model="loginForm.password"
                    class="w-full pl-12 pr-12 py-3.5 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium"
                    placeholder="请输入密码" />
                  <button type="button" @click="showPassword = !showPassword"
                    class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
                    <component :is="showPassword ? Eye : EyeOff" class="w-5 h-5" />
                  </button>
                </div>
                <div class="flex justify-end mt-1">
                  <button type="button" @click="switchView('forgot')"
                    class="text-xs font-medium text-indigo-500 hover:text-indigo-600 transition-colors">
                    忘记密码？
                  </button>
                </div>
              </div>
            </div>

            <ErrorMessage :msg="errorMsg" v-if="errorMsg" />

            <button
              class="w-full py-3.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-2xl font-semibold shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/40 transform active:scale-[0.98] transition-all duration-200 flex items-center justify-center gap-2"
              :disabled="loading">
              <Loader2 v-if="loading" class="w-5 h-5 animate-spin" />
              <span>{{ loading ? '验证中...' : '立即登录' }}</span>
            </button>
          </form>

          <!-- REGISTER FORM -->
          <form v-else-if="view === 'register'" @submit.prevent="handleRegister"
            class="space-y-5 animate-in slide-in-from-left-4 duration-300">
            <div class="grid grid-cols-2 gap-4">
              <div class="col-span-2 space-y-1.5">
                <div class="flex justify-between items-end ml-1">
                  <label class="text-xs font-semibold text-slate-500 uppercase tracking-widest">用户名</label>
                  <span class="text-[10px] text-slate-400 font-light">注册后不可修改</span>
                </div>
                <div class="relative group">
                  <User
                    class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                  <input type="text" v-model="regForm.username"
                    class="w-full pl-12 pr-4 py-3 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium"
                    placeholder="设置用户名" />
                </div>
                <p v-if="errors.username" class="text-xs text-red-500 ml-1">{{ errors.username }}</p>
              </div>

              <div class="col-span-2 space-y-1.5">
                <div class="flex justify-between items-end ml-1">
                  <label class="text-xs font-semibold text-slate-500 uppercase tracking-widest">邮箱</label>
                  <span class="text-[10px] text-slate-400 font-light">建议使用学校邮箱认证</span>
                </div>
                <div class="relative group">
                  <Mail
                    class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                  <input type="email" v-model="regForm.email"
                    class="w-full pl-12 pr-4 py-3 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium"
                    placeholder="campus@edu.cn" />
                </div>
                <p v-if="errors.email" class="text-xs text-red-500 ml-1">{{ errors.email }}</p>
              </div>

              <div class="space-y-1.5">
                <div class="relative group">
                  <Lock
                    class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                  <input :type="showRegPassword ? 'text' : 'password'" v-model="regForm.password"
                    class="w-full pl-12 pr-12 py-3 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium"
                    placeholder="设置密码" />
                  <button type="button" @click="showRegPassword = !showRegPassword"
                    class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
                    <component :is="showRegPassword ? Eye : EyeOff" class="w-5 h-5" />
                  </button>
                </div>
              </div>
              <div class="space-y-1.5">
                <div class="relative group">
                  <Lock
                    class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                  <input :type="showRegConfirmPassword ? 'text' : 'password'" v-model="regForm.confirmPassword"
                    class="w-full pl-12 pr-12 py-3 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium"
                    placeholder="确认密码" />
                  <button type="button" @click="showRegConfirmPassword = !showRegConfirmPassword"
                    class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
                    <component :is="showRegConfirmPassword ? Eye : EyeOff" class="w-5 h-5" />
                  </button>
                </div>
              </div>

              <div class="col-span-2 flex gap-3">
                <input type="text" v-model="regForm.captcha"
                  class="flex-1 px-4 py-3 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium uppercase text-center tracking-widest"
                  placeholder="验证码" maxlength="4" />
                <button type="button" @click="refreshCaptcha"
                  class="px-6 py-2 bg-slate-100 hover:bg-slate-200 border-0 rounded-2xl text-indigo-600 font-mono font-bold tracking-widest transition-colors">
                  {{ captchaCode }}
                </button>
              </div>
            </div>

            <ErrorMessage :msg="errorMsg" v-if="errorMsg" />
            <SuccessMessage :msg="successMsg" v-if="successMsg" />

            <button
              class="w-full py-3.5 bg-slate-900 hover:bg-slate-800 text-white rounded-2xl font-semibold shadow-lg shadow-slate-900/20 transform active:scale-[0.98] transition-all duration-200 flex items-center justify-center gap-2"
              :disabled="loading">
              <Loader2 v-if="loading" class="w-5 h-5 animate-spin" />
              <span>注册并加入</span>
            </button>
          </form>

          <!-- FORGOT PASSWORD FORM -->
          <form v-else-if="view === 'forgot'" @submit.prevent="handleForgotSubmit"
            class="space-y-6 animate-in slide-in-from-right-4 duration-300">

            <!-- 进度指示器 -->
            <div class="flex items-center justify-center gap-2 py-2">
              <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all', forgotStep >= 1 ? 'bg-indigo-600 text-white' : 'bg-slate-200 text-slate-500']">1</div>
              <div :class="['w-12 h-1 rounded-full transition-all', forgotStep >= 2 ? 'bg-indigo-600' : 'bg-slate-200']"></div>
              <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all', forgotStep >= 2 ? 'bg-indigo-600 text-white' : 'bg-slate-200 text-slate-500']">2</div>
              <div :class="['w-12 h-1 rounded-full transition-all', forgotStep >= 3 ? 'bg-indigo-600' : 'bg-slate-200']"></div>
              <div :class="['w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all', forgotStep >= 3 ? 'bg-indigo-600 text-white' : 'bg-slate-200 text-slate-500']">3</div>
            </div>

            <div class="space-y-4">
              <!-- Step 1: 输入邮箱 -->
              <div v-if="forgotStep === 1" class="space-y-4">
                <div class="text-center mb-4">
                  <p class="text-sm text-slate-600">请输入您的注册邮箱，我们将发送验证码</p>
                </div>
                <div class="space-y-2">
                  <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">注册邮箱</label>
                  <div class="relative group">
                    <Mail
                      class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                    <input type="email" v-model="forgotForm.email"
                      class="w-full pl-12 pr-4 py-3.5 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium"
                      placeholder="请输入您的注册邮箱" />
                  </div>
                </div>
                <button type="button" @click="sendResetCode"
                  class="w-full py-3.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-2xl font-semibold shadow-lg shadow-indigo-500/30 transform active:scale-[0.98] transition-all duration-200 flex items-center justify-center gap-2"
                  :disabled="loading || !forgotForm.email">
                  <Loader2 v-if="loading" class="w-5 h-5 animate-spin" />
                  <span>{{ loading ? '发送中...' : '发送验证码' }}</span>
                </button>
              </div>

              <!-- Step 2: 输入验证码 -->
              <div v-else-if="forgotStep === 2" class="space-y-4">
                <div class="text-center mb-4">
                  <p class="text-sm text-slate-600">验证码已发送至 <span class="font-medium text-indigo-600">{{ forgotForm.email }}</span></p>
                  <p class="text-xs text-slate-400 mt-1">请查收邮件并输入6位验证码</p>
                </div>
                <div class="space-y-2">
                  <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">验证码</label>
                  <div class="relative group">
                    <Key
                      class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                    <input type="text" v-model="forgotForm.code" maxlength="6"
                      class="w-full pl-12 pr-4 py-3.5 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium text-center tracking-[0.5em] text-lg"
                      placeholder="000000" />
                  </div>
                </div>
                <div class="flex gap-3">
                  <button type="button" @click="forgotStep = 1"
                    class="flex-1 py-3 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-2xl font-medium transition-colors">
                    上一步
                  </button>
                  <button type="button" @click="verifyResetCode"
                    class="flex-1 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-2xl font-semibold shadow-lg shadow-indigo-500/30 transition-all"
                    :disabled="loading || !forgotForm.code || forgotForm.code.length < 4">
                    <Loader2 v-if="loading" class="w-5 h-5 animate-spin inline mr-2" />
                    验证
                  </button>
                </div>
                <button type="button" @click="sendResetCode" :disabled="loading"
                  class="w-full text-center text-sm text-indigo-500 hover:text-indigo-600 font-medium">
                  没收到？重新发送
                </button>
              </div>

              <!-- Step 3: 设置新密码 -->
              <div v-else-if="forgotStep === 3" class="space-y-4">
                <div class="text-center mb-4">
                  <p class="text-sm text-slate-600">请设置您的新密码</p>
                </div>
                <div class="space-y-3">
                  <div class="space-y-2">
                    <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">新密码</label>
                    <div class="relative group">
                      <Lock
                        class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                      <input :type="showForgotPassword ? 'text' : 'password'" v-model="forgotForm.newPassword"
                        class="w-full pl-12 pr-12 py-3.5 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium"
                        placeholder="请输入新密码（至少6位）" />
                      <button type="button" @click="showForgotPassword = !showForgotPassword"
                        class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
                        <component :is="showForgotPassword ? Eye : EyeOff" class="w-5 h-5" />
                      </button>
                    </div>
                  </div>
                  <div class="space-y-2">
                    <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider ml-1">确认新密码</label>
                    <div class="relative group">
                      <Lock
                        class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                      <input :type="showForgotConfirmPassword ? 'text' : 'password'" v-model="forgotForm.confirmNewPassword"
                        class="w-full pl-12 pr-12 py-3.5 bg-slate-50 border-0 rounded-2xl text-slate-900 placeholder:text-slate-400 focus:bg-white focus:ring-2 focus:ring-indigo-500/20 transition-all font-medium"
                        placeholder="请再次输入新密码" />
                      <button type="button" @click="showForgotConfirmPassword = !showForgotConfirmPassword"
                        class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
                        <component :is="showForgotConfirmPassword ? Eye : EyeOff" class="w-5 h-5" />
                      </button>
                    </div>
                  </div>
                </div>
                <button type="submit"
                  class="w-full py-3.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-2xl font-semibold shadow-lg shadow-indigo-500/30 transform active:scale-[0.98] transition-all duration-200 flex items-center justify-center gap-2"
                  :disabled="loading">
                  <Loader2 v-if="loading" class="w-5 h-5 animate-spin" />
                  <span>{{ loading ? '重置中...' : '重置密码' }}</span>
                </button>
              </div>

              <ErrorMessage :msg="errorMsg" v-if="errorMsg" />
              <SuccessMessage :msg="successMsg" v-if="successMsg" />

              <!-- 底部返回登录链接 -->
              <div class="text-center pt-2">
                <span class="text-sm text-slate-500">想起密码了？</span>
                <button type="button" @click="switchView('login')" class="text-sm font-medium text-indigo-600 hover:text-indigo-700 ml-1">
                  返回登录
                </button>
              </div>
            </div>
          </form>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, h } from 'vue'
import { useUserStore, useUIStore } from '../../stores'
import { X, User, Mail, Lock, Key, Eye, EyeOff, Loader2, ArrowLeft } from 'lucide-vue-next'
import { useFormValidation, useCaptcha } from '../../composables'

// Components for messages
const ErrorMessage = (props) => h('div', {
  class: 'p-3 rounded-xl bg-red-50 text-red-500 text-xs font-medium flex items-center gap-2 animate-in fade-in slide-in-from-top-2'
}, [h('span', { class: 'w-1.5 h-1.5 rounded-full bg-red-500' }), props.msg])

const SuccessMessage = (props) => h('div', {
  class: 'p-3 rounded-xl bg-green-50 text-green-600 text-xs font-medium flex items-center gap-2 animate-in fade-in slide-in-from-top-2'
}, [h('span', { class: 'w-1.5 h-1.5 rounded-full bg-green-500' }), props.msg])

const userStore = useUserStore()
const uiStore = useUIStore()
const { validateLoginForm, validateRegisterForm, validateEmail, validatePassword } = useFormValidation()
const { captchaCode, refresh: refreshCaptcha, generate: generateCaptcha } = useCaptcha()

// View State: 'login' | 'register' | 'forgot'
const view = ref('login')
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// Login State
const loginForm = reactive({
  email: '',
  password: ''
})
const showPassword = ref(false)
const showRegPassword = ref(false)
const showRegConfirmPassword = ref(false)

// Register State
const regForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  captcha: ''
})
const errors = reactive({})

// Forgot Password State
const forgotStep = ref(1)
const forgotForm = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmNewPassword: ''
})
const showForgotPassword = ref(false)
const showForgotConfirmPassword = ref(false)

// Dynamic Content
const viewTitle = computed(() => {
  if (view.value === 'login') return '欢迎回来'
  if (view.value === 'register') return '创建新账号'
  return '找回密码'
})
const viewSubtitle = computed(() => {
  if (view.value === 'login') return '登录以继续您的校园连接之旅'
  if (view.value === 'register') return '填写以下信息快速加入我们'
  return '我们会向您的邮箱发送验证信息'
})

// === Actions ===

function switchView(newView) {
  view.value = newView
  errorMsg.value = ''
  successMsg.value = ''
  if (newView === 'register') refreshCaptcha()
  if (newView === 'forgot') {
    forgotStep.value = 1
    forgotForm.email = ''
    forgotForm.code = ''
    forgotForm.newPassword = ''
    forgotForm.confirmNewPassword = ''
  }
}

onMounted(() => {
  generateCaptcha()
})

// Login Layout
async function handleLogin() {
  errorMsg.value = ''

  // 邮箱格式验证
  const emailValidation = validateEmail(loginForm.email)
  if (!emailValidation.isValid) {
    errorMsg.value = emailValidation.errorMsg || '请输入有效的邮箱地址'
    errors.login = true
    return
  }

  // 密码验证
  if (!loginForm.password || loginForm.password.length < 1) {
    errorMsg.value = '请输入密码'
    errors.login = true
    return
  }

  loading.value = true
  try {
    const res = await userStore.login({
      email: loginForm.email,
      password: loginForm.password
    })

    if (res.success) {
      uiStore.closeModal()
    } else {
      errorMsg.value = res.error || '登录失败'
      errors.login = true
    }
  } catch (e) {
    errorMsg.value = '系统错误'
  } finally {
    loading.value = false
  }
}

// Register Logic
async function handleRegister() {
  // Clear errors
  Object.keys(errors).forEach(k => delete errors[k])
  errorMsg.value = ''

  // Validation
  const validation = validateRegisterForm({
    username: regForm.username,
    email: regForm.email,
    password: regForm.password,
    confirmPassword: regForm.confirmPassword,
    captchaInput: regForm.captcha
  }, captchaCode.value)

  if (!validation.isValid) {
    errors[validation.errorField] = validation.errorMsg // Specific field error
    errorMsg.value = validation.errorMsg // Global msg
    if (validation.errorField === 'captcha') refreshCaptcha()
    return
  }

  loading.value = true
  try {
    const res = await userStore.register({
      username: regForm.username,
      email: regForm.email,
      password: regForm.password,
      confirmPassword: regForm.confirmPassword
    })

    if (res.success) {
      successMsg.value = '注册成功！'
      setTimeout(() => switchView('login'), 1500)
    } else {
      errorMsg.value = res.error || '注册失败'
      refreshCaptcha()
    }
  } catch (e) {
    errorMsg.value = '系统错误'
  } finally {
    loading.value = false
  }
}

// Forgot Password Logic
async function sendResetCode() {
  errorMsg.value = ''
  successMsg.value = ''
  const emailVal = validateEmail(forgotForm.email)

  if (!emailVal.isValid) {
    errorMsg.value = emailVal.errorMsg
    return
  }

  loading.value = true
  try {
    const res = await userStore.sendPasswordResetCode(forgotForm.email)
    if (res.success) {
      forgotStep.value = 2
      successMsg.value = '验证码已发送到您的邮箱'
      errorMsg.value = ''
    } else {
      errorMsg.value = res.error || '发送失败，请稍后重试'
    }
  } catch (e) {
    errorMsg.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 验证验证码
function verifyResetCode() {
  errorMsg.value = ''
  if (!forgotForm.code || forgotForm.code.length < 4) {
    errorMsg.value = '请输入有效的验证码'
    return
  }
  // 验证码格式正确，进入下一步设置新密码
  forgotStep.value = 3
  successMsg.value = ''
}

async function handleForgotSubmit() {
  errorMsg.value = ''
  if (!forgotForm.code || forgotForm.code.length < 4) {
    errorMsg.value = '请输入有效的验证码'
    return
  }

  const passVal = validatePassword(forgotForm.newPassword)
  if (!passVal.isValid) {
    errorMsg.value = passVal.errorMsg
    return
  }

  if (forgotForm.newPassword !== forgotForm.confirmNewPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  try {
    const res = await userStore.resetPassword(forgotForm)
    if (res.success) {
      successMsg.value = '重置成功，请登录'
      setTimeout(() => switchView('login'), 1500)
    } else {
      errorMsg.value = res.error
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* Custom Animation Utilities because tailwindcss-animate plugin is not available in CDN */
.animate-in {
  animation-duration: 0.3s;
  animation-fill-mode: both;
}

.fade-in {
  animation-name: fadeIn;
}

.zoom-in-95 {
  animation-name: zoomIn95;
}

.slide-in-from-right-4 {
  animation-name: slideInFromRight;
}

.slide-in-from-left-4 {
  animation-name: slideInFromLeft;
}

.slide-in-from-top-2 {
  animation-name: slideInFromTop;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes zoomIn95 {
  from {
    opacity: 0;
    transform: scale(0.95);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes slideInFromRight {
  from {
    opacity: 0;
    transform: translateX(1rem);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInFromLeft {
  from {
    opacity: 0;
    transform: translateX(-1rem);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInFromTop {
  from {
    opacity: 0;
    transform: translateY(-0.5rem);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
