import type { MessageInstance } from 'antd/es/message/interface'

// 全局 message 实例持有者
let messageApi: MessageInstance | null = null

export const setMessageApi = (api: MessageInstance) => {
  messageApi = api
  console.log('Message API 已设置') // 调试用
}

export const getMessageApi = (): MessageInstance | null => {
  return messageApi
}

// 便捷方法 - 添加空值检查和控制台警告
export const showMessage = {
  success: (content: string) => {
    if (messageApi) {
      messageApi.success(content)
    } else {
      console.warn('Message API 未初始化，无法显示消息:', content)
    }
  },
  error: (content: string) => {
    if (messageApi) {
      messageApi.error(content)
    } else {
      console.warn('Message API 未初始化，无法显示错误:', content)
    }
  },
  warning: (content: string) => {
    if (messageApi) {
      messageApi.warning(content)
    } else {
      console.warn('Message API 未初始化，无法显示警告:', content)
    }
  },
  info: (content: string) => {
    if (messageApi) {
      messageApi.info(content)
    } else {
      console.warn('Message API 未初始化，无法显示信息:', content)
    }
  },
  loading: (content: string) => {
    if (messageApi) {
      messageApi.loading(content)
    } else {
      console.warn('Message API 未初始化，无法显示加载:', content)
    }
  },
}
