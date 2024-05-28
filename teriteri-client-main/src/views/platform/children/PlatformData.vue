<template>
    <div class="platform-data">
      <div class="navbar">
        <div class="navbar-this" style="color: floralwhite" @click="this.$router.push('/platform/data-up')">数据概览 </div>
        <div class="navbar-item" @click="this.$router.push('/platform/diagnosis')">账号诊断</div>
        <div class="navbar-item" @click="this.$router.push('/platform/manuAnalysis')">稿件分析</div>
      </div>
      <div class="container">
        <div class="box-text">
          <div class="info" style="color: #FF0066">核心数据</div>
        </div>
        <div class="box" @mouseover="highlight(1)" @mouseleave="removeHighlight(1)">
          <span v-if="highlighted === 1"></span>
          <div class="text">播放量</div>
          <div class="text-data" style="margin-top:50px;">{{handleNum(viewSum)}}</div>
        </div>
        <div class="box" @mouseover="highlight(1)" @mouseleave="removeHighlight(1)">
          <span v-if="highlighted === 1"></span>
          <div class="text">获赞量</div>
          <div class="text-data" style="margin-top:50px;">{{handleNum(likeSum)}}</div>
        </div>
        <div class="box" @mouseover="highlight(1)" @mouseleave="removeHighlight(1)">
          <span v-if="highlighted === 1"></span>
          <div class="text">收藏量</div>
          <div class="text-data" style="margin-top:50px;">{{handleNum(favoriteSum)}}</div>
        </div>
        <div class="box" @mouseover="highlight(1)" @mouseleave="removeHighlight(1)">
          <span v-if="highlighted === 1"></span>
          <div class="text">关注量</div>
          <div class="text-data" style="margin-top:50px;">{{handleNum(followSum)}}</div>
        </div>
      </div>
      <div class="line"></div>
      <div ref="chart" style="width: 100%; height: 500px; margin-top: 50px"></div>
    </div>
</template>

<script>
import { ref } from 'vue';
import * as echarts from 'echarts';
import { handleNum } from '@/utils/utils.js';

export default{
  data() {
    return {
      highlighted: null,
      favoriteSum: 0,
      followSum: 0,
      viewSum: 0,
      likeSum: 0,
      favorite: [],
      follow: [],
      view: [],
      like: []
    };
  },
  methods: {
    async getData() {
      const res = await this.$get('/user_record', {
        params: { uid: this.$store.state.user.uid },
        headers: { Authorization: "Bearer " + localStorage.getItem("teri_token") }
      });
      if (!res.data) return;
      //console.log("now: ", res.data.data);
      if (res.data.data) {
        this.favorite = res.data.data.favorite;
        this.follow = res.data.data.follow;
        this.view = res.data.data.view;
        this.like = res.data.data.like;
        //this.calculateTotals();
        //this.drawChart();
      }
      return true;
    },
    calculateTotals() {
      this.favoriteSum = this.favorite.reduce((acc, curr) => acc + curr, 0);
      this.followSum = this.follow.reduce((acc, curr) => acc + curr, 0);
      this.viewSum = this.view.reduce((acc, curr) => acc + curr, 0);
      this.likeSum = this.like.reduce((acc, curr) => acc + curr, 0);
    },

    drawChart() {
      const active = ref('1');
      const hoverIndex = ref(null);
      const handleSelect = (index) => {
        active.value = index;
      };

      const chart = ref(null);

      const myChart = echarts.init(this.$refs.chart);
      const currentDate = new Date();
      const days = [];
      for (let i = 1; i <= 7; i++) {
        const date = new Date(currentDate);
        date.setDate(currentDate.getDate() - i);
        days.unshift(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }));
      }
      const data = {
        days: days,
        views: this.view,
        favorites: this.favorite,
        follows: this.follow,
        likes: this.like
      };

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['观看量', '收藏量', '关注量', '获赞量']
        },
        xAxis: {
          type: 'category',
          data: data.days
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '观看量',
            type: 'line',
            data: data.views,
            itemStyle: {
              color: '#FF0066',
              borderWidth: 10
            }
          },
          {
            name: '收藏量',
            type: 'line',
            data: data.favorites,
            itemStyle: {
              color: '#FF69B4',
              borderWidth: 10
            }
          },
          {
            name: '关注量',
            type: 'line',
            data: data.follows,
            itemStyle: {
              color: '#FF8CA0',
              borderWidth: 10
            }
          },
          {
            name: '获赞量',
            type: 'line',
            data: data.likes,
            itemStyle: {
              color: '#FFB6AF',
              borderWidth: 10
            }
          }
        ]
      };

      myChart.setOption(option);

      myChart.on('mousemove', params => {
        const pointInPixel = [params.offsetX, params.offsetY];
        const pointInGrid = this.myChart.convertFromPixel('grid', pointInPixel);
        const dataIndex = Math.round(pointInGrid[0]);
        const value = [];
        myChart.getOption().series.forEach(series => {
          value.push(series.data[dataIndex]);
        });
        myChart.dispatchAction({
          type: 'showTip',
          seriesIndex: 0,
          dataIndex: dataIndex,
          position: pointInGrid,
          data: value
        });
      });

      return {
        chart,
        active,
        hoverIndex,
        handleSelect
      };
    },
    handleNum(number) {
      return handleNum(number);
    },
    highlight(boxNumber) {
      this.highlighted = boxNumber;
    },
    removeHighlight(boxNumber) {
      if (this.highlighted === boxNumber) {
        this.highlighted = null;
      }
    }
  },
  async created() {
    await this.getData();
    this.calculateTotals();
    this.drawChart();
  },
};
</script>

<style scoped>
.platform-data {
    width: 100%;
    border-radius: 30px 30px 30px 30px;
    background-color: white;
}

.container {
  display: flex;
  justify-content: center;
  height: 25vh;
}
.text {
  text-align-last:justify;
  padding-right:80px;
  margin-top:1px;
  text-align: left;
  color: black;
  font-size:25px;
}
.text-data {
  margin-top: 10px;
  text-align: center;
  color: white;
  font-size:30px;
}
.box {
  width: 200px;
  height: 150px;
  background-color: darkgray;
  border-radius: 20px;
  display: flex;
  justify-content: center;
  margin-left:15px;
  margin-top:20px;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s ease;
}
.box:hover {
  background-color: pink;
}
.box-text {
  width: 100px;
  height: 100px;
  display: flex;
  background-color: white;
  margin-top:45px;
  margin-right:130px;
  font-size: 25px;
  justify-content: center;
  align-items: center;
}
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
  margin-bottom: 20px;
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
.line {
  margin: 0 auto;
  border-top: 3px solid darkgray; /* 直线样式 */
  width: 80%; /* 直线宽度占满页面 */
}
</style>