<template>
  <div class="flex h-full">
    <!-- Contacts List -->
    <div class="w-72 border-r border-gray-100 dark:border-gray-700 flex flex-col bg-white dark:bg-gray-800">
      <div class="p-4 border-b border-gray-100 dark:border-gray-700">
        <div class="flex justify-between items-center mb-3">
          <h2 class="text-lg font-bold text-gray-900 dark:text-white">消息中心</h2>
          <button @click="showCreateGroup = true" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full text-gray-500 hover:text-brand-purple transition-colors" title="创建群聊">
            <UserPlus class="w-5 h-5" />
          </button>
        </div>
        <div class="relative mb-3">
          <Search class="absolute left-3 top-2.5 w-4 h-4 text-gray-400" />
          <input v-model="searchQuery" type="text" placeholder="搜索联系人..." class="w-full pl-9 pr-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-full text-sm focus:ring-2 focus:ring-brand-purple outline-none" />
        </div>
        <div class="flex bg-gray-100 dark:bg-gray-700 rounded-full p-1">
          <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id" :class="['flex-1 py-1.5 text-xs font-medium rounded-full transition-all', activeTab === tab.id ? 'bg-white dark:bg-gray-600 text-brand-purple shadow-sm' : 'text-gray-500 hover:text-gray-700']">{{ tab.label }}</button>
        </div>
      </div>
      <div class="flex-1 overflow-y-auto custom-scrollbar relative">
        <div v-for="contact in filteredContacts" :key="contact.id" 
          @click="selectContact(contact)" 
          @contextmenu.prevent="showContextMenu($event, contact)"
          :class="['p-3 flex gap-3 cursor-pointer transition-colors border-b border-gray-50 dark:border-gray-700/30', 
            selectedContact?.id === contact.id ? 'bg-brand-purple/5 border-l-2 border-l-brand-purple' : 'hover:bg-gray-50 dark:hover:bg-gray-700/50',
            contact.isPinned ? 'bg-amber-50/50 dark:bg-amber-900/10' : '']">
          <div class="relative flex-shrink-0">
            <img v-if="contact.isGroup && contact.avatar" :src="contact.avatar" class="w-12 h-12 rounded-xl object-cover" />
            <div v-else-if="contact.isGroup" :class="['w-12 h-12 rounded-xl flex items-center justify-center text-white font-bold text-sm', contact.color || 'bg-brand-purple']">{{ contact.name.substring(0, 2) }}</div>
            <img v-else :src="contact.avatar" class="w-12 h-12 rounded-full object-cover" />
            <span v-if="contact.unread" class="absolute -top-1 -right-1 min-w-[18px] h-[18px] bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1">{{ contact.unread > 99 ? '99+' : contact.unread }}</span>
            <span v-if="contact.isPinned" class="absolute -bottom-1 -right-1 text-amber-500"><Pin class="w-3 h-3" /></span>
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex justify-between items-baseline">
              <span class="font-semibold text-sm text-gray-900 dark:text-white truncate flex items-center gap-1">
                {{ contact.name }}
                <VerifiedBadge v-if="!contact.isGroup" :type="contact.verifyType" />
              </span>
              <span class="text-[10px] text-gray-400 flex-shrink-0 ml-1">{{ contact.time }}</span>
            </div>
            <p class="text-xs text-gray-500 dark:text-gray-400 truncate mt-0.5">
              <span v-if="contact.isGroup && contact.lastSender" class="text-gray-400">{{ contact.lastSender }}: </span>{{ formatLastMessage(contact.lastMessage) }}
            </p>
          </div>
        </div>
        <div v-if="filteredContacts.length === 0" class="p-8 text-center text-gray-400 text-sm">暂无消息</div>
        
        <!-- Context Menu -->
        <div v-if="contextMenu.visible" 
          :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }"
          class="fixed z-[300] bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 py-1 min-w-[140px] animate-in fade-in zoom-in-95 duration-100">
          <button @click="togglePin" class="w-full px-4 py-2 text-left text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 flex items-center gap-2">
            <Pin class="w-4 h-4" />
            {{ contextMenu.contact?.isPinned ? '取消置顶' : '置顶聊天' }}
          </button>
          <button @click="markAsRead" class="w-full px-4 py-2 text-left text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 flex items-center gap-2">
            <CheckCircle class="w-4 h-4" />
            标记已读
          </button>
          <div class="border-t border-gray-100 dark:border-gray-700 my-1"></div>
          <button @click="deleteFromContextMenu" class="w-full px-4 py-2 text-left text-sm text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 flex items-center gap-2">
            <Trash2 class="w-4 h-4" />
            删除聊天
          </button>
        </div>
      </div>
    </div>

    <!-- Chat Area -->
    <div class="flex-1 flex flex-col bg-gray-50 dark:bg-gray-900">
      <template v-if="selectedContact">
        <!-- Chat Header -->
        <div class="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between bg-white dark:bg-gray-800">
          <div class="flex items-center gap-3 cursor-pointer" @click="!selectedContact.isGroup && handleAvatarClick(selectedContact)">
            <img v-if="selectedContact.isGroup && selectedContact.avatar" :src="selectedContact.avatar" class="w-10 h-10 rounded-xl object-cover" />
            <div v-else-if="selectedContact.isGroup" :class="['w-10 h-10 rounded-xl flex items-center justify-center text-white font-bold text-sm', selectedContact.color || 'bg-brand-purple']">{{ selectedContact.name.substring(0, 2) }}</div>
            <img v-else :src="selectedContact.avatar" class="w-10 h-10 rounded-full object-cover hover:opacity-80 transition-opacity" />
            <div>
              <h3 class="font-bold text-gray-900 dark:text-white text-sm flex items-center gap-2">
                {{ selectedContact.name }}
                <VerifiedBadge v-if="!selectedContact.isGroup" :type="selectedContact.verifyType || selectedContact.role" />
                <span v-if="selectedContact.isGroup" class="text-xs font-normal text-gray-400">{{ selectedContact.memberCount }}人</span>
              </h3>
              <p v-if="!selectedContact.isGroup" class="text-xs text-gray-400">私聊</p>
            </div>
          </div>
          <div class="flex items-center gap-1">
            <button v-if="selectedContact.isGroup" @click="showGroupInfo = !showGroupInfo" :class="['p-2 rounded-full transition-colors', showGroupInfo ? 'bg-brand-purple/10 text-brand-purple' : 'hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-500']"><Users class="w-5 h-5" /></button>
            <button @click="uiStore.closeModal()" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full text-gray-500"><X class="w-5 h-5" /></button>
          </div>
        </div>

        <div class="flex-1 flex overflow-hidden">
          <!-- Messages -->
          <div class="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar">
            <div v-for="msg in currentMessages" :key="msg.id" :class="['flex gap-2', msg.isMine ? 'justify-end' : 'justify-start']">
              <img v-if="!msg.isMine" :src="msg.avatar || selectedContact.avatar" class="w-8 h-8 rounded-full flex-shrink-0 cursor-pointer hover:opacity-80" @click="handleMsgAvatarClick(msg)" />
              <div :class="['max-w-[65%] flex flex-col', msg.isMine ? 'items-end' : 'items-start']">
                <span v-if="!msg.isMine && selectedContact.isGroup" class="text-xs text-gray-400 mb-1 ml-1">{{ msg.sender }}</span>
                <!-- Image (no bubble) -->
                <img v-if="msg.image && !msg.content" :src="msg.image" class="rounded-xl max-w-[240px] max-h-[240px] object-cover cursor-pointer shadow-md hover:shadow-lg transition-shadow" @click="openImagePreview(msg.image)" />
                <!-- Text bubble -->
                <div v-else-if="msg.content" :class="['px-3 py-2 rounded-2xl text-sm', msg.isMine ? 'bg-brand-purple text-white rounded-br-sm' : 'bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-200 rounded-bl-sm shadow-sm']">
                  <img v-if="msg.image" :src="msg.image" class="rounded-lg max-w-[200px] max-h-[150px] object-cover mb-2 cursor-pointer" @click="openImagePreview(msg.image)" />
                  <p>{{ msg.content }}</p>
                </div>
                <span class="text-[10px] text-gray-400 mt-1">{{ msg.time }}</span>
              </div>
              <img v-if="msg.isMine" :src="userStore.currentUser?.avatar || 'https://via.placeholder.com/32'" class="w-8 h-8 rounded-full flex-shrink-0" />
            </div>
          </div>

          <!-- Group Info Sidebar -->
          <div v-if="selectedContact.isGroup && showGroupInfo" class="w-64 border-l border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4 overflow-y-auto custom-scrollbar flex flex-col">
            <h4 class="font-bold text-gray-900 dark:text-white text-sm mb-4">群聊信息</h4>
            
            <!-- Group Settings (Owner/Admin) -->
            <div v-if="isGroupAdmin" class="mb-4 space-y-2">
              <button @click="showEditGroup = true" class="w-full flex items-center gap-2 px-3 py-2 bg-gray-50 dark:bg-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors">
                <Settings class="w-4 h-4" /> 群设置
              </button>
            </div>

            <!-- Announcement -->
            <div class="mb-4 p-3 bg-orange-50 dark:bg-orange-900/20 rounded-xl border border-orange-100 dark:border-orange-800/30">
              <div class="flex items-center justify-between mb-1">
                <div class="flex items-center gap-1 text-orange-600 dark:text-orange-400 text-xs font-bold">
                  <Megaphone class="w-3 h-3" /> 群公告
                </div>
                <button v-if="isGroupAdmin" @click="showEditAnnouncement = true" class="text-orange-500 hover:text-orange-600"><Edit2 class="w-3 h-3" /></button>
              </div>
              <p class="text-xs text-gray-600 dark:text-gray-300">{{ selectedContact.announcement || '暂无公告' }}</p>
            </div>

            <!-- Members -->
            <div class="flex-1">
              <div class="flex justify-between items-center mb-2">
                <span class="text-xs font-bold text-gray-500">群成员 ({{ selectedContact.memberCount }})</span>
                <button v-if="isGroupOwner" @click="showManageMembers = true" class="text-xs text-brand-purple hover:underline">管理</button>
              </div>
              <div class="grid grid-cols-4 gap-2">
                <div v-for="member in selectedContact.members" :key="member.id" class="flex flex-col items-center cursor-pointer group relative" @click="handleMemberClick(member)">
                  <div class="relative">
                    <img :src="member.avatar" class="w-10 h-10 rounded-full object-cover hover:opacity-80 transition-opacity" />
                    <span v-if="member.role === 'owner'" class="absolute -bottom-0.5 -right-0.5 bg-orange-500 text-white text-[8px] px-1 rounded">群主</span>
                    <span v-else-if="member.role === 'admin'" class="absolute -bottom-0.5 -right-0.5 bg-blue-500 text-white text-[8px] px-1 rounded">管理</span>
                  </div>
                  <span class="text-[10px] text-gray-500 mt-1 truncate w-full text-center">{{ member.name }}</span>
                </div>
                <div class="flex flex-col items-center cursor-pointer" @click="showAddMember = true">
                  <div class="w-10 h-10 rounded-full border-2 border-dashed border-gray-300 flex items-center justify-center text-gray-400 hover:border-brand-purple hover:text-brand-purple transition-colors"><Plus class="w-4 h-4" /></div>
                </div>
              </div>
            </div>
            <button class="w-full mt-4 py-2 text-red-500 text-xs font-medium hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors">退出群聊</button>
          </div>
        </div>

        <!-- Input Area -->
        <div class="p-3 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
          <div v-if="imagePreview" class="mb-2 relative inline-block">
            <img :src="imagePreview" class="h-20 rounded-lg" />
            <button @click="imagePreview = null" class="absolute -top-2 -right-2 w-5 h-5 bg-gray-800 text-white rounded-full flex items-center justify-center hover:bg-red-500"><X class="w-3 h-3" /></button>
          </div>
          <div class="flex items-center gap-2 mb-2">
            <label class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg cursor-pointer text-gray-500 hover:text-brand-purple transition-colors">
              <ImageIcon class="w-5 h-5" /><input type="file" accept="image/*" class="hidden" @change="handleImageSelect" />
            </label>
            <button class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg text-gray-500 hover:text-brand-purple transition-colors"><Smile class="w-5 h-5" /></button>
            <button class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg text-gray-500 hover:text-brand-purple transition-colors"><Paperclip class="w-5 h-5" /></button>
          </div>
          <div class="flex items-center gap-2">
            <input v-model="messageInput" type="text" placeholder="发送消息 (Enter 发送)..." class="flex-1 bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl px-4 py-2.5 text-sm focus:ring-2 focus:ring-brand-purple outline-none" @keydown.enter="sendMessage" />
            <button @click="sendMessage" :disabled="!messageInput.trim() && !imagePreview" class="p-3 bg-brand-purple text-white rounded-xl hover:bg-indigo-600 disabled:opacity-50 transition-colors shadow-lg shadow-brand-purple/20"><Send class="w-5 h-5" /></button>
          </div>
        </div>
      </template>

      <div v-else class="flex-1 flex items-center justify-center text-gray-400">
        <div class="text-center">
          <MessageCircle class="w-16 h-16 mx-auto mb-4 opacity-30" />
          <p class="text-sm">选择一个联系人开始聊天</p>
        </div>
      </div>
    </div>

    <!-- Create Group Modal -->
    <div v-if="showCreateGroup" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/50 backdrop-blur-sm" @click.self="showCreateGroup = false">
      <div class="bg-white dark:bg-gray-800 rounded-2xl w-[400px] max-h-[80vh] overflow-hidden shadow-xl">
        <div class="p-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center">
          <h3 class="font-bold text-gray-900 dark:text-white">创建群聊</h3>
          <button @click="showCreateGroup = false" class="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full"><X class="w-5 h-5 text-gray-500" /></button>
        </div>
        <div class="p-4 space-y-4">
          <div class="flex items-center gap-4">
            <label class="relative cursor-pointer">
              <div v-if="newGroup.avatar" class="w-16 h-16 rounded-xl overflow-hidden"><img :src="newGroup.avatar" class="w-full h-full object-cover" /></div>
              <div v-else class="w-16 h-16 rounded-xl bg-gradient-to-br from-brand-purple to-blue-500 flex items-center justify-center text-white"><Camera class="w-6 h-6" /></div>
              <input type="file" accept="image/*" class="hidden" @change="handleGroupAvatarSelect" />
              <div class="absolute -bottom-1 -right-1 w-5 h-5 bg-brand-purple rounded-full flex items-center justify-center text-white"><Plus class="w-3 h-3" /></div>
            </label>
            <div class="flex-1">
              <label class="block text-xs font-medium text-gray-500 mb-1">群名称</label>
              <input v-model="newGroup.name" type="text" placeholder="输入群名称" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-brand-purple outline-none" />
            </div>
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-500 mb-2">选择成员</label>
            <div class="max-h-48 overflow-y-auto space-y-2">
              <label v-for="friend in availableFriends" :key="friend.id" class="flex items-center gap-3 p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer">
                <input type="checkbox" v-model="newGroup.members" :value="friend.id" class="w-4 h-4 text-brand-purple rounded focus:ring-brand-purple" />
                <img :src="friend.avatar" class="w-8 h-8 rounded-full" />
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ friend.name }}</span>
              </label>
            </div>
          </div>
        </div>
        <div class="p-4 border-t border-gray-100 dark:border-gray-700">
          <button @click="createGroup" :disabled="!newGroup.name.trim() || newGroup.members.length === 0" class="w-full py-2 bg-brand-purple text-white rounded-lg font-medium hover:bg-indigo-600 disabled:opacity-50 transition-colors">创建群聊 ({{ newGroup.members.length }}人)</button>
        </div>
      </div>
    </div>

    <!-- Edit Group Modal -->
    <div v-if="showEditGroup" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/50 backdrop-blur-sm" @click.self="showEditGroup = false">
      <div class="bg-white dark:bg-gray-800 rounded-2xl w-[400px] overflow-hidden shadow-xl">
        <div class="p-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center">
          <h3 class="font-bold text-gray-900 dark:text-white">群设置</h3>
          <button @click="showEditGroup = false" class="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full"><X class="w-5 h-5 text-gray-500" /></button>
        </div>
        <div class="p-4 space-y-4">
          <div class="flex items-center gap-4">
            <label class="relative cursor-pointer">
              <img v-if="editGroupData.avatar" :src="editGroupData.avatar" class="w-16 h-16 rounded-xl object-cover" />
              <div v-else :class="['w-16 h-16 rounded-xl flex items-center justify-center text-white font-bold', editGroupData.color || 'bg-brand-purple']">{{ editGroupData.name?.substring(0, 2) }}</div>
              <input type="file" accept="image/*" class="hidden" @change="handleEditGroupAvatarSelect" />
              <div class="absolute -bottom-1 -right-1 w-5 h-5 bg-brand-purple rounded-full flex items-center justify-center text-white"><Camera class="w-3 h-3" /></div>
            </label>
            <div class="flex-1">
              <label class="block text-xs font-medium text-gray-500 mb-1">群名称</label>
              <input v-model="editGroupData.name" type="text" class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-brand-purple outline-none" />
            </div>
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-500 mb-1">群颜色</label>
            <div class="flex gap-2">
              <button v-for="c in colorOptions" :key="c" @click="editGroupData.color = c" :class="['w-8 h-8 rounded-full transition-all', c, editGroupData.color === c ? 'ring-2 ring-offset-2 ring-brand-purple scale-110' : '']"></button>
            </div>
          </div>
        </div>
        <div class="p-4 border-t border-gray-100 dark:border-gray-700">
          <button @click="saveGroupSettings" class="w-full py-2 bg-brand-purple text-white rounded-lg font-medium hover:bg-indigo-600 transition-colors">保存设置</button>
        </div>
      </div>
    </div>

    <!-- Edit Announcement Modal -->
    <div v-if="showEditAnnouncement" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/50 backdrop-blur-sm" @click.self="showEditAnnouncement = false">
      <div class="bg-white dark:bg-gray-800 rounded-2xl w-[400px] overflow-hidden shadow-xl">
        <div class="p-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center">
          <h3 class="font-bold text-gray-900 dark:text-white">编辑群公告</h3>
          <button @click="showEditAnnouncement = false" class="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full"><X class="w-5 h-5 text-gray-500" /></button>
        </div>
        <div class="p-4">
          <textarea v-model="announcementText" rows="4" placeholder="输入群公告内容..." class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-brand-purple outline-none resize-none"></textarea>
        </div>
        <div class="p-4 border-t border-gray-100 dark:border-gray-700 flex gap-2">
          <button @click="showEditAnnouncement = false" class="flex-1 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg font-medium hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors">取消</button>
          <button @click="saveAnnouncement" class="flex-1 py-2 bg-brand-purple text-white rounded-lg font-medium hover:bg-indigo-600 transition-colors">发布</button>
        </div>
      </div>
    </div>

    <!-- Add Member Modal -->
    <div v-if="showAddMember" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/50 backdrop-blur-sm" @click.self="showAddMember = false">
      <div class="bg-white dark:bg-gray-800 rounded-2xl w-[400px] max-h-[80vh] overflow-hidden shadow-xl">
        <div class="p-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center">
          <h3 class="font-bold text-gray-900 dark:text-white">添加成员</h3>
          <button @click="showAddMember = false" class="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full"><X class="w-5 h-5 text-gray-500" /></button>
        </div>
        <div class="p-4">
          <div class="relative mb-4">
            <Search class="absolute left-3 top-2.5 w-4 h-4 text-gray-400" />
            <input type="text" placeholder="搜索好友..." class="w-full pl-9 pr-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-lg text-sm focus:ring-2 focus:ring-brand-purple outline-none" />
          </div>
          <div class="max-h-64 overflow-y-auto space-y-2">
            <label v-for="friend in nonGroupMembers" :key="friend.id" class="flex items-center gap-3 p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer">
              <input type="checkbox" v-model="membersToAdd" :value="friend.id" class="w-4 h-4 text-brand-purple rounded focus:ring-brand-purple" />
              <img :src="friend.avatar" class="w-8 h-8 rounded-full" />
              <span class="text-sm text-gray-700 dark:text-gray-300">{{ friend.name }}</span>
            </label>
            <div v-if="nonGroupMembers.length === 0" class="text-center py-4 text-gray-400 text-sm">所有好友已在群内</div>
          </div>
        </div>
        <div class="p-4 border-t border-gray-100 dark:border-gray-700">
          <button @click="addMembers" :disabled="membersToAdd.length === 0" class="w-full py-2 bg-brand-purple text-white rounded-lg font-medium hover:bg-indigo-600 disabled:opacity-50 transition-colors">添加 ({{ membersToAdd.length }}人)</button>
        </div>
      </div>
    </div>

    <!-- Manage Members Modal -->
    <div v-if="showManageMembers" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/50 backdrop-blur-sm" @click.self="showManageMembers = false">
      <div class="bg-white dark:bg-gray-800 rounded-2xl w-[400px] max-h-[80vh] overflow-hidden shadow-xl">
        <div class="p-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center">
          <h3 class="font-bold text-gray-900 dark:text-white">成员管理</h3>
          <button @click="showManageMembers = false" class="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full"><X class="w-5 h-5 text-gray-500" /></button>
        </div>
        <div class="p-4">
          <div class="relative mb-4">
            <Search class="absolute left-3 top-2.5 w-4 h-4 text-gray-400" />
            <input v-model="memberSearchQuery" type="text" placeholder="搜索成员..." class="w-full pl-9 pr-4 py-2 bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-lg text-sm focus:ring-2 focus:ring-brand-purple outline-none" />
          </div>
          <div class="max-h-64 overflow-y-auto space-y-2">
            <div v-for="member in filteredGroupMembers" :key="member.id" class="flex items-center justify-between p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700">
              <div class="flex items-center gap-3">
                <div class="relative">
                  <img :src="member.avatar" class="w-10 h-10 rounded-full" />
                  <span v-if="member.role === 'owner'" class="absolute -bottom-0.5 -right-0.5 bg-orange-500 text-white text-[8px] px-1 rounded">群主</span>
                  <span v-else-if="member.role === 'admin'" class="absolute -bottom-0.5 -right-0.5 bg-blue-500 text-white text-[8px] px-1 rounded">管理</span>
                </div>
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ member.name }}</span>
              </div>
              <div v-if="member.id !== 'me' && member.role !== 'owner'" class="flex items-center gap-2">
                <button v-if="member.role === 'admin'" @click="setMemberRole(member, 'member')" class="px-2 py-1 text-xs bg-gray-100 dark:bg-gray-600 text-gray-600 dark:text-gray-300 rounded hover:bg-gray-200 dark:hover:bg-gray-500 transition-colors">取消管理员</button>
                <button v-else @click="setMemberRole(member, 'admin')" class="px-2 py-1 text-xs bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded hover:bg-blue-200 dark:hover:bg-blue-900/50 transition-colors">设为管理员</button>
                <button @click="removeMember(member)" class="px-2 py-1 text-xs bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 rounded hover:bg-red-200 dark:hover:bg-red-900/50 transition-colors">移除</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Image Preview Modal -->
    <div v-if="fullImagePreview" class="fixed inset-0 z-[200] bg-black/90 flex items-center justify-center p-4" @click="fullImagePreview = null">
      <button class="absolute top-4 right-4 text-white/70 hover:text-white p-2"><X class="w-8 h-8" /></button>
      <img :src="fullImagePreview" class="max-w-full max-h-[90vh] object-contain rounded-lg" @click.stop />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useUIStore, useUserStore, useMessageStore } from '../../stores'
import { messageApi, uploadApi } from '../../api'
import { X, Search, Send, MessageCircle, Users, Megaphone, Plus, Image as ImageIcon, Smile, Paperclip, UserPlus, Settings, Edit2, Camera, Trash2, Pin, CheckCircle } from 'lucide-vue-next'
import VerifiedBadge from '../common/VerifiedBadge.vue'

const uiStore = useUIStore()
const userStore = useUserStore()
const messageStore = useMessageStore()

const activeTab = ref('all')
const selectedContact = ref(null)
const messageInput = ref('')
const showGroupInfo = ref(false)
const imagePreview = ref(null)
const imageFile = ref(null) // 存储原始文件用于上传
const fullImagePreview = ref(null)
const showCreateGroup = ref(false)
const showEditGroup = ref(false)
const showEditAnnouncement = ref(false)
const showAddMember = ref(false)
const showManageMembers = ref(false)
const announcementText = ref('')
const membersToAdd = ref([])
const searchQuery = ref('')
const memberSearchQuery = ref('')

const newGroup = ref({ name: '', avatar: null, members: [] })
const editGroupData = ref({ name: '', avatar: null, color: '' })

const tabs = [{ id: 'all', label: '全部' }, { id: 'friends', label: '好友' }, { id: 'groups', label: '群聊' }]
const colorOptions = ['bg-blue-500', 'bg-purple-500', 'bg-pink-500', 'bg-orange-500', 'bg-green-500', 'bg-red-500', 'bg-indigo-500', 'bg-teal-500']

const contacts = computed(() => messageStore.contacts)

// 消息存储 - 使用 conversationId 作为 key
const messages = ref({})
const loadingMessages = ref(false)

// 右键菜单状态
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  contact: null
})

const filteredContacts = computed(() => {
  let result = messageStore.contacts
  if (activeTab.value === 'friends') result = result.filter(c => !c.isGroup)
  else if (activeTab.value === 'groups') result = result.filter(c => c.isGroup)
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(c => c.name.toLowerCase().includes(q) || c.lastMessage?.toLowerCase().includes(q))
  }
  return result
})

const currentMessages = computed(() => selectedContact.value ? (messages.value[selectedContact.value.id] || []) : [])

const availableFriends = computed(() => messageStore.contacts.filter(c => !c.isGroup))

const nonGroupMembers = computed(() => {
  if (!selectedContact.value?.isGroup) return []
  const memberIds = selectedContact.value.members.map(m => m.id)
  return messageStore.contacts.filter(c => !c.isGroup && !memberIds.includes(c.id))
})

const isGroupAdmin = computed(() => {
  if (!selectedContact.value?.isGroup) return false
  const me = selectedContact.value.members.find(m => m.id === 'me')
  return me?.role === 'owner' || me?.role === 'admin'
})

const isGroupOwner = computed(() => {
  if (!selectedContact.value?.isGroup) return false
  const me = selectedContact.value.members.find(m => m.id === 'me')
  return me?.role === 'owner'
})

const filteredGroupMembers = computed(() => {
  if (!selectedContact.value?.isGroup) return []
  if (!memberSearchQuery.value.trim()) return selectedContact.value.members
  const q = memberSearchQuery.value.toLowerCase()
  return selectedContact.value.members.filter(m => m.name.toLowerCase().includes(q))
})

// 右键菜单函数
function showContextMenu(event, contact) {
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    contact: contact
  }
  
  // 点击其他地方关闭菜单
  setTimeout(() => {
    document.addEventListener('click', hideContextMenu, { once: true })
  }, 10)
}

function hideContextMenu() {
  contextMenu.value.visible = false
}

function togglePin() {
  if (!contextMenu.value.contact) return
  contextMenu.value.contact.isPinned = !contextMenu.value.contact.isPinned
  
  // 重新排序：置顶的放前面
  messageStore.contacts.sort((a, b) => {
    if (a.isPinned && !b.isPinned) return -1
    if (!a.isPinned && b.isPinned) return 1
    return 0
  })
  
  hideContextMenu()
}

function markAsRead() {
  if (!contextMenu.value.contact) return
  contextMenu.value.contact.unread = 0
  messageStore.markContactAsRead(contextMenu.value.contact.id)
  hideContextMenu()
}

async function deleteFromContextMenu() {
  if (!contextMenu.value.contact) return
  
  const contact = contextMenu.value.contact
  hideContextMenu()
  
  if (confirm(`确定要删除与"${contact.name}"的聊天记录吗？此操作不可撤销。`)) {
    try {
      await messageApi.deleteConversation(contact.id)
      
      // 从本地列表中移除
      const idx = messageStore.contacts.findIndex(c => c.id === contact.id)
      if (idx !== -1) {
        messageStore.contacts.splice(idx, 1)
      }
      
      // 如果删除的是当前选中的会话，清空选中
      if (selectedContact.value?.id === contact.id) {
        selectedContact.value = null
      }
      
      delete messages.value[contact.id]
    } catch (e) {
      console.error('删除会话失败:', e)
      alert('删除失败，请重试')
    }
  }
}

async function selectContact(contact) {
  selectedContact.value = contact
  messageStore.markContactAsRead(contact.id)
  showGroupInfo.value = false
  
  // 从后端获取消息历史
  await fetchMessages(contact.id)
}

// 确认删除会话
function confirmDeleteConversation() {
  if (!selectedContact.value) return
  
  const name = selectedContact.value.name
  if (confirm(`确定要删除与"${name}"的聊天记录吗？此操作不可撤销。`)) {
    deleteConversation()
  }
}

// 删除会话
async function deleteConversation() {
  if (!selectedContact.value) return
  
  const conversationId = selectedContact.value.id
  try {
    await messageApi.deleteConversation(conversationId)
    
    // 从本地列表中移除
    const idx = messageStore.contacts.findIndex(c => c.id === conversationId)
    if (idx !== -1) {
      messageStore.contacts.splice(idx, 1)
    }
    
    // 清空当前选中
    selectedContact.value = null
    delete messages.value[conversationId]
    
  } catch (e) {
    console.error('删除会话失败:', e)
    alert('删除失败，请重试')
  }
}

// 获取会话消息历史
async function fetchMessages(conversationId) {
  loadingMessages.value = true
  try {
    const res = await messageApi.getMessages(conversationId)
    const records = res.data?.records || res.data || []
    
    // 转换消息格式，然后反转顺序（后端返回最新在前，聊天界面需要最旧在上）
    const transformedMessages = records.map(msg => {
      let imageUrl = null
      let textContent = msg.content || ''
      
      // 只有当 type 是 IMAGE 且 content 看起来像有效的 URL/路径时才处理为图片
      const isImage = msg.type === 'IMAGE'
      const looksLikeImagePath = msg.content && (
        msg.content.startsWith('http') || 
        msg.content.startsWith('/uploads') ||
        msg.content.startsWith('blob:') ||
        msg.content.includes('/') // 包含路径分隔符
      )
      
      if (isImage && looksLikeImagePath) {
        // 图片消息：content 是图片 URL
        imageUrl = msg.content
        // 如果是相对路径，添加后端前缀
        if (imageUrl && !imageUrl.startsWith('http') && !imageUrl.startsWith('blob:')) {
          imageUrl = '/api' + (imageUrl.startsWith('/') ? '' : '/') + imageUrl
        }
        textContent = '' // 图片消息不显示文字
      }
      
      // 处理头像 URL
      let avatarUrl = msg.senderAvatar || null
      if (avatarUrl && !avatarUrl.startsWith('http') && !avatarUrl.startsWith('blob:')) {
        avatarUrl = '/api' + (avatarUrl.startsWith('/') ? '' : '/') + avatarUrl
      }
      
      return {
        id: msg.id,
        content: textContent,
        image: imageUrl,
        time: formatMessageTime(msg.createdAt),
        isMine: String(msg.senderId) === String(userStore.currentUser?.id),
        senderId: msg.senderId, // 添加发送者 ID
        sender: msg.senderName || '',
        avatar: avatarUrl
      }
    })
    
    // 反转顺序：最旧的消息在上面，最新的在下面
    messages.value[conversationId] = transformedMessages.reverse()
  } catch (e) {
    console.error('获取消息历史失败:', e)
    messages.value[conversationId] = []
  } finally {
    loadingMessages.value = false
  }
}

// 格式化消息时间
function formatMessageTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  if (diff < 172800000) return '昨天'
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// 格式化最后一条消息（图片显示为[图片]）
function formatLastMessage(message) {
  if (!message) return ''
  // 如果消息包含图片路径，显示"[图片]"
  if (message.startsWith('/uploads/') || message.startsWith('http') || message.includes('/images/')) {
    return '[图片]'
  }
  return message
}

function handleAvatarClick(contact) {
  if (!contact.isGroup) {
    // 使用 targetId（对方用户 ID）
    let userId = contact.targetId
    
    // 如果没有 targetId，尝试从消息中获取对方的 senderId
    if (!userId && selectedContact.value) {
      const conversationMsgs = messages.value[selectedContact.value.id] || []
      const otherUserMsg = conversationMsgs.find(m => !m.isMine && m.senderId)
      if (otherUserMsg) {
        userId = otherUserMsg.senderId
      }
    }
    
    if (userId) {
      uiStore.closeModal()
      uiStore.openModal('PROFILE', { id: userId, name: contact.name, avatar: contact.avatar, role: 'student' })
    }
  }
}

function handleMsgAvatarClick(msg) {
  if (msg.senderId && msg.avatar) {
    uiStore.closeModal()
    uiStore.openModal('PROFILE', { id: msg.senderId, name: msg.sender, avatar: msg.avatar, role: 'student' })
  }
}

function handleMemberClick(member) {
  if (member.id !== 'me') {
    uiStore.closeModal()
    uiStore.openModal('PROFILE', { id: member.id, name: member.name, avatar: member.avatar, role: 'student' })
  }
}

function handleImageSelect(e) {
  if (e.target.files?.[0]) {
    imageFile.value = e.target.files[0] // 保存原始文件
    imagePreview.value = URL.createObjectURL(e.target.files[0])
  }
}
function openImagePreview(src) { fullImagePreview.value = src }
function handleGroupAvatarSelect(e) { if (e.target.files?.[0]) newGroup.value.avatar = URL.createObjectURL(e.target.files[0]) }
function handleEditGroupAvatarSelect(e) { if (e.target.files?.[0]) editGroupData.value.avatar = URL.createObjectURL(e.target.files[0]) }

async function sendMessage() {
  if ((!messageInput.value.trim() && !imagePreview.value) || !selectedContact.value) return
  
  const content = messageInput.value.trim()
  const conversationId = selectedContact.value.id
  
  // 立即显示在 UI 上（乐观 UI）
  const tempMsg = {
    id: 'temp_' + Date.now(),
    content: content,
    image: imagePreview.value || null,
    time: '发送中...',
    isMine: true,
    sending: true
  }
  
  if (!messages.value[conversationId]) messages.value[conversationId] = []
  messages.value[conversationId].push(tempMsg)
  
  // 清空输入
  const savedContent = messageInput.value
  const savedImagePreview = imagePreview.value
  const savedImageFile = imageFile.value
  messageInput.value = ''
  imagePreview.value = null
  imageFile.value = null
  
  try {
    let messageContent = savedContent
    let messageType = 'TEXT'
    
    // 如果有图片，先上传到服务器
    if (savedImageFile) {
      const uploadRes = await uploadApi.uploadImage(savedImageFile)
      // API 返回格式: { code: 200, data: { url: "图片URL" }, message: "success" }
      // uploadRes.data 是 Axios 响应的 data，里面的 data.url 才是实际的 URL
      const imageUrl = uploadRes.data?.data?.url || uploadRes.data?.url || uploadRes.data
      console.log('Upload response:', uploadRes.data, 'Extracted URL:', imageUrl)
      if (imageUrl && typeof imageUrl === 'string') {
        messageContent = imageUrl
        messageType = 'IMAGE'
      } else {
        console.error('Failed to extract image URL from upload response:', uploadRes.data)
      }
    }
    
    const res = await messageApi.sendMessage({
      conversationId: conversationId,
      content: messageContent,
      type: messageType
    })
    
    // 用后端返回的数据替换临时消息
    const idx = messages.value[conversationId].findIndex(m => m.id === tempMsg.id)
    if (idx !== -1) {
      // 如果是图片消息，使用服务器返回的图片 URL
      const serverImageUrl = messageType === 'IMAGE' ? messageContent : null
      messages.value[conversationId][idx] = {
        id: res.data?.id || tempMsg.id,
        content: messageType === 'TEXT' ? savedContent : '',
        image: serverImageUrl,
        time: formatMessageTime(res.data?.createdAt) || '刚刚',
        isMine: true,
        sending: false
      }
    }
    
    // 更新会话列表中的最后一条消息
    messageStore.updateContactLastMessage(conversationId, messageType === 'IMAGE' ? '[图片]' : savedContent, '我')
    
  } catch (e) {
    console.error('发送消息失败:', e)
    // 标记为发送失败
    const idx = messages.value[conversationId].findIndex(m => m.id === tempMsg.id)
    if (idx !== -1) {
      messages.value[conversationId][idx].time = '发送失败'
      messages.value[conversationId][idx].failed = true
    }
  }
}

function createGroup() {
  if (!newGroup.value.name.trim() || newGroup.value.members.length === 0) return
  const members = newGroup.value.members.map(id => {
    const c = messageStore.contacts.find(x => x.id === id)
    return { id: c.id, name: c.name, avatar: c.avatar, role: 'member' }
  })
  members.push({ id: 'me', name: '我', avatar: userStore.currentUser?.avatar, role: 'owner' })
  const newGroupContact = {
    id: 'g' + Date.now(), name: newGroup.value.name, avatar: newGroup.value.avatar, color: colorOptions[Math.floor(Math.random() * colorOptions.length)],
    lastMessage: '群聊已创建', lastSender: '', time: '刚刚', unread: 0, isGroup: true, memberCount: members.length, ownerId: 'me', announcement: '', members
  }
  messageStore.contacts.unshift(newGroupContact)
  messages.value[newGroupContact.id] = [{ id: 1, content: '群聊已创建', time: '刚刚', isMine: false, sender: '系统', isSystem: true }]
  selectContact(newGroupContact)
  newGroup.value = { name: '', avatar: null, members: [] }
  showCreateGroup.value = false
}

function saveGroupSettings() {
  if (!selectedContact.value) return
  selectedContact.value.name = editGroupData.value.name
  selectedContact.value.avatar = editGroupData.value.avatar
  selectedContact.value.color = editGroupData.value.color
  showEditGroup.value = false
}

function saveAnnouncement() {
  if (!selectedContact.value) return
  selectedContact.value.announcement = announcementText.value
  showEditAnnouncement.value = false
}

function addMembers() {
  if (!selectedContact.value || membersToAdd.value.length === 0) return
  membersToAdd.value.forEach(id => {
    const c = messageStore.contacts.find(x => x.id === id)
    if (c) selectedContact.value.members.push({ id: c.id, name: c.name, avatar: c.avatar, role: 'member' })
  })
  selectedContact.value.memberCount = selectedContact.value.members.length
  membersToAdd.value = []
  showAddMember.value = false
}

function setMemberRole(member, role) {
  if (!selectedContact.value) return
  const idx = selectedContact.value.members.findIndex(m => m.id === member.id)
  if (idx !== -1) selectedContact.value.members[idx].role = role
}

function removeMember(member) {
  if (!selectedContact.value) return
  selectedContact.value.members = selectedContact.value.members.filter(m => m.id !== member.id)
  selectedContact.value.memberCount = selectedContact.value.members.length
}

onMounted(async () => {
  // 从后端获取会话列表
  await messageStore.fetchConversations()
  
  const initialData = uiStore.modalData.data
  
  // 情况1：从消息预览点击，传入的是 conversationId（已有会话）
  if (initialData && initialData.conversationId) {
    const existingConv = messageStore.contacts.find(c => 
      c.id === initialData.conversationId || String(c.id) === String(initialData.conversationId)
    )
    if (existingConv) {
      selectContact(existingConv)
    }
    return
  }
  
  // 情况2：从用户主页点击"私信"，传入的是用户 id
  if (initialData && initialData.id && initialData.name && 
      String(initialData.id) !== String(userStore.currentUser?.id)) {
    // 如果是从用户主页点击“私信”进来，需要获取或创建会话
    try {
      const res = await messageApi.getOrCreatePrivateConversation(initialData.id)
      if (res.data) {
        const conversation = {
          id: res.data.id,
          targetId: initialData.id,
          name: res.data.targetName || res.data.name || initialData.name,
          avatar: res.data.targetAvatar || res.data.avatar || initialData.avatar,
          lastMessage: res.data.lastMessage || '',
          time: res.data.updatedAt ? formatMessageTime(res.data.updatedAt) : '刚刚',
          unread: res.data.unreadCount || 0,
          isGroup: res.data.type === 'GROUP'
        }
        
        // 检查是否已存在
        const existingIdx = messageStore.contacts.findIndex(c => c.id === conversation.id)
        if (existingIdx === -1) {
          messageStore.addContact(conversation)
        }
        
        selectContact(conversation)
      }
    } catch (e) {
      console.error('创建私聊会话失败:', e)
    }
  }
})

// Watch for edit group modal
watch(showEditGroup, (val) => {
  if (val && selectedContact.value) {
    editGroupData.value = { name: selectedContact.value.name, avatar: selectedContact.value.avatar, color: selectedContact.value.color }
  }
})
watch(showEditAnnouncement, (val) => {
  if (val && selectedContact.value) announcementText.value = selectedContact.value.announcement || ''
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 2px; }
</style>
