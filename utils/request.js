const request = (options) => {
  const app = getApp();
  return new Promise((resolve, reject) => {
    // 构建请求头，携带Token
    const header = {
      'content-type': 'application/json'
    };
    if (app.globalData.token) {
      header['Authorization'] = app.globalData.token;
    }
    
    wx.request({
      url: app.globalData.baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: res => {
        if (res.data.code === 200) {
          resolve(res.data.data);
        } else if (res.data.code === 401) {
          // 未登录，清除登录状态
          app.globalData.token = '';
          wx.removeStorageSync('userInfo');
          wx.showToast({
            title: '请先登录',
            icon: 'none'
          });
          // 跳转登录页
          setTimeout(() => {
            wx.navigateTo({ url: '/pages/login/login' });
          }, 1500);
          reject(res.data);
        } else {
          wx.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          });
          reject(res.data);
        }
      },
      fail: err => {
        wx.showToast({
          title: '网络异常',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};

export { request };
