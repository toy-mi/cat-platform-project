<template>
  <div class="adoption-process">
    <div class="container">
      <h1 class="page-title">领养流程</h1>
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="提交申请" description="填写领养申请表" />
        <el-step title="志愿者初审" description="审核申请材料" />
        <el-step title="线下回访" description="志愿者上门回访" />
        <el-step title="管理员终审" description="最终审核确认" />
        <el-step title="签订协议" description="签署领养协议" />
        <el-step title="领养完成" description="带猫咪回家" />
      </el-steps>
      <div class="action">
        <el-button type="primary" size="large" @click="$router.push('/cats')">开始申请</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
const currentStep = 0 // 可固定为0，仅展示流程
</script>

<style scoped>
.adoption-process {
  padding: 3rem 0;
}
.page-title {
  text-align: center;
  margin-bottom: 3rem;
}
.action {
  text-align: center;
  margin-top: 3rem;
}
</style>