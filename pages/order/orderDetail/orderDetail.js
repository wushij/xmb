const app = getApp();

Page({
  data: {
    order: null,
    loading: true
  },

  onLoad(options) {
    const orderId = options.orderId;
    if (orderId) {
      this.getOrderDetail(orderId);
    } else {
      wx.showToast({ title: '订单ID不存在', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
    }
  },

  getOrderDetail(orderId) {
    app.request({ url: `/order/${orderId}` }).then(res => {
      // 格式化时间
      let createTime = res.createTime || '';
      let payTime = res.payTime || '';
      let deliveryTime = res.deliveryTime || '';
      let receiveTime = res.receiveTime || '';
      
      if (createTime) createTime = createTime.replace('T', ' ').substring(0, 19);
      if (payTime) payTime = payTime.replace('T', ' ').substring(0, 19);
      if (deliveryTime) deliveryTime = deliveryTime.replace('T', ' ').substring(0, 19);
      if (receiveTime) receiveTime = receiveTime.replace('T', ' ').substring(0, 19);

      // 状态逻辑：0-待支付 1-已支付(商家备货中) 2-配送中/待取货 3-已完成 4-已取消 5-退款中 6-已退款
      const deliveryType = res.deliveryType || 0;
      let statusName = '';
      
      switch (res.status) {
        case 0: statusName = '待支付'; break;
        case 1: statusName = '商家备货中'; break;
        case 2: statusName = deliveryType === 1 ? '待取货' : '配送中'; break;
        case 3: statusName = '已完成'; break;
        case 4: statusName = '已取消'; break;
        case 5: statusName = '退款中'; break;
        case 6: statusName = '已退款'; break;
        default: statusName = res.statusName || '';
      }

      const order = {
        id: res.id,
        orderNo: res.orderNo,
        status: statusName,
        statusNum: res.status,
        totalPrice: res.totalPrice,
        deliveryFee: res.deliveryFee || 0,
        goodsPrice: res.goodsPrice || res.totalPrice,
        remark: res.remark || '无',
        receiverName: res.receiverName,
        receiverPhone: res.receiverPhone,
        address: res.address,
        deliveryType: deliveryType,
        pickupAddress: res.pickupAddress || '小卖部门店(具体地址待补充)',
        createTime,
        payTime,
        deliveryTime,
        receiveTime,
        goodsList: res.items ? res.items.map(item => ({
          goodsId: item.goodsId,
          name: item.goodsName,
          imgUrl: app.getImageUrl(item.goodsImage),
          price: item.goodsPrice,
          num: item.num,
          totalPrice: item.totalPrice
        })) : []
      };

      this.setData({ order, loading: false });
    }).catch(err => {
      console.error('获取订单详情失败:', err);
      wx.showToast({ title: '获取订单详情失败', icon: 'none' });
      this.setData({ loading: false });
    });
  },

  // 取消订单
  cancelOrder() {
    const orderId = this.data.order.id;

    wx.showModal({
      title: '确认取消',
      content: '是否确定取消该订单？',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: `/order/cancel/${orderId}`,
            method: 'PUT'
          }).then(() => {
            wx.showToast({ title: '取消成功', icon: 'success' });
            setTimeout(() => wx.navigateBack(), 1500);
          }).catch(err => {
            console.error('取消订单失败:', err);
          });
        }
      }
    });
  },

  // 立即付款
  payOrder() {
    const orderId = this.data.order.id;
    const price = this.data.order.totalPrice;

    wx.showModal({
      title: '付款提示',
      content: `应付金额：¥${price}，确认支付？`,
      confirmText: '确认支付',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: `/order/pay/${orderId}`,
            method: 'PUT'
          }).then(() => {
            wx.showToast({ title: '支付成功', icon: 'success' });
            setTimeout(() => {
              wx.redirectTo({
                url: '/pages/order/waitDelivery/waitDelivery'
              });
            }, 1500);
          }).catch(err => {
            console.error('支付失败:', err);
          });
        }
      }
    });
  },

  // 申请退款
  applyRefund() {
    const orderId = this.data.order.id;
    const price = this.data.order.totalPrice;

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
            setTimeout(() => wx.navigateBack(), 1500);
          }).catch(err => {
            console.error('退款失败:', err);
          });
        }
      }
    });
  },

  // 确认收货
  confirmReceive() {
    const orderId = this.data.order.id;

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
            setTimeout(() => wx.navigateBack(), 1500);
          }).catch(err => {
            console.error('确认收货失败:', err);
          });
        }
      }
    });
  },

  // 复制订单编号
  copyOrderNo() {
    wx.setClipboardData({
      data: this.data.order.orderNo,
      success: () => {
        wx.showToast({ title: '已复制订单编号', icon: 'success' });
      }
    });
  },

  // 联系客服
  contactService() {
    wx.showModal({
      title: '联系客服',
      content: '如有问题请拨打客服电话：400-123-4567',
      showCancel: false
    });
  }
});
