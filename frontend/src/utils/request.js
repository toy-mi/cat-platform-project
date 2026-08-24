import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
    // 基础路径：后端接口的统一前缀
    baseURL: 'http://localhost:8080',  // 你的后端地址，记得修改端口号
    timeout: 10000  // 请求超时时间
})

// 请求拦截器：在发送请求前做些什么（比如添加 Token）
request.interceptors.request.use(
    config => {
        // 从 localStorage 获取 token（登录成功后存储）
        const token = localStorage.getItem('token')
        if (token) {
            // 将 token 添加到请求头
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器：处理返回的数据和错误
request.interceptors.response.use(
    response => {
        // 如果你的后端返回格式统一为 { code, message, data }
        const res = response.data
        // 根据 code 判断请求是否成功（200 表示成功）
        if (res.code !== 200) {
            // 处理业务错误（如用户名已存在）
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        } else {
            return res  // 返回数据，使用时直接得到 { code, message, data }
        }
    },
    error => {
        // 处理 HTTP 错误状态（如 401 未授权、404 不存在等）
        console.error('请求错误：', error)
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    ElMessage.error('未授权，请重新登录')
                    // 可以跳转到登录页
                    break
                case 403:
                    ElMessage.error('拒绝访问')
                    break
                case 404:
                    ElMessage.error('请求地址不存在')
                    break
                case 500:
                    ElMessage.error('服务器内部错误')
                    break
                default:
                    ElMessage.error(`连接失败：${error.message}`)
            }
        } else {
            ElMessage.error('网络连接失败，请检查后端是否启动')
        }
        return Promise.reject(error)
    }
)

export default request