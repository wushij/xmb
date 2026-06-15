const app = getApp();

Page({
  data: {
    cartList: [],
    totalPrice: 0,
    goodsTotalPrice: 0, // 商品总金额（不含配送费）
    deliveryFee: 0, // 配送费
    isFreeDelivery: false, // 是否免配送费
    needMorePrice: '0.00', // 距离免配送费还需金额
    address: null,
    remark: '',
    loading: false,
    // 配送方式：0-配送 1-自提
    deliveryType: 0,
    // 订单创建后的支付操作栏
    showPayBar: false,
    currentOrder: null,
    // 配送配置（从后端获取）
    config: {
      deliveryFee: 3,
      freeDeliveryThreshold: 19.9,
      minOrderAmount: 12
    }
  },

  onLoad() {
    this.loadDeliveryConfig();
    this.getCartList();
    this.getDefaultAddress();
  },

  // 加载配送配置
  loadDeliveryConfig() {
    app.request({ url: '/config/delivery' }).then(res => {
      if (res) {
        this.setData({ config: res });
      }
    }).catch(err => {
      console.error('加载配送配置失败:', err);
    });
  },

  onShow() {
    // 从地址选择页返回时，读取选中的地址
    const selectedAddress = wx.getStorageSync('selectedAddress');
    if (selectedAddress) {
      this.setData({ address: selectedAddress });
      wx.removeStorageSync('selectedAddress');
    }
  },

  // 获取购物车选中商品
  getCartList() {
    app.request({ url: '/cart/list' }).then(res => {
      const cartList = res.filter(item => item.selected).map(item => ({
        id: item.id,
        goodsId: item.goodsId,
        name: item.goodsName,
        nowPrice: item.goodsPrice,
        imgUrl: app.getImageUrl(item.goodsImage),
        num: item.num,
        totalPrice: item.totalPrice,
        stock: item.stock
      }));

      const goodsTotalPrice = cartList.reduce((sum, item) => {
        return sum + Number(item.totalPrice || 0);
      }, 0);

      // 根据配送方式计算配送费
      this.calculateDeliveryFee(goodsTotalPrice);

      this.setData({ 
        cartList, 
        goodsTotalPrice: goodsTotalPrice.toFixed(2)
      });
    }).catch(err => {
      console.error('获取购物车失败:', err);
    });
  },

  // 计算配送费
  calculateDeliveryFee(goodsTotalPrice) {
    const config = this.data.config;
    const deliveryType = this.data.deliveryType;
    
    let deliveryFee, isFreeDelivery, needMorePrice;
    
    if (deliveryType === 1) {
      // 自提：免配送费
      deliveryFee = 0;
      isFreeDelivery = true;
      needMorePrice = '0.00';
    } else {
      // 配送：根据满减规则
      isFreeDelivery = goodsTotalPrice >= config.freeDeliveryThreshold;
      deliveryFee = isFreeDelivery ? 0 : config.deliveryFee;
      needMorePrice = isFreeDelivery ? '0.00' : (config.freeDeliveryThreshold - goodsTotalPrice).toFixed(2);
    }
    
    const totalPrice = (goodsTotalPrice + deliveryFee).toFixed(2);

    this.setData({ 
      deliveryFee,
      isFreeDelivery,
      needMorePrice,
      totalPrice 
    });
  },

  // 选择配送方式
  selectDeliveryType(e) {
    const type = parseInt(e.currentTarget.dataset.type);
    const goodsTotalPrice = Number(this.data.goodsTotalPrice);
    
    this.setData({ deliveryType: type });
    
    // 重新计算配送费
    this.calculateDeliveryFee(goodsTotalPrice);
  },

  // 获取默认地址
  getDefaultAddress() {
    app.request({ url: '/address/default' }).then(res => {
      if (res) {
        this.setData({ 
          address: {
            id: res.id,
            name: res.name,
            phone: res.phone,
            detail: `${res.province || ''}${res.city || ''}${res.district || ''}${res.detail || ''}`
          }
        });
      }
    }).catch(err => {
      console.error('获取地址失败:', err);
    });
  },

  // 选择地址
  chooseAddress() {
    wx.navigateTo({
      url: '/pages/address/address?select=1'
    });
  },

  // 输入备注
  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  // 提交订单
  submitOrder() {
    if (this.data.loading) return;

    if (!this.data.address) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' });
      return;
    }

    if (this.data.cartList.length === 0) {
      wx.showToast({ title: '购物车为空', icon: 'none' });
      return;
    }

    // 校验起送价格
    const config = this.data.config;
    const goodsTotalPrice = Number(this.data.goodsTotalPrice);
    if (goodsTotalPrice < config.minOrderAmount) {
      wx.showToast({ 
        title: `商品金额未达起送价格¥${config.minOrderAmount}`, 
        icon: 'none',
        duration: 2000
      });
      return;
    }

    this.setData({ loading: true });

    app.request({
      url: '/order',
      method: 'POST',
      data: {
        addressId: this.data.address.id,
        deliveryType: this.data.deliveryType,
        remark: this.data.remark
      }
    }).then(res => {
      wx.showToast({ title: '下单成功', icon: 'success' });
      // 显示支付操作栏
      this.setData({
        showPayBar: true,
        currentOrder: {
          id: res.id || res,
          totalPrice: this.data.totalPrice
        },
        loading: false
      });
      // 更新购物车角标
      wx.removeTabBarBadge({ index: 2 });
    }).catch(err => {
      console.error('下单失败:', err);
      this.setData({ loading: false });
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
            // 跳转到待发货页面
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

  // 取消支付 - 跳转到待付款页面
  cancelPay() {
    wx.showModal({
      title: '取消支付',
      content: '取消后订单将保存在待付款列表，可稍后支付',
      confirmText: '确认取消',
      success: (res) => {
        if (res.confirm) {
          wx.redirectTo({
            url: '/pages/order/waitPay/waitPay'
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
  }
});
