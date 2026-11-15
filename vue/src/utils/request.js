// @/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL || '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        console.log('====== 发起请求 ======')
        console.log('请求URL:', config.baseURL + config.url)
        console.log('请求方法:', config.method)
        console.log('请求参数:', config.params)
        console.log('请求数据:', config.data)

        // 获取用户token
        const user = JSON.parse(localStorage.getItem('system-user') || '{}')
        if (user.token) {
            config.headers.Authorization = `Bearer ${user.token}`
            console.log('请求携带Token:', user.token.substring(0, 20) + '...')
        } else {
            console.log('请求未携带Token')
        }

        return config
    },
    error => {
        console.error('请求发送失败:', error)
        ElMessage.error('请求发送失败: ' + error.message)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        console.log('====== 收到响应 ======')
        console.log('响应状态:', response.status)
        console.log('响应数据:', response.data)

        const res = response.data

        // 检查响应数据
        if (!res) {
            const error = new Error('服务器返回空响应')
            ElMessage.error(error.message)
            return Promise.reject(error)
        }

        // 检查响应码 - 支持数字和字符串格式的200
        if (res.code !== 200 && res.code !== '200') {
            const errorMsg = res.msg || `操作失败 (${res.code})`
            console.error('API返回错误:', errorMsg)
            ElMessage.error(errorMsg)

            // 处理401特殊逻辑
            if (res.code === 401 || res.code === '401') {
                console.log('Token已过期，跳转到登录页面')
                localStorage.removeItem('system-user')
                setTimeout(() => {
                    window.location.href = '/login'
                }, 100)
            }

            return Promise.reject(new Error(errorMsg))
        }

        // 成功返回
        console.log('API调用成功')
        return res
    },
    error => {
        console.error('====== 请求错误 ======')
        console.error('错误对象:', error)
        console.error('错误状态:', error.response?.status)
        console.error('错误数据:', error.response?.data)
        console.error('错误配置:', error.config?.url)
        console.error('错误消息:', error.message)

        // 处理HTTP错误
        if (error.response) {
            const status = error.response.status
            let errorMessage = ''

            switch (status) {
                case 400:
                    errorMessage = '请求参数错误'
                    console.error('请求参数:', error.config?.data)
                    break
                case 401:
                    errorMessage = '未授权访问，请重新登录'
                    localStorage.removeItem('system-user')
                    setTimeout(() => {
                        window.location.href = '/login'
                    }, 100)
                    break
                case 403:
                    errorMessage = '权限不足，无法访问'
                    break
                case 404:
                    errorMessage = `请求的接口不存在: ${error.config?.url}`
                    break
                case 500:
                    errorMessage = '服务器内部错误，请稍后再试'
                    break
                case 502:
                    errorMessage = '网关错误，后端服务可能未启动'
                    break
                case 504:
                    errorMessage = '网关超时，请检查后端服务状态'
                    break
                default:
                    errorMessage = `请求失败（${status}）`
            }

            console.error('HTTP错误:', errorMessage)
            ElMessage.error(errorMessage)
            return Promise.reject(new Error(errorMessage))
        }
        // 处理网络错误
        else if (error.request) {
            const errorMessage = '网络连接失败，请检查：\n1. 后端服务是否启动\n2. 网络连接是否正常\n3. 跨域配置是否正确'
            console.error('网络错误:', errorMessage)
            ElMessage.error(errorMessage)
            return Promise.reject(new Error(errorMessage))
        }
        // 其他错误
        else {
            const errorMessage = error.message || '未知错误'
            console.error('其他错误:', errorMessage)
            ElMessage.error(errorMessage)
            return Promise.reject(new Error(errorMessage))
        }
    }
)

export default request