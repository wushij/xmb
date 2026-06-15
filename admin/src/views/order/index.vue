<template>
  <div class="order-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">订单列表</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 130px">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="配送中/待取货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="退款中" :value="5" />
            <el-option label="已退款" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" class="custom-table">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="receiverName" label="收货人" width="80" align="center" />
        <el-table-column prop="receiverPhone" label="联系电话" width="110" align="center" />
        <el-table-column label="配送方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.deliveryType === 1 ? 'success' : 'primary'" size="small">
              {{ row.deliveryType === 1 ? '自提' : '配送' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品数量" width="90" align="center">
          <template #default="{ row }">
            <span class="num-text">{{ row.totalNum || (row.items ? row.items.reduce((sum, item) => sum + item.num, 0) : 0) }} 件</span>
          </template>
        </el-table-column>
        <el-table-column prop="payPrice" label="实付金额" width="100" align="center">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.payPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status, row.deliveryType)" class="status-tag">{{ getStatusName(row.status, row.deliveryType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" min-width="140" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" class="detail-btn" @click="handleDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="订单详情" size="500px">
      <div class="order-detail" v-if="currentOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.status, currentOrder.deliveryType)">
              {{ getStatusName(currentOrder.status, currentOrder.deliveryType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="配送方式">
            <el-tag :type="currentOrder.deliveryType === 1 ? 'success' : 'primary'" size="small">
              {{ currentOrder.deliveryType === 1 ? '到店自提' : '配送到家' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人" v-if="currentOrder.deliveryType === 0">{{ currentOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话" v-if="currentOrder.deliveryType === 0">{{ currentOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" v-if="currentOrder.deliveryType === 0">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="取货人" v-if="currentOrder.deliveryType === 1">{{ currentOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话" v-if="currentOrder.deliveryType === 1">{{ currentOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="取货地址" v-if="currentOrder.deliveryType === 1">{{ currentOrder.pickupAddress || '小卖部门店(具体地址待补充)' }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.totalPrice }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="color: #f56c6c; font-weight: bold">¥{{ currentOrder.payPrice }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单备注">{{ currentOrder.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="goods-list">
          <h4>商品列表</h4>
          <div class="goods-item" v-for="item in currentOrder.items" :key="item.goodsId">
            <el-image :src="getImageUrl(item.goodsImage)" class="goods-image" fit="cover">
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="goods-info">
              <div class="goods-name">{{ item.goodsName }}</div>
              <div class="goods-price">¥{{ item.goodsPrice }} x {{ item.num }}</div>
            </div>
            <div class="goods-total">¥{{ item.totalPrice }}</div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions" v-if="currentOrder.status === 1">
          <el-button 
            :type="currentOrder.deliveryType === 1 ? 'success' : 'primary'" 
            @click="handleShip"
          >
            {{ currentOrder.deliveryType === 1 ? '备货完成' : '发货' }}
          </el-button>
        </div>
        <div class="order-actions" v-if="currentOrder.status === 5">
          <el-button type="danger" @click="handleRefund">同意退款</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View } from '@element-plus/icons-vue'
import { getOrderList, getOrderDetail, updateOrderStatus } from '@/api'

const loading = ref(false)
const tableData = ref([])
const drawerVisible = ref(false)
const currentOrder = ref(null)

// 图片URL处理
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return 'https://wushij.online' + path
}

// 时间格式化
const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  orderNo: '',
  status: null,
  dateRange: null
})

// 状态映射 - 后台管理显示
// 0-待支付 1-已支付 2-配送中/待取货 3-已完成 4-已取消 5-退款中 6-已退款
const statusMap = {
  0: { name: '待支付', type: 'warning' },
  1: { name: '已支付', type: 'primary' },
  2: { name: '配送中', type: 'info' },
  3: { name: '已完成', type: 'success' },
  4: { name: '已取消', type: 'danger' },
  5: { name: '退款中', type: 'warning' },
  6: { name: '已退款', type: 'danger' }
}

const getStatusName = (status, deliveryType) => {
  // 自提订单的配送中状态显示"待取货"
  if (deliveryType === 1 && status === 2) {
    return '待取货'
  }
  return statusMap[status]?.name || '未知'
}

const getStatusType = (status, deliveryType) => {
  return statusMap[status]?.type || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (searchForm.dateRange) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    delete params.dateRange

    const res = await getOrderList(params)
    tableData.value = res.records || res
    total.value = res.total || tableData.value.length
  } catch (error) {
    console.error('加载订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, { orderNo: '', status: null, dateRange: null })
  pageNum.value = 1
  loadData()
}

const handleDetail = async (row) => {
  try {
    currentOrder.value = await getOrderDetail(row.id)
    drawerVisible.value = true
  } catch (error) {
    console.error('获取订单详情失败:', error)
  }
}

const handleShip = () => {
  const actionText = currentOrder.value.deliveryType === 1 ? '备货完成' : '发货'
  ElMessageBox.confirm(`确定要${actionText}吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await updateOrderStatus(currentOrder.value.id, 2)
        ElMessage.success(`${actionText}成功`)
        drawerVisible.value = false
        loadData()
      } catch (error) {
        console.error(`${actionText}失败:`, error)
      }
    })
    .catch(() => {})
}

const handleRefund = () => {
  ElMessageBox.confirm('确定同意退款吗？退款金额将原路返回，库存将自动恢复。', '退款确认', { type: 'warning' })
    .then(async () => {
      try {
        await updateOrderStatus(currentOrder.value.id, 6)
        ElMessage.success('退款成功')
        drawerVisible.value = false
        loadData()
      } catch (error) {
        console.error('退款失败:', error)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-page {
  padding: 0;
}

.page-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.search-form {
  margin-bottom: 20px;
}

/* 表格样式 */
.custom-table {
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
}

.custom-table :deep(.el-table__body-wrapper),
.custom-table :deep(.el-table__header-wrapper) {
  width: 100% !important;
}

.custom-table :deep(.el-table__body),
.custom-table :deep(.el-table__header) {
  width: 100% !important;
}

:deep(.el-table th) {
  background-color: #f5f7fa !important;
  font-weight: 600;
  color: #606266;
}

:deep(.el-table td),
:deep(.el-table th) {
  text-align: center;
}

.num-text {
  font-weight: 500;
  color: #606266;
}

.price-text {
  color: #f56c6c;
  font-weight: bold;
}

.status-tag {
  border-radius: 20px;
}

.time-text {
  color: #909399;
  font-size: 13px;
}

.detail-btn {
  border-radius: 6px;
  padding: 5px 12px;
  font-weight: 500;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

.detail-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4);
}

.order-detail {
  padding: 0 20px;
}

.goods-list {
  margin-top: 20px;
}

.goods-list h4 {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.goods-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.goods-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  margin-right: 12px;
}

.goods-info {
  flex: 1;
}

.goods-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.goods-price {
  font-size: 12px;
  color: #999;
}

.goods-total {
  font-size: 14px;
  color: #f56c6c;
  font-weight: bold;
}

.order-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  text-align: center;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

/* 增强下拉框可见性 */
:deep(.el-select .el-input__wrapper) {
  border: 2px solid #dcdfe6 !important;
  border-radius: 6px;
}

:deep(.el-select .el-input__wrapper:hover) {
  border-color: #409eff !important;
}

:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  border-color: #409eff !important;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2) !important;
}

/* 增强订单状态标签可见性 */
:deep(.el-tag--warning) {
  background-color: #fff8e1;
  border: 2px solid #ff9800;
  color: #e65100;
  font-weight: 600;
}

:deep(.el-tag--primary) {
  background-color: #e3f2fd;
  border: 2px solid #2196f3;
  color: #1565c0;
  font-weight: 600;
}

:deep(.el-tag--success) {
  background-color: #e8f5e9;
  border: 2px solid #4caf50;
  color: #2e7d32;
  font-weight: 600;
}

:deep(.el-tag--danger) {
  background-color: #ffebee;
  border: 2px solid #f44336;
  color: #c62828;
  font-weight: 600;
}

:deep(.el-tag--info) {
  background-color: #f5f5f5;
  border: 2px solid #9e9e9e;
  color: #616161;
  font-weight: 600;
}
</style>
