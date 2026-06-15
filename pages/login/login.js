const app = getApp();

Page({
  data: {
    userAvatar: '',
    nickName: ''
  },

  onLoad(options) {
    const userInfo = wx.getStorageSync('userInfo') || {};
    if (userInfo.avatarUrl && userInfo.nickName) {
      this.setData({
        userAvatar: userInfo.avatarUrl,
        nickName: userInfo.nickName
      });
    }
  },

  onChooseAvatar(e) {
    if (!e.detail.avatarUrl) {
      wx.showToast({ title: '已取消选择头像', icon: 'none' });
      return;
    }
    const avatarUrl = e.detail.avatarUrl;
    this.setData({ userAvatar: avatarUrl });
    console.log("选中的头像地址：", avatarUrl);
  },

  onNicknameInput(e) {
    const nickName = e.detail.value;
    this.setData({ nickName });
    console.log("输入/选择的昵称：", nickName);
  },

  handleConfirm() {
    const { userAvatar, nickName } = this.data;
    
    if (!userAvatar) {
      wx.showToast({ title: '请先选择头像', icon: 'none' });
      return;
    }
    if (!nickName) {
      wx.showToast({ title: '请输入/选择昵称', icon: 'none' });
      return;
    }

    // 先获取微信登录code
    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          wx.showToast({ title: '微信登录失败', icon: 'none' });
          return;
        }
        
        // 调用后端登录接口
        app.request({
          url: '/auth/login',
          method: 'POST',
          data: {
            code: loginRes.code,
            nickname: nickName,
            avatar: userAvatar
          }
        }).then(res => {
      // 保存token到全局和缓存
      app.globalData.token = res.token;
      wx.setStorageSync('token', res.token);
      
      // 保存用户信息（包含完整资料）
      const userInfo = {
        userId: res.userId,
        nickName: res.nickname,
        avatarUrl: res.avatar,
        phone: res.phone || '',
        gender: res.gender || '未设置',
        birthday: res.birthday || '未设置'
      };
      wx.setStorageSync('userInfo', userInfo);
      
      wx.showToast({ title: '登录成功', icon: 'success' });
      
      // 跳转到个人中心
          wx.switchTab({
            url: '/pages/my/my',
            fail: () => {
              wx.showToast({ title: '跳转失败，请重试', icon: 'none' });
            }
          });
        }).catch(err => {
          console.error('登录失败:', err);
        });
      },
      fail: (err) => {
        console.error('wx.login失败:', err);
        wx.showToast({ title: '微信登录失败', icon: 'none' });
      }
    });
  },

  onShow() {
    console.log('登录页面显示');
  },

  onUnload() {
    console.log('登录页面卸载');
  }
});