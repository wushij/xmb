import request from '@/utils/request'

// 登录
export const login = (data) => {
  return request.post('/admin/login', data)
}

// 获取统计数据
export const getStatistics = (params) => {
  return request.get('/admin/statistics', { params })
}

// 获取销售对比数据
export const getSalesCompare = (params) => {
  return request.get('/admin/sales-compare', { params })
}

// 获取订单趋势
export const getOrderTrend = () => {
  return request.get('/admin/order-trend')
}

// 获取热销商品
export const getHotGoods = () => {
  return request.get('/admin/hot-goods')
}

// 分类相关
export const getCategoryList = (params) => {
  return request.get('/admin/category/list', { params })
}

export const createCategory = (data) => {
  return request.post('/admin/category', data)
}

export const updateCategory = (data) => {
  return request.put('/admin/category', data)
}

export const deleteCategory = (id) => {
  return request.delete(`/admin/category/${id}`)
}

// 商品相关
export const getGoodsList = (params) => {
  return request.get('/admin/goods/list', { params })
}

export const createGoods = (data) => {
  return request.post('/admin/goods', data)
}

export const updateGoods = (data) => {
  return request.put('/admin/goods', data)
}

export const deleteGoods = (id) => {
  return request.delete(`/admin/goods/${id}`)
}

export const batchDeleteGoods = (ids) => {
  return request.post('/admin/goods/batch-delete', ids)
}

export const updateGoodsStatus = (id, status) => {
  return request.put(`/admin/goods/status/${id}`, { status })
}

// 订单相关
export const getOrderList = (params) => {
  return request.get('/admin/order/list', { params })
}

export const getOrderDetail = (id) => {
  return request.get(`/admin/order/${id}`)
}

export const updateOrderStatus = (id, status) => {
  return request.put(`/admin/order/status/${id}`, { status })
}

// 文件上传
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 配置相关
export const getDeliveryConfig = () => {
  return request.get('/admin/config/delivery')
}

export const updateDeliveryConfig = (data) => {
  return request.put('/admin/config/delivery', data)
}
