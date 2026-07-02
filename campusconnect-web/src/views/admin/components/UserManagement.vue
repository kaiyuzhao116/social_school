<template>
  <div class="h-full overflow-y-auto pr-4 pb-32 no-scrollbar">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-end mb-8 gap-6">
      <div>
        <h1 class="text-4xl font-extrabold text-slate-800 tracking-tight">用户管理</h1>
        <p class="text-slate-500 mt-2 font-medium">查看和管理社区成员，处理违规账号。</p>
      </div>
      
      <div class="flex gap-4 w-full md:w-auto z-20">
        <div class="relative flex-1 md:w-72 group">
          <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-hover:text-sage-500 transition-colors w-4 h-4" />
          <input 
            type="text" 
            placeholder="搜索姓名或邮箱..." 
            v-model="searchTerm"
            class="w-full pl-11 pr-4 py-3 bg-white rounded-2xl border border-slate-200 text-sm font-medium outline-none focus:ring-2 focus:ring-sage-300 transition-shadow shadow-sm hover:shadow-md"
          />
        </div>
        
        <div class="relative">
          <button 
            @click="showFilterMenu = !showFilterMenu"
            :class="['h-full bg-white text-slate-600 px-5 rounded-2xl shadow-sm font-bold text-sm border border-slate-200 flex gap-2.5 items-center whitespace-nowrap transition-all hover:bg-slate-50', showFilterMenu ? 'border-sage-400 ring-2 ring-sage-100' : '']"
          >
            <Filter :size="16" /> 
            <span class="hidden sm:inline">筛选视图</span>
            <ChevronDown :size="14" :class="['transition-transform duration-300', showFilterMenu ? 'rotate-180' : '']"/>
          </button>
          
          <div v-if="showFilterMenu" class="absolute right-0 top-full mt-3 w-64 bg-white rounded-2xl shadow-2xl border border-slate-100 p-5 z-50">
            <div class="mb-5">
              <label class="text-xs font-bold text-slate-400 uppercase mb-3 block tracking-wider">用户角色</label>
              <div class="space-y-2.5">
                <label 
                  v-for="role in ['All', ...Object.values(UserRole)]" 
                  :key="role" 
                  class="flex items-center gap-3 text-sm text-slate-600 cursor-pointer hover:bg-slate-50 p-2 rounded-lg transition-colors -mx-2"
                >
                  <div :class="['w-4 h-4 rounded-full border-2 flex items-center justify-center', roleFilter === role ? 'border-sage-500' : 'border-slate-300']">
                    <div v-if="roleFilter === role" class="w-2 h-2 rounded-full bg-sage-500"></div>
                  </div>
                  <input type="radio" name="role" :checked="roleFilter === role" @change="roleFilter = role" class="hidden"/>
                  <span :class="roleFilter === role ? 'font-bold text-slate-800' : ''">{{ role === 'All' ? '全部角色' : role }}</span>
                </label>
              </div>
            </div>
            <div class="pt-4 border-t border-slate-100">
              <label class="text-xs font-bold text-slate-400 uppercase mb-3 block tracking-wider">账号状态</label>
              <div class="space-y-2.5">
                <label 
                  v-for="status in ['All', '正常', '封禁']" 
                  :key="status" 
                  class="flex items-center gap-3 text-sm text-slate-600 cursor-pointer hover:bg-slate-50 p-2 rounded-lg transition-colors -mx-2"
                >
                  <div :class="['w-4 h-4 rounded-full border-2 flex items-center justify-center', statusFilter === status ? 'border-sage-500' : 'border-slate-300']">
                    <div v-if="statusFilter === status" class="w-2 h-2 rounded-full bg-sage-500"></div>
                  </div>
                  <input type="radio" name="status" :checked="statusFilter === status" @change="statusFilter = status" class="hidden"/>
                  <span :class="statusFilter === status ? 'font-bold text-slate-800' : ''">{{ status === 'All' ? '全部状态' : status }}</span>
                </label>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="flex h-full items-center justify-center">
      <Loader2 class="w-8 h-8 animate-spin text-sage-600" />
    </div>

    <div v-else class="glass-panel rounded-[2rem] overflow-hidden shadow-sm border border-white/60">
      <div class="overflow-x-auto">
        <table class="w-full text-left min-w-[900px]">
          <thead class="bg-slate-50/50 border-b border-slate-100">
            <tr>
              <th class="px-8 py-6 font-bold text-slate-400 text-xs uppercase tracking-wider">用户信息</th>
              <th class="px-8 py-6 font-bold text-slate-400 text-xs uppercase tracking-wider">角色权限</th>
              <th class="px-8 py-6 font-bold text-slate-400 text-xs uppercase tracking-wider">当前状态</th>
              <th class="px-8 py-6 font-bold text-slate-400 text-xs uppercase tracking-wider">注册时间</th>
              <th class="px-8 py-6 font-bold text-slate-400 text-xs uppercase tracking-wider text-right">操作管理</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-if="filteredUsers.length === 0">
              <td colspan="5" class="p-16 text-center text-slate-400 font-medium">没有找到符合条件的用户</td>
            </tr>
            <tr v-for="user in filteredUsers" :key="user.id" class="hover:bg-white/60 transition-colors group">
              <td class="px-8 py-5">
                <div class="flex items-center gap-4">
                  <img :src="user.avatar" class="w-12 h-12 rounded-full object-cover ring-2 ring-white shadow-sm" alt="" />
                  <div>
                    <input 
                      v-if="editingId === user.id"
                      v-model="editForm.name"
                      class="border border-sage-300 rounded-lg px-2 py-1 text-sm w-32 outline-none focus:ring-2 focus:ring-sage-200"
                    />
                    <p v-else class="font-bold text-slate-700 text-base">{{ user.name }}</p>
                    <p class="text-sm font-medium text-slate-400 mt-0.5">{{ user.email }}</p>
                  </div>
                </div>
              </td>
              <td class="px-8 py-5">
                <select 
                  v-if="editingId === user.id"
                  v-model="editForm.role"
                  class="border border-sage-300 rounded-lg px-2 py-1 text-xs outline-none focus:ring-2 focus:ring-sage-200"
                >
                  <option v-for="role in Object.values(UserRole)" :key="role" :value="role">{{ role }}</option>
                </select>
                <span 
                  v-else
                  :class="[
                    'px-3 py-1 rounded-full text-xs font-bold',
                    user.role === UserRole.ADMIN ? 'bg-purple-100 text-purple-600 border border-purple-200' : 
                    user.role === UserRole.MODERATOR ? 'bg-blue-100 text-blue-600 border border-blue-200' : 
                    'bg-slate-100 text-slate-600 border border-slate-200'
                  ]"
                >
                  {{ user.role }}
                </span>
              </td>
              <td class="px-8 py-5">
                <span :class="['px-3 py-1 rounded-full text-xs font-bold flex items-center gap-1.5 w-fit', user.status === '正常' ? 'bg-emerald-100 text-emerald-600 border border-emerald-200' : 'bg-red-100 text-red-600 border border-red-200']">
                  <CheckCircle v-if="user.status === '正常'" :size="14" class="fill-current text-emerald-600/20"/>
                  <Ban v-else :size="14" class="fill-current text-red-600/20"/>
                  {{ user.status }}
                </span>
              </td>
              <td class="px-8 py-5 text-slate-500 text-sm font-medium">
                {{ user.joinDate }}
              </td>
              <td class="px-8 py-5">
                <div class="flex justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                  <template v-if="editingId === user.id">
                    <button @click="handleSave" class="p-2 bg-green-500 text-white rounded-xl shadow-md shadow-green-200 hover:bg-green-600 transition-all"><Save :size="16"/></button>
                    <button @click="handleCancel" class="p-2 bg-slate-200 text-slate-500 rounded-xl hover:bg-slate-300 transition-all"><X :size="16"/></button>
                  </template>
                  <template v-else>
                    <button @click="handleEdit(user)" class="p-2.5 bg-white border border-slate-200 text-slate-500 hover:text-blue-600 hover:border-blue-200 hover:bg-blue-50 rounded-xl transition-all" title="编辑">
                      <Edit2 :size="16" />
                    </button>
                    <button 
                      @click="toggleStatus(user.id)" 
                      :class="['p-2.5 border rounded-xl transition-all', user.status === '正常' ? 'bg-white border-slate-200 text-slate-500 hover:text-red-600 hover:border-red-200 hover:bg-red-50' : 'bg-red-50 border-red-100 text-red-500 hover:bg-red-100']" 
                      :title="user.status === '正常' ? '封禁' : '解封'"
                    >
                      <Ban :size="16" />
                    </button>
                  </template>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Filter, Ban, Edit2, Save, X, CheckCircle, Search, ChevronDown, Loader2 } from 'lucide-vue-next'
import { UserRole } from '../types'
import { dataService } from '../services/dataService'

const users = ref([])
const isLoading = ref(true)
const editingId = ref(null)
const editForm = ref({})

const searchTerm = ref('')
const showFilterMenu = ref(false)
const roleFilter = ref('All')
const statusFilter = ref('All')

onMounted(async () => {
  isLoading.value = true
  try {
    const data = await dataService.fetchUsers()
    // 转换后端数据格式
    users.value = (data || []).map(u => ({
      id: u.id,
      name: u.nickname || u.username,
      email: u.email || '',
      avatar: u.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(u.nickname || u.username)}&background=6366f1&color=fff`,
      role: u.role === 'ADMIN' ? UserRole.ADMIN : u.role === 'MODERATOR' ? UserRole.MODERATOR : UserRole.USER,
      status: u.status === 'NORMAL' ? '正常' : u.status === 'BANNED' ? '封禁' : u.status,
      joinDate: u.createdAt ? new Date(u.createdAt).toLocaleDateString('zh-CN') : '未知'
    }))
  } catch (e) {
    console.error('加载用户失败:', e)
    users.value = []
  } finally {
    isLoading.value = false
  }
})

const filteredUsers = computed(() => {
  return users.value.filter(u => {
    const matchesSearch = u.name.includes(searchTerm.value) || u.email.includes(searchTerm.value)
    const matchesRole = roleFilter.value === 'All' || u.role === roleFilter.value
    const matchesStatus = statusFilter.value === 'All' || u.status === statusFilter.value
    return matchesSearch && matchesRole && matchesStatus
  })
})

const handleEdit = (user) => {
  editingId.value = user.id
  editForm.value = { ...user }
}

const handleSave = async () => {
  if (editingId.value) {
    const updatedUser = { ...users.value.find(u => u.id === editingId.value), ...editForm.value }
    await dataService.updateUser(updatedUser)
    users.value = users.value.map(u => u.id === editingId.value ? updatedUser : u)
    editingId.value = null
  }
}

const handleCancel = () => {
  editingId.value = null
  editForm.value = {}
}

const toggleStatus = async (id) => {
  const user = users.value.find(u => u.id === id)
  if (user) {
    const newStatus = user.status === '正常' ? '封禁' : '正常'
    const updatedUser = { ...user, status: newStatus }
    await dataService.updateUser(updatedUser)
    users.value = users.value.map(u => u.id === id ? updatedUser : u)
  }
}
</script>
