Page({
  data: {
    noticeEnabled: true,
    showCustomModal: false,
    showPrivacyModal: false
  },

  onLoad(options) {
    // 加载设置
    const noticeStatus = wx.getStorageSync('noticeEnabled');
    
    if (noticeStatus !== undefined) {
      this.setData({ noticeEnabled: noticeStatus });
    }
  },

  // 客服/帮助中心弹窗
  goToHelp() {
    this.setData({ showCustomModal: true });
  },
  hideCustomModal() {
    this.setData({ showCustomModal: false });
  },
  stopPropagation() {},

  // 隐私设置弹窗
  goToPrivacy() {
    this.setData({ showPrivacyModal: true });
  },
  hidePrivacyModal() {
    this.setData({ showPrivacyModal: false });
  },

  // 消息通知开关
  onNoticeSwitchChange(e) {
    const isEnabled = e.detail.value;
    this.setData({ noticeEnabled: isEnabled });
    wx.setStorageSync('noticeEnabled', isEnabled);
    wx.showToast({
      title: isEnabled ? '已开启消息通知' : '已关闭消息通知',
      icon: 'none',
      duration: 1500
    });
  },

  // 意见反馈
  goToFeedback() {
    wx.showToast({
      title: '意见反馈功能开发中',
      icon: 'none',
      duration: 2000
    });
  },

  // 关于我们
  goToAbout() {
    wx.navigateTo({
      url: '/pages/about/about'
    });
  },

  // 检查更新
  checkUpdate() {
    wx.showLoading({ title: '检查更新中...' });
    
    // 模拟检查更新
    setTimeout(() => {
      wx.hideLoading();
      wx.showToast({
        title: '已是最新版本',
        icon: 'success',
        duration: 2000
      });
    }, 1500);
  }
});