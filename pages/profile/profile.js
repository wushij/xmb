const app = getApp();

Page({
  data: {
    userInfo: {},
    // 日期选择器弹窗控制
    showDatePicker: false,
    selectedBirthday: '',
    // 年月日数据
    years: [],
    months: [],
    days: [],
    dateValue: [0, 0, 0], // 当前选中的年月日索引
    tempBirthday: '', // 临时存储选择的生日
    // 完整手机号（用于保存）
    fullPhone: ''
  },

  onShow() {
    // 读取登录缓存（必须和登录页的缓存key一致）
    const loginUser = wx.getStorageSync('userInfo') || {};
    // 未登录直接跳转登录页
    if (!loginUser.nickName || !loginUser.avatarUrl) {
      wx.navigateTo({
        url: '/pages/login/login' // 替换为你的登录页路径
      });
      return;
    }

    // 已登录：完全复用登录信息，补充扩展字段
    const defaultBirthday = `${new Date().getFullYear()}-${new Date().getMonth() + 1}-${new Date().getDate()}`;
    
    // 初始化年月日数据
    this.initDateData();
    
    this.setData({
      userInfo: {
        avatarUrl: loginUser.avatarUrl,
        nickName: loginUser.nickName,
        phone: loginUser.phone || "",
        gender: loginUser.gender || "未设置",
        birthday: loginUser.birthday || "未设置"
      },
      selectedBirthday: loginUser.birthday || defaultBirthday,
      fullPhone: loginUser.phone || '' // 保存完整手机号
    });
  },
  
  // 初始化年月日数据
  initDateData() {
    const currentYear = new Date().getFullYear();
    const years = [];
    for (let i = currentYear; i >= 1950; i--) {
      years.push(i);
    }
    
    const months = [];
    for (let i = 1; i <= 12; i++) {
      months.push(i);
    }
    
    const days = [];
    for (let i = 1; i <= 31; i++) {
      days.push(i);
    }
    
    this.setData({ years, months, days });
  },

  /**
   * 更换头像
   */
  changeAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['original', 'compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempAvatar = res.tempFilePaths[0];
        this.setData({
          "userInfo.avatarUrl": tempAvatar
        });
        // 同步到登录缓存
        const loginUser = wx.getStorageSync('userInfo');
        wx.setStorageSync('userInfo', { ...loginUser, avatarUrl: tempAvatar });
      }
    });
  },

  /**
   * 编辑用户名
   */
  editNickName() {
    const { nickName } = this.data.userInfo;
    wx.showModal({
      title: '修改用户名',
      editable: true,
      content: nickName,
      success: (res) => {
        if (res.confirm && res.content.trim()) {
          const newName = res.content.trim();
          this.setData({ "userInfo.nickName": newName });
          // 同步到登录缓存
          const loginUser = wx.getStorageSync('userInfo');
          wx.setStorageSync('userInfo', { ...loginUser, nickName: newName });
        }
      }
    });
  },

  /**
   * 编辑手机号
   */
  editPhone() {
    const { fullPhone } = this.data;
    wx.showModal({
      title: '修改手机号',
      editable: true,
      placeholderText: '请输入11位手机号',
      content: fullPhone, // 显示完整手机号
      success: (res) => {
        if (res.confirm && res.content.trim()) {
          const newPhone = res.content.trim();
          if (/^1[3-9]\d{9}$/.test(newPhone)) {
            // 保存脱敏格式用于显示
            const hidePhone = newPhone.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2");
            this.setData({ 
              "userInfo.phone": hidePhone,
              fullPhone: newPhone // 保存完整手机号
            });
            // 同步到登录缓存（保存完整手机号）
            const loginUser = wx.getStorageSync('userInfo');
            wx.setStorageSync('userInfo', { ...loginUser, phone: newPhone });
          } else {
            wx.showToast({ title: '手机号格式错误', icon: 'none' });
          }
        }
      }
    });
  },

  /**
   * 编辑性别
   */
  editGender() {
    const { gender } = this.data.userInfo;
    const genderList = ['男', '女', '未设置'];
    wx.showActionSheet({
      itemList: genderList,
      success: (res) => {
        const newGender = genderList[res.tapIndex];
        this.setData({ "userInfo.gender": newGender });
        // 同步到登录缓存
        const loginUser = wx.getStorageSync('userInfo');
        wx.setStorageSync('userInfo', { ...loginUser, gender: newGender });
      }
    });
  },

  /**
   * 打开生日选择弹窗
   */
  editBirthday() {
    // 解析当前生日，设置初始选中值
    const birthday = this.data.userInfo.birthday;
    if (birthday && birthday !== '未设置') {
      const [year, month, day] = birthday.split('-').map(Number);
      const yearIndex = this.data.years.indexOf(year);
      const monthIndex = month - 1;
      const dayIndex = day - 1;
      
      this.setData({
        showDatePicker: true,
        dateValue: [yearIndex >= 0 ? yearIndex : 0, monthIndex, dayIndex],
        tempBirthday: birthday
      });
    } else {
      this.setData({
        showDatePicker: true,
        dateValue: [0, 0, 0],
        tempBirthday: `${this.data.years[0]}-1-1`
      });
    }
  },

  /**
   * 滚动选择日期
   */
  bindDateChange(e) {
    const val = e.detail.value;
    const year = this.data.years[val[0]];
    const month = this.data.months[val[1]];
    const day = this.data.days[val[2]];
    
    this.setData({
      tempBirthday: `${year}-${month}-${day}`
    });
  },

  /**
   * 确认选择生日
   */
  onDateConfirm() {
    const newBirthday = this.data.tempBirthday;
    this.setData({
      "userInfo.birthday": newBirthday,
      selectedBirthday: newBirthday,
      showDatePicker: false
    });
    // 同步到登录缓存
    const loginUser = wx.getStorageSync('userInfo');
    wx.setStorageSync('userInfo', { ...loginUser, birthday: newBirthday });
  },

  /**
   * 关闭生日选择弹窗
   */
  onDateCancel() {
    this.setData({ showDatePicker: false });
  },

  /**
   * 保存所有修改到数据库
   */
  saveProfile() {
    const { userInfo, fullPhone } = this.data;
    const loginUser = wx.getStorageSync('userInfo');
    
    // 构建请求数据
    const data = {};
    if (userInfo.nickName) data.nickname = userInfo.nickName;
    if (userInfo.avatarUrl) data.avatar = userInfo.avatarUrl;
    
    // 手机号：如果有完整手机号就传给后端
    if (fullPhone && fullPhone !== '未设置') {
      data.phone = fullPhone;
    }
    
    if (userInfo.gender && userInfo.gender !== '未设置') data.gender = userInfo.gender;
    if (userInfo.birthday && userInfo.birthday !== '未设置') data.birthday = userInfo.birthday;
    
    // 调用后端API保存到数据库
    app.request({
      url: '/user/profile',
      method: 'PUT',
      data
    }).then(() => {
      // 同步到本地缓存（保存完整手机号）
      const newUserInfo = { ...loginUser };
      if (userInfo.nickName) newUserInfo.nickName = userInfo.nickName;
      if (userInfo.avatarUrl) newUserInfo.avatarUrl = userInfo.avatarUrl;
      if (fullPhone) newUserInfo.phone = fullPhone;
      if (userInfo.gender) newUserInfo.gender = userInfo.gender;
      if (userInfo.birthday) newUserInfo.birthday = userInfo.birthday;
      wx.setStorageSync('userInfo', newUserInfo);
      
      wx.showToast({
        title: '保存成功',
        icon: 'success'
      });
    }).catch(err => {
      console.error('保存失败:', err);
      wx.showToast({
        title: '保存失败',
        icon: 'none'
      });
    });
  },

  onLoad(options) {},
  onReady() {}
});