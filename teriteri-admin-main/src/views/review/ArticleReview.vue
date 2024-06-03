<template>
  <div class="flex-fill">
    <div class="v-container">
      <div class="v-card">
        <div class="article-table-card">
          <div class="v-table" v-loading="loading">
            <div class="top">
              <div class="navbar">
                <div class="bar-item" :class="articleStatus === 0 ? 'active' : ''" @click="changeStatus(0)">待审核</div>
                <div class="bar-item" :class="articleStatus === 1 ? 'active' : ''" @click="changeStatus(1)">已过审</div>
                <div class="bar-item" :class="articleStatus === 2 ? 'active' : ''" @click="changeStatus(2)">未过审</div>
              </div>
              <div class="top-right">
                <div class="refresh" @click="reloadArticles">刷新</div>
                <div class="total">共 {{ total }} 条</div>
              </div>
            </div>
            <div class="v-table__wrapper">
              <table>
                <thead>
                <tr>
                  <th style="min-width: 90px;">AID</th>
                  <th style="min-width: 176px;">封面</th>
                  <th style="min-width: 200px;">标题</th>
                  <th style="min-width: 120px;">投稿用户</th>
                  <th style="min-width: 150px;">投稿用户uid</th>
                  <th style="min-width: 100px;">状态</th>
                  <th style="min-width: 80px;"></th>
                </tr>
                </thead>
                <tbody v-if="articles.length != 0">
                <tr v-for="(item, index) in articles" :key="index">
                  <td style="min-width: 90px;"># {{ item.article.aid }}</td>
                  <td style="width: 176px;">
                    <img :src="item.article.coverUrl" class="cover" alt="">
                  </td>
                  <td style="min-width: 200px;">{{ item.article.title }}</td>
                  <td style="min-width: 120px;">
                    <span class="nickname">{{ item.user.nickname }}</span>
                  </td>
                  <td style="min-width: 150px;">{{ item.user.uid }}</td>
                  <td style="min-width: 100px;">
                    <div class="status" v-if="item.article.status === 0">
                      <i class="iconfont icon-shenhezhong"></i>
                      <span>待审核</span>
                    </div>
                    <div class="status" v-if="item.article.status === 1">
                      <i class="iconfont icon-wancheng"></i>
                      <span>已通过</span>
                    </div>
                    <div class="status" v-if="item.article.status === 2">
                      <i class="iconfont icon-shibai"></i>
                      <span>未通过</span>
                    </div>
                  </td>
                  <td style="min-width: 80px;">
                    <span class="detail-btn" @click="openNewPage({
                                                    name: 'articleDetail',
                                                    params: {aid: item.article.aid}
                                                })">详情</span>
                  </td>
                </tr>
                </tbody>
              </table>
              <div class="no-more" v-if="!loading && articles.length == 0">
                <img src="~assets/img/silly.png" alt="" >
                <span>没有找到任何数据</span>
              </div>
            </div>
            <div class="v-table-page">
              <el-pagination
                  background
                  layout="prev, pager, next"
                  :total="total"
                  :page-size="10"
                  :pager-count="pagerCount"
                  :current-page="page"
                  @current-change="pageChange"
              ></el-pagination>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ArticleReview",
  data() {
    return {
      articleStatus: 0,   // 要查询的视频状态，0 正在审核，1 通过审核，2 打回整改，3 违规封禁/已删源
      articles: [],
      page: 1,
      total: 100,
      pagerCount: 7,
      loading: true,
    }
  },
  methods: {
    // 请求
    // 查询视频数量
    async getTotal() {
      const res = await this.$get('/review/article/total', {
        params: {
          astatus: this.articleStatus,
        },
        headers: {
          Authorization: "Bearer " + localStorage.getItem("teri_token"),
        },
      });
      if (res.data.data) {
        this.total = res.data.data;
      } else {
        this.total = 0;
        this.articles = [];
      }
    },

    // 查询当前页的视频
    async getArticles() {
      const res = await this.$get('/review/article/getpage', {
        params: {
          astatus: this.articleStatus,
          page: this.page,
          quantity: 10,
        },
        headers: {
          Authorization: "Bearer " + localStorage.getItem("teri_token"),
        },
      });
      console.log("1", res.data.data);
      if (res.data.data) {
        this.articles = res.data.data;
      } else {
        this.articles = [];
      }
      console.log("专栏列表: ",  this.articles.length);
    },


    // 事件
    // 切换类型
    changeStatus(astatus) {
      this.articleStatus = astatus;
      if (this.page !== 1) {
        this.page = 1;  // 这里页码改变会触发重加载
      } else {
        this.reloadArticles();
      }
    },

    // 改变页码时的回调
    async pageChange(page) {
      this.page = page;
      await this.reloadArticles();
    },

    // 重新加载视频列表
    async reloadArticles() {
      this.loading = true;
      await this.getTotal();
      if (this.total > 0) {
        await this.getArticles();
      }
      this.loading = false;
    },

    // 判断是否小窗
    /*changeWidth() {
      if (window.innerWidth < 480) {
        this.pagerCount = 3;
      } else {
        this.pagerCount = 7;
      }
    },*/

    // 打开新标签页
    openNewPage(route) {
      console.log("breakpoint 2");
      window.open(this.$router.resolve(route).href, '_blank');
    },
  },
  async created() {
    //this.changeWidth();
    await this.getTotal();
    if (this.total > 0) {
      await this.getArticles();
    }
    this.loading = false;
  },
  /*mounted() {
    // 监听窗口大小变化，判断是否小窗
    window.addEventListener('resize', this.changeWidth);
  },
  unmounted() {
    window.removeEventListener('resize', this.changeWidth);
  },*/
}
</script>

<style scoped>
.article-table-card {
  height: calc(100vh - 96px);
  position: relative;
  overflow: hidden !important;
  overflow-anchor: none;
  -ms-overflow-style: none;
  touch-action: auto;
  -ms-touch-action: auto;
}

.v-table {
  --v-table-row-height: 120px;
}
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  border-bottom: 1px solid #e7e7e7;
}

.navbar, .top-right {
  display: flex;
  flex: 0 0 auto;
}
.top-right {
  margin-left: 100px;
}

.bar-item {
  flex: 0 0 auto;
  height: 64px;
  padding-bottom: 18px;
  padding-top: 26px;
  margin-left: 40px;
  font-size: 16px;
  color: #505050;
  cursor: pointer;
}

.active {
  color: var(--brand_pink);
  font-weight: 600;
  border-bottom: 3px solid var(--brand_pink);
}

.top-right>div {
  flex: 0 0 auto;
  line-height: 54px;
  margin-right: 30px;
  padding-top: 10px;
}

.refresh {
  cursor: pointer;
  color: var(--brand_blue);
}

.refresh:hover {
  color: var(--Lb6);
}

.v-table__wrapper {
  height: calc(100% - 150px);
}

.v-table__wrapper table {
  padding: 0 4px 8px;
}

.cover {
  height: 81px;
  width: 144px;
  object-fit: cover;
  box-shadow: 2px 2px 8px #0000001f;
}

.nickname {
  cursor: pointer;
}

.nickname:hover {
  color: var(--text1);
}

.category {
  color: #fff;
  line-height: 18px;
  padding: 2px 8px;
  border-radius: 10px;
}

.status {
  display: flex;
  align-items: center;
}

.status .iconfont {
  font-size: 12px;
  margin-right: 5px;
}

.icon-shenhezhong {
  color: var(--pay_yellow);
}

.icon-wancheng {
  color: var(--success_green);
}

.icon-shibai {
  color: var(--stress_red);
}

.detail-btn {
  cursor: pointer;
  color: var(--brand_blue);
}

.detail-btn:hover {
  color: var(--Lb6);
  text-decoration: underline;
}

.no-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 150px;
  width: 100%;
}

.no-more img {
  height: 80px;
}

.no-more span {
  font-size: 20px;
  color: var(--text3);
  line-height: 40px;
}

.v-table-page {
  width: 100%;
  display: flex;
  justify-content: center;
}
</style>