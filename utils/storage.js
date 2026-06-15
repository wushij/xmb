/**
 * 小程序本地缓存工具类
 * 功能：存储/读取/删除/更新缓存，增强容错，避免JSON解析报错
 * 解决问题：1. [object Object] 非法格式解析报错  2. 解析失败时误删userInfo缓存  3. 登录状态丢失
 */
const storage = {
  /**
   * 存储数据
   * @param {string} key 缓存键名
   * @param {any} value 缓存值（支持任意类型，自动序列化对象/数组）
   */
  set(key, value) {
    // 处理 undefined/null，避免 JSON.stringify 报错
    if (value === undefined || value === null) {
      wx.setStorageSync(key, '');
      return;
    }

    try {
      // 统一序列化对象/数组为 JSON 字符串
      const storeValue = typeof value === 'object' ? JSON.stringify(value) : value;
      wx.setStorageSync(key, storeValue);
    } catch (err) {
      console.error(`[缓存存储失败] key: ${key}，原因：`, err);
      // 存储失败时清空脏数据，避免后续解析异常
      this.remove(key);
    }
  },

  /**
   * 获取数据
   * @param {string} key 缓存键名
   * @param {any} defaultValue 解析失败/无数据时的默认值，默认null
   * @returns {any} 解析后的缓存值或默认值
   */
  get(key, defaultValue = null) {
    try {
      const rawData = wx.getStorageSync(key);

      // 空值直接返回默认值（覆盖空字符串/undefined/null）
      if (!rawData) {
        return defaultValue;
      }

      // 核心优化：提前拦截 [object Object] 这类非法字符串，避免 JSON.parse 报错
      if (typeof rawData === 'string' && rawData.startsWith('[object ')) {
        console.warn(`[缓存非法格式] key: ${key}，自动清理并返回默认值`);
        this.remove(key);
        return defaultValue;
      }

      // 尝试解析 JSON（仅针对字符串类型）
      const parsedData = typeof rawData === 'string' ? JSON.parse(rawData) : rawData;

      // 兼容购物车逻辑：cartList 必须返回数组
      if (key === 'cartList' && !Array.isArray(parsedData)) {
        return [];
      }

      return parsedData;
    } catch (err) {
      console.error(`[缓存解析失败] key: ${key}，原因：`, err);
      // 关键优化：userInfo 解析失败时不删缓存，仅返回空对象，避免登录状态丢失
      if (key === 'userInfo') {
        return {};
      }
      // 其他key解析失败时清理缓存，返回默认值
      this.remove(key);
      return key === 'cartList' ? [] : defaultValue;
    }
  },

  /**
   * 删除指定缓存
   * @param {string} key 缓存键名
   */
  remove(key) {
    try {
      wx.removeStorageSync(key);
    } catch (err) {
      console.error(`[缓存删除失败] key: ${key}，原因：`, err);
    }
  },

  /**
   * 清空所有本地缓存
   */
  clear() {
    try {
      wx.clearStorageSync();
    } catch (err) {
      console.error(`[缓存清空失败] 原因：`, err);
    }
  },

  /**
   * 安全更新缓存（对象浅合并，数组直接替换）
   * @param {string} key 缓存键名
   * @param {any} newData 新数据
   */
  update(key, newData) {
    try {
      const oldData = this.get(key);
      let finalData = newData;

      // 对象类型：浅合并新旧数据（保留旧字段，覆盖新字段）
      if (oldData && typeof oldData === 'object' && !Array.isArray(oldData) && 
          newData && typeof newData === 'object' && !Array.isArray(newData)) {
        finalData = { ...oldData, ...newData };
      }
      // 数组类型：直接用新数组替换旧数组（外部自行处理追加逻辑）
      else if (Array.isArray(oldData) && Array.isArray(newData)) {
        finalData = newData;
      }

      this.set(key, finalData);
    } catch (err) {
      console.error(`[缓存更新失败] key: ${key}，原因：`, err);
    }
  },

  /**
   * 判断缓存是否存在（仅判断有有效数据，空字符串不算）
   * @param {string} key 缓存键名
   * @returns {boolean} 缓存是否存在
   */
  has(key) {
    try {
      const rawData = wx.getStorageSync(key);
      return !!rawData && rawData !== '';
    } catch (err) {
      console.error(`[缓存检测失败] key: ${key}，原因：`, err);
      return false;
    }
  },

  /**
   * 按指定类型获取缓存（增强类型容错）
   * @param {string} key 缓存键名
   * @param {string} type 目标类型：array/object/string/number/boolean
   * @returns {any} 符合类型的缓存值或对应类型默认值
   */
  getWithType(key, type) {
    const data = this.get(key);
    // 类型映射表：不同类型返回对应默认值
    const typeHandler = {
      array: () => Array.isArray(data) ? data : [],
      object: () => (data && typeof data === 'object' && !Array.isArray(data)) ? data : {},
      string: () => typeof data === 'string' ? data : '',
      number: () => {
        const num = Number(data);
        return !isNaN(num) ? num : 0;
      },
      boolean: () => {
        // 兼容字符串形式的布尔值（如 "true"/"false"）
        if (data === 'true') return true;
        if (data === 'false') return false;
        return typeof data === 'boolean' ? data : false;
      }
    };

    return typeHandler[type] ? typeHandler[type]() : data;
  }
};

export { storage };