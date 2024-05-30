<template>
    <div class="platform-data">
      <div class="navbar">
        <div class="navbar-this" style="color: floralwhite" @click="this.$router.push('/platform/data-up')">数据概览 </div>
        <div class="navbar-item" @click="this.$router.push('/platform/diagnosis')">账号诊断</div>
        <div class="navbar-item" @click="this.$router.push('/platform/manuAnalysis')">稿件分析</div>
      </div>
      <div class="container">
        <div class="box-text">
          <div class="info">核心数据</div>
        </div>
        <div class="box" @mouseover="handleMouseOver(1)" @mouseout="handleMouseOut(1)">
          <div class="text" :style="{color: textColor(1)}">播放量</div>
          <div class="text-data" :style="{color: textDataColor(1)}">{{handleNum(viewSum)}}</div>
        </div>
        <div class="box" @mouseover="handleMouseOver(2)" @mouseout="handleMouseOut(2)">
          <div class="text" :style="{color: textColor(2)}">获赞量</div>
          <div class="text-data" :style="{color: textDataColor(2)}">{{handleNum(likeSum)}}</div>
        </div>
        <div class="box" @mouseover="handleMouseOver(3)" @mouseout="handleMouseOut(3)">
          <div class="text" :style="{color: textColor(3)}">收藏量</div>
          <div class="text-data" :style="{color: textDataColor(3)}">{{handleNum(favoriteSum)}}</div>
        </div>
        <div class="box" @mouseover="handleMouseOver(4)" @mouseout="handleMouseOut(4)">
          <div class="text" :style="{color: textColor(4)}">关注量</div>
          <div class="text-data" :style="{color: textDataColor(4)}">{{handleNum(followSum)}}</div>
        </div>
      </div>
      <div class="chart-background">
        <div ref="chart" style="width: 100%; height: 500px; margin-top: 50px"></div>
      </div>
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
      like: [],
      isHovered: {
        1: false,
        2: false,
        3: false,
        4: false
      }
    };
  },
  methods: {
    handleMouseOver(num) {
      this.isHovered[num] = true;
    },
    handleMouseOut(num) {
      this.isHovered[num] = false;
    },
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
              color: '#001627',
              borderWidth: 10
            }
          },
          {
            name: '收藏量',
            type: 'line',
            data: data.favorites,
            itemStyle: {
              color: '#004B76',
              borderWidth: 10
            }
          },
          {
            name: '关注量',
            type: 'line',
            data: data.follows,
            itemStyle: {
              color: '#008AC5',
              borderWidth: 10
            }
          },
          {
            name: '获赞量',
            type: 'line',
            data: data.likes,
            itemStyle: {
              color: '#40C5F1',
              borderWidth: 10
            }
          }
        ]
      };

      myChart.setOption(option);

      myChart.on('mousemove', params => {
        const pointInPixel = [params.offsetX, params.offsetY];
        const pointInGrid = myChart.convertFromPixel('grid', pointInPixel);
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
    }
  },
  async created() {
    await this.getData();
    this.calculateTotals();
    this.drawChart();
  },
  computed: {
    textColor() {
      return (num) => {
        return this.isHovered[num] ? 'white' : '#61666D';
      }
    },
    textDataColor() {
      return (num) => {
        return this.isHovered[num] ? 'white' : 'black';
      }
    }
  }
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
  //text-align-last:justify;
  padding-right:110px;
  margin-top:5px;
  text-align: left;
  color: var(--Ga7);
  font-size: 18px;
  font-weight: bold;
}
.text-data {
  padding-right:123px;
  margin-top: 10px;
  text-align: left;
  margin-bottom: 15px;
  font-stretch: extra-condensed;
  font-weight: bold;
  color: black;
  font-size:20px;
}
.box {
  width: 200px;
  height: 100px;
  background-color: var(--Ga1);
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
  background-color: var(--Lb5);
}
.box-text {
  width: 120px;
  height: 150px;
  display: flex;
  background-color: white;
  margin-top:-6px;
  margin-right:100px;
  justify-content: space-around;
  align-items: center;
}
.info {
  color: black;
  font-weight: bold;
  font-size: 30px;
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
  margin-bottom: 40px;
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
.chart-background {
  display: flex;
  background-color: white;
  justify-content: center;
  border-top-left-radius: 40px;
  border-top-right-radius: 40px;
  border-top: 4px solid var(--Lb2);
  margin-top: -40px;
}
</style>