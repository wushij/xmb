import { request } from './utils/request';
import { storage } from './utils/storage';

// 图片URL处理函数
const getImageUrl = (path) => {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  
  // 后端上传的图片路径(/uploads/ 或 /images/ 开头)
  if (path.startsWith('/uploads/')) {
    const url = 'https://wushij.online' + path;
    // console.log('[getImageUrl] uploads路径:', path, '->', url); // 调试时取消注释
    return url;
  }
    
  // 判断是否是后台上传的图片(UUID格式文件名,32位十六进制)
  if (path.startsWith('/images/')) {
    const fileName = path.split('/').pop();
    const nameWithoutExt = fileName.split('.')[0];
    // UUID格式:32位十六进制字符
    if (/^[a-f0-9]{32}$/i.test(nameWithoutExt)) {
      const url = 'https://wushij.online' + path;
      // console.log('[getImageUrl] UUID图片:', path, '->', url); // 调试时取消注释
      return url;
    }
  }
  
  // 小程序本地图片直接返回
  // console.log('[getImageUrl] 本地图片:', path); // 调试时取消注释
  return path;
};

App({
  onLaunch() {
    // 从缓存恢复token
    const userInfo = wx.getStorageSync('userInfo') || {};
    if (userInfo && userInfo.userId) {
      // 尝试从全局变量或缓存恢复token（需要配合登录时保存）
      this.globalData.token = wx.getStorageSync('token') || '';
      this.globalData.userInfo = userInfo;
    }
    
    // 原有登录逻辑保持不变
    wx.login({
      success: res => {
        console.log('登录成功', res.code);
      }
    });
  },
  globalData: {
    userInfo: null,
    // 开发环境:真机调试请改为电脑局域网IP,模拟器用127.0.0.1
    baseUrl: 'https://wushij.online/api',
    token: ''
  },
  request,
  storage,
  getImageUrl
});