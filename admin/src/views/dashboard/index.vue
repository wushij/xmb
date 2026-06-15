<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="4" v-for="item in statCards" :key="item.key">
        <el-card class="stat-card" :body-style="{ padding: '16px' }">
          <div class="stat-header">
            <div class="stat-title">
              <el-icon class="stat-icon" :style="{ color: item.color }">
                <component :is="item.icon" />
              </el-icon>
              <span class="stat-label">{{ item.label }}</span>
            </div>
            <el-dropdown v-if="item.selectable" trigger="click" @command="handleStatDateChange">
              <span class="stat-date-btn">
                {{ statDateLabel }} <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="today">今日</el-dropdown-item>
                  <el-dropdown-item command="yesterday">昨日</el-dropdown-item>
                  <el-dropdown-item command="dayBefore">前日</el-dropdown-item>
                  <el-dropdown-item command="week">本周</el-dropdown-item>
                  <el-dropdown-item command="month">本月</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="stat-value">{{ statistics[item.key] || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 销售对比分析 -->
    <el-row :gutter="20" class="compare-row">
      <el-col :span="8">
        <el-card class="compare-card">
          <template #header>
            <div class="card-header">
              <span>日销售对比</span>
              <div class="date-pickers">
                <el-date-picker
                  v-model="selectedDate1"
                  type="date"
                  placeholder="日期A"
                  format="MM-DD"
                  value-format="YYYY-MM-DD"
                  :clearable="false"
                  @change="loadSalesCompare"
                  style="width: 100px"
                />
                <span class="vs-text">vs</span>
                <el-date-picker
                  v-model="selectedDate2"
                  type="date"
                  placeholder="日期B"
                  format="MM-DD"
                  value-format="YYYY-MM-DD"
                  :clearable="false"
                  @change="loadSalesCompare"
                  style="width: 100px"
                />
              </div>
            </div>
          </template>
          <div class="compare-content">
            <div class="compare-item">
              <span class="compare-label">{{ selectedDate1 || '日期A' }} 销售额</span>
              <span class="compare-value">¥{{ salesCompare.todayAmount || '0.00' }}</span>
            </div>
            <div class="compare-item">
              <span class="compare-label">{{ selectedDate2 || '日期B' }} 销售额</span>
              <span class="compare-value muted">¥{{ salesCompare.yesterdayAmount || '0.00' }}</span>
            </div>
            <div class="compare-item">
              <span class="compare-label">{{ selectedDate1 || '日期A' }} 订单</span>
              <span class="compare-value">{{ salesCompare.todayOrderCount || 0 }} 单</span>
            </div>
            <div class="compare-item">
              <span class="compare-label">{{ selectedDate2 || '日期B' }} 订单</span>
              <span class="compare-value muted">{{ salesCompare.yesterdayOrderCount || 0 }} 单</span>
            </div>
            <div class="growth-section">
              <div class="growth-item">
                <span class="growth-label">销售额增长</span>
                <span :class="['growth-rate', getGrowthClass(salesCompare.dayGrowthRate)]">
                  {{ formatGrowthRate(salesCompare.dayGrowthRate) }}
                </span>
              </div>
              <div class="growth-item">
                <span class="growth-label">订单数增长</span>
                <span :class="['growth-rate', getGrowthClass(salesCompare.dayOrderGrowthRate)]">
                  {{ formatGrowthRate(salesCompare.dayOrderGrowthRate) }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="compare-card">
          <template #header>
            <div class="card-header">
              <span>月销售对比</span>
              <div class="date-pickers">
                <el-date-picker
                  v-model="selectedMonth1"
                  type="month"
                  placeholder="月份A"
                  format="YYYY-MM"
                  value-format="YYYY-MM"
                  :clearable="false"
                  @change="loadSalesCompare"
                  style="width: 95px"
                />
                <span class="vs-text">vs</span>
                <el-date-picker
                  v-model="selectedMonth2"
                  type="month"
                  placeholder="月份B"
                  format="YYYY-MM"
                  value-format="YYYY-MM"
                  :clearable="false"
                  @change="loadSalesCompare"
                  style="width: 95px"
                />
              </div>
            </div>
          </template>
          <div class="compare-content">
            <div class="compare-item">
              <span class="compare-label">{{ selectedMonth1 || '月份A' }} 销售额</span>
              <span class="compare-value">¥{{ salesCompare.monthAmount || '0.00' }}</span>
            </div>
            <div class="compare-item">
              <span class="compare-label">{{ selectedMonth2 || '月份B' }} 销售额</span>
              <span class="compare-value muted">¥{{ salesCompare.lastMonthAmount || '0.00' }}</span>
            </div>
            <div class="compare-item">
              <span class="compare-label">{{ selectedMonth1 || '月份A' }} 订单</span>
              <span class="compare-value">{{ salesCompare.monthOrderCount || 0 }} 单</span>
            </div>
            <div class="compare-item">
              <span class="compare-label">{{ selectedMonth2 || '月份B' }} 订单</span>
              <span class="compare-value muted">{{ salesCompare.lastMonthOrderCount || 0 }} 单</span>
            </div>
            <div class="growth-section">
              <div class="growth-item">
                <span class="growth-label">销售额增长</span>
                <span :class="['growth-rate', getGrowthClass(salesCompare.monthGrowthRate)]">
                  {{ formatGrowthRate(salesCompare.monthGrowthRate) }}
                </span>
              </div>
              <div class="growth-item">
                <span class="growth-label">订单数增长</span>
                <span :class="['growth-rate', getGrowthClass(salesCompare.monthOrderGrowthRate)]">
                  {{ formatGrowthRate(salesCompare.monthOrderGrowthRate) }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="compare-card">
          <template #header>
            <div class="card-header">
              <span>年销售对比</span>
              <div class="date-pickers">
                <el-date-picker
                  v-model="selectedYear1"
                  type="year"
                  placeholder="年份A"
                  format="YYYY"
                  value-format="YYYY"
                  :clearable="false"
                  @change="loadSalesCompare"
                  style="width: 85px"
                />
                <span class="vs-text">vs</span>
                <el-date-picker
                  v-model="selectedYear2"
                  type="year"
                  placeholder="年份B"
                  format="YYYY"
                  value-format="YYYY"
                  :clearable="false"
                  @change="loadSalesCompare"
                  style="width: 85px"
                />
              </div>
            </div>
          </template>
          <div class="compare-content">
            <div class="compare-item">
              <span class="compare-label">{{ selectedYear1 || '年份A' }} 年销售额</span>
              <span class="compare-value">¥{{ salesCompare.yearAmount || '0.00' }}</span>
            </div>
            <div class="compare-item">
              <span class="compare-label">{{ selectedYear2 || '年份B' }} 年销售额</span>
              <span class="compare-value muted">¥{{ salesCompare.lastYearAmount || '0.00' }}</span>
            </div>
            <div class="compare-item single">
              <span class="compare-label">同比增长率</span>
              <span :class="['growth-rate', 'large', getGrowthClass(salesCompare.yearGrowthRate)]">
                {{ formatGrowthRate(salesCompare.yearGrowthRate) }}
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <span>近7天订单趋势</span>
          </template>
          <div ref="orderChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <span>热销商品 Top5</span>
          </template>
          <el-table :data="hotGoods" style="width: 100%" :show-header="true">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="sales" label="销量" width="80" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ArrowDown } from '@element-plus/icons-vue'
import { getStatistics, getOrderTrend, getHotGoods, getSalesCompare } from '@/api'

const orderChartRef = ref(null)
let chartInstance = null

const statistics = ref({
  todayOrderCount: 0,
  todaySalesAmount: 0,
  totalOrderCount: 0,
  totalSalesAmount: 0,
  totalGoodsCount: 0,
  totalCategoryCount: 0
})

const statDateType = ref('today')
const statDateLabel = ref('今日')

const today = new Date()
const yesterday = new Date(today)
yesterday.setDate(yesterday.getDate() - 1)

const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatMonth = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

const formatYear = (date) => {
  return String(date.getFullYear())
}

const statCards = [
  { key: 'todayOrderCount', label: '订单数', icon: 'Document', color: '#409EFF', selectable: true },
  { key: 'todaySalesAmount', label: '销售额', icon: 'Money', color: '#67C23A', selectable: true },
  { key: 'totalOrderCount', label: '总订单数', icon: 'Tickets', color: '#E6A23C', selectable: false },
  { key: 'totalSalesAmount', label: '总销售额', icon: 'Wallet', color: '#F56C6C', selectable: false },
  { key: 'totalGoodsCount', label: '商品总数', icon: 'Goods', color: '#909399', selectable: false },
  { key: 'totalCategoryCount', label: '分类总数', icon: 'Menu', color: '#9B59B6', selectable: false }
]

const handleStatDateChange = async (command) => {
  statDateType.value = command
  const labels = {
    today: '今日',
    yesterday: '昨日',
    dayBefore: '前日',
    week: '本周',
    month: '本月'
  }
  statDateLabel.value = labels[command] || '今日'
  await loadStatistics()
}

const getStatDateRange = () => {
  const now = new Date()
  const type = statDateType.value
  
  switch (type) {
    case 'today':
      return { date: formatDate(now) }
    case 'yesterday': {
      const d = new Date(now)
      d.setDate(d.getDate() - 1)
      return { date: formatDate(d) }
    }
    case 'dayBefore': {
      const d = new Date(now)
      d.setDate(d.getDate() - 2)
      return { date: formatDate(d) }
    }
    case 'week': {
      const day = now.getDay() || 7
      const monday = new Date(now)
      monday.setDate(monday.getDate() - day + 1)
      return { startDate: formatDate(monday), endDate: formatDate(now) }
    }
    case 'month': {
      const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
      return { startDate: formatDate(firstDay), endDate: formatDate(now) }
    }
    default:
      return { date: formatDate(now) }
  }
}

const selectedDate1 = ref(formatDate(today))
const selectedDate2 = ref(formatDate(yesterday))

const lastMonth = new Date(today)
lastMonth.setMonth(lastMonth.getMonth() - 1)

const selectedMonth1 = ref(formatMonth(today))
const selectedMonth2 = ref(formatMonth(lastMonth))

const lastYear = new Date(today)
lastYear.setFullYear(lastYear.getFullYear() - 1)

const selectedYear1 = ref(formatYear(today))
const selectedYear2 = ref(formatYear(lastYear))

const salesCompare = ref({
  todayAmount: '0.00',
  yesterdayAmount: '0.00',
  dayGrowthRate: 0,
  dayOrderGrowthRate: 0,
  monthAmount: '0.00',
  lastMonthAmount: '0.00',
  monthGrowthRate: 0,
  monthOrderGrowthRate: 0,
  yearAmount: '0.00',
  lastYearAmount: '0.00',
  yearGrowthRate: 0,
  todayOrderCount: 0,
  yesterdayOrderCount: 0,
  monthOrderCount: 0,
  lastMonthOrderCount: 0
})

const hotGoods = ref([])

const formatGrowthRate = (rate) => {
  if (rate === null || rate === undefined) return '0.00%'
  const num = parseFloat(rate)
  const prefix = num >= 0 ? '+' : ''
  return `${prefix}${num.toFixed(2)}%`
}

const getGrowthClass = (rate) => {
  if (rate === null || rate === undefined) return 'neutral'
  const num = parseFloat(rate)
  if (num > 0) return 'positive'
  if (num < 0) return 'negative'
  return 'neutral'
}

const loadStatistics = async () => {
  try {
    const dateRange = getStatDateRange()
    const res = await getStatistics(dateRange)
    statistics.value = res || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadSalesCompare = async () => {
  try {
    const params = {
      date1: selectedDate1.value,
      date2: selectedDate2.value,
      month1: selectedMonth1.value,
      month2: selectedMonth2.value,
      year1: selectedYear1.value ? parseInt(selectedYear1.value) : null,
      year2: selectedYear2.value ? parseInt(selectedYear2.value) : null
    }
    const res = await getSalesCompare(params)
    salesCompare.value = res || {}
  } catch (error) {
    console.error('加载销售对比数据失败:', error)
  }
}

const loadData = async () => {
  try {
    const [trendRes, hotRes] = await Promise.all([
      getOrderTrend(),
      getHotGoods()
    ])
    hotGoods.value = hotRes || []
    initChart(trendRes || [])
    
    await Promise.all([loadStatistics(), loadSalesCompare()])
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const initChart = (data) => {
  if (!orderChartRef.value) return
  
  chartInstance = echarts.init(orderChartRef.value)
  
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date)
    },
    yAxis: { type: 'value' },
    series: [{
      name: '订单数',
      type: 'line',
      smooth: true,
      data: data.map(item => item.count),
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409EFF' }
    }]
  }
  
  chartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.dashboard {
  min-height: 100%;
  overflow: visible;
}

.stat-row {
  margin-bottom: 20px;
  overflow: visible;
}

.stat-card {
  height: 100%;
  border-radius: 8px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  overflow: hidden;
}

.stat-header {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  margin-bottom: 12px;
  gap: 8px;
}

.stat-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-icon {
  font-size: 18px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.stat-date-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #fff;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}

.stat-date-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.stat-date-btn .el-icon {
  font-size: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  text-align: center;
}

.compare-row {
  margin-bottom: 20px;
  overflow: visible;
}

.compare-card {
  height: 100%;
}

.compare-card :deep(.el-card__body) {
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-pickers {
  display: flex;
  align-items: center;
  gap: 6px;
}

.vs-text {
  color: #909399;
  font-size: 12px;
  font-weight: 500;
}

.compare-content {
  padding: 10px 0;
}

.compare-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.compare-item:last-child {
  border-bottom: none;
}

.compare-item.single {
  margin-top: 20px;
  padding: 15px 0;
  justify-content: center;
  flex-direction: column;
  align-items: center;
}

.compare-label {
  color: #666;
  font-size: 14px;
}

.compare-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.compare-value.muted {
  color: #999;
  font-size: 14px;
  font-weight: normal;
}

.growth-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #e0e0e0;
}

.growth-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}

.growth-label {
  color: #888;
  font-size: 13px;
}

.growth-rate {
  font-size: 14px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.growth-rate.large {
  font-size: 24px;
  padding: 8px 16px;
  margin-top: 8px;
}

.growth-rate.positive {
  color: #67C23A;
  background-color: #f0f9eb;
}

.growth-rate.negative {
  color: #F56C6C;
  background-color: #fef0f0;
}

.growth-rate.neutral {
  color: #909399;
  background-color: #f4f4f5;
}

.chart-card {
  height: 400px;
}

.chart-card :deep(.el-card__body) {
  overflow: hidden;
  padding: 20px;
}

.chart {
  height: 320px;
  width: 100%;
}

/* 隐藏el-table滚动条 */
:deep(.el-table__body-wrapper) {
  overflow-x: hidden;
}

:deep(.el-table__header-wrapper) {
  overflow-x: hidden;
}
</style>
