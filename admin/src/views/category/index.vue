<template>
  <div class="category-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">分类列表</span>
          <el-button type="primary" class="add-btn" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增分类
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="100">
          <template #default="{ row }">
            <span class="name-text">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="70" align="center">
          <template #default="{ row }">
            <div class="icon-cell">
              <el-image 
                v-if="row.icon" 
                :src="getImageUrl(row.icon)" 
                class="category-icon"
                fit="contain"
              >
                <template #error>
                  <div class="icon-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="icon-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="60" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small" effect="plain">{{ row.sort }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.status === 1 ? 'success' : 'danger'" 
              :effect="row.status === 1 ? 'light' : 'plain'"
              class="status-tag"
            >
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="140" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" size="small" class="action-btn edit-btn" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" size="small" class="action-btn delete-btn" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类图标">
          <el-upload
            class="icon-uploader"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
          >
            <el-image v-if="form.icon" :src="getImageUrl(form.icon)" class="icon-preview" fit="contain" />
            <div v-else class="icon-upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>点击上传</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { Edit, Delete, Check, Close } from '@element-plus/icons-vue'
import { getCategoryList, createCategory, updateCategory, deleteCategory, uploadFile } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
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

// 时间格式化
const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

// 上传前校验
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

// 处理上传
const handleUpload = async ({ file }) => {
  try {
    const url = await uploadFile(file)
    form.icon = url
    ElMessage.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
  }
}

const form = reactive({
  id: null,
  name: '',
  icon: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getCategoryList()
    // 按sort字段升序排序
    tableData.value = data.sort((a, b) => (a.sort || 0) - (b.sort || 0))
  } catch (error) {
    console.error('加载分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', icon: '', sort: 0, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCategory(form)
      ElMessage.success('修改成功')
    } else {
      await createCategory(form)
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

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该分类吗？删除后不可恢复', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.category-page {
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

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}

:deep(.el-table__row:hover > td) {
  background-color: #ecf5ff !important;
}

.name-text {
  font-weight: 500;
  color: #303133;
}

/* 图标单元格 */
.icon-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.category-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  background: #f5f7fa;
  padding: 4px;
}

.icon-placeholder {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  color: #909399;
  font-size: 20px;
  border-radius: 8px;
}

/* 状态标签 */
.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.status-tag .el-icon {
  margin-right: 2px;
  vertical-align: middle;
}

.time-text {
  color: #909399;
  font-size: 13px;
}

/* 操作按钮 */
.action-btns {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  border-radius: 6px;
  padding: 5px 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.edit-btn {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

.edit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4);
}

.delete-btn {
  background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
  border: none;
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3);
}

.delete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(245, 108, 108, 0.4);
}

/* 弹窗样式 */
.custom-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 16px 20px;
  margin: 0;
}

.custom-dialog :deep(.el-dialog__title) {
  font-weight: 600;
}

/* 图标上传 */
.icon-uploader {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  width: 88px;
  height: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.icon-uploader:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}

.icon-preview {
  width: 88px;
  height: 88px;
  border-radius: 6px;
}

.icon-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  font-size: 14px;
}

.icon-upload-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 6px;
}
</style>
