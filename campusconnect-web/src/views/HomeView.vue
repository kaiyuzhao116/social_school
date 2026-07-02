<template>
  <div class="flex flex-row gap-8">
    <!-- Left Sidebar -->
    <div class="w-80 shrink-0">
      <Sidebar />
    </div>

    <!-- Main Feed -->
    <div class="flex-1 min-w-0">
      <Hero />
      <GroupBuySection />
      <Feed />
    </div>
  </div>
</template>

<script setup>
import Sidebar from '../components/Sidebar.vue'
import Hero from '../components/Hero.vue'
import Feed from '../components/Feed.vue'
import GroupBuySection from '../components/GroupBuySection.vue'
</script>
