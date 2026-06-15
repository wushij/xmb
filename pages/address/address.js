const app = getApp();

Page({
  data: {
    isEditMode: false,
    isSelectMode: false,
    addressList: [],
    addressForm: {
      id: '',
      name: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detail: '',
      isDefault: false
    }
  },

  onLoad(options) {
    this.checkLogin();
    // 判断是否是选择地址模式
    if (options.select === '1') {
      this.setData({ isSelectMode: true });
    }
  },

  onShow() {
    if (this.checkLogin()) {
      this.loadAddressList();
    }
  },

  checkLogin() {
    if (!app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      setTimeout(() => {
        wx.navigateBack({ fail: () => wx.navigateTo({ url: '/pages/login/login' }) });
      }, 1000);
      return false;
    }
    return true;
  },

  loadAddressList() {
    app.request({ url: '/address/list' }).then(res => {
      this.setData({ addressList: res });
    }).catch(err => {
      console.error('获取地址失败:', err);
    });
  },

  enterEditMode(e) {
    const address = e.currentTarget.dataset.address || {};
    if (!address.id) {
      this.setData({
        isEditMode: true,
        addressForm: {
          id: '',
          name: '',
          phone: '',
          province: '',
          city: '',
          district: '',
          detail: '',
          isDefault: false
        }
      });
    } else {
      this.setData({
        isEditMode: true,
        addressForm: { ...address, isDefault: address.isDefault === 1 }
      });
    }
  },

  cancelEdit() {
    this.setData({ isEditMode: false });
  },

  onInputChange(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [`addressForm.${field}`]: e.detail.value });
  },

  onRegionChange(e) {
    const [province, city, district] = e.detail.value;
    this.setData({
      'addressForm.province': province,
      'addressForm.city': city,
      'addressForm.district': district
    });
  },

  onDefaultChange(e) {
    this.setData({ 'addressForm.isDefault': e.detail.value });
  },

  saveAddress() {
    const { addressForm } = this.data;
    const { name, phone, province, city, district, detail } = addressForm;

    if (!name.trim()) return wx.showToast({ title: '请填写收货人', icon: 'none' });
    if (!/^1[3-9]\d{9}$/.test(phone.trim())) return wx.showToast({ title: '手机号格式错误', icon: 'none' });
    if (!province || !city || !district) return wx.showToast({ title: '请选择省市区', icon: 'none' });
    if (!detail.trim()) return wx.showToast({ title: '请填写详细地址', icon: 'none' });

    const data = {
      name: name.trim(),
      phone: phone.trim(),
      province,
      city,
      district,
      detail: detail.trim(),
      isDefault: addressForm.isDefault ? 1 : 0
    };

    if (addressForm.id) {
      app.request({
        url: `/address/${addressForm.id}`,
        method: 'PUT',
        data
      }).then(() => {
        wx.showToast({ title: '修改成功', icon: 'success' });
        this.setData({ isEditMode: false });
        this.loadAddressList();
      }).catch(err => console.error('修改地址失败:', err));
    } else {
      app.request({
        url: '/address',
        method: 'POST',
        data
      }).then(() => {
        wx.showToast({ title: '新增成功', icon: 'success' });
        this.setData({ isEditMode: false });
        this.loadAddressList();
      }).catch(err => console.error('新增地址失败:', err));
    }
  },

  deleteAddress(e) {
    const addressId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: `/address/${addressId}`,
            method: 'DELETE'
          }).then(() => {
            wx.showToast({ title: '删除成功', icon: 'success' });
            this.loadAddressList();
          }).catch(err => console.error('删除地址失败:', err));
        }
      }
    });
  },

  setDefaultAddress(e) {
    const addressId = e.currentTarget.dataset.id;
    app.request({
      url: `/address/default/${addressId}`,
      method: 'PUT'
    }).then(() => {
      wx.showToast({ title: '设置成功', icon: 'success' });
      this.loadAddressList();
    }).catch(err => console.error('设置默认地址失败:', err));
  },

  // 选择地址（订单确认页调用）
  selectAddress(e) {
    const address = e.currentTarget.dataset.address;
    if (this.data.isSelectMode && address) {
      // 将选中的地址存入缓存
      wx.setStorageSync('selectedAddress', {
        id: address.id,
        name: address.name,
        phone: address.phone,
        detail: `${address.province || ''}${address.city || ''}${address.district || ''}${address.detail || ''}`
      });
      wx.navigateBack();
    }
  }
});