const app = getApp();

Page({
  data: {
    refundedList: []
  },

  onShow() {
    this.getRefundedlist();
  },

  // 从后端获取退款中+已退款订单
  getRefundedlist() {
    if (!app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      wx.navigateBack();
      return;
    }

    // status=5 退款中，status=6 已退款
    app.request({ url: '/order/page', data: { pageNum: 1, pageSize: 50, status: 5 } }).then(res5 => {
      app.request({ url: '/order/page', data: { pageNum: 1, pageSize: 50, status: 6 } }).then(res6 => {
        const allOrders = (res5.records || []).concat(res6.records || []);
        const refundedList = allOrders.map(item => {
          let createTime = item.createTime || '';
          if (createTime) {
            createTime = createTime.replace('T', ' ').substring(0, 19);
          }
          
          // 根据实际status显示状态
          const statusText = item.status === 5 ? '退款中' : '已退款';
          
          return {
            orderId: item.id,
            orderNo: item.orderNo,
            totalPrice: item.totalPrice,
            goodsPrice: item.goodsPrice || item.totalPrice,
            deliveryFee: item.deliveryFee !== undefined ? item.deliveryFee : 3,
            isFreeDelivery: (item.goodsPrice || item.totalPrice) >= 19.9,
            status: statusText,
            createTime: createTime,
            goodsList: item.items ? item.items.map(goods => ({
              goodsId: goods.goodsId,
              name: goods.goodsName,
              imgUrl: app.getImageUrl(goods.goodsImage),
              nowPrice: goods.goodsPrice,
              num: goods.num
            })) : []
          };
        });
        this.setData({ refundedList });
      });
    }).catch(err => {
      console.error('获取退款订单失败:', err);
    });
  },

  // 查看退款详情
  viewRefundDetail(e) {
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
