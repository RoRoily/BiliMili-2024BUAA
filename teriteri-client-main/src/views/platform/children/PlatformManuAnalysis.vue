<template>
  <div class="platform-manuscriptAnalysis">
    <div class="navbar">
      <div class="navbar-item" @click="this.$router.push('/platform/data-up')">数据概览</div>
      <div class="navbar-item" @click="this.$router.push('/platform/diagnosis')">账号诊断</div>
      <div class="navbar-this" style="color: floralwhite" @click="this.$router.push('/platform/manuAnalysis')">稿件分析</div>
    </div>
    稿件分析
  </div>
</template>

<script>
export default {
  name: "PlatformManuAnalysis",
}
</script>

<style scoped>
@keyframes jump {
  0%, 100% {
    transform: translateY(0); /* 起始和结束状态，图标回到原位 */
  }
  50% {
    transform: translateY(-5px); /* 中间状态，图标向上跳动 20px */
  }
}
.navbar {
  display: flex;
  justify-content: space-around;
  //border-radius: 20px;
  border-radius: 30px 30px 0 0;
  width: 100%;
  padding: 20px 0;
  background-color: #fa799e;
}
.navbar-item {
  cursor: pointer;
  font-size:20px;
  transition: color 0.3s ease;
}
.navbar-item:hover {
  color: floralwhite;
  //transform: translateY(-5px);
  animation: jump 0.3s;
}
.navbar-this {
  cursor: pointer;
  font-size:20px;
}
</style>