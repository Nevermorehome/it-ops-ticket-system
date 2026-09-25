<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-radio-group v-model="quick" @change="applyQuick">
            <el-radio-button value="day">日报(今日)</el-radio-button>
            <el-radio-button value="week">周报(近7天)</el-radio-button>
            <el-radio-button value="month">月报(近30天)</el-radio-button>
            <el-radio-button value="custom">自定义</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="quick === 'custom'">
          <el-date-picker
            v-model="range"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadAll">统计</el-button>
          <el-button-group v-hasPermi="['report:export']">
            <el-button :icon="Download" @click="doExport('excel')">Excel</el-button>
            <el-button :icon="Document" @click="doExport('word')">Word</el-button>
            <el-button :icon="Printer" @click="doExport('pdf')">PDF</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-card>

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
          <template #header>工单趋势(新建/解决)</template>
          <div ref="trendRef" class="chart" style="height: 340px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span>分布统计</span>
              <el-select v-model="groupBy" size="small" style="width: 110px" @change="loadDistribution">
                <el-option label="按分类" value="category" />
                <el-option label="按优先级" value="priority" />
                <el-option label="按来源" value="source" />
                <el-option label="按状态" value="status" />
              </el-select>
            </div>
          </template>
          <div ref="pieRef" class="chart" style="height: 340px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" style="margin-top: 14px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>处理人解决排行</template>
          <el-table :data="rank" size="small" stripe>
            <el-table-column type="index" label="排名" width="70" />
            <el-table-column prop="name" label="处理人" />
            <el-table-column label="解决数" width="180">
              <template #default="{ row }">
                <el-progress :percentage="rankPercent(Number(row.value))" :format="() => row.value" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>部门工单量</template>
          <el-table :data="dept" size="small" stripe height="330">
            <el-table-column prop="name" label="部门" />
            <el-table-column prop="value" label="工单数" width="100" sortable />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { Search, Download, Document, Printer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { reportApi } from '@/api/report'

const quick = ref('week')
const range = ref<[string, string]>([
  dayjs().subtract(6, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD')
])
const groupBy = ref('category')
const overview = ref<any>({})

const cards = computed(() => [
  { key: 'created', label: '新建工单', value: overview.value.created ?? 0, color: '#409eff' },
  { key: 'resolved', label: '已解决', value: overview.value.resolved ?? 0, color: '#67c23a' },
  { key: 'closed', label: '已关闭', value: overview.value.closed ?? 0, color: '#909399' },
  { key: 'pending', label: '待受理', value: overview.value.pending ?? 0, color: '#e6a23c' },
  { key: 'suspended', label: '挂起中', value: overview.value.suspended ?? 0, color: '#f56c6c' },
  {
    key: 'avg',
    label: '平均时长(小时)',
    value: overview.value.avgDuration ? Math.round((Number(overview.value.avgDuration) / 3600) * 100) / 100 : 0,
    color: '#9254de'
  }
])

const trendRef = ref<HTMLElement>()
const pieRef = ref<HTMLElement>()
let trendChart: echarts.ECharts
let pieChart: echarts.ECharts
const rank = ref<any[]>([])
const dept = ref<any[]>([])

function applyQuick(val: string) {
  const today = dayjs()
  if (val === 'day') range.value = [today.format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
  if (val === 'week') range.value = [today.subtract(6, 'day').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
  if (val === 'month') range.value = [today.subtract(29, 'day').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
  if (val !== 'custom') loadAll()
}

async function loadOverview() {
  const res = await reportApi.overview(range.value[0], range.value[1])
  overview.value = res.data || {}
}
async function loadTrend() {
  const res = await reportApi.trend(range.value[0], range.value[1])
  const list = res.data || []
  trendChart.setOption(
    {
      tooltip: { trigger: 'axis' },
      legend: { data: ['新建', '解决'] },
      grid: { left: 40, right: 20, top: 40, bottom: 30 },
      xAxis: { type: 'category', data: list.map((i: any) => i.date.slice(5)), boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '新建',
          type: 'bar',
          barMaxWidth: 18,
          itemStyle: { color: '#409eff', borderRadius: [3, 3, 0, 0] },
          data: list.map((i: any) => Number(i.created))
        },
        {
          name: '解决',
          type: 'line',
          smooth: true,
          itemStyle: { color: '#67c23a' },
          data: list.map((i: any) => Number(i.resolved))
        }
      ]
    },
    true
  )
}
async function loadDistribution() {
  const res = await reportApi.distribution(groupBy.value, range.value[0], range.value[1])
  const list = res.data || []
  pieChart.setOption(
    {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { type: 'scroll', bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['38%', '64%'],
          center: ['50%', '45%'],
          label: { formatter: '{b}\n{c}' },
          data: list.map((i: any) => ({ name: i.name, value: Number(i.value) }))
        }
      ]
    },
    true
  )
}
async function loadRankDept() {
  const [r1, r2] = await Promise.all([
    reportApi.rank(range.value[0], range.value[1]),
    reportApi.dept(range.value[0], range.value[1])
  ])
  rank.value = r1.data || []
  dept.value = r2.data || []
}

function rankPercent(v: number) {
  const max = Math.max(1, ...rank.value.map((r) => Number(r.value)))
  return Math.round((v / max) * 100)
}

async function loadAll() {
  if (!range.value || !range.value[0] || !range.value[1]) {
    ElMessage.warning('请选择统计区间')
    return
  }
  await Promise.all([loadOverview(), loadTrend(), loadDistribution(), loadRankDept()])
}

async function doExport(type: string) {
  await reportApi.export(type, range.value?.[0], range.value?.[1])
  ElMessage.success('导出成功')
}

onMounted(async () => {
  applyQuick('week')
  await nextTick()
  trendChart = echarts.init(trendRef.value!)
  pieChart = echarts.init(pieRef.value!)
  window.addEventListener('resize', () => {
    trendChart?.resize()
    pieChart?.resize()
  })
  loadAll()
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 14px;
}
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
