const app = getApp();

Page({
  data: {
    searchValue: '',
    categoryList: [],
    currentCate: 0,
    allGoodsList: [],
    goodsList: [],
    imgLoadError: false,
    freeDeliveryAmount: 19.90 // 默认免配送费金额
  },

  onLoad() {
    this.loadCategoryList();
  },

  onShow() {
    const cacheCateId = Number(wx.getStorageSync('targetCateId'));
    console.log("分类页onShow读取的cateId：", cacheCateId);
    
    const targetCateId = cacheCateId > 0 ? cacheCateId : 0;
    this.setData({ currentCate: targetCateId });
    this.loadGoodsList(targetCateId);
    this.loadConfig(); // 加载配置
    
    wx.removeStorageSync('targetCateId');
    this.updateCartBadge();
  },

  // 加载分类列表
  loadCategoryList() {
    app.request({ url: '/category/list' }).then(res => {
      if (!res || !Array.isArray(res)) {
        console.error('分类数据格式错误:', res);
        return;
      }
      // 为每个分类添加imgUrl，使用后端返回的icon字段（与首页一致）
      const categoryList = [
        { id: 0, name: '本店优惠', imgUrl: '/images/tabbar/home.jpeg' },
        ...res.map(item => ({
          id: item.id,
          name: item.name,
          imgUrl: app.getImageUrl(item.icon)
        }))
      ];
      console.log('分类列表:', categoryList);
      this.setData({ categoryList });
    }).catch(err => {
      console.error('加载分类失败:', err);
    });
  },

  // 加载配置（免配送费金额）
  loadConfig() {
    app.request({ url: '/config/delivery' }).then(res => {
      if (res && res.freeDeliveryThreshold !== undefined) {
        this.setData({ freeDeliveryAmount: res.freeDeliveryThreshold });
      }
    }).catch(err => {
      console.error('加载配置失败，使用默认值:', err);
    });
  },

  // 加载商品列表
  loadGoodsList(categoryId) {
    const params = { pageNum: 1, pageSize: 100 };
    if (categoryId > 0) {
      params.categoryId = categoryId;
    }
    
    app.request({ url: '/goods/page', data: params }).then(res => {
      const goodsList = res.records.map(item => ({
        goodsId: item.id,
        name: item.name,
        nowPrice: item.nowPrice,
        oldPrice: item.oldPrice,
        discount: item.discount,
        stock: item.stock,
        categoryId: item.categoryId,
        imgUrl: app.getImageUrl(item.image)
      }));
      
      // 本店优惠：显示现价小于原价的商品（有优惠）
      const displayList = categoryId === 0 
        ? goodsList.filter(item => item.oldPrice && Number(item.nowPrice) < Number(item.oldPrice)) 
        : goodsList;
      
      this.setData({ goodsList: displayList, allGoodsList: goodsList });
    }).catch(err => {
      console.error('加载商品失败:', err);
    });
  },

  // 切换分类
  changeCate(e) {
    const cateId = Number(e.currentTarget.dataset.id);
    this.setData({ currentCate: cateId });
    this.loadGoodsList(cateId);
  },

  // 搜索输入
  onSearchInput(e) {
    this.setData({ searchValue: e.detail.value });
  },

  // 搜索确认
  onSearchConfirm() {
    const searchKey = this.data.searchValue.trim();
    if (!searchKey) return;
    
    app.request({ url: '/goods/page', data: { pageNum: 1, pageSize: 50, keyword: searchKey } }).then(res => {
      const goodsList = res.records.map(item => ({
        goodsId: item.id,
        name: item.name,
        nowPrice: item.nowPrice,
        oldPrice: item.oldPrice,
        discount: item.discount,
        stock: item.stock,
        categoryId: item.categoryId,
        imgUrl: app.getImageUrl(item.image)
      }));
      this.setData({ goodsList });
    }).catch(err => {
      console.error('搜索失败:', err);
    });
  },

  // 清空搜索
  onClearSearch() {
    this.setData({ searchValue: '' });
    this.loadGoodsList(this.data.currentCate);
  },

  // 加入购物车
  addToCart(e) {
    const goods = e.currentTarget.dataset.goods;
    if (!goods) return;

    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      setTimeout(() => {
        wx.navigateTo({ url: '/pages/login/login' });
      }, 1500);
      return;
    }

    app.request({
      url: '/cart/add',
      method: 'POST',
      data: { goodsId: goods.goodsId, num: 1 }
    }).then(() => {
      wx.showToast({ title: '加入购物车', icon: 'success' });
      this.updateCartBadge();
      // 刷新商品列表,更新库存显示
      this.loadGoodsList(this.data.currentCate);
    }).catch(err => {
      console.error('加入购物车失败:', err);
    });
  },

  // 更新购物车角标
  updateCartBadge() {
    if (!app.globalData.token) {
      wx.removeTabBarBadge({ index: 2 });
      return;
    }
    
    app.request({ url: '/cart/count' }).then(count => {
      const num = count || 0;
      if (num > 0) {
        wx.setTabBarBadge({ 
          index: 2, 
          text: String(num > 99 ? '99+' : num) 
        });
      } else {
        wx.removeTabBarBadge({ index: 2 });
      }
    }).catch(err => {
      console.error('更新角标失败:', err);
    });
  },

  // 商品详情跳转
  goDetail(e) {
    const goodsId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/goods/goods?goodsId=${goodsId}`
    });
  },

  onImgError() {
    this.setData({ imgLoadError: true });
  },

  // 购物车变化回调（由goods-item组件触发）
  cartChange() {
    this.updateCartBadge();
    // 刷新商品列表,更新库存显示
    this.loadGoodsList(this.data.currentCate);
  }
});