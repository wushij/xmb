const app = getApp();

Page({
  data: {
    cartList: [],
    totalPrice: 0,
    isLogin: false,
    currentOrder: null,  // 当前创建的订单
    showOrderBar: false,  // 是否显示订单操作栏
    // 配送配置
    config: {
      minOrderAmount: 12,
      deliveryFee: 3,
      freeDeliveryThreshold: 19.9
    },
    notEnoughAmount: '0.00'  // 距离起送价还差多少
  },

  onLoad() {
    this.loadDeliveryConfig();
  },

  onShow() {
    // 重置订单操作栏状态
    this.setData({ currentOrder: null, showOrderBar: false });
    this.checkLogin();
    if (this.data.isLogin) {
      this.getCartList();
    }
  },

  // 加载配送配置
  loadDeliveryConfig() {
    app.request({ url: '/config/delivery' }).then(res => {
      if (res) {
        this.setData({ config: res });
        this.updateNotEnoughAmount();
      }
    }).catch(err => {
      console.error('加载配送配置失败:', err);
    });
  },

  // 检查登录状态
  checkLogin() {
    const userInfo = wx.getStorageSync('userInfo');
    const isLogin = !!(userInfo && app.globalData.token);
    this.setData({ isLogin });
  },

  // 获取购物车列表
  getCartList() {
    app.request({ url: '/cart/list' }).then(res => {
      const cartList = res.map(item => ({
        id: item.id,
        goodsId: item.goodsId,
        name: item.goodsName,
        nowPrice: item.goodsPrice,
        imgUrl: app.getImageUrl(item.goodsImage),
        num: item.num,
        totalPrice: item.totalPrice,
        selected: item.selected,
        stock: item.stock
      }));
      
      const totalPrice = cartList.reduce((sum, item) => {
        if (item.selected) {
          return sum + Number(item.totalPrice || 0);
        }
        return sum;
      }, 0).toFixed(2);
      
      this.setData({ cartList, totalPrice });
      this.updateCartBadge(cartList.length);
      this.updateNotEnoughAmount();
    }).catch(err => {
      console.error('获取购物车失败:', err);
    });
  },

  // 更新距离起送价的差额
  updateNotEnoughAmount() {
    const totalPrice = Number(this.data.totalPrice);
    const minOrderAmount = Number(this.data.config.minOrderAmount);
    if (totalPrice < minOrderAmount) {
      this.setData({ 
        notEnoughAmount: (minOrderAmount - totalPrice).toFixed(2)
      });
    } else {
      this.setData({ notEnoughAmount: '0.00' });
    }
  },

  // 更新角标
  updateCartBadge(count) {
    if (count > 0) {
      wx.setTabBarBadge({ index: 2, text: String(count) });
    } else {
      wx.removeTabBarBadge({ index: 2 });
    }
  },

  // 减少数量
  minusNum(e) {
    const id = e.currentTarget.dataset.id;
    const item = this.data.cartList.find(i => i.id === id);
    if (!item) return;
    
    if (item.num <= 1) {
      wx.showToast({ title: '数量不能小于1', icon: 'none' });
      return;
    }
    
    const newNum = item.num - 1;
    app.request({
      url: `/cart/num/${id}?num=${newNum}`,
      method: 'PUT'
    }).then(() => {
      this.getCartList();
    }).catch(err => {
      console.error('更新数量失败:', err);
    });
  },

  // 增加数量
  plusNum(e) {
    const id = e.currentTarget.dataset.id;
    const item = this.data.cartList.find(i => i.id === id);
    if (!item) return;
    
    if (item.num >= item.stock) {
      wx.showToast({ title: '库存不足', icon: 'none' });
      return;
    }
    
    const newNum = item.num + 1;
    app.request({
      url: `/cart/num/${id}?num=${newNum}`,
      method: 'PUT'
    }).then(() => {
      this.getCartList();
    }).catch(err => {
      console.error('更新数量失败:', err);
    });
  },

  // 删除商品
  delGoods(e) {
    const id = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认删除',
      content: '确定要删除该商品吗？',
      confirmText: '删除',
      confirmColor: '#e63946',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: `/cart/${id}`,
            method: 'DELETE'
          }).then(() => {
            wx.showToast({ title: '删除成功', icon: 'success' });
            this.getCartList();
          }).catch(err => {
            console.error('删除失败:', err);
          });
        }
      }
    });
  },

  // 图片加载失败
  onImgError(e) {
    const index = e.currentTarget.dataset.index;
    const cartList = this.data.cartList;
    cartList[index].imgLoadError = true;
    this.setData({ cartList });
  },

  goShop() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  // 去结算 - 跳转到订单确认页面选择地址
  goPay() {
    if (!this.data.isLogin) {
      wx.showModal({
        title: '温馨提示',
        content: '您还未登录，登录后才能进行结算哦~',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/login/login' });
          }
        }
      });
      return;
    }

    if (this.data.cartList.length === 0) {
      wx.showToast({ title: '购物车为空', icon: 'none' });
      return;
    }

    // 校验起送价格
    const totalPrice = Number(this.data.totalPrice);
    const minOrderAmount = Number(this.data.config.minOrderAmount);
    if (totalPrice < minOrderAmount) {
      wx.showToast({ 
        title: `还差¥${this.data.notEnoughAmount}达到起送价`, 
        icon: 'none',
        duration: 2000
      });
      return;
    }

    // 跳转到订单确认页面，让用户选择地址
    wx.navigateTo({
      url: '/pages/order/createOrder/createOrder'
    });
  },

  // 立即支付
  payOrder() {
    const orderId = this.data.currentOrder.id;
    const price = this.data.currentOrder.totalPrice;

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
            this.setData({ currentOrder: null, showOrderBar: false });
          }).catch(err => {
            console.error('支付失败:', err);
          });
        }
      }
    });
  },

  // 取消支付 - 跳转到待付款页面
  cancelPay() {
    wx.showModal({
      title: '取消支付',
      content: '取消后订单将保存在待付款列表，可稍后支付',
      confirmText: '确认取消',
      success: (res) => {
        if (res.confirm) {
          wx.navigateTo({
            url: '/pages/order/waitPay/waitPay'
          });
        }
      }
    });
  }
});
