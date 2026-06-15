const app = getApp();

Page({
  data: {
    orderList: []
  },

  onShow() {
    if (!app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      wx.navigateBack();
      return;
    }
    this.getOrderList();
  },

  getOrderList() {
    app.request({ url: '/order/page', data: { pageNum: 1, pageSize: 50 } }).then(res => {
      const orderList = res.records.map(item => {
        let createTime = item.createTime || '';
        if (createTime) {
          createTime = createTime.replace('T', ' ').substring(0, 19);
        }
        
        // 计算是否免配送费（商品金额>=19.9）
        const goodsPrice = item.goodsPrice || item.totalPrice;
        const isFreeDelivery = goodsPrice >= 19.9;
        
        // 状态逻辑：0-待支付 1-已支付(商家备货中) 2-配送中/待取货 3-已完成 4-已取消 5-退款中 6-已退款
        const deliveryType = item.deliveryType || 0;
        let statusName = '';
        
        // 根据状态码和配送方式设置状态文案
        switch (item.status) {
          case 0: statusName = '待支付'; break;
          case 1: statusName = '商家备货中'; break;
          case 2: statusName = deliveryType === 1 ? '待取货' : '配送中'; break;
          case 3: statusName = '已完成'; break;
          case 4: statusName = '已取消'; break;
          case 5: statusName = '退款中'; break;
          case 6: statusName = '已退款'; break;
          default: statusName = item.statusName || '';
        }
        
        return {
          orderId: item.id,
          orderNo: item.orderNo,
          totalPrice: item.totalPrice,
          goodsPrice: goodsPrice,
          deliveryFee: item.deliveryFee !== undefined ? item.deliveryFee : (isFreeDelivery ? 0 : 3),
          isFreeDelivery: isFreeDelivery,
          status: statusName,
          statusNum: item.status,
          deliveryType: deliveryType,
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
      this.setData({ orderList });
    }).catch(err => {
      console.error('获取订单失败:', err);
    });
  },

  payOrder(e) {
    const orderId = e.currentTarget.dataset.orderid;
    const price = e.currentTarget.dataset.price;

    wx.showModal({
      title: '付款提示',
      content: `订单应付金额：¥${price}，确认支付？`,
      confirmText: '确认支付',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: `/order/pay/${orderId}`,
            method: 'PUT'
          }).then(() => {
            wx.showToast({ title: '支付成功', icon: 'success' });
            this.getOrderList();
          }).catch(err => {
            console.error('支付失败:', err);
          });
        }
      }
    });
  },

  cancelOrder(e) {
    const orderId = e.currentTarget.dataset.orderid;

    wx.showModal({
      title: '确认取消',
      content: '是否确定取消该订单？',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: `/order/cancel/${orderId}`,
            method: 'PUT'
          }).then(() => {
            wx.showToast({ title: '订单已取消', icon: 'success' });
            this.getOrderList();
          }).catch(err => {
            console.error('取消失败:', err);
          });
        }
      }
    });
  },

  applyRefund(e) {
    const orderId = e.currentTarget.dataset.orderid;
    const price = e.currentTarget.dataset.price;

    wx.showModal({
      title: '申请退款',
      content: `退款金额：¥${price}，是否确认退款？`,
      confirmText: '确认退款',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: `/order/refund/${orderId}`,
            method: 'PUT'
          }).then(() => {
            wx.showToast({ title: '退款申请已提交', icon: 'success' });
            this.getOrderList();
          }).catch(err => {
            console.error('退款失败:', err);
          });
        }
      }
    });
  },

  viewDetail(e) {
    const orderId = e.currentTarget.dataset.orderid;
    wx.navigateTo({
      url: `/pages/order/orderDetail/orderDetail?orderId=${orderId}`
    });
  },

  // 确认收货
  confirmReceive(e) {
    const orderId = e.currentTarget.dataset.orderid;

    wx.showModal({
      title: '确认收货',
      content: '是否确认已收到商品？',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: `/order/receive/${orderId}`,
            method: 'PUT'
          }).then(() => {
            wx.showToast({ title: '已确认收货', icon: 'success' });
            this.getOrderList();
          }).catch(err => {
            console.error('确认收货失败:', err);
          });
        }
      }
    });
  },

  // 根据状态返回颜色
  getStatusColor(status) {
    const colorMap = {
      '待支付': '#e63946',
      '商家备货中': '#ff9500',
      '配送中': '#1890ff',
      '待取货': '#ff9500',
      '已完成': '#52c41a',
      '已取消': '#999',
      '退款中': '#fa8c16',
      '已退款': '#999'
    };
    return colorMap[status] || '#666';
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
