const app = getApp();

Page({
  data: {
    cateList: [],
    hotGoodsList: []
  },

  onLoad() {
    this.loadCategoryList();
    this.loadHotGoods();
  },

  onShow() {
    this.updateCartBadge();
    // 每次显示页面时刷新商品列表,确保库存数据是最新的
    this.loadHotGoods();
  },

  // 加载分类列表
  loadCategoryList() {
    app.request({ url: '/category/list' }).then(res => {
      // 只取前6个分类
      const cateList = res.slice(0, 6).map(item => ({
        id: item.id,
        name: item.name,
        cateId: item.id,
        imgUrl: app.getImageUrl(item.icon)
      }));
      this.setData({ cateList });
    }).catch(err => {
      console.error('加载分类失败:', err);
    });
  },

  // 加载热销商品
  loadHotGoods() {
    app.request({ url: '/goods/hot', data: { limit: 20 } }).then(res => {
      const hotGoodsList = res.map(item => ({
        goodsId: item.id,
        name: item.name,
        nowPrice: item.nowPrice,
        oldPrice: item.oldPrice,
        discount: item.discount,
        stock: item.stock,
        categoryId: item.categoryId,
        imgUrl: app.getImageUrl(item.image)
      }));
      this.setData({ hotGoodsList });
    }).catch(err => {
      console.error('加载热销商品失败:', err);
    });
  },

  // 专区点击跳转逻辑（原有逻辑不变）
  goCategory(e) {
    const cateId = Number(e.currentTarget.dataset.cateid);
    console.log("首页存入缓存的cateId：", cateId);

    if (!cateId || isNaN(cateId)) {
      wx.showToast({
        title: '分类参数错误',
        icon: 'none',
        duration: 1500
      });
      return;
    }

    wx.setStorageSync('targetCateId', cateId);
    wx.switchTab({
      url: '/pages/category/category',
      fail: (err) => {
        console.error("跳转失败：", err);
        wx.showToast({
          title: '页面跳转失败，请检查配置',
          icon: 'none',
          duration: 2000
        });
      }
    });
  },

  // 跳转到分类页
  goToCategory() {
    wx.switchTab({
      url: '/pages/category/category'
    });
  },

  // 购物车角标更新逻辑（原有逻辑不变）
  cartChange() {
    this.updateCartBadge();
  },

  // 更新购物车角标
  updateCartBadge() {
    if (!app.globalData.token) {
      wx.removeTabBarBadge({ index: 2 });
      return;
    }
    app.request({ url: '/cart/count' }).then(res => {
      const count = res || 0;
      if (count > 0) {
        wx.setTabBarBadge({
          index: 2,
          text: String(count > 99 ? '99+' : count)
        });
      } else {
        wx.removeTabBarBadge({ index: 2 });
      }
    }).catch(err => {
      console.error('获取购物车数量失败:', err);
      wx.removeTabBarBadge({ index: 2 });
    });
  }
});