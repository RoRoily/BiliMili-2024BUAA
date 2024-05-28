<template>
    <div class="platform-upload">
        <!-- 导航栏 -->
        <NavBar :navBarItem="navBarData" @clickBarItem="clickBarItem" :style="isNavBarShow ? '' : 'display: none;'"></NavBar>
        <router-view @changeNavBarShow="changeNavBarShow"></router-view>
    </div>
</template>

<script>
import NavBar from '@/components/navbar/NavBar.vue';

export default {
    name: "PlatformUpload",
    components: {
        NavBar,
    },
    data() {
        return {
            // 是否显示导航栏
            isNavBarShow: true,
            // 导航栏列表
            navBarData: [
                { name: "视频管理", /*path: '/platform/upload-manager/manuscript/video'*/ },
                { name: "专栏管理", /*path: '/platform/upload-manager/manuscript/column' 需要更改router/js*/ },
            ]
        }
    },
    methods: {
        // 事件
        // 点击navBar的回调
        clickBarItem(path) {
            if (path != this.$route.path) {
                this.$router.push(path);
            }
        },
        // 投稿视频时控制导航栏显隐
        changeNavBarShow(flag) {
            this.isNavBarShow = flag;
        }
    }
}
</script>

<style scoped>

</style>