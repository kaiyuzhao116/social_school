<template>
  <footer
    class="bg-white dark:bg-gray-900 border-t border-gray-100 dark:border-gray-800 mt-12"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div
        class="flex flex-col md:flex-row justify-between items-center gap-6"
      >
        <div class="flex items-center gap-2">
          <div
            class="w-8 h-8 bg-brand-purple rounded-lg flex items-center justify-center text-white font-bold text-lg shadow-md shadow-brand-purple/20"
          >
            C
          </div>
          <div class="flex flex-col">
            <span class="font-bold text-gray-900 dark:text-white leading-tight"
              >CampusConnect</span
            >
            <span class="text-[10px] text-gray-400 tracking-wider uppercase"
              >校园生活助手</span
            >
          </div>
        </div>

        <div
          class="flex flex-wrap justify-center gap-x-8 gap-y-2 text-sm font-medium text-gray-500 dark:text-gray-400"
        >
          <a href="#" class="hover:text-brand-purple transition-colors"
            >关于我们</a
          >
          <a href="#" class="hover:text-brand-purple transition-colors"
            >社区准则</a
          >
          <a href="#" class="hover:text-brand-purple transition-colors"
            >隐私政策</a
          >
          <a href="#" class="hover:text-brand-purple transition-colors"
            >帮助中心</a
          >
          <a href="#" class="hover:text-brand-purple transition-colors"
            >联系合作</a
          >
        </div>

        <div class="text-xs text-gray-400 dark:text-gray-500">
          &copy; {{ currentYear }} CampusConnect. All rights reserved.
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
const currentYear = new Date().getFullYear()
</script>
