<template>
  <div class="config-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><Setting /></el-icon>
          <span class="header-title">配送配置</span>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" v-loading="loading" label-position="top">
        <el-row :gutter="40">
          <el-col :span="8">
            <el-form-item label="起送价格" prop="minOrderAmount">
              <div class="input-wrapper">
                <el-input-number 
                  v-model="form.minOrderAmount" 
                  :precision="2" 
                  :min="0" 
                  :step="1"
                  :controls="true"
                  class="config-input"
                />
                <span class="input-unit">元</span>
              </div>
              <div class="input-desc">低于此金额无法下单</div>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="配送费" prop="deliveryFee">
              <div class="input-wrapper">
                <el-input-number 
                  v-model="form.deliveryFee" 
                  :precision="2" 
                  :min="0" 
                  :step="1"
                  :controls="true"
                  class="config-input"
                />
                <span class="input-unit">元</span>
              </div>
              <div class="input-desc">未满门槛时收取</div>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="满免配送费门槛" prop="freeDeliveryThreshold">
              <div class="input-wrapper">
                <el-input-number 
                  v-model="form.freeDeliveryThreshold" 
                  :precision="2" 
                  :min="0" 
                  :step="1"
                  :controls="true"
                  class="config-input"
                />
                <span class="input-unit">元</span>
              </div>
              <div class="input-desc">达到此金额免配送费</div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="40" style="margin-top: 20px;">
          <el-col :span="24">
            <el-form-item label="自提门店地址" prop="pickupAddress">
              <el-input 
                v-model="form.pickupAddress" 
                type="textarea"
                :rows="3"
                placeholder="请输入自提门店的详细地址"
                maxlength="200"
                show-word-limit
              />
              <div class="input-desc">用户选择自提时显示的取货地址</div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item style="margin-top: 20px;">
          <el-button type="primary" @click="handleSave" :loading="saving" size="large">
            <el-icon><Check /></el-icon>
            <span style="margin-left: 6px;">保存配置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 配置预览卡片 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover" class="preview-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><View /></el-icon>
              <span class="header-title">配置预览</span>
            </div>
          </template>
          <div class="preview-list">
            <div class="preview-item">
              <span class="preview-label">起送价格</span>
              <span class="preview-value">¥{{ form.minOrderAmount }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">配送费</span>
              <span class="preview-value">¥{{ form.deliveryFee }}</span>
            </div>
            <div class="preview-item">
              <span class="preview-label">满免门槛</span>
              <span class="preview-value">¥{{ form.freeDeliveryThreshold }}</span>
            </div>
            <div class="preview-item full-width">
              <span class="preview-label">自提地址</span>
              <span class="preview-value address">{{ form.pickupAddress || '未设置' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover" class="rule-card">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><InfoFilled /></el-icon>
              <span class="header-title">规则说明</span>
            </div>
          </template>
          <div class="rule-list">
            <div class="rule-item">
              <el-icon class="rule-icon warn"><CircleCloseFilled /></el-icon>
              <span>商品金额 <b class="text-red">低于 ¥{{ form.minOrderAmount }}</b> 时，无法下单</span>
            </div>
            <div class="rule-item">
              <el-icon class="rule-icon warn"><WarningFilled /></el-icon>
              <span>商品金额 <b class="text-red">低于 ¥{{ form.freeDeliveryThreshold }}</b> 时，需支付配送费 ¥{{ form.deliveryFee }}</span>
            </div>
            <div class="rule-item">
              <el-icon class="rule-icon success"><CircleCheckFilled /></el-icon>
              <span>商品金额 <b class="text-green">达到 ¥{{ form.freeDeliveryThreshold }}</b> 时，免配送费</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Check, View, InfoFilled, CircleCloseFilled, WarningFilled, CircleCheckFilled } from '@element-plus/icons-vue'
import { getDeliveryConfig, updateDeliveryConfig } from '@/api'

const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)

const form = ref({
  minOrderAmount: 12,
  deliveryFee: 3,
  freeDeliveryThreshold: 19.9,
  pickupAddress: '小卖部门店(具体地址待补充)'
})

const rules = {
  minOrderAmount: [
    { required: true, message: '请输入起送价格', trigger: 'blur' }
  ],
  deliveryFee: [
    { required: true, message: '请输入配送费', trigger: 'blur' }
  ],
  freeDeliveryThreshold: [
    { required: true, message: '请输入满免配送费门槛', trigger: 'blur' }
  ]
}

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await getDeliveryConfig()
    if (res) {
      form.value.minOrderAmount = res.minOrderAmount || 12
      form.value.deliveryFee = res.deliveryFee || 3
      form.value.freeDeliveryThreshold = res.freeDeliveryThreshold || 19.9
      form.value.pickupAddress = res.pickupAddress || '小卖部门店(具体地址待补充)'
    }
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      await updateDeliveryConfig(form.value)
      ElMessage.success('保存成功')
    } catch (error) {
      console.error('保存配置失败:', error)
      ElMessage.error('保存配置失败')
    } finally {
      saving.value = false
    }
  })
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.config-page {
  max-width: 1000px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
  color: #409EFF;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-input {
  flex: 1;
}

.config-input :deep(.el-input__inner) {
  text-align: center;
  font-weight: 600;
}

.input-unit {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  min-width: 20px;
}

.input-desc {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  text-align: left;
}

/* 预览卡片 */
.preview-card {
  height: 100%;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 8px;
}

.preview-label {
  font-size: 14px;
  color: #606266;
}

.preview-value {
  font-size: 20px;
  font-weight: 700;
  color: #409EFF;
}

.preview-item.full-width {
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.preview-value.address {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  word-break: break-all;
  line-height: 1.6;
}

/* 规则卡片 */
.rule-card {
  height: 100%;
}

.rule-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  line-height: 1.6;
  font-size: 13px;
  color: #606266;
}

.rule-icon {
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 2px;
}

.rule-icon.warn {
  color: #E6A23C;
}

.rule-icon.success {
  color: #67C23A;
}

.text-red {
  color: #F56C6C;
}

.text-green {
  color: #67C23A;
}

/* Element Plus 样式覆盖 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
  padding-bottom: 8px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-card__body) {
  padding: 24px;
}
</style>
