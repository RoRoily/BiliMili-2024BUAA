<template>
    <div class="platform-home">
        <!-- 上栏：热搜词条 -->
        <div class="hot-activity">热门活动</div>
        <ol class="top-section">
            <li v-for="item in hotSearchTerms" :key="item.id" class="hot-search-item">
                <a :href="item.url" target="_blank">{{ item.term }}</a>
                <img src="~assets/img/icon_hot.png" alt="" class="trending-mark">
            </li>
        </ol>
        <!-- 中栏：网站公告 -->
        <div class="announcement">网站公告</div>
        <ol class="middle-section">
            <li v-for="item in announcements" :key="item.id" class="announcement-item">
                <div class="title">{{ item.title }}</div>
                <div class="content">{{ item.content }}</div>
            </li>
        </ol>


        <!-- 下栏：创作素材 -->
        <div class="creative-material">素材广场</div>
        <div class="bottom-section">
            <!-- 创作素材列表 -->
            <!-- <div v-for="material in creativeMaterials" :key="material.id" class="material-item">
                {{ material.name }} ({{ material.type }})
            </div> -->
            <div class="platform-upload">
                <!-- 导航栏 -->
                <NavBar :navBarItem="navBarData" @clickBarItem="clickBarItem" :style="isNavBarShow ? '' : 'display: none;'"></NavBar>
                <router-view @changeNavBarShow="changeNavBarShow"></router-view>
            </div>
        </div>
    </div>
</template>

<script>
import NavBar from '@/components/navbar/NavBar.vue';

export default {
    name: "PlatformHome",
    components: {
        NavBar,
    },
    data() {
        return {
            isNavBarShow: true,
            // 假设的热搜词条数据
            hotSearchTerms: [
                { id: 1, term: '欢迎来到BiliMili！你所热爱的，就是你的生活', url: '/' },
                { id: 2, term: '王者荣耀超燃操作混剪', url: 'https://www.bilibili.com/video/BV1ya411A7Jx/' },
                { id: 3, term: '《明日方舟》：原神的另一种可能', url: 'https://www.bilibili.com/video/BV1cf4y1W771/' },
                // 更多热搜词条...
            ],

            // 假设的公告数据
            announcements: [
                { id: 1, title: '系统维护公告', content: '尊敬的用户，我们将于下周进行系统维护，请注意影响。' },
                { id: 2, title: '活动公告', content: '《明日方舟》活动已于今天开始，欢迎各位玩家参与！' },
            ],

            // 假设的创作素材数据
            creativeMaterials: [
                { id: 1, name: '背景音乐', type: '音频' },
                { id: 2, name: '高清图片', type: '图像' },
                { id: 3, name: '视频剪辑', type: '视频' },
                // 更多创作素材...
            ],
            // 导航栏列表
            navBarData: [
                { name: "视频素材", path: '/' },
                { name: "贴纸素材", path: '/' },
                { name: "音乐素材", path: '/' },
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
.hot-activity,
.announcement,
.creative-material {
    margin: 40px;
    font-size: 28px;
    font-weight: bold;
    text-align: center;
    color: #505050;
}
.top-section, .middle-section, .bottom-section{
    /* height: 64px; */
    background-color: pink;
    border: 3px solid;
    border-color: #ff6699;
    border-radius: 30px;
    padding: 25px;
    margin: 40px;
    font-size: 16px;
    color: #505050;
    /* height: 100%; */
}

.top-section{
    font-size: 22px;
    line-height: 3;
}

ul {
    padding-top: 10px;
    padding-bottom: 10px;
}

li {
    margin-left: 25px;
    margin-right: 25px;
    padding-left: 10px;
    padding-right: 10px;
    padding-top: 15px;
    padding-bottom: 15px;
    cursor: pointer;
}
.trending-mark {
    height: 20px;
    margin-left: 10px;
    margin-right: 16px;
}
</style>


