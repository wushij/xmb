<template>
  <div class="goods-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">商品列表</span>
          <el-button type="primary" class="add-btn" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增商品
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable style="width: 130px">
            <el-option 
              v-for="item in categoryList" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 110px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        class="custom-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="image" label="封面" width="100" align="center">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.image)" 
              class="goods-image"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="90" align="center">
          <template #default="{ row }">
            <span class="category-text">{{ row.categoryName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nowPrice" label="现价" width="80" align="center">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.nowPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="oldPrice" label="原价" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.oldPrice" class="old-price-text">¥{{ row.oldPrice }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="60" align="center">
          <template #default="{ row }">
            <span :class="row.stock < 5 ? 'low-stock' : ''">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" class="status-tag">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="100" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" size="small" class="action-btn edit-btn" @click="handleEdit(row)">编辑</el-button>
              <el-button 
                :type="row.status === 1 ? 'warning' : 'success'" 
                size="small"
                class="action-btn"
                @click="handleStatusChange(row)"
              >
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-button type="danger" size="small" class="action-btn delete-btn" @click="handleDelete(row)">删除</el-button>
            </div>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option 
              v-for="item in categoryList" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="现价" prop="nowPrice">
              <el-input-number v-model="form.nowPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价">
              <el-input-number v-model="form.oldPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
          >
            <el-image v-if="form.image" :src="getImageUrl(form.image)" class="avatar" fit="cover">
              <template #error>
                <div class="avatar-uploader-icon">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getGoodsList, 
  createGoods, 
  updateGoods, 
  deleteGoods, 
  batchDeleteGoods,
  updateGoodsStatus,
  getCategoryList,
  uploadFile 
} from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const categoryList = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

// 图片URL处理
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // 后端静态资源路径
  return 'https://wushij.online' + path
}

// 日期格式化
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  name: '',
  categoryId: null,
  status: null
})

const form = reactive({
  id: null,
  name: '',
  categoryId: null,
  nowPrice: 0,
  oldPrice: null,
  stock: 0,
  image: '',
  status: 1,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  nowPrice: [{ required: true, message: '请输入现价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const loadCategory = async () => {
  try {
    categoryList.value = await getCategoryList()
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getGoodsList({ 
      ...searchForm, 
      pageNum: pageNum.value, 
      pageSize: pageSize.value 
    })
    tableData.value = res.records || res
    total.value = res.total || tableData.value.length
  } catch (error) {
    console.error('加载商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, { name: '', categoryId: null, status: null })
  pageNum.value = 1
  loadData()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { 
    id: null, 
    name: '', 
    categoryId: null, 
    nowPrice: 0, 
    oldPrice: null, 
    stock: 0, 
    image: '', 
    status: 1, 
    description: '' 
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleUpload = async ({ file }) => {
  try {
    const url = await uploadFile(file)
    form.image = url
    ElMessage.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateGoods(form)
      ElMessage.success('修改成功')
    } else {
      await createGoods(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  
  try {
    await updateGoodsStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteGoods(row.id)
        ElMessage.success('删除成功')
        loadData()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {})
}

const handleBatchDelete = () => {
  const ids = selectedRows.value.map(row => row.id)
  ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 个商品吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await batchDeleteGoods(ids)
        ElMessage.success('删除成功')
        loadData()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadCategory()
  loadData()
})
</script>

<style scoped>
.goods-page {
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

.add-btn {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
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

.goods-image {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  overflow: hidden;
}

.goods-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.image-placeholder {
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  color: #909399;
  font-size: 24px;
  border-radius: 6px;
}

.category-text {
  color: #606266;
}

.price-text {
  color: #f56c6c;
  font-weight: 600;
}

.old-price-text {
  text-decoration: line-through;
  color: #999;
}

.low-stock {
  color: #f56c6c;
  font-weight: 600;
}

.status-tag {
  border-radius: 20px;
}

.time-text {
  color: #909399;
  font-size: 12px;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: nowrap;
}

.action-btn {
  border-radius: 4px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.edit-btn {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
}

.edit-btn:hover {
  transform: translateY(-1px);
}

.action-btn--warning {
  background: linear-gradient(135deg, #E6A23C 0%, #f0c78a 100%);
  border: none;
}

.action-btn--success {
  background: linear-gradient(135deg, #67C23A 0%, #95d475 100%);
  border: none;
}

.delete-btn {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  border: none;
}

.delete-btn:hover {
  transform: translateY(-1px);
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

/* 增强状态标签可见性 */
:deep(.el-tag--success) {
  background-color: #e8f5e9;
  border: 2px solid #4caf50;
  color: #2e7d32;
  font-weight: 600;
}

:deep(.el-tag--info) {
  background-color: #f5f5f5;
  border: 2px solid #9e9e9e;
  color: #616161;
  font-weight: 600;
}

/* 弹窗上传组件 */
.avatar-uploader {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.avatar-uploader:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 6px;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>
