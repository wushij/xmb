const app = getApp();

Page({
  data: {
    orderList: [],
    countdownTimers: {} // 存储倒计时定时器
  },

  onShow() {
    this.getOrderList();
  },

  onUnload() {
    // 页面卸载时清除所有定时器
    this.clearAllTimers();
  },

  onHide() {
    // 页面隐藏时清除所有定时器
    this.clearAllTimers();
  },

  // 清除所有定时器
  clearAllTimers() {
    Object.values(this.data.countdownTimers).forEach(timer => {
      clearInterval(timer);
    });
    this.setData({ countdownTimers: {} });
  },

  // 从后端获取待付款订单
  getOrderList() {
    // 先清除旧的定时器
    this.clearAllTimers();

    if (!app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      wx.navigateBack();
      return;
    }

    app.request({ url: '/order/page', data: { pageNum: 1, pageSize: 20, status: 0 } }).then(res => {
      const orderList = res.records.map(item => {
        // 格式化时间
        let createTime = item.createTime || '';
        if (createTime) {
          createTime = createTime.replace('T', ' ').substring(0, 19);
        }

        return {
          orderId: item.id,
          orderNo: item.orderNo,
          totalPrice: item.totalPrice,
          goodsPrice: item.goodsPrice || item.totalPrice,
          deliveryFee: item.deliveryFee !== undefined ? item.deliveryFee : 3,
          isFreeDelivery: (item.goodsPrice || item.totalPrice) >= 19.9,
          status: '待付款',
          createTime: createTime,
          createTimeRaw: item.createTime, // 保留原始时间用于倒计时
          countdown: '', // 倒计时显示
          isTimeout: false, // 是否已超时
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

      // 启动倒计时
      orderList.forEach(order => {
        if (order.createTimeRaw) {
          this.startCountdown(order.orderId, order.createTimeRaw);
        }
      });
    }).catch(err => {
      console.error('获取订单失败:', err);
    });
  },

  // 启动倒计时
  startCountdown(orderId, createTime) {
    const TIMEOUT_MINUTES = 15; // 15分钟超时

    const updateCountdown = () => {
      const orderList = this.data.orderList;
      const order = orderList.find(o => o.orderId === orderId);
      if (!order) return;

      const createTimestamp = new Date(createTime.replace('T', ' ')).getTime();
      const nowTimestamp = Date.now();
      const elapsed = nowTimestamp - createTimestamp;
      const remaining = TIMEOUT_MINUTES * 60 * 1000 - elapsed;

      if (remaining <= 0) {
        // 已超时
        order.countdown = '已超时';
        order.isTimeout = true;
        this.setData({ orderList });
        clearInterval(this.data.countdownTimers[orderId]);
        return;
      }

      // 计算剩余时间
      const minutes = Math.floor(remaining / (60 * 1000));
      const seconds = Math.floor((remaining % (60 * 1000)) / 1000);
      order.countdown = `${minutes}分${seconds < 10 ? '0' : ''}${seconds}秒`;
      this.setData({ orderList });
    };

    // 立即执行一次
    updateCountdown();

    // 每秒更新
    const timer = setInterval(updateCountdown, 1000);
    this.setData({
      countdownTimers: {
        ...this.data.countdownTimers,
        [orderId]: timer
      }
    });
  },

  // 取消订单
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
            wx.showToast({ title: '取消成功', icon: 'success' });
            this.getOrderList();
          }).catch(err => {
            console.error('取消订单失败:', err);
          });
        }
      }
    });
  },

  // 立即付款
  payOrder(e) {
    const orderId = e.currentTarget.dataset.orderid;
    const price = e.currentTarget.dataset.price || '0.00';

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
            this.getOrderList();
          }).catch(err => {
            console.error('支付失败:', err);
          });
        }
      }
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
