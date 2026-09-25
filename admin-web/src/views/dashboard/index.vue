<template>
  <div class="app-container">
    <el-row :gutter="14">
      <el-col v-for="card in cards" :key="card.key" :span="4">
        <div class="stat-card" :style="{ borderTopColor: card.color }">
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14" style="margin-top: 14px">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span>近 7 日工单趋势</span>
              <el-radio-group v-model="days" size="small" @change="loadTrend">
                <el-radio-button :value="7">近7天</el-radio-button>
                <el-radio-button :value="14">近14天</el-radio-button>
                <el-radio-button :value="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendRef" class="chart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>工单状态分布</template>
          <div ref="pieRef" class="chart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" style="margin-top: 14px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>处理人工单排行(近30天)</template>
          <el-table :data="rank" size="small" stripe>
            <el-table-column type="index" label="排名" width="70" />
            <el-table-column prop="name" label="处理人" />
            <el-table-column prop="value" label="解决工单数" width="120" sortable />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>分类分布(近30天)</template>
          <div ref="catRef" class="chart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { reportApi } from '@/api/report'

const overview = ref<any>({})
const cards = computed(() => [
  { key: 'created', label: '区间新建', value: overview.value.created ?? 0, color: '#409eff' },
  { key: 'resolved', label: '区间解决', value: overview.value.resolved ?? 0, color: '#67c23a' },
  { key: 'closed', label: '区间关闭', value: overview.value.closed ?? 0, color: '#909399' },
  { key: 'pending', label: '待受理', value: overview.value.pending ?? 0, color: '#e6a23c' },
  { key: 'processing', label: '处理中/挂起', value: overview.value.processing ?? 0, color: '#f56c6c' },
  { key: 'avg', label: '平均时长(小时)', value: toHours(overview.value.avgDuration), color: '#9254de' }
])

function toHours(v: any) {
  return v ? Math.round((Number(v) / 3600) * 100) / 100 : 0
}

function range(days: number) {
  return {
    start: dayjs().subtract(days - 1, 'day').format('YYYY-MM-DD'),
    end: dayjs().format('YYYY-MM-DD')
  }
}

const days = ref(7)
const trendRef = ref<HTMLElement>()
const pieRef = ref<HTMLElement>()
const catRef = ref<HTMLElement>()
let trendChart: echarts.ECharts
let pieChart: echarts.ECharts
let catChart: echarts.ECharts
const rank = ref<any[]>([])

async function loadOverview() {
  const r = range(30)
  const res = await reportApi.overview(r.start, r.end)
  overview.value = res.data || {}
}

async function loadTrend() {
  const r = range(days.value)
  const res = await reportApi.trend(r.start, r.end)
  const list = res.data || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新建', '解决'] },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: list.map((i: any) => i.date.slice(5)), boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '新建',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#409eff' },
        data: list.map((i: any) => Number(i.created))
      },
      {
        name: '解决',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#67c23a' },
        data: list.map((i: any) => Number(i.resolved))
      }
    ]
  })
}

async function loadPie() {
  const res = await reportApi.distribution('status')
  const list = res.data || []
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        label: { formatter: '{b}\n{c}' },
        data: list.map((i: any) => ({ name: i.name, value: Number(i.value) }))
      }
    ]
  })
}

async function loadRank() {
  const r = range(30)
  const [rankRes, catRes] = await Promise.all([
    reportApi.rank(r.start, r.end),
    reportApi.distribution('category', r.start, r.end)
  ])
  rank.value = (rankRes.data || []).slice(0, 10)
  const cats = catRes.data || []
  catChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { type: 'scroll', bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: '62%',
        center: ['50%', '44%'],
        data: cats.map((i: any) => ({ name: i.name, value: Number(i.value) }))
      }
    ]
  })
}

onMounted(async () => {
  await nextTick()
  trendChart = echarts.init(trendRef.value!)
  pieChart = echarts.init(pieRef.value!)
  catChart = echarts.init(catRef.value!)
  window.addEventListener('resize', resize)
  await Promise.all([loadOverview(), loadTrend(), loadPie(), loadRank()])
})

function resize() {
  trendChart?.resize()
  pieChart?.resize()
  catChart?.resize()
}
</script>

<style scoped>
.stat-card {
  background: #fff;
  border-radius: 6px;
  padding: 18px 14px;
  border-top: 3px solid #409eff;
  text-align: center;
}
.stat-value {
  font-size: 26px;
  font-weight: 600;
}
.stat-label {
  color: #909399;
  font-size: 13px;
  margin-top: 6px;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
