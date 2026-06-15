const app = getApp();

Page({
  data: {
    orderList: []
  },

  onShow() {
    this.getOrderList();
  },

  // 从后端获取待发货订单
  getOrderList() {
    if (!app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      wx.navigateBack();
      return;
    }

    // status=1 表示待发货
    app.request({ url: '/order/page', data: { pageNum: 1, pageSize: 20, status: 1 } }).then(res => {
      const orderList = res.records.map(item => {
        let createTime = item.createTime || '';
        let payTime = item.payTime || '';
        if (createTime) {
          createTime = createTime.replace('T', ' ').substring(0, 19);
        }
        if (payTime) {
          payTime = payTime.replace('T', ' ').substring(0, 19);
        }
        
        // status=1 统一显示商家备货中
        const statusText = '商家备货中';
        const deliveryType = item.deliveryType || 0;
        
        return {
          orderId: item.id,
          orderNo: item.orderNo,
          totalPrice: item.totalPrice,
          goodsPrice: item.goodsPrice || item.totalPrice,
          deliveryFee: item.deliveryFee !== undefined ? item.deliveryFee : 3,
          isFreeDelivery: (item.goodsPrice || item.totalPrice) >= 19.9,
          status: statusText,
          deliveryType: deliveryType,
          createTime: createTime,
          payTime: payTime,
          goodsList: item.items ? item.items.map(goods => ({
            goodsId: goods.goodsId,
            name: goods.goodsName,
            imgUrl: app.getImageUrl(goods.goodsImage),
            nowPrice: goods.goodsPrice,
            num: goods.num
          })) : []
        };
      });
      this.setData({ orderList });
    }).catch(err => {
      console.error('获取待发货订单失败:', err);
    });
  },

  // 提醒发货
  remindDelivery(e) {
    const orderId = e.currentTarget.dataset.orderid;
    wx.showToast({ title: '已提醒商家发货', icon: 'success' });
  },

  // 查看订单详情
  viewDetail(e) {
    const orderId = e.currentTarget.dataset.orderid;
    wx.navigateTo({
      url: `/pages/order/orderDetail/orderDetail?orderId=${orderId}`
    });
  },

  // 跳转购物
  goShopping() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  onLoad(options) {},
  onReady() {}
});
