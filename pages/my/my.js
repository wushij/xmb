const app = getApp();

Page({
  data: {
    isLogin: false,
    userName: "",
    userPhone: "",
    avatarUrl: "/images/my/头像.png",
    waitPayNum: 0,
    waitDeliveryNum: 0,
    refundingNum: 0,
    refundedNum: 0,
    allOrderNum: 0
  },

  onShow() {
    this.checkLogin();
  },

  onLoad(options) {
    this.setData({
      isLogin: false,
      userName: "",
      userPhone: "",
      waitPayNum: 0,
      waitDeliveryNum: 0,
      refundingNum: 0,
      refundedNum: 0,
      allOrderNum: 0
    });
  },

  // 检查登录状态
  checkLogin() {
    const userInfo = wx.getStorageSync('userInfo') || {};
    if (userInfo.nickName && userInfo.avatarUrl && app.globalData.token) {
      this.setData({
        isLogin: true,
        userName: userInfo.nickName,
        avatarUrl: userInfo.avatarUrl,
        userPhone: userInfo.phone || ""
      });
      this.calcOrderNum();
    } else {
      this.setData({
        isLogin: false,
        userName: "",
        userPhone: "",
        avatarUrl: "/images/my/头像.png",
        waitPayNum: 0,
        waitDeliveryNum: 0,
        refundingNum: 0,
        refundedNum: 0,
        allOrderNum: 0
      });
    }
  },

  // 计算订单数量 - 新状态：0-待支付 1-已支付 2-配送中 3-已完成 4-已取消 5-退款中 6-已退款
  calcOrderNum() {
    app.request({ url: '/order/count' }).then(res => {
      this.setData({
        waitPayNum: res[0],           // 待支付
        waitDeliveryNum: res[1],  // 已支付 = 待发货
        refundingNum: res[5],          // 退款中
        refundedNum: res[6],           // 已退款
        allOrderNum: res.reduce((a, b) => a + b, 0)
      });
    }).catch(() => {});
  },

  goToWaitPay() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      this.toLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/order/waitPay/waitPay' });
  },
  
  goToWaitDelivery() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      this.toLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/order/waitDelivery/waitDelivery' });
  },
  
  goToRefunded() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      this.toLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/order/refunded/refunded' });
  },
  
  goToAllOrder() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      this.toLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/order/allOrder/allOrder' });
  },
  
  goToProfile() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      this.toLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/profile/profile' });
  },
  
  goToAddress() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      this.toLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/address/address' });
  },
  
  goToAbout() {
    wx.navigateTo({ url: '/pages/about/about' });
  },
  
  goToSettings() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      this.toLogin();
      return;
    }
    wx.navigateTo({ url: '/pages/settings/settings' });
  },

  toLogin() {
    if (this.data.isLogin) {
      wx.navigateTo({ url: '/pages/profile/profile' });
    } else {
      wx.navigateTo({ url: '/pages/login/login' });
    }
  },

  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.globalData.token = '';
          wx.removeStorageSync('userInfo');
          this.setData({
            isLogin: false,
            userName: "",
            userPhone: "",
            avatarUrl: "/images/my/头像.png",
            waitPayNum: 0,
            waitDeliveryNum: 0,
            refundingNum: 0,
            refundedNum: 0,
            allOrderNum: 0
          });
          wx.showToast({ title: '退出成功', icon: 'success' });
        }
      }
    });
  }
});
