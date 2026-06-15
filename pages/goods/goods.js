const app = getApp();

Page({
  data: {
    goodsInfo: {},
    imgLoadError: false,
    cartNum: 0
  },

  onLoad(options) {
    const goodsId = options.goodsId;
    if (!goodsId) {
      wx.showToast({ title: '商品不存在', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }
    this.loadGoodsInfo(goodsId);
  },

  onShow() {
    this.updateCartNum();
    // 如果已经加载了商品信息,刷新它以获取最新库存
    if (this.data.goodsInfo.goodsId) {
      this.loadGoodsInfo(this.data.goodsInfo.goodsId);
    }
  },

  loadGoodsInfo(goodsId) {
    app.request({ url: `/goods/${goodsId}` }).then(res => {
      const goodsInfo = {
        goodsId: res.id,
        name: res.name,
        nowPrice: res.nowPrice,
        oldPrice: res.oldPrice,
        discount: res.discount,
        stock: res.stock,
        categoryId: res.categoryId,
        imgUrl: app.getImageUrl(res.image),
        desc: res.description
      };
      this.setData({ goodsInfo });
    }).catch(err => {
      wx.showToast({ title: '商品不存在', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
    });
  },

  onImgError() {
    this.setData({ imgLoadError: true });
  },

  updateCartNum() {
    if (!app.globalData.token) {
      this.setData({ cartNum: 0 });
      wx.removeTabBarBadge({ index: 2 });
      return;
    }
    app.request({ url: '/cart/count' }).then(count => {
      const num = count || 0;
      this.setData({ cartNum: num });
      // 同时更新tabbar角标
      if (num > 0) {
        wx.setTabBarBadge({ 
          index: 2, 
          text: String(num > 99 ? '99+' : num) 
        });
      } else {
        wx.removeTabBarBadge({ index: 2 });
      }
    }).catch(() => {});
  },

  addToCart() {
    const goods = this.data.goodsInfo;
    if (!goods || !goods.goodsId) {
      wx.showToast({ title: '商品信息异常', icon: 'none' });
      return;
    }

    // 检查库存
    if (!goods.stock || goods.stock <= 0) {
      wx.showToast({ title: '商品已售罄', icon: 'none' });
      return;
    }

    if (!app.globalData.token) {
      wx.showModal({
        title: '提示',
        content: '请先登录后再加入购物车',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/login/login' });
          }
        }
      });
      return;
    }

    app.request({
      url: '/cart/add',
      method: 'POST',
      data: { goodsId: goods.goodsId, num: 1 }
    }).then(() => {
      wx.showToast({ title: '加入购物车成功', icon: 'success' });
      this.updateCartNum();
      // 刷新商品信息,更新库存显示
      this.loadGoodsInfo(goods.goodsId);
    }).catch(err => {
      console.error('加入购物车失败:', err);
      if (err && err.message) {
        wx.showToast({ title: err.message, icon: 'none' });
      }
    });
  },

  buyNow() {
    this.addToCart();
    setTimeout(() => {
      wx.switchTab({ url: '/pages/cart/cart' });
    }, 1000);
  },

  goCart() {
    wx.switchTab({ url: '/pages/cart/cart' });
  },

  onShareAppMessage() {
    return {
      title: this.data.goodsInfo.name || '好物推荐',
      path: `/pages/goods/goods?goodsId=${this.data.goodsInfo.goodsId}`,
      imageUrl: this.data.goodsInfo.imgUrl
    };
  }
});
