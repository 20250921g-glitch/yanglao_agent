import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：附带 token（后端读取 'token' 请求头）
request.interceptors.request.use(config => {
  const token = localStorage.getItem('app_token')
  if (token) {
    config.headers['token'] = token
  }
  return config
})

// 响应解包：{code,message,data}
request.interceptors.response.use(
  resp => {
    const res = resp.data
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      localStorage.removeItem('app_token')
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => Promise.reject(error)
)

export const appCaptcha = () => request.get('/app/captcha')
export const appSendSms = (phone) => request.post('/app/sms/send', { phone })
export const appRegister = (data) => request.post('/app/user/register', data)
export const appLogin = (data) => request.post('/app/user/login', data)
export const appInfo = () => request.get('/app/user/info')
export const appUpdateUser = (data) => request.post('/app/user/update', data)

// ===== C端健康档案（读 + 自助维护）=====
export const getMyElders = () => request.get('/app/health/elders')
export const getElderArchive = (id) => request.get(`/app/health/elder/${id}`)
export const getHealthRecords = (params) => request.get('/app/health/records', { params })
export const getDiseases = (status = 1) => request.get('/app/health/diseases', { params: { status } })
export const getFamily = (elderId) => request.get('/app/health/family', { params: { elderId } })
// 自助维护：老人档案
export const addElder = (data) => request.post('/app/health/elder', data)
export const updateElderApi = (id, data) => request.put(`/app/health/elder/${id}`, data)
export const deleteElderApi = (id) => request.delete(`/app/health/elder/${id}`)
// 自助维护：健康记录
export const addHealthRecordApi = (data) => request.post('/app/health/record', data)
export const deleteHealthRecordApi = (id) => request.delete(`/app/health/record/${id}`)
export const getHealthAdvice = (elderId, force = false) => {
  const body = {}
  if (elderId) body.elderId = elderId
  if (force) body.force = true
  return request.post('/app/ai/health-advice', body)
}

// ===== C端活动报名 =====
export const getActivityList = (params) => request.get('/app/activity/list', { params })
export const getActivityDetail = (id) => request.get(`/app/activity/${id}`)
export const registerActivity = (data) => request.post('/app/activity/register', data)
export const myActivities = (params) => request.get('/app/activity/my', { params })
export const cancelActivity = (registrationId) => request.post(`/app/activity/cancel/${registrationId}`)

// ===== C端邻里圈 =====
export const getDynamicList = (params) => request.get('/app/dynamic/list', { params })
export const getDynamicDetail = (id) => request.get(`/app/dynamic/${id}`)
export const publishDynamic = (data) => request.post('/app/dynamic/publish', data)
export const likeDynamic = (dynamicId) => request.post('/app/dynamic/like', { dynamicId })
export const favoriteDynamic = (dynamicId) => request.post('/app/dynamic/favorite', { dynamicId })
export const commentDynamic = (dynamicId, content) => request.post('/app/dynamic/comment', { dynamicId, content })
export const myDynamics = (params) => request.get('/app/dynamic/my', { params })
export const shareDynamic = (id) => request.post(`/app/dynamic/share/${id}`)

// ===== C端养老服务 =====
export const getServiceProjects = (params) => request.get('/app/service/projects', { params })
export const getServiceProjectDetail = (id) => request.get(`/app/service/project/${id}`)
export const getServiceElders = () => request.get('/app/service/elders')
export const createServiceOrder = (data) => request.post('/app/service/order', data)
export const myServiceOrders = (params) => request.get('/app/service/my-orders', { params })
export const cancelServiceOrder = (orderId) => request.post(`/app/service/cancel/${orderId}`)

// ===== C端养老商城 =====
export const getMallProducts = (params) => request.get('/app/mall/products', { params })
export const getMallProductDetail = (id) => request.get(`/app/mall/product/${id}`)
export const getMallElders = () => request.get('/app/mall/elders')
export const createMallOrder = (data) => request.post('/app/mall/order', data)
export const myMallOrders = (params) => request.get('/app/mall/my-orders', { params })
export const cancelMallOrder = (orderId) => request.post(`/app/mall/cancel/${orderId}`)

// ===== C端消息通知 =====
export const getMessageList = (params) => request.get('/app/message/list', { params })
export const getUnreadCount = () => request.get('/app/message/unread-count')
export const readMessage = (id) => request.post(`/app/message/read/${id}`)
export const readAllMessages = () => request.post('/app/message/read-all')
export const deleteMessage = (id) => request.post(`/app/message/delete/${id}`)

export default request
