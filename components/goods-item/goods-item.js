const app = getApp();

Component({
  properties: {
    goods: {
      type: Object,
      value: {}
    }
  },

  data: {
    imgLoadError: false
  },

  methods: {
    onImgError(e) {
      this.setData({ imgLoadError: true });
      console.error("商品图片加载失败:", {
        imgUrl: this.properties.goods.imgUrl,
        errorDetail: e.detail
      });
    },

    goDetail(e) {
      const goodsId = e.currentTarget.dataset.id;
      wx.navigateTo({
        url: `/pages/goods/goods?goodsId=${goodsId}`
      });
    },

    addToCart(e) {
      const goods = e.currentTarget.dataset.goods;
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
        wx.showToast({ title: '加入购物车成功', icon: 'success', duration: 1000 });
        this.triggerEvent('cartChange');
      }).catch(err => {
        console.error('加入购物车失败:', err);
        // 后端返回的库存不足错误
        if (err && err.message) {
          wx.showToast({ title: err.message, icon: 'none' });
        }
      });
    }
  }
});