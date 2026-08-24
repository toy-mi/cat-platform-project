<template>
  <div class="community-manage">
    <h2>社区管理</h2>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 动态管理标签页 -->
      <el-tab-pane label="动态管理" name="posts">
        <PostManage />
      </el-tab-pane>

      <!-- 评论管理标签页 -->
      <el-tab-pane label="评论管理" name="comments">
        <CommentManage />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import PostManage from './PostManage.vue'
import CommentManage from './CommentManage.vue'

const activeTab = ref('posts')
</script>

<style scoped>
.community-manage {
  padding: 20px;
}
</style>