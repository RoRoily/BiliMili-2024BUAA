<template>
  <div class="flex-fill">
    <div class="v-container">
      <div class="video-detail__layout">
        <div class="left">
          <v-md-preview :text="markdownTxt" style="width: 600px; height: 1000px; border: 2px solid;"></v-md-preview>
          <div class="v-card options">
            <div class="options-top">
              <div class="status" v-if="article.status === 0">
                <i class="iconfont icon-shenhezhong"></i>
                <span>待审核</span>
              </div>
              <div class="status" v-if="article.status === 1">
                <i class="iconfont icon-wancheng"></i>
                <span>已通过</span>
              </div>
              <div class="status" v-if="article.status === 2">
                <i class="iconfont icon-shibai"></i>
                <span>未通过</span>
              </div>
              <div class="items">
                <el-button type="success" plain class="options-item pass" @click="updateArticleStatus(1)">
                  <el-icon v-if="isMiniWidth"><Select /></el-icon>
                  <span v-else>通过审核</span>
                </el-button>
                <el-button type="warning" plain class="options-item no-pass" @click="updateArticleStatus(2)">
                  <el-icon v-if="isMiniWidth"><CloseBold /></el-icon>
                  <span v-else>不予过审</span>
                </el-button>
                <el-button type="danger" plain class="options-item ban" @click="beforeDelete">
                  <el-icon v-if="isMiniWidth"><Delete /></el-icon>
                  <span v-else>永久删除</span>
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <div class="detail">
          <div class="v-card detail-card">
            <div class="detail-item">
              <div class="item-title">标题</div>
              <div class="item-content">{{ article.title }}</div>
            </div>
            <div class="detail-item">
              <div class="item-title">简介</div>
              <div class="item-content"><span class="v-text descr" v-html="formatText(article.descr)"></span></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { linkify } from '@/utils/utils.js';
import { ElMessage, ElMessageBox } from 'element-plus';


export default {
  name: "ArticleDetail",
  data() {
    return {
      article: {},  // 视频信息
      user: {},   // 投稿用户信息
      markdownTxt: '',
    }
  },
  methods: {
    // 请求
    // 获取视频详细信息
    async getArticleDetail() {
      const res = await this.$get('/review/article/getone', {
        params: {
          aid: this.$route.params.aid,
        },
        headers: {
          Authorization: "Bearer " + localStorage.getItem("teri_token"),
        },
      });
      if (res.data.data) {
        console.log("视频详情: ", res.data.data);
        this.article = res.data.data.article;
        this.user = res.data.data.user;
      }
    },

    // 更新视频状态
    async updateArticleStatus(astatus) {
      this.$store.state.isLoading = true;
      const formData = new FormData();
      formData.append('aid', this.$route.params.aid);
      formData.append('status', astatus);
      const res = await this.$post('/article/change/status', formData, {
        headers: {
          Authorization: "Bearer " + localStorage.getItem("teri_token"),
        }
      });
      if (!res.data) {
        ElMessage.error("特丽丽被玩坏了(¯﹃¯)");
        this.$store.state.isLoading = false;
      }
      if (res.data.code === 200) {
        if (astatus === 3) {
          this.$router.push('/review/article/form');
          this.$store.state.isLoading = false;
        } else {
          await this.getArticleDetail();
          this.$store.state.isLoading = false;
        }
      } else {
        this.$store.state.isLoading = false;
      }
    },

    // 事件
    // 窗口大小改变时更新 player 的高度
    /*updatePlayerHeight() {
      const playerElement = document.getElementById('player');
      const playerWidth = playerElement.offsetWidth;
      const playerHeight = playerWidth * (9 / 16);
      playerElement.style.height = `${playerHeight}px`;
    },

    // 判断是否窗
    // 判断是否小窗
    changeWidth() {
      if (window.innerWidth < 480) {
        this.isMiniWidth = true;
      } else {
        this.isMiniWidth = false;
      }
    },*/

    // 将文本中的链接格式化成超链接
    formatText(text) {
      return linkify(text);
    },

    // 取消上传前的最后通牒
    beforeDelete() {
      ElMessageBox.confirm(
          '该操作会删除视频源文件，无法复原，确定删除吗？',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
      )
          .then(() => {
            this.updateArticleStatus(3);
          })
          .catch(() => {})
    },
    async fetchMarkdown() {
      try {
        console.log("fetchMarkdown");
        alert("函数被调用");
        const aid = this.$route.params.aid;
        const response = await this.$get('/column/markdown', {
          params: { aid },
          headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
        });
        this.markdownTxt = response.data.data.content;
      } catch (error) {
        console.error('获取Markdown文档失败', error);
        alert("发生错误"+error);
        // 在这里添加你的错误处理逻辑，比如显示错误信息给用户
      }
    },
  },
  async created() {
    //this.changeWidth();
    await this.getArticleDetail();
    await this.fetchMarkdown();
  },
  /*mounted() {
    this.updatePlayerHeight();
    window.addEventListener('resize', this.updatePlayerHeight);
    window.addEventListener('resize', this.changeWidth);
  },
  unmounted() {
    window.removeEventListener('resize', this.updatePlayerHeight);
    window.removeEventListener('resize', this.changeWidth);
  }*/
}
</script>

<style scoped>
.v-container {
  position: relative;
}

.video-detail__layout {
  position: relative;
  width: 100%;
  display: flex;
}

.left {
  width: 66%;
  max-width: 672px;
}

.player {
  box-shadow: 2px 2px 10px #0000003f;
  background-color: black;
  width: 100%;
}

.player video {
  width: 100%;
  height: 100%;
}

.options {
  margin-top: 16px;
}

.options-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 16px;
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

.options-item {
  padding: 0 10px;
}

.detail {
  flex: 1;
  margin: 0 0 0 1px;
  min-width: 400px;
  color: var(--text2);
}

.detail-card {
  padding: 0 16px 30px 20px;
}

.detail-item {
  display: flex;
  margin-top: 20px;
  min-height: 25px;
}

.item-title {
  flex: 0 0 auto;
  width: 70px;
  color: var(--text1);
  font-size: 16px;
  font-weight: 600;
}

.item-content {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
}

.type {
  flex: 0 0 auto;
  width: 45px;
}

.icon-jinzhi {
  font-size: 14px;
  color: var(--stress_red);
  margin-right: 4px;
}

.tag-container {
  text-align: center;
  padding: 0 12px;
  margin: 0px 12px 12px 0;
  height: 25px;
  border-radius: 14px;
  background: #f1f2f3;
  font-size: 12px;
  line-height: 25px;
  border: none;
}

.descr {
  width: 100%;
  padding: 10px;
  background-color: #fafafa;
  border: 1px solid #eee;
  border-radius: 8px;
}

@media (max-width: 700px) {
  .video-detail__layout {
    flex-direction: column;
  }

  .left {
    width: auto;
  }

  .detail {
    margin: 16px 0 0 0;
    min-width: auto;
  }

  .item-title {
    width: 50px;
  }
}

@media (min-width: 700.1px) and (max-width: 800px) {
  .detail {
    min-width: 300px;
  }

  .item-title {
    width: 50px;
  }
}
</style>