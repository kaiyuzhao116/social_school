<template>
  <div class="h-full relative">
  <!-- Edit Profile View -->
  <div v-if="view === 'edit'" class="flex flex-col h-full bg-white dark:bg-gray-900">
    <!-- Header -->
    <div class="px-6 py-4 border-b border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 flex justify-between items-center shrink-0">
      <button @click="view = 'profile'" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full text-gray-500 flex items-center gap-1">
        <ArrowLeft class="w-5 h-5" /> <span class="text-sm font-bold">返回</span>
      </button>
      <h2 class="text-lg font-bold text-gray-900 dark:text-white">编辑资料</h2>
      <button 
        @click="saveProfile" 
        :disabled="saving"
        class="px-5 py-1.5 bg-brand-purple hover:bg-indigo-600 disabled:opacity-50 disabled:cursor-not-allowed text-white text-sm font-bold rounded-full transition-colors shadow-sm"
      >
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto custom-scrollbar p-6">
      <div class="max-w-3xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Left Sidebar -->
        <div class="space-y-6 lg:col-span-1">
          <!-- Visuals -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700">
            <h3 class="text-sm font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
              <ImageIcon class="w-4 h-4 text-brand-purple" /> 形象设置
            </h3>
            <div class="space-y-6">
              <!-- Cover -->
              <div>
                <label class="block text-xs font-medium text-gray-500 mb-2">封面图片</label>
                <div class="relative w-full h-32 rounded-xl overflow-hidden bg-gray-100 group cursor-pointer border-2 border-dashed border-gray-200 hover:border-brand-purple transition-colors">
                  <img v-if="isValidCoverUrl(editData.coverImage)" :src="editData.coverImage" @error="handleEditCoverError" class="w-full h-full object-cover" />
                  <div v-else class="flex items-center justify-center h-full text-gray-400 text-xs bg-gradient-to-r from-blue-100 to-purple-100 dark:from-blue-900/20 dark:to-purple-900/20">
                    <span>点击上传封面</span>
                  </div>
                  <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                    <Camera class="w-6 h-6 text-white" />
                  </div>
                  <input type="file" class="absolute inset-0 opacity-0 cursor-pointer" accept="image/*" @change="(e) => handleImageUpload(e, 'cover')" />
                </div>
              </div>
              <!-- Avatar -->
              <div>
                <label class="block text-xs font-medium text-gray-500 mb-2">个人头像</label>
                <div class="flex items-center gap-4">
                  <div class="relative w-20 h-20 rounded-full overflow-hidden bg-gray-100 group cursor-pointer ring-2 ring-offset-2 ring-gray-100 dark:ring-gray-700">
                    <img :src="getAvatarUrl(editData.avatar, editData.name)" @error="handleAvatarError" class="w-full h-full object-cover" />
                    <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                      <Camera class="w-5 h-5 text-white" />
                    </div>
                    <input type="file" class="absolute inset-0 opacity-0 cursor-pointer" accept="image/*" @change="(e) => handleImageUpload(e, 'avatar')" />
                  </div>
                  <div class="text-xs text-gray-400">建议使用正方形图片<br/>支持 JPG, PNG</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Identity -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700">
            <h3 class="text-sm font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
              <Shield class="w-4 h-4 text-blue-500" /> 身份认证
            </h3>
            <div class="space-y-3">
              <!-- Student -->
              <div :class="['p-3 rounded-xl border flex flex-col gap-2 transition-colors', editData.role === 'student' ? 'bg-blue-50 border-blue-200 dark:bg-blue-900/20 dark:border-blue-800' : 'bg-gray-50 border-gray-200 dark:bg-gray-700/50 dark:border-gray-600']">
                <div class="flex items-center gap-2">
                  <GraduationCap :class="['w-4 h-4', editData.role === 'student' ? 'text-blue-600' : 'text-gray-400']" />
                  <span :class="['text-sm font-bold', editData.role === 'student' ? 'text-blue-700 dark:text-blue-300' : 'text-gray-600 dark:text-gray-400']">在校学生</span>
                </div>
                <span v-if="editData.role === 'student'" class="text-xs text-blue-500 flex items-center gap-1"><Check class="w-3 h-3" /> 已认证</span>
              </div>
              <!-- Teacher -->
              <div :class="['p-3 rounded-xl border flex flex-col gap-2 transition-colors', editData.role === 'teacher' ? 'bg-amber-50 border-amber-200 dark:bg-amber-900/20 dark:border-amber-800' : verificationStatus.teacher === 'pending' ? 'bg-yellow-50 border-yellow-200 dark:bg-yellow-900/20 dark:border-yellow-800' : 'bg-gray-50 border-gray-200 dark:bg-gray-700/50 dark:border-gray-600']">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <Briefcase :class="['w-4 h-4', editData.role === 'teacher' ? 'text-amber-600' : 'text-gray-400']" />
                    <span :class="['text-sm font-bold', editData.role === 'teacher' ? 'text-amber-700 dark:text-amber-300' : 'text-gray-600 dark:text-gray-400']">教职工</span>
                  </div>
                  <span v-if="editData.role === 'teacher'" class="text-xs text-amber-500 flex items-center gap-1"><Check class="w-3 h-3" /> 已认证</span>
                  <span v-else-if="verificationStatus.teacher === 'pending'" class="text-xs text-yellow-600 flex items-center gap-1"><Clock class="w-3 h-3" /> 审核中</span>
                  <span v-else-if="verificationStatus.teacher === 'rejected'" class="text-xs text-red-500 flex items-center gap-1"><XCircle class="w-3 h-3" /> 已拒绝</span>
                </div>
                <button v-if="editData.role !== 'teacher' && verificationStatus.teacher !== 'pending'" @click="openVerifyModal('teacher')" class="text-xs bg-white dark:bg-gray-600 px-2 py-1 rounded border shadow-sm self-start hover:bg-gray-50 transition-colors">
                  {{ verificationStatus.teacher === 'rejected' ? '重新申请' : '申请认证' }}
                </button>
              </div>
              <!-- Department -->
              <div :class="['p-3 rounded-xl border flex flex-col gap-2 transition-colors', editData.role === 'department' ? 'bg-purple-50 border-purple-200 dark:bg-purple-900/20 dark:border-purple-800' : verificationStatus.department === 'pending' ? 'bg-yellow-50 border-yellow-200 dark:bg-yellow-900/20 dark:border-yellow-800' : 'bg-gray-50 border-gray-200 dark:bg-gray-700/50 dark:border-gray-600']">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <Building2 :class="['w-4 h-4', editData.role === 'department' ? 'text-purple-600' : 'text-gray-400']" />
                    <span :class="['text-sm font-bold', editData.role === 'department' ? 'text-purple-700 dark:text-purple-300' : 'text-gray-600 dark:text-gray-400']">官方部门</span>
                  </div>
                  <span v-if="editData.role === 'department'" class="text-xs text-purple-500 flex items-center gap-1"><Check class="w-3 h-3" /> 已认证</span>
                  <span v-else-if="verificationStatus.department === 'pending'" class="text-xs text-yellow-600 flex items-center gap-1"><Clock class="w-3 h-3" /> 审核中</span>
                  <span v-else-if="verificationStatus.department === 'rejected'" class="text-xs text-red-500 flex items-center gap-1"><XCircle class="w-3 h-3" /> 已拒绝</span>
                </div>
                <button v-if="editData.role !== 'department' && verificationStatus.department !== 'pending'" @click="openVerifyModal('department')" class="text-xs bg-white dark:bg-gray-600 px-2 py-1 rounded border shadow-sm self-start hover:bg-gray-50 transition-colors">
                  {{ verificationStatus.department === 'rejected' ? '重新申请' : '申请入驻' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Content -->
        <div class="space-y-6 lg:col-span-2">
          <!-- Basic Info -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700">
            <h3 class="text-sm font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
              <User class="w-4 h-4 text-brand-purple" /> 基本信息
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-medium text-gray-500 mb-1">昵称</label>
                <input v-model="editData.name" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple" />
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-500 mb-1">学号 / 工号 (不可修改)</label>
                <input :value="editData.id" disabled class="w-full bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl px-4 py-2 text-sm text-gray-500 cursor-not-allowed" />
              </div>
              <div class="col-span-2">
                <label class="block text-xs font-medium text-gray-500 mb-1">个性签名</label>
                <textarea v-model="editData.bio" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple resize-none h-20" placeholder="介绍一下你自己..."></textarea>
              </div>
            </div>
          </div>

          <!-- Campus Info -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700">
            <h3 class="text-sm font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
              <School class="w-4 h-4 text-brand-purple" /> 校园信息
            </h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-4 gap-y-6">
              <div>
                <label class="block text-xs font-medium text-gray-500 mb-1">学院</label>
                <input v-model="editData.college" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple" placeholder="例：计算机学院" />
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-500 mb-1">专业</label>
                <input v-model="editData.major" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple" placeholder="例：软件工程" />
              </div>
              <div class="relative">
                <div class="flex justify-between items-center mb-1">
                  <label class="block text-xs font-medium text-gray-500">班级</label>
                  <button @click="togglePrivacy('className')" class="text-gray-400 hover:text-brand-purple">
                    <EyeOff v-if="editData.privacy?.className" class="w-3.5 h-3.5" />
                    <Eye v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
                <input v-model="editData.className" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple" placeholder="例：软件2201" />
              </div>
              <div class="relative">
                <div class="flex justify-between items-center mb-1">
                  <label class="block text-xs font-medium text-gray-500">宿舍</label>
                  <button @click="togglePrivacy('dormitory')" class="text-gray-400 hover:text-brand-purple">
                    <EyeOff v-if="editData.privacy?.dormitory" class="w-3.5 h-3.5" />
                    <Eye v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
                <input v-model="editData.dormitory" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple" placeholder="例：东区3栋 402" />
              </div>
              <div class="relative">
                <div class="flex justify-between items-center mb-1">
                  <label class="block text-xs font-medium text-gray-500">年龄</label>
                  <button @click="togglePrivacy('age')" class="text-gray-400 hover:text-brand-purple">
                    <EyeOff v-if="editData.privacy?.age" class="w-3.5 h-3.5" />
                    <Eye v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
                <input v-model="editData.age" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple" placeholder="18" />
              </div>
              <div class="relative">
                <div class="flex justify-between items-center mb-1">
                  <label class="block text-xs font-medium text-gray-500">性别</label>
                  <button @click="togglePrivacy('gender')" class="text-gray-400 hover:text-brand-purple">
                    <EyeOff v-if="editData.privacy?.gender" class="w-3.5 h-3.5" />
                    <Eye v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
                <select v-model="editData.gender" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple">
                  <option value="">选择性别</option>
                  <option value="男">男</option>
                  <option value="女">女</option>
                  <option value="其他">其他</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Tags -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700">
            <h3 class="text-sm font-bold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
              <Hash class="w-4 h-4 text-brand-purple" /> 个人标签
            </h3>
            <div class="flex flex-wrap gap-2 mb-3">
              <span v-for="tag in editData.hobbies" :key="tag" class="px-3 py-1 rounded-full text-sm font-medium flex items-center gap-1 bg-brand-purple/10 text-brand-purple">
                {{ tag }}
                <button @click="removeTag(tag)" class="hover:text-red-500"><X class="w-3 h-3" /></button>
              </span>
            </div>
            <input @keydown.enter="handleTagInput" placeholder="输入标签按回车添加..." class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple" />
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Profile View -->
  <div v-else-if="view === 'profile'" class="flex flex-col h-full bg-white dark:bg-gray-900 overflow-y-auto custom-scrollbar relative">
    <button @click="uiStore.closeModal()" class="absolute top-4 right-4 z-20 p-2 bg-black/20 hover:bg-black/40 text-white rounded-full backdrop-blur-sm transition-colors">
      <X class="w-5 h-5" />
    </button>

    <!-- Cover Image -->
    <div class="h-40 md:h-48 bg-gradient-to-r from-blue-400 to-purple-500 relative shrink-0">
      <img v-if="user.coverImage && !user.coverImage.includes('random=')" :src="user.coverImage" @error="handleCoverError" class="w-full h-full object-cover" />
    </div>

    <div class="max-w-4xl mx-auto w-full px-6 pb-12">
      <div class="flex flex-col md:flex-row gap-6 relative">
        <!-- Avatar -->
        <div class="-mt-12 md:-mt-16 shrink-0 flex flex-col items-center md:items-start z-10 relative">
          <div class="relative group">
            <img :src="getAvatarUrl(user.avatar, user.name)" @error="handleAvatarError" class="w-24 h-24 md:w-32 md:h-32 rounded-full border-[3px] border-white dark:border-gray-900 shadow-lg object-cover bg-white" :alt="user.name" />
            <!-- 学生认证标识 -->
            <div v-if="(user.isVerified && user.role === 'student') || user.verifyType === 'STUDENT'" class="absolute bottom-1 right-1 bg-blue-500 text-white p-1 rounded-full border-2 border-white dark:border-gray-900 shadow-sm" title="学生认证">
              <Check class="w-3 h-3" />
            </div>
            <!-- 教职工认证标识 -->
            <div v-else-if="user.role === 'teacher' || user.verifyType === 'TEACHER'" class="absolute bottom-1 right-1 bg-amber-500 text-white p-1 rounded-full border-2 border-white dark:border-gray-900 shadow-sm" title="教职工认证">
              <Briefcase class="w-3 h-3" />
            </div>
            <!-- 部门认证标识 -->
            <div v-else-if="user.role === 'department' || user.verifyType === 'ORG'" class="absolute bottom-1 right-1 bg-purple-500 text-white p-1 rounded-full border-2 border-white dark:border-gray-900 shadow-sm" title="官方部门">
              <Building2 class="w-3 h-3" />
            </div>
          </div>
        </div>

        <!-- Name & Info -->
        <div class="pt-2 md:pt-3 flex-1 min-w-0 md:pl-2">
          <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-4 text-center md:text-left">
            <div>
              <h1 class="text-xl md:text-2xl font-bold text-gray-900 dark:text-white mb-1 flex items-center justify-center md:justify-start gap-2">
                {{ user.name }}
                <span v-if="user.role === 'teacher' || user.verifyType === 'TEACHER'" class="ml-2 px-1.5 py-0.5 rounded text-[11px] font-medium bg-amber-50 text-amber-600 border border-amber-200">教师</span>
                <span v-if="user.role === 'department' || user.verifyType === 'ORG'" class="ml-1 text-blue-500" title="官方认证">
                  <BadgeCheck class="w-5 h-5 fill-blue-500 text-white" />
                </span>
              </h1>
              <div class="flex items-center justify-center md:justify-start gap-3 text-gray-500 text-xs md:text-sm">
                <span class="font-mono text-gray-400">ID: {{ user.id }}</span>
                <span class="text-gray-300">|</span>
                <span @click="openFollowList('following')" class="hover:text-brand-purple cursor-pointer transition-colors"><strong>{{ user.followingCount || user.following?.length || 0 }}</strong> 关注</span>
                <span @click="openFollowList('followers')" class="hover:text-brand-purple cursor-pointer transition-colors"><strong>{{ user.followerCount || user.followers?.length || 0 }}</strong> 粉丝</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex justify-center gap-2 mt-2 md:mt-0 flex-wrap">
              <template v-if="isMe">
                <button @click="showIdCard = true; isFlipped = false" class="p-2 rounded-full bg-brand-purple/5 text-brand-purple hover:bg-brand-purple/10 transition-colors" title="电子校卡">
                  <CreditCard class="w-5 h-5" />
                </button>
                <button @click="view = 'settings'" class="p-2 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 hover:bg-gray-200 hover:text-gray-900 transition-colors" title="账号设置">
                  <Settings class="w-5 h-5" />
                </button>
                <button @click="view = 'edit'" class="flex items-center gap-1.5 px-4 py-1.5 bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-200 rounded-full font-bold text-xs transition-colors">
                  <Edit2 class="w-3.5 h-3.5" /> 编辑
                </button>
                <button @click="handleLogout" class="flex items-center gap-1.5 px-4 py-1.5 bg-red-50 dark:bg-red-900/20 hover:bg-red-100 dark:hover:bg-red-900/40 text-red-600 dark:text-red-400 rounded-full font-bold text-xs transition-colors">
                  <LogOut class="w-3.5 h-3.5" /> 退出
                </button>
              </template>
              <template v-else>
                <button @click="handleFollowToggle" :class="['flex items-center gap-2 px-6 py-2 rounded-full font-bold text-sm transition-all shadow-md', isFollowing ? 'bg-gray-200 text-gray-700 hover:bg-gray-300' : 'bg-brand-purple text-white hover:bg-indigo-600']">
                  <UserCheck v-if="isFollowing" class="w-4 h-4" />
                  <UserPlus v-else class="w-4 h-4" />
                  {{ isFollowing ? '已关注' : '关注' }}
                </button>
                <button @click="handleSendMessage" class="p-2 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-600 hover:bg-gray-200 transition-colors" title="私信">
                  <MessageCircle class="w-5 h-5" />
                </button>
              </template>
            </div>
          </div>

          <!-- Bio -->
          <div class="mt-4 text-gray-600 dark:text-gray-300 text-sm leading-relaxed relative pl-4 border-l-4 border-brand-purple/20 italic">
            {{ user.bio || '这个人很懒，什么都没有写...' }}
          </div>
        </div>
      </div>

      <!-- Info Grid -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mt-8">
        <div class="bg-blue-50/80 dark:bg-blue-900/10 px-4 py-3 rounded-2xl flex flex-col justify-center">
          <span class="text-[10px] font-bold text-blue-400 uppercase tracking-wider mb-0.5 flex items-center gap-1.5"><School class="w-3 h-3" /> 学院</span>
          <p class="text-sm font-bold text-gray-800 dark:text-gray-100 truncate">{{ user.college || '未填写' }}</p>
        </div>
        <div class="bg-indigo-50/80 dark:bg-indigo-900/10 px-4 py-3 rounded-2xl flex flex-col justify-center">
          <span class="text-[10px] font-bold text-indigo-400 uppercase tracking-wider mb-0.5 flex items-center gap-1.5"><BookOpen class="w-3 h-3" /> 专业</span>
          <p class="text-sm font-bold text-gray-800 dark:text-gray-100 truncate">{{ user.major || '未填写' }}</p>
        </div>
        <div v-if="isMe || !user.privacy?.className" class="bg-gray-50 dark:bg-gray-800 px-4 py-3 rounded-2xl flex flex-col justify-center">
          <span class="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-0.5">班级</span>
          <p class="text-sm font-bold text-gray-800 dark:text-gray-100 truncate">{{ user.className || '未填写' }}</p>
        </div>
        <div v-if="isMe || !user.privacy?.dormitory" class="bg-gray-50 dark:bg-gray-800 px-4 py-3 rounded-2xl flex flex-col justify-center">
          <span class="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-0.5">宿舍</span>
          <p class="text-sm font-bold text-gray-800 dark:text-gray-100 truncate">{{ user.dormitory || '未填写' }}</p>
        </div>
      </div>

      <!-- Tags -->
      <div v-if="user.hobbies && user.hobbies.length > 0" class="mt-6">
        <h3 class="text-sm font-bold text-gray-900 dark:text-white mb-3 flex items-center gap-2">
          <Sparkles class="w-4 h-4 text-brand-purple" /> 个人标签
        </h3>
        <div class="flex flex-wrap gap-2">
          <span v-for="tag in user.hobbies" :key="tag" class="px-3 py-1 rounded-full text-xs font-bold shadow-sm bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300">{{ tag }}</span>
        </div>
      </div>

      <!-- User's Posts -->
      <div class="mt-8 pt-6 border-t border-gray-100 dark:border-gray-800">
        <h3 class="text-base font-bold text-gray-900 dark:text-white mb-4">
          {{ isMe ? '我的动态' : 'TA的动态' }}
          <span class="text-sm font-normal text-gray-500 ml-2">({{ userPosts.length }})</span>
        </h3>
        <div class="columns-1 md:columns-2 gap-4 space-y-4">
          <div v-for="post in userPosts" :key="post.id" @click="handlePostClick(post)" class="break-inside-avoid mb-4 bg-white dark:bg-gray-800 p-4 rounded-2xl border border-gray-100 dark:border-gray-700 hover:shadow-lg transition-all cursor-pointer group">
            <p class="text-sm text-gray-800 dark:text-gray-200 line-clamp-3 mb-3 break-all">{{ stripHtml(post.content) }}</p>
            <div v-if="post.images && post.images.length > 0" class="h-28 w-full rounded-lg overflow-hidden mb-3">
              <img :src="post.images[0]" class="w-full h-full object-cover group-hover:scale-105 transition-transform" />
            </div>
            <div class="flex justify-between items-center text-xs text-gray-400">
              <span>{{ post.timeAgo }}</span>
              <div class="flex gap-3">
                <span class="flex items-center gap-1"><ThumbsUp class="w-3 h-3" /> {{ post.likes }}</span>
                <span class="flex items-center gap-1"><MessageSquare class="w-3 h-3" /> {{ post.comments }}</span>
              </div>
            </div>
          </div>
          <div v-if="userPosts.length === 0" class="col-span-full py-8 text-center text-gray-400 bg-gray-50 dark:bg-gray-800 rounded-2xl border border-dashed border-gray-200 dark:border-gray-700">暂无动态</div>
        </div>
      </div>
    </div>

    <!-- Virtual ID Card Overlay -->
    <div v-if="showIdCard" class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4" @click="showIdCard = false">
      <div class="relative w-[480px] h-[280px] [perspective:1000px] cursor-pointer" @click.stop="isFlipped = !isFlipped">
        <div :class="['relative w-full h-full transition-all duration-700 [transform-style:preserve-3d] shadow-2xl rounded-2xl', isFlipped ? '[transform:rotateY(180deg)]' : '']">
          <!-- Front -->
          <!-- Front (Cover / Decorative Side) -->
          <div class="absolute inset-0 w-full h-full [backface-visibility:hidden] rounded-2xl overflow-hidden bg-white shadow-2xl">
            <!-- Background Image -->
            <img :src="IdCardBg" class="absolute inset-0 w-full h-full object-cover brightness-[0.95]" />
            
            <!-- Subtle gradient for legibility -->
            <div class="absolute inset-0 bg-gradient-to-b from-black/20 via-transparent to-black/20 pointer-events-none"></div>

            <!-- Content Overlay -->
            <div class="absolute inset-0 p-5 flex flex-col justify-between z-10 pointer-events-none">
              
              <!-- Top Left: Logo & School Name (Clean White Style) -->
              <div class="flex items-center gap-3">
                <!-- Simple Circular Logo -->
                <div class="w-10 h-10 rounded-full border-[1.5px] border-white/90 flex items-center justify-center backdrop-blur-[1px]">
                   <School class="w-5 h-5 text-white" stroke-width="2.5" />
                </div>
                <div class="flex flex-col items-start pt-1">
                   <!-- Calligraphy Style School Name -->
                   <span class="text-xl text-white leading-none tracking-widest drop-shadow-md" style="font-family: 'KaiTi', 'STKaiti', 'SimKai', 'Microsoft YaHei', serif; font-weight: 800;">校园连接大学</span>
                   <span class="text-[7px] font-bold text-white/80 uppercase tracking-[0.2em] mt-0.5 drop-shadow-sm font-sans">CAMPUSCONNECT UNIVERSITY</span>
                </div>
              </div>

              <!-- Bottom Right: Minimal ID (Reference Style) -->
              <div class="text-right pb-1 pr-1">
                <span class="text-lg font-medium text-white tracking-widest font-sans drop-shadow-md" style="font-family: Arial, sans-serif; text-shadow: 0 1px 2px rgba(0,0,0,0.5);">
                  ID: {{ user.id }}
                </span>
              </div>

            </div>
          </div>

          <!-- Back -->
          <!-- Back (User Info Side) -->
          <div class="absolute inset-0 w-full h-full [backface-visibility:hidden] rounded-2xl overflow-hidden bg-white [transform:rotateY(180deg)] shadow-inner flex">
            
            <!-- 1. Background Layer: Building Line Art (Corrected) -->
            <!-- Aligned to bottom 0 to show the full building structure -->
            <img :src="IdCardBackBg" class="absolute bottom-0 left-0 w-full h-[85%] object-contain object-bottom opacity-25 pointer-events-none mix-blend-multiply grayscale-[0.2]" />

            <!-- 2. Left Vertical Text (Dynamic based on Identity) -->
            <div class="absolute top-0 bottom-0 left-4 flex items-center justify-center pointer-events-none select-none z-0">
               <h1 class="text-[72px] font-black tracking-widest text-transparent opacity-[0.06]" 
                   style="-webkit-text-stroke: 2px #334155; writing-mode: vertical-lr; text-orientation: mixed; font-family: 'Arial Black', sans-serif;">
                 {{ identityInfo.bgText }}
               </h1>
            </div>

            <!-- 3. Main Content Area -->
            <div class="flex-1 relative z-10 flex flex-col p-6 pr-2">
              
              <!-- Top Header -->
              <div class="flex items-center justify-between mb-5 pr-4 border-b border-gray-100 pb-2">
                 <div class="flex items-center gap-2.5">
                    <School class="w-7 h-7 text-indigo-900" stroke-width="1.8" />
                    <div class="flex flex-col">
                       <span class="text-[13px] font-black text-slate-900 leading-none tracking-wide">CAMPUS CONNECT</span>
                       <span class="text-[7px] font-bold text-slate-500 uppercase tracking-[0.2em] mt-0.5">UNIVERSITY</span>
                    </div>
                 </div>
                 <span class="text-[6px] font-bold text-slate-400 tracking-widest border border-slate-200 px-1.5 py-0.5 rounded-sm">NO. {{ user.id }}</span>
              </div>

              <!-- Middle: User Info -->
              <div class="flex gap-5 items-start">
                 <!-- Avatar -->
                 <div class="shrink-0 flex flex-col gap-2">
                    <div class="w-[88px] h-[112px] bg-white rounded-lg p-1 shadow-sm border border-slate-100">
                        <img :src="getAvatarUrl(user.avatar, user.name)" @error="handleAvatarError" class="w-full h-full object-cover rounded-[4px]" />
                    </div>
                 </div>

                 <!-- Text Details -->
                 <div class="flex flex-col flex-1 min-w-0 pt-1">
                    <div class="mb-4">
                      <h2 class="text-[28px] font-black text-slate-900 leading-none mb-1.5 tracking-tight">{{ user.name }}</h2>
                      <!-- Dynamic Badge -->
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded text-[9px] font-black uppercase tracking-wider bg-[#b1e1ff] text-[#1e3a8a] shadow-sm">
                        {{ identityInfo.title }}
                      </span>
                    </div>

                    <!-- Details Grid -->
                    <div class="flex flex-col gap-3">
                       <div>
                          <p class="text-[8px] font-extrabold text-slate-400 uppercase tracking-widest mb-0.5 flex items-center gap-1">
                            <span class="w-1 h-1 bg-[#b1e1ff] rounded-full"></span> Department
                          </p>
                          <p class="text-sm font-bold text-slate-800 truncate leading-tight">{{ user.college || 'School of Engineering' }}</p>
                        </div>
                    </div>
                 </div>
              </div>

               <!-- Bottom Footer: Valid Date & QR -->
               <div class="mt-auto flex items-end justify-between pr-4 pb-0">
                  <div class="pb-1">
                    <p class="text-[7px] font-bold text-slate-400 uppercase tracking-widest mb-0.5">Valid Until</p>
                    <div class="flex items-baseline gap-1">
                       <span class="text-lg font-bold text-slate-800 font-mono tracking-tight">2028</span>
                       <span class="text-[10px] font-bold text-slate-500 font-mono">.06.30</span>
                    </div>
                  </div>
                  
                  <!-- QR Code (Clean & Minimalist) -->
                  <div class="w-[68px] h-[68px] p-1 bg-white rounded-lg shadow-md border border-slate-100 flex items-center justify-center shrink-0 mb-[-6px] mr-1">
                     <img v-if="idCardQrcode" :src="idCardQrcode" class="w-full h-full object-contain mix-blend-multiply opacity-90" />
                     <ScanLine v-else class="w-6 h-6 text-slate-300" />
                  </div>
               </div>

            </div>

            <!-- 4. Right Vertical Strip -->
            <div class="w-12 h-full bg-[#b1e1ff] flex items-center justify-center shrink-0 relative overflow-hidden shadow-[-4px_0_10px_rgba(0,0,0,0.05)]">
               <div class="absolute inset-0 bg-gradient-to-b from-white/40 to-transparent"></div>
               <div class="absolute inset-0 opacity-10" style="background-image: radial-gradient(circle, #1e3a8a 1px, transparent 1px); background-size: 8px 8px;"></div>
               <span class="text-[#1e3a8a] font-black text-[11px] tracking-[0.35em] whitespace-nowrap opacity-90 relative z-10" style="writing-mode: vertical-rl; text-orientation: mixed; transform: rotate(180deg);">
                  CAMPUS CONNECT
               </span>
            </div>

          </div>
        </div>
      </div>
      <button @click="showIdCard = false" class="absolute top-6 right-6 text-white/70 hover:text-white z-50 p-2 bg-white/10 rounded-full backdrop-blur-md"><X class="w-6 h-6" /></button>
    </div>
  </div>

  <!-- Settings View -->
  <div v-else-if="view === 'settings'" class="flex flex-col h-full bg-gray-50 dark:bg-gray-900">
    <div class="px-6 py-4 border-b border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 flex justify-between items-center shrink-0">
      <button @click="view = 'profile'" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full text-gray-500 flex items-center gap-1">
        <ArrowLeft class="w-5 h-5" /> <span class="text-sm font-bold">返回</span>
      </button>
      <h2 class="text-lg font-bold text-gray-900 dark:text-white">账号设置</h2>
      <div class="w-20"></div>
    </div>
    <div class="flex-1 overflow-y-auto custom-scrollbar p-6">
      <div class="max-w-2xl mx-auto space-y-6">
        <!-- Account Security -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
          <div class="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-bold text-gray-900 dark:text-white flex items-center gap-2"><Lock class="w-4 h-4 text-brand-purple" /> 账号安全</h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <div class="p-4 flex items-center justify-between hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center"><Key class="w-5 h-5 text-blue-500" /></div>
                <div>
                  <p class="font-medium text-gray-900 dark:text-white text-sm">修改密码</p>
                  <p class="text-xs text-gray-500">定期更换密码保障账号安全</p>
                </div>
              </div>
              <ChevronRight class="w-5 h-5 text-gray-400" />
            </div>
            <div class="p-4 flex items-center justify-between hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-green-50 dark:bg-green-900/20 flex items-center justify-center"><Smartphone class="w-5 h-5 text-green-500" /></div>
                <div>
                  <p class="font-medium text-gray-900 dark:text-white text-sm">绑定手机</p>
                  <p class="text-xs text-gray-500">{{ user.phone || '未绑定' }}</p>
                </div>
              </div>
              <ChevronRight class="w-5 h-5 text-gray-400" />
            </div>
            <div class="p-4 flex items-center justify-between hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-purple-50 dark:bg-purple-900/20 flex items-center justify-center"><Mail class="w-5 h-5 text-purple-500" /></div>
                <div>
                  <p class="font-medium text-gray-900 dark:text-white text-sm">绑定邮箱</p>
                  <p class="text-xs text-gray-500">{{ user.email || '未绑定' }}</p>
                </div>
              </div>
              <ChevronRight class="w-5 h-5 text-gray-400" />
            </div>
          </div>
        </div>

        <!-- Privacy Settings -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
          <div class="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-bold text-gray-900 dark:text-white flex items-center gap-2"><EyeOff class="w-4 h-4 text-brand-purple" /> 隐私设置</h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <div class="p-4 flex items-center justify-between">
              <div>
                <p class="font-medium text-gray-900 dark:text-white text-sm">允许陌生人私信</p>
                <p class="text-xs text-gray-500">关闭后仅互相关注的人可以私信</p>
              </div>
              <button @click="settingsData.allowStrangerMessage = !settingsData.allowStrangerMessage" :class="['w-12 h-6 rounded-full transition-colors relative', settingsData.allowStrangerMessage ? 'bg-brand-purple' : 'bg-gray-200 dark:bg-gray-600']">
                <span :class="['absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all', settingsData.allowStrangerMessage ? 'right-1' : 'left-1']"></span>
              </button>
            </div>
            <div class="p-4 flex items-center justify-between">
              <div>
                <p class="font-medium text-gray-900 dark:text-white text-sm">显示在线状态</p>
                <p class="text-xs text-gray-500">他人可看到你是否在线</p>
              </div>
              <button @click="settingsData.showOnlineStatus = !settingsData.showOnlineStatus" :class="['w-12 h-6 rounded-full transition-colors relative', settingsData.showOnlineStatus ? 'bg-brand-purple' : 'bg-gray-200 dark:bg-gray-600']">
                <span :class="['absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all', settingsData.showOnlineStatus ? 'right-1' : 'left-1']"></span>
              </button>
            </div>
            <div class="p-4 flex items-center justify-between">
              <div>
                <p class="font-medium text-gray-900 dark:text-white text-sm">公开关注列表</p>
                <p class="text-xs text-gray-500">他人可查看你的关注和粉丝</p>
              </div>
              <button @click="settingsData.showFollowList = !settingsData.showFollowList" :class="['w-12 h-6 rounded-full transition-colors relative', settingsData.showFollowList ? 'bg-brand-purple' : 'bg-gray-200 dark:bg-gray-600']">
                <span :class="['absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all', settingsData.showFollowList ? 'right-1' : 'left-1']"></span>
              </button>
            </div>
          </div>
        </div>

        <!-- Notification Settings -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
          <div class="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-bold text-gray-900 dark:text-white flex items-center gap-2"><Bell class="w-4 h-4 text-brand-purple" /> 通知设置</h3>
          </div>
          <div class="divide-y divide-gray-100 dark:divide-gray-700">
            <div class="p-4 flex items-center justify-between">
              <div>
                <p class="font-medium text-gray-900 dark:text-white text-sm">点赞通知</p>
                <p class="text-xs text-gray-500">有人点赞你的内容时通知</p>
              </div>
              <button @click="settingsData.notifyLike = !settingsData.notifyLike" :class="['w-12 h-6 rounded-full transition-colors relative', settingsData.notifyLike ? 'bg-brand-purple' : 'bg-gray-200 dark:bg-gray-600']">
                <span :class="['absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all', settingsData.notifyLike ? 'right-1' : 'left-1']"></span>
              </button>
            </div>
            <div class="p-4 flex items-center justify-between">
              <div>
                <p class="font-medium text-gray-900 dark:text-white text-sm">评论通知</p>
                <p class="text-xs text-gray-500">有人评论你的内容时通知</p>
              </div>
              <button @click="settingsData.notifyComment = !settingsData.notifyComment" :class="['w-12 h-6 rounded-full transition-colors relative', settingsData.notifyComment ? 'bg-brand-purple' : 'bg-gray-200 dark:bg-gray-600']">
                <span :class="['absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all', settingsData.notifyComment ? 'right-1' : 'left-1']"></span>
              </button>
            </div>
            <div class="p-4 flex items-center justify-between">
              <div>
                <p class="font-medium text-gray-900 dark:text-white text-sm">新粉丝通知</p>
                <p class="text-xs text-gray-500">有人关注你时通知</p>
              </div>
              <button @click="settingsData.notifyFollow = !settingsData.notifyFollow" :class="['w-12 h-6 rounded-full transition-colors relative', settingsData.notifyFollow ? 'bg-brand-purple' : 'bg-gray-200 dark:bg-gray-600']">
                <span :class="['absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all', settingsData.notifyFollow ? 'right-1' : 'left-1']"></span>
              </button>
            </div>
            <div class="p-4 flex items-center justify-between">
              <div>
                <p class="font-medium text-gray-900 dark:text-white text-sm">系统通知</p>
                <p class="text-xs text-gray-500">接收系统公告和活动通知</p>
              </div>
              <button @click="settingsData.notifySystem = !settingsData.notifySystem" :class="['w-12 h-6 rounded-full transition-colors relative', settingsData.notifySystem ? 'bg-brand-purple' : 'bg-gray-200 dark:bg-gray-600']">
                <span :class="['absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-all', settingsData.notifySystem ? 'right-1' : 'left-1']"></span>
              </button>
            </div>
          </div>
        </div>

        <!-- Verification Status -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
          <div class="p-4 border-b border-gray-100 dark:border-gray-700">
            <h3 class="font-bold text-gray-900 dark:text-white flex items-center gap-2"><Shield class="w-4 h-4 text-brand-purple" /> 认证申请记录</h3>
          </div>
          <div class="p-4">
            <div v-if="verificationHistory.length === 0" class="text-center py-6 text-gray-400 text-sm">暂无认证申请记录</div>
            <div v-else class="space-y-3">
              <div v-for="record in verificationHistory" :key="record.id" class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700/50 rounded-xl">
                <div class="flex items-center gap-3">
                  <div :class="['w-8 h-8 rounded-lg flex items-center justify-center', record.type === 'teacher' ? 'bg-amber-100 text-amber-600' : 'bg-purple-100 text-purple-600']">
                    <Briefcase v-if="record.type === 'teacher'" class="w-4 h-4" />
                    <Building2 v-else class="w-4 h-4" />
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-900 dark:text-white">{{ record.type === 'teacher' ? '教职工认证' : '官方部门入驻' }}</p>
                    <p class="text-xs text-gray-500">{{ record.time }}</p>
                  </div>
                </div>
                <span :class="['px-2 py-1 rounded-full text-xs font-medium', record.status === 'pending' ? 'bg-yellow-100 text-yellow-700' : record.status === 'approved' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700']">
                  {{ record.status === 'pending' ? '审核中' : record.status === 'approved' ? '已通过' : '已拒绝' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Danger Zone -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-red-200 dark:border-red-900/50 overflow-hidden">
          <div class="p-4 border-b border-red-100 dark:border-red-900/30 bg-red-50 dark:bg-red-900/20">
            <h3 class="font-bold text-red-600 dark:text-red-400 flex items-center gap-2"><AlertTriangle class="w-4 h-4" /> 危险操作</h3>
          </div>
          <div class="p-4 space-y-3">
            <button class="w-full p-3 text-left rounded-xl border border-gray-200 dark:border-gray-700 hover:border-red-300 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors group">
              <p class="font-medium text-gray-900 dark:text-white text-sm group-hover:text-red-600">注销账号</p>
              <p class="text-xs text-gray-500">永久删除账号及所有数据，此操作不可恢复</p>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Followers/Following Overlay (Preview Window) -->
  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0 scale-95"
    enter-to-class="opacity-100 scale-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="opacity-100 scale-100"
    leave-to-class="opacity-0 scale-95"
  >
    <div v-if="showFollowLayer" class="absolute inset-0 z-50 flex items-center justify-center p-4">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-black/20 backdrop-blur-[2px]" @click="showFollowLayer = false"></div>
      
      <!-- Card -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-md h-[420px] max-h-[80%] flex flex-col z-10 overflow-hidden ring-1 ring-black/5">
        <!-- Header -->
        <div class="px-5 py-3 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center bg-white dark:bg-gray-800 shrink-0">
          <h3 class="font-bold text-base text-gray-900 dark:text-white">{{ listTitle }}</h3>
          <button @click="showFollowLayer = false" class="p-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-400 hover:text-gray-600 transition-colors">
            <X class="w-5 h-5" />
          </button>
        </div>
        
        <!-- List Content -->
        <div class="flex-1 overflow-y-auto custom-scrollbar bg-gray-50/50 dark:bg-gray-900/50">
          <div v-if="loadingList" class="flex justify-center py-12">
            <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-brand-purple"></div>
          </div>
          <div v-else-if="followList.length === 0" class="text-center py-16 flex flex-col items-center gap-2 text-gray-400">
            <div class="w-12 h-12 bg-gray-100 dark:bg-gray-700/50 rounded-full flex items-center justify-center mb-2">
              <User class="w-6 h-6 text-gray-300 dark:text-gray-500" />
            </div>
            <span class="text-sm">暂无用户</span>
          </div>
          <div v-else class="divide-y divide-gray-100 dark:divide-gray-800">
            <div v-for="u in followList" :key="u.id" class="px-4 py-3 flex items-center justify-between hover:bg-white dark:hover:bg-gray-800 transition-colors group bg-white dark:bg-gray-800">
              <div class="flex items-center gap-3 cursor-pointer min-w-0 flex-1 mr-2" @click="openUserProfile(u)">
                <img :src="getAvatarUrl(u.avatar, u.username || u.name)" @error="handleAvatarError" class="w-9 h-9 rounded-full object-cover border border-gray-100 dark:border-gray-700" />
                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-1.5">
                    <p class="font-bold text-sm text-gray-900 dark:text-white truncate">{{ u.nickname || u.username || u.name }}</p>
                    <span v-if="u.role === 'teacher'" class="px-1.5 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-700">教师</span>
                  </div>
                  <p class="text-xs text-gray-400 truncate">{{ u.bio || '无个性签名' }}</p>
                </div>
              </div>
              <button 
                v-if="u.id !== userStore.currentUser?.id"
                @click.stop="toggleFollowUser(u)"
                :class="['shrink-0 px-3 py-1 text-xs font-bold transition-all border rounded-full', 
                  u.isFollowing 
                    ? 'bg-gray-50 dark:bg-gray-700 text-gray-500 border-gray-200 dark:border-gray-600 hover:border-red-200 hover:text-red-500 hover:bg-red-50' 
                    : 'bg-brand-purple text-white border-transparent hover:bg-indigo-600 shadow-sm']"
              >
                {{ u.isFollowing ? '已关注' : '关注' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Verification Modal (Global) -->
  <div v-if="showVerifyModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" @click="showVerifyModal = false">
    <div class="bg-white dark:bg-gray-800 rounded-2xl w-full max-w-lg shadow-2xl" @click.stop>
      <div class="p-6 border-b border-gray-100 dark:border-gray-700">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', verifyType === 'teacher' ? 'bg-amber-100 text-amber-600' : 'bg-purple-100 text-purple-600']">
              <Briefcase v-if="verifyType === 'teacher'" class="w-5 h-5" />
              <Building2 v-else class="w-5 h-5" />
            </div>
            <div>
              <h3 class="font-bold text-gray-900 dark:text-white">{{ verifyType === 'teacher' ? '教职工认证' : '官方部门入驻' }}</h3>
              <p class="text-xs text-gray-500">提交申请后将由管理员审核</p>
            </div>
          </div>
          <button @click="showVerifyModal = false" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full"><X class="w-5 h-5 text-gray-400" /></button>
        </div>
      </div>
      <div class="p-6 space-y-4 max-h-[60vh] overflow-y-auto">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ verifyType === 'teacher' ? '工号' : '部门名称' }}</label>
          <input v-model="verifyForm.identifier" :placeholder="verifyType === 'teacher' ? '请输入您的工号' : '请输入部门全称'" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2.5 text-sm focus:ring-2 focus:ring-brand-purple" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ verifyType === 'teacher' ? '所属院系' : '部门类型' }}</label>
          <input v-model="verifyForm.department" :placeholder="verifyType === 'teacher' ? '例：计算机科学与技术学院' : '例：学生事务、后勤服务'" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2.5 text-sm focus:ring-2 focus:ring-brand-purple" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ verifyType === 'teacher' ? '职称/职务' : '负责人姓名' }}</label>
          <input v-model="verifyForm.title" :placeholder="verifyType === 'teacher' ? '例：副教授、讲师' : '请输入负责人姓名'" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2.5 text-sm focus:ring-2 focus:ring-brand-purple" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">联系电话</label>
          <input v-model="verifyForm.phone" placeholder="请输入联系电话" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2.5 text-sm focus:ring-2 focus:ring-brand-purple" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">证明材料</label>
          <div class="border-2 border-dashed border-gray-200 dark:border-gray-600 rounded-xl p-4 text-center hover:border-brand-purple transition-colors cursor-pointer relative">
            <input type="file" accept="image/*,.pdf" multiple class="absolute inset-0 opacity-0 cursor-pointer" @change="handleVerifyFileUpload" />
            <Upload class="w-8 h-8 text-gray-400 mx-auto mb-2" />
            <p class="text-sm text-gray-500">点击上传证明材料</p>
            <p class="text-xs text-gray-400 mt-1">支持图片或PDF，最多3个文件</p>
          </div>
          <div v-if="verifyForm.files.length > 0" class="flex flex-wrap gap-2 mt-2">
            <div v-for="(file, idx) in verifyForm.files" :key="idx" class="flex items-center gap-2 px-3 py-1.5 bg-gray-100 dark:bg-gray-700 rounded-lg text-xs">
              <FileText class="w-4 h-4 text-gray-500" />
              <span class="truncate max-w-[120px]">{{ file.name }}</span>
              <button @click="verifyForm.files.splice(idx, 1)" class="text-gray-400 hover:text-red-500"><X class="w-3 h-3" /></button>
            </div>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">申请说明 (选填)</label>
          <textarea v-model="verifyForm.reason" placeholder="补充说明您的申请原因..." class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2.5 text-sm focus:ring-2 focus:ring-brand-purple resize-none h-20"></textarea>
        </div>
      </div>
      <div class="p-6 border-t border-gray-100 dark:border-gray-700 flex gap-3">
        <button @click="showVerifyModal = false" class="flex-1 py-2.5 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 rounded-xl font-medium hover:bg-gray-200 transition-colors">取消</button>
        <button @click="submitVerification" :disabled="!canSubmitVerify || isSubmittingVerify" class="flex-1 py-2.5 bg-brand-purple text-white rounded-xl font-medium hover:bg-indigo-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
          {{ isSubmittingVerify ? '提交中...' : '提交申请' }}
        </button>
      </div>
    </div>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import QRCode from 'qrcode'
import { useUserStore, useUIStore, usePostStore } from '../../stores'
import { fileApi, verificationApi, userApi } from '../../api'
import {
  X, Check, LogOut, UserPlus, UserCheck, MessageCircle, School, BookOpen, Sparkles,
  ThumbsUp, MessageSquare, ArrowLeft, Settings, Edit2, CreditCard, ScanLine, User,
  Image as ImageIcon, Camera, Shield, GraduationCap, Briefcase, Building2, Eye, EyeOff, Hash,
  Clock, XCircle, Upload, FileText, Lock, Key, Smartphone, Mail, ChevronRight, Bell, AlertTriangle, BadgeCheck
} from 'lucide-vue-next'

const userStore = useUserStore()
const uiStore = useUIStore()
const postStore = usePostStore()

// 使用公共头像/封面工具函数
import { getAvatarUrl, handleAvatarError, isValidCoverUrl, handleCoverError, DEFAULT_AVATAR } from '../../utils/avatar'
import IdCardBg from '@/assets/id-card-bg.jpg'
import IdCardBackBg from '@/assets/id-card-back-bg.png'

// 编辑页面封面加载失败处理
function handleEditCoverError(e) {
  e.target.style.display = 'none'
  if (editData.value) {
    editData.value.coverImage = ''
  }
}

const view = ref('profile')
const showIdCard = ref(false)
const isFlipped = ref(false)
const idCardQrcode = ref('')

async function generateQRCode() {
  if (!user.value) return
  
  const text = `【CampusConnect 身份认证】
姓名: ${user.value.name}
ID: ${user.value.id}
学院: ${user.value.college || '未填写'}
专业: ${user.value.major || '未填写'}
有效期: 2024 - 2028
状态: 已认证`

  try {
    idCardQrcode.value = await QRCode.toDataURL(text, {
      width: 200,
      margin: 1,
      color: {
        dark: '#000000',
        light: '#ffffff'
      }
    })
  } catch (err) {
    console.error('QR Code generation failed:', err)
  }
}

watch(showIdCard, (newVal) => {
  if (newVal) {
    generateQRCode()
  }
})
const isSaving = ref(false)
const editData = ref({})
const tagInput = ref('')

// 存储从 API 获取的完整用户数据（用于查看其他用户时）
const fullUserData = ref(null)

// Verification
const showVerifyModal = ref(false)
const verifyType = ref('teacher')
const verifyForm = ref({ identifier: '', department: '', title: '', phone: '', files: [], reason: '' })
const verificationStatus = ref({ teacher: null, department: null }) // null, 'pending', 'approved', 'rejected'
const verificationHistory = ref([])

// Settings
const settingsData = ref({
  allowStrangerMessage: true,
  showOnlineStatus: true,
  showFollowList: true,
  notifyLike: true,
  notifyComment: true,
  notifyFollow: true,
  notifySystem: true
})

const canSubmitVerify = computed(() => {
  return verifyForm.value.identifier.trim() && verifyForm.value.department.trim() && verifyForm.value.phone.trim()
})

// 判断是否是自己
const isMe = computed(() => userStore.currentUser?.id === (uiStore.modalData.data?.id || userStore.currentUser?.id))

const user = computed(() => {
  let userData
  if (isMe.value) {
    userData = userStore.currentUser || {}
  } else {
    // 优先使用从 API 获取的完整数据
    userData = fullUserData.value || uiStore.modalData.data || {}
  }
  
  // 确保 hobbies 是数组格式
  let hobbies = userData.hobbies || []
  if (hobbies && !Array.isArray(hobbies)) {
    try {
      hobbies = JSON.parse(hobbies)
      if (!Array.isArray(hobbies)) hobbies = []
    } catch (e) {
      hobbies = []
    }
  }
  
  return { 
    ...userData, 
    name: userData.nickname || userData.name || userData.username || '未知用户',
    hobbies 
  }
})
// 改为 ref，通过 API 直接查询是否关注（最可靠的方式）
const isFollowing = ref(false)
const userPosts = computed(() => postStore.posts.filter(p => p.user.id === user.value?.id))

// 当打开个人主页时，获取最新用户数据
onMounted(async () => {
  const targetUserId = uiStore.modalData.data?.id
  
  // 关键修复：无论查看谁的主页，都先刷新一下当前登录用户的数据
  // 这样能确保 userStore.currentUser.following（关注名单）是最新的
  // 从而保证“关注”按钮的状态显示正确
  userStore.fetchCurrentUser().catch(e => console.error('后台刷新当前用户失败:', e))
  
  // 如果查看的是自己的主页，上面已经刷新了，这里不需要额外操作（除非是逻辑分流）
  if (!targetUserId || targetUserId === userStore.currentUser?.id) {
    // 已经在上面刷新了，这里可以留空或者做特定的处理
  } else {
    // 查看其他用户时，直接调用 API 查询是否关注
    try {
      const followRes = await userApi.isFollowing(targetUserId)
      isFollowing.value = followRes.data === true
    } catch (e) {
      console.error('查询关注状态失败:', e)
      isFollowing.value = false
    }
    
    // 获取该用户的完整数据（如封面、签名等）
    try {
      const res = await userApi.getUserById(targetUserId)
      if (res.data) {
        console.log('User data loaded:', res.data)
        fullUserData.value = res.data
      }
    } catch (e) {
      console.error('获取用户信息失败:', e)
    }
  }
  
  // 获取认证状态，用于正确显示 ID 卡身份
  loadVerificationStatus()
})

const identityInfo = computed(() => {
  // 1. 优先使用已通过的认证状态
  if (verificationStatus.value?.teacher === 'approved') {
    return { title: 'Faculty Member', bgText: 'TEACHER' }
  }
  if (verificationStatus.value?.department === 'approved') {
    return { title: 'Campus Admin', bgText: 'ADMIN' }
  }
  
  // 2. 其次使用用户角色 (user.role)
  const role = (user.value.role || '').toLowerCase()
  if (role === 'teacher') return { title: 'Faculty Member', bgText: 'TEACHER' }
  if (role === 'admin') return { title: 'Administrator', bgText: 'ADMIN' }
  
  // 3. 默认为本科生 (Undergraduate)
  return { title: 'Undergraduate', bgText: 'STUDENT' }
})

watch(() => view.value, (val) => {
  if (val === 'edit') {
    const userData = JSON.parse(JSON.stringify(userStore.currentUser || {}))
    // 确保 hobbies 是数组格式
    if (userData.hobbies && !Array.isArray(userData.hobbies)) {
      try {
        userData.hobbies = JSON.parse(userData.hobbies)
        if (!Array.isArray(userData.hobbies)) userData.hobbies = []
      } catch (e) {
        userData.hobbies = []
      }
    }
    if (!userData.hobbies) userData.hobbies = []
    // 确保 privacy 是对象格式
    if (userData.privacy && typeof userData.privacy !== 'object') {
      try {
        userData.privacy = JSON.parse(userData.privacy)
      } catch (e) {
        userData.privacy = {}
      }
    }
    if (!userData.privacy) userData.privacy = {}
    editData.value = userData
  }
}, { immediate: true })

function stripHtml(html) { return html?.replace(/<[^>]+>/g, '') || '' }

function handleLogout() {
  userStore.logout()
  uiStore.closeModal()
}

async function handleFollowToggle() {
  const result = await userStore.toggleFollow(user.value.id)
  if (result.success) {
    // 重新获取目标用户数据以更新粉丝数
    try {
      const res = await userApi.getUserById(user.value.id)
      if (res.data) {
        fullUserData.value = res.data
      }
    } catch (e) {
      console.error('刷新用户数据失败:', e)
    }
  }
}

function handleSendMessage() { uiStore.openModal('MESSAGES_FULL', user.value) }

function handlePostClick(post) {
  uiStore.closeModal()
  uiStore.openModal('POST_DETAIL', post)
}

function togglePrivacy(field) {
  if (!editData.value.privacy) editData.value.privacy = {}
  editData.value.privacy[field] = !editData.value.privacy[field]
}

// Followers/Following List
const followList = ref([])
const loadingList = ref(false)
const listTitle = ref('')
const showFollowLayer = ref(false) // Control the overlay

async function openFollowList(type) {
  // view.value = 'users' // No longer switching view
  showFollowLayer.value = true
  loadingList.value = true
  followList.value = []
  
  // 双重保险：打开列表前，再次刷新我的关注名单，确保状态最新
  await userStore.fetchCurrentUser().catch(() => {})
  
  try {
    let res
    if (type === 'following') {
      listTitle.value = '关注列表'
      res = await userApi.getFollowing(user.value.id)
    } else {
      listTitle.value = '粉丝列表'
      res = await userApi.getFollowers(user.value.id)
    }
    
    if (res.data && Array.isArray(res.data.records)) {
      const myId = userStore.currentUser?.id
      const myFollowing = userStore.currentUser?.following || []
      
      // 如果查看的是我自己的关注列表，那么列表里的所有人肯定都是我已关注的
      const isMyFollowingList = user.value.id === myId && type === 'following'

      followList.value = res.data.records.map(u => ({
        ...u,
        // 如果是我的关注列表，直接 true；否则检查 ID 是否在我的关注列表中（统一转字符串比较）
        isFollowing: isMyFollowingList || myFollowing.some(id => String(id) === String(u.id))
      }))
    }
  } catch (e) {
    console.error('获取列表失败:', e)
  } finally {
    loadingList.value = false
  }
}

async function toggleFollowUser(targetUser) {
  const result = await userStore.toggleFollow(targetUser.id)
  if (result.success) {
    targetUser.isFollowing = !targetUser.isFollowing
  }
}

function openUserProfile(targetUser) {
  if (targetUser.id === user.value.id) return
  
  // Close current modal and open new one
  uiStore.closeModal()
  setTimeout(() => {
    uiStore.openModal('PROFILE', targetUser)
  }, 100)
}

const uploadingImage = ref(false)

async function handleImageUpload(e, type) {
  const file = e.target.files?.[0]
  if (!file) return
  
  // 先显示本地预览
  const previewUrl = URL.createObjectURL(file)
  if (type === 'avatar') editData.value.avatar = previewUrl
  else editData.value.coverImage = previewUrl
  
  // 上传到服务器
  uploadingImage.value = true
  try {
    const res = await fileApi.upload(file, type)
    // 获取服务器返回的URL
    const serverUrl = 'http://localhost:8080/api' + res.data
    if (type === 'avatar') {
      editData.value.avatar = serverUrl
    } else {
      editData.value.coverImage = serverUrl
    }
    // 上传成功后才清理预览URL（因为已经有服务器URL了）
    URL.revokeObjectURL(previewUrl)
  } catch (err) {
    const errorMsg = err.response?.data?.message || err.message || '未知错误'
    alert('图片上传失败: ' + errorMsg + '\n\n请确保后端服务已启动并重启')
    // 上传失败时保留预览URL，不清除，让用户能看到预览
    // 注意：这里不调用 URL.revokeObjectURL(previewUrl)
  } finally {
    uploadingImage.value = false
  }
}

function handleTagInput(e) {
  const val = e.target.value.trim()
  if (val && !editData.value.hobbies?.includes(val)) {
    if (!editData.value.hobbies) editData.value.hobbies = []
    editData.value.hobbies.push(val)
    e.target.value = ''
  }
}

function removeTag(tag) {
  editData.value.hobbies = editData.value.hobbies?.filter(t => t !== tag)
}

async function handleSave() {
  isSaving.value = true
  try {
    // 映射前端字段名到后端字段名
    const backendData = {
      nickname: editData.value.name,
      avatar: editData.value.avatar,
      coverImage: editData.value.coverImage,
      bio: editData.value.bio,
      gender: editData.value.gender,
      college: editData.value.college,
      major: editData.value.major,
      className: editData.value.className,
      dormitory: editData.value.dormitory,
      age: editData.value.age ? parseInt(editData.value.age) : null,
      // 确保 hobbies 和 privacy 正确序列化（避免双重编码）
      hobbies: Array.isArray(editData.value.hobbies) 
        ? JSON.stringify(editData.value.hobbies) 
        : (editData.value.hobbies || null),
      privacy: typeof editData.value.privacy === 'object' 
        ? JSON.stringify(editData.value.privacy) 
        : (editData.value.privacy || null)
    }
    
    // 调用后端API保存
    const result = await userStore.updateUser(backendData)
    
    if (result.success) {
      // 同步更新本地用户数据（保持前端字段名）
      userStore.currentUser.name = editData.value.name
      userStore.currentUser.avatar = editData.value.avatar
      userStore.currentUser.coverImage = editData.value.coverImage
      userStore.currentUser.bio = editData.value.bio
      userStore.currentUser.gender = editData.value.gender
      userStore.currentUser.college = editData.value.college
      userStore.currentUser.major = editData.value.major
      userStore.currentUser.className = editData.value.className
      userStore.currentUser.dormitory = editData.value.dormitory
      userStore.currentUser.age = editData.value.age
      userStore.currentUser.hobbies = editData.value.hobbies
      userStore.currentUser.privacy = editData.value.privacy
      
      // 更新帖子中的用户信息
      postStore.updateUserPosts(editData.value)
      
      view.value = 'profile'
    } else {
      console.error('保存失败:', result.error)
      alert('保存失败: ' + (result.error || '未知错误'))
    }
  } catch (e) {
    console.error('保存出错:', e)
    alert('保存失败，请重试')
  } finally {
    isSaving.value = false
  }
}

// Verification functions
function openVerifyModal(type) {
  verifyType.value = type
  verifyForm.value = { identifier: '', department: '', title: '', phone: '', files: [], reason: '' }
  showVerifyModal.value = true
}

function handleVerifyFileUpload(e) {
  const files = Array.from(e.target.files || [])
  files.forEach(file => {
    if (verifyForm.value.files.length < 3) {
      verifyForm.value.files.push(file)
    }
  })
}

const isSubmittingVerify = ref(false)

async function submitVerification() {
  if (!canSubmitVerify.value || isSubmittingVerify.value) return
  
  isSubmittingVerify.value = true
  try {
    // 上传证明材料文件
    let idCardImageUrl = ''
    if (verifyForm.value.files.length > 0) {
      const uploadRes = await fileApi.upload(verifyForm.value.files[0], 'verification')
      idCardImageUrl = 'http://localhost:8080/api' + uploadRes.data
    }
    
    // 构建提交数据 - 区分教职工和部门
    const identityType = verifyType.value === 'teacher' ? 'TEACHER' : 'ORG'
    const submitData = {
      identityType: identityType,
      realName: verifyForm.value.title,
      idNumber: verifyForm.value.identifier,
      department: verifyForm.value.department,
      idCardImage: idCardImageUrl
    }
    
    // 调用API提交
    await verificationApi.submit(submitData)
    
    // 更新本地状态 - 根据认证类型正确设置
    if (verifyType.value === 'teacher') {
      verificationStatus.value.teacher = 'pending'
    } else {
      verificationStatus.value.department = 'pending'
    }
    
    // 添加到历史记录
    verificationHistory.value.unshift({
      id: Date.now(),
      type: verifyType.value,
      status: 'pending',
      time: new Date().toLocaleString('zh-CN')
    })
    
    showVerifyModal.value = false
    alert('申请已提交，请等待管理员审核')
  } catch (error) {
    console.error('提交认证失败:', error)
    alert('提交失败: ' + (error.message || '请稍后重试'))
  } finally {
    isSubmittingVerify.value = false
  }
}

// 从API加载认证状态
async function loadVerificationStatus() {
  try {
    const res = await verificationApi.getStatus()
    if (res && res.data) {
      // 根据后端返回的数据格式解析
      // 后端可能返回单个认证记录或分类状态
      if (res.data.identityType) {
        // 单条认证记录格式
        const status = res.data.status?.toLowerCase() || 'none'
        const mappedStatus = status === 'pending' ? 'pending' : 
                            status === 'approved' || status === '已通过' ? 'approved' : 
                            status === 'rejected' || status === '已拒绝' ? 'rejected' : null
        
        if (res.data.identityType === 'TEACHER') {
          verificationStatus.value.teacher = mappedStatus
        } else if (res.data.identityType === 'ORG') {
          verificationStatus.value.department = mappedStatus
        }
      } else {
        // 分类状态格式
        verificationStatus.value.teacher = res.data.teacher === 'none' ? null : res.data.teacher
        verificationStatus.value.department = res.data.department === 'none' ? null : res.data.department
      }
      
      // 标准化历史记录中的类型
      const rawHistory = res.data.history || []
      verificationHistory.value = rawHistory.map(record => {
        let type = record.type || record.identityType || ''
        // 将后端的类型转换为前端使用的类型
        if (type === 'TEACHER' || type === '教师' || type === '教职工') {
          type = 'teacher'
        } else if (type === 'ORG' || type === '部门' || type === 'DEPARTMENT') {
          type = 'department'
        }
        // 标准化状态
        let status = record.status || ''
        if (status === 'PENDING' || status === '待审核') {
          status = 'pending'
        } else if (status === 'APPROVED' || status === '已通过') {
          status = 'approved'
        } else if (status === 'REJECTED' || status === '已拒绝') {
          status = 'rejected'
        }
        return {
          ...record,
          type,
          status,
          time: record.time || record.createdAt ? new Date(record.createdAt || record.time).toLocaleString('zh-CN') : ''
        }
      })
      
      // 如果认证已通过，更新用户角色
      if (verificationStatus.value.teacher === 'approved' && editData.value.role !== 'teacher') {
        editData.value.role = 'teacher'
        userStore.currentUser.role = 'teacher'
      }
      if (verificationStatus.value.department === 'approved' && editData.value.role !== 'department') {
        editData.value.role = 'department'
        userStore.currentUser.role = 'department'
      }
    }
  } catch (error) {
    console.error('加载认证状态失败:', error)
  }
}

// 组件挂载时加载认证状态
onMounted(() => {
  if (userStore.currentUser) {
    loadVerificationStatus()
  }
})
</script>

<style>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 3px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #9ca3af; }

/* Premium ID Card Styles */
.id-card-shine {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0) 0%, 
    rgba(255, 255, 255, 0.05) 45%, 
    rgba(255, 255, 255, 0.3) 50%, 
    rgba(255, 255, 255, 0.05) 55%, 
    rgba(255, 255, 255, 0) 100%
  );
  background-size: 200% 200%;
  animation: shine 6s infinite linear;
}

@keyframes shine {
  0% { background-position: -200% -200%; }
  100% { background-position: 200% 200%; }
}

.id-card-pattern {
  background-image: radial-gradient(circle at 2px 2px, rgba(255,255,255,0.05) 1px, transparent 0);
  background-size: 20px 20px;
}

.id-card-chip {
  background: linear-gradient(135deg, #ffd700 0%, #b8860b 50%, #ffd700 100%);
  position: relative;
  overflow: hidden;
}

.id-card-chip::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(90deg, rgba(0,0,0,0.1) 1px, transparent 1px),
    linear-gradient(0deg, rgba(0,0,0,0.1) 1px, transparent 1px);
  background-size: 4px 4px;
}
</style>
