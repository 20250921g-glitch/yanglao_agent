import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['token'] = userStore.token
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request

// ============ Auth ============
export const login = (data) => request.post('/sys/user/login', data)
export const getUserInfo = () => request.get('/sys/user/info')
export const resetPassword = (data) => request.post('/sys/user/reset-password', data)
export const getUserPerms = (userId) => request.get(`/sys/permission/user-perms/${userId}`)
export const getOperationLogPage = (params) => request.get('/system/operation-log/page', { params })

// ============ Dashboard ============
export const getDashboardStats = () => request.get('/dashboard/stats')
export const getStatCards = () => request.get('/dashboard/stat-cards')
export const getTagDistribution = () => request.get('/dashboard/tag-distribution')
export const getServiceTypeDistribution = () => request.get('/dashboard/service-type-distribution')
export const getUserTrend = () => request.get('/dashboard/user-trend')
export const getProductTop = () => request.get('/dashboard/product-top')
export const getWorkerTop = () => request.get('/dashboard/worker-top')

// ============ 健康管理 ============
export const getElderPage = (params) => request.get('/health/elder/page', { params })
export const getElderList = () => request.get('/health/elder/list')
export const addElder = (data) => request.post('/health/elder', data)
export const updateElder = (data) => request.put('/health/elder', data)
export const deleteElder = (id) => request.delete(`/health/elder/${id}`)
export const getElderById = (id) => request.get(`/health/elder/${id}`)

export const getRecordPage = (params) => request.get('/health/record/page', { params })
export const addRecord = (data) => request.post('/health/record', data)
export const deleteRecord = (id) => request.delete(`/health/record/${id}`)
export const getHealthDataByType = (elderId, recordType) => request.get('/health/record/list', { params: { elderId, recordType } })
export const getHealthRecordStats = (elderId, recordType) => request.get('/health/record/stats', { params: { elderId, recordType } })

  export const getFamilyList = (elderId) => request.get(`/health/family/list/${elderId}`)
export const addFamily = (data) => request.post('/health/family', data)
export const updateFamily = (data) => request.put('/health/family', data)
export const deleteFamily = (id) => request.delete(`/health/family/${id}`)

// ============ 商品管理 ============
// ProductCategory 商品分类
export const getProductCategoryPage = (params) => request.get('/product/category/page', { params })
export const getProductCategory = (id) => request.get(`/product/category/${id}`)
export const getProductCategoryList = (serviceType) => request.get('/product/category/list', { params: { serviceType } })
export const createProductCategory = (data) => request.post('/product/category', data)
export const updateProductCategory = (id, data) => request.put(`/product/category/${id}`, data)
export const deleteProductCategory = (id) => request.delete(`/product/category/${id}`)

// Product 商品管理
export const getProductPage = (params) => request.get('/product/page', { params })
export const getProduct = (id) => request.get(`/product/${id}`)
export const addProduct = (data) => request.post('/product', data)
export const updateProduct = (data) => request.put('/product', data)
export const deleteProduct = (id) => request.delete(`/product/${id}`)

// ProductParam 商品参数
export const getProductParamPage = (params) => request.get('/product/param/page', { params })
export const getProductParamList = (serviceType) => request.get('/product/param/list', { params: { serviceType } })
export const getProductParam = (id) => request.get(`/product/param/${id}`)
export const addProductParam = (data) => request.post('/product/param', data)
export const updateProductParam = (id, data) => request.put(`/product/param/${id}`, data)
export const deleteProductParam = (id) => request.delete(`/product/param/${id}`)

// ProductSetting 商品设置
export const getProductSettings = () => request.get('/product/setting/list')
export const saveProductSettings = (data) => request.post('/product/setting/save', data)

// ============ 运营管理 ============
export const getEvaluationPage = (params) => request.get('/operation/evaluation/page', { params })
export const addEvaluation = (data) => request.post('/operation/evaluation', data)
export const deleteEvaluation = (id) => request.delete(`/operation/evaluation/${id}`)

export const getFoodPage = (params) => request.get('/operation/food/page', { params })
export const addFood = (data) => request.post('/operation/food', data)
export const updateFood = (data) => request.put('/operation/food', data)
export const deleteFood = (id) => request.delete(`/operation/food/${id}`)

// ============ 交易管理 ============
export const getServiceOrderPage = (params) => request.get('/trade/service-order/page', { params })
export const getServiceOrderDetail = (id) => request.get(`/trade/service-order/${id}`)
export const addServiceOrder = (data) => request.post('/trade/service-order', data)
export const updateServiceOrder = (id, data) => request.put(`/trade/service-order/${id}`, data)
export const assignWorker = (id, nurseId, nurseName) => request.put(`/trade/service-order/assign/${id}?nurseId=${nurseId}&nurseName=${encodeURIComponent(nurseName)}`)
export const cancelServiceOrder = (id) => request.put(`/trade/service-order/cancel/${id}`)
export const startService = (id) => request.put(`/trade/service-order/start/${id}`)
export const completeService = (id) => request.put(`/trade/service-order/complete/${id}`)

export const getProductOrderPage = (params) => request.get('/trade/product-order/page', { params })
export const getProductOrderDetail = (id) => request.get(`/trade/product-order/${id}`)
export const addProductOrder = (data) => request.post('/trade/product-order', data)
export const updateProductOrderStatus = (id, status) => request.put(`/trade/product-order/status/${id}?status=${status}`)
export const updateProductOrderPrice = (id, data) => request.put(`/trade/product-order/price/${id}`, data)
export const closeProductOrder = (id) => request.put(`/trade/product-order/close/${id}`)
export const dispatchProductOrder = (id, params) => request.put(`/trade/product-order/dispatch/${id}?workerId=${params.workerId}&workerName=${encodeURIComponent(params.workerName)}&appointmentTime=${params.appointmentTime ? encodeURIComponent(params.appointmentTime) : ''}&operator=${encodeURIComponent(params.operator || 'admin')}`)

// Refund 售后
export const getRefundPage = (params) => request.get('/trade/refund/page', { params })
export const getRefundDetail = (id) => request.get(`/trade/refund/${id}`)
export const applyRefund = (data) => request.post('/trade/refund', data)
export const approveRefund = (id, auditor, remark) => request.put(`/trade/refund/approve/${id}?auditor=${encodeURIComponent(auditor || 'admin')}&remark=${encodeURIComponent(remark || '')}`)
export const rejectRefund = (id, auditor, remark) => request.put(`/trade/refund/reject/${id}?auditor=${encodeURIComponent(auditor || 'admin')}&remark=${encodeURIComponent(remark || '')}`)
export const processRefund = (id) => request.put(`/trade/refund/process/${id}`)
export const cancelRefund = (id) => request.put(`/trade/refund/cancel/${id}`)

// OrderEvaluation 评价
export const getOrderEvaluationPage = (params) => request.get('/trade/evaluation/page', { params })
export const replyOrderEvaluation = (id, data) => request.put(`/trade/evaluation/${id}/reply`, data)
export const updateEvaluationStatus = (id, status) => request.put(`/trade/evaluation/${id}/status?status=${status}`)

// Withdrawal 提现
export const getWithdrawalPage = (params) => request.get('/trade/withdrawal/page', { params })
export const handleWithdrawal = (id, data) => request.put(`/trade/withdrawal/${id}/audit`, data)

// RefundReason 退款原因
export const getRefundReasonList = () => request.get('/trade/refund-reason/list')
export const addRefundReason = (data) => request.post('/trade/refund-reason', data)
export const updateRefundReason = (data) => request.put('/trade/refund-reason', data)
export const deleteRefundReason = (id) => request.delete(`/trade/refund-reason/${id}`)

// Transaction 收支明细
export const getTransactionPage = (params) => request.get('/trade/transaction/page', { params })

// ============ 用户管理（App用户） ============
export const getAppUserPage = (params) => request.get('/user/page', { params })
export const getAppUser = (id) => request.get(`/user/${id}`)
export const createAppUser = (data) => request.post('/user', data)
export const updateAppUser = (id, data) => request.put(`/user/${id}`, data)
export const deleteAppUser = (id) => request.delete(`/user/${id}`)
export const updateAppUserStatus = (id, status) => request.put(`/user/status/${id}?status=${status}`)
export const getAppUserTags = (id) => request.get(`/user/${id}/tags`)
export const updateAppUserTags = (id, data) => request.put(`/user/${id}/tags`, data)

// UserTag 用户标签
export const getUserTagPage = (params) => request.get('/user/tag/page', { params })
export const getUserTagList = () => request.get('/user/tag/list')
export const addUserTag = (data) => request.post('/user/tag', data)
export const updateUserTag = (data) => request.put('/user/tag', data)
export const deleteUserTag = (id) => request.delete(`/user/tag/${id}`)

// MemberLevel 会员等级
export const getMemberLevelPage = (params) => request.get('/user/level/page', { params })
export const getMemberLevelList = () => request.get('/user/level/list')
export const addMemberLevel = (data) => request.post('/user/level', data)
export const updateMemberLevel = (data) => request.put('/user/level', data)
export const deleteMemberLevel = (id) => request.delete(`/user/level/${id}`)

// Coupon 优惠券
export const getCouponPage = (params) => request.get('/user/coupon/page', { params })
export const addCoupon = (data) => request.post('/user/coupon', data)
export const updateCoupon = (data) => request.put('/user/coupon', data)
export const deleteCoupon = (id) => request.delete(`/user/coupon/${id}`)
export const updateCouponStatus = (id, status) => request.put(`/user/coupon/status/${id}?status=${status}`)

// AppMessage 消息
export const getAppMessagePage = (params) => request.get('/user/message/page', { params })
export const addAppMessage = (data) => request.post('/user/message', data)
export const deleteAppMessage = (id) => request.delete(`/user/message/${id}`)

// ============ 服务管理 ============
export const getServiceWorkerPage = (params) => request.get('/service/worker/page', { params })
export const getServiceWorker = (id) => request.get(`/service/worker/${id}`)
export const createServiceWorker = (data) => request.post('/service/worker', data)
export const updateServiceWorker = (id, data) => request.put(`/service/worker/${id}`, data)
export const deleteServiceWorker = (id) => request.delete(`/service/worker/${id}`)
export const getServiceWorkerList = () => request.get('/service/worker/list')
export const updateServiceWorkerStatus = (id, status) => request.put(`/service/worker/status/${id}?status=${status}`)
export const auditServiceWorker = (id, data) => request.put(`/service/worker/audit/${id}`, data)
export const getServiceWorkerTags = (id) => request.get(`/service/worker/${id}/tags`)
export const updateServiceWorkerTags = (id, data) => request.put(`/service/worker/${id}/tags`, data)

// ServiceWorkerTag 服务人员标签
export const getServiceWorkerTagPage = (params) => request.get('/service/worker-tag/page', { params })
export const getServiceWorkerTag = (id) => request.get(`/service/worker-tag/${id}`)
export const createServiceWorkerTag = (data) => request.post('/service/worker-tag', data)
export const updateServiceWorkerTag = (id, data) => request.put(`/service/worker-tag/${id}`, data)
export const deleteServiceWorkerTag = (id) => request.delete(`/service/worker-tag/${id}`)

// AuditRecord 审核记录
export const getAuditRecordPage = (params) => request.get('/service/audit/page', { params })
export const getServiceAudit = (id) => request.get(`/service/audit/${id}`)
export const auditRecord = (id, data) => request.put(`/service/audit/${id}/audit`, data)

// CommissionRecord 佣金记录
export const getCommissionRecordPage = (params) => request.get('/service/commission/page', { params })

// TipRecord 打赏记录
export const getTipRecordPage = (params) => request.get('/service/tip/page', { params })

// ServiceOrderSetting 工单设置
export const getServiceOrderSettingPage = (params) => request.get('/service/setting/page', { params })
export const getServiceOrderSetting = (id) => request.get(`/service/setting/${id}`)
export const createServiceOrderSetting = (data) => request.post('/service/setting', data)
export const updateServiceOrderSetting = (id, data) => request.put(`/service/setting/${id}`, data)
export const deleteServiceOrderSetting = (id) => request.delete(`/service/setting/${id}`)

// ============ 系统管理（后台用户） ============
export const getSysUserPage = (params) => request.get('/sys/user/page', { params })
export const addSysUser = (data) => request.post('/sys/user', data)
export const updateSysUser = (data) => request.put('/sys/user', data)
export const deleteSysUser = (id) => request.delete(`/sys/user/${id}`)
export const getSysUserById = (id) => request.get(`/sys/user/${id}`)

export const getSysRolePage = (params) => request.get('/sys/role/page', { params })
export const getSysRoleList = () => request.get('/sys/role/list')
export const addSysRole = (data) => request.post('/sys/role', data)
export const updateSysRole = (data) => request.put('/sys/role', data)
export const deleteSysRole = (id) => request.delete(`/sys/role/${id}`)
export const getSysRoleById = (id) => request.get(`/sys/role/${id}`)
export const getRoleMenuIds = (roleId) => request.get(`/sys/role/${roleId}/menus`)
export const saveRoleMenus = (roleId, menuIds) => request.post(`/sys/role/${roleId}/menus`, menuIds)

export const getSysMenuTree = () => request.get('/sys/menu/tree')
export const getSysMenuList = () => request.get('/sys/menu/list')
export const getUserMenus = (userId) => request.get(`/sys/menu/user/${userId}`)
export const addSysMenu = (data) => request.post('/sys/menu', data)
export const updateSysMenu = (data) => request.put('/sys/menu', data)
export const deleteSysMenu = (id) => request.delete(`/sys/menu/${id}`)

// ============ 用户扩展 ============
// 获取用户优惠券
export const getUserCoupons = (userId) => request.get('/user/coupon/page', { params: { userId } })

// ============ 员工管理 ============
export const getStaffPage = (params) => request.get('/system/staff/page', { params })
export const getStaffById = (id) => request.get(`/system/staff/${id}`)
export const addStaff = (data) => request.post('/system/staff', data)
export const updateStaff = (data) => request.put('/system/staff', data)
export const deleteStaff = (id) => request.delete(`/system/staff/${id}`)

// ============ 活动管理 ============
export const getActivityPage = (params) => request.get('/operation/activity/page', { params })
export const getActivityById = (id) => request.get(`/operation/activity/${id}`)
export const addActivity = (data) => request.post('/operation/activity', data)
export const updateActivity = (data) => request.put('/operation/activity', data)
export const deleteActivity = (id) => request.delete(`/operation/activity/${id}`)
export const getActivityList = () => request.get('/operation/activity/list')

// ============ 活动报名管理 ============
export const getActivityRegistrationPage = (params) => request.get('/operation/activity-registration/page', { params })
export const getActivityRegistrationById = (id) => request.get(`/operation/activity-registration/${id}`)
export const addActivityRegistration = (data) => request.post('/operation/activity-registration', data)
export const updateActivityRegistrationStatus = (id, status) => request.put(`/operation/activity-registration/${id}/status`, { params: { status } })
export const deleteActivityRegistration = (id) => request.delete(`/operation/activity-registration/${id}`)

// ============ 养老机构管理 ============
export const getInstitutionPage = (params) => request.get('/system/institution/page', { params })
export const getInstitutionList = (params) => request.get('/system/institution/list', { params })
export const getInstitutionById = (id) => request.get(`/system/institution/${id}`)
export const addInstitution = (data) => request.post('/system/institution', data)
export const updateInstitution = (data) => request.put('/system/institution', data)
export const deleteInstitution = (id) => request.delete(`/system/institution/${id}`)
export const updateInstitutionStatus = (id, status) => request.put(`/system/institution/status/${id}`, { params: { status } })

// ============ 健康资讯管理 ============
export const getHealthNewsPage = (params) => request.get('/operation/health-news/page', { params })
export const getHealthNewsById = (id) => request.get(`/operation/health-news/${id}`)
export const addHealthNews = (data) => request.post('/operation/health-news', data)
export const updateHealthNews = (data) => request.put('/operation/health-news', data)
export const deleteHealthNews = (id) => request.delete(`/operation/health-news/${id}`)

// ============ 轮播图管理 ============
export const getBannerPage = (params) => request.get('/operation/banner/page', { params })
export const getBannerById = (id) => request.get(`/operation/banner/${id}`)
export const addBanner = (data) => request.post('/operation/banner', data)
export const updateBanner = (data) => request.put('/operation/banner', data)
export const deleteBanner = (id) => request.delete(`/operation/banner/${id}`)

// ============ 健康服务订单 ============
export const getHealthServiceOrderPage = (params) => request.get('/health/service-order/page', { params })
export const getHealthServiceOrderById = (id) => request.get(`/health/service-order/${id}`)

// ============ 积分管理 ============
export const getPointsPage = (params) => request.get('/user/points/page', { params })
export const getPointsStats = (userId, type) => request.get('/user/points/stats', { params: { userId, type } })
export const adjustPoints = (userId, amount, type, source, remark) => request.put('/user/points/adjust', { params: { userId, amount, type, source, remark } })

// ============ 积分规则配置 ============
export const getPointRulePage = (params) => request.get('/point-rule/page', { params })
export const getPointRule = (id) => request.get(`/point-rule/${id}`)
export const addPointRule = (data) => request.post('/point-rule', data)
export const updatePointRule = (data) => request.put('/point-rule', data)
export const deletePointRule = (id) => request.delete(`/point-rule/${id}`)

// ============ 动态管理 ============
export const getDynamicPage = (params) => request.get('/operation/dynamic/page', { params })
export const getDynamicById = (id) => request.get(`/operation/dynamic/${id}`)
export const approveDynamic = (id, remark) => request.put(`/operation/dynamic/approve/${id}`, { params: { remark } })
export const rejectDynamic = (id, remark) => request.put(`/operation/dynamic/reject/${id}`, { params: { remark } })
export const offlineDynamic = (id) => request.put(`/operation/dynamic/offline/${id}`)
export const addDynamic = (data) => request.post('/operation/dynamic', data)
export const deleteDynamic = (id) => request.delete(`/operation/dynamic/${id}`)

// ============ 食谱管理 ============
export const getRecipePage = (params) => request.get('/operation/recipe/page', { params })
export const getRecipeList = (status) => request.get('/operation/recipe/list', { params: { status } })
export const getRecipeById = (id) => request.get(`/operation/recipe/${id}`)
export const addRecipe = (data) => request.post('/operation/recipe', data)
export const updateRecipe = (data) => request.put('/operation/recipe', data)
export const deleteRecipe = (id) => request.delete(`/operation/recipe/${id}`)
export const changeRecipeStatus = (id, status) => request.put(`/operation/recipe/status/${id}`, { params: { status } })

// ============ 疾病管理 ============
export const getDiseasePage = (params) => request.get('/health/disease/page', { params })
export const getDiseaseList = (status) => request.get('/health/disease/list', { params: { status } })
export const getDiseaseById = (id) => request.get(`/health/disease/${id}`)
export const addDisease = (data) => request.post('/health/disease', data)
export const updateDisease = (data) => request.put('/health/disease', data)
export const deleteDisease = (id) => request.delete(`/health/disease/${id}`)
export const changeDiseaseStatus = (id, status) => request.put(`/health/disease/status/${id}`, { params: { status } })

// ============ 视频管理 ============
export const getVideoPage = (params) => request.get('/operation/video/page', { params })
export const getVideoList = (status) => request.get('/operation/video/list', { params: { status } })
export const getVideoById = (id) => request.get(`/operation/video/${id}`)
export const addVideo = (data) => request.post('/operation/video', data)
export const updateVideo = (data) => request.put('/operation/video', data)
export const deleteVideo = (id) => request.delete(`/operation/video/${id}`)
export const changeVideoStatus = (id, status) => request.put(`/operation/video/status/${id}`, { params: { status } })

// ============ 运营管理 - 话题管理 ============
export const getTopicPage = (params) => request.get('/operation/topic/page', { params })
export const addTopic = (data) => request.post('/operation/topic', data)
export const updateTopic = (data) => request.put('/operation/topic', data)
export const deleteTopic = (id) => request.delete(`/operation/topic/${id}`)

// ============ 运营管理 - 社区管理 ============
export const getCommunityPage = (params) => request.get('/operation/community/page', { params })
export const addCommunity = (data) => request.post('/operation/community', data)
export const updateCommunity = (data) => request.put('/operation/community', data)
export const deleteCommunity = (id) => request.delete(`/operation/community/${id}`)

// ============ 运营管理 - 评论管理 ============
export const getCommentPage = (params) => request.get('/operation/comment/page', { params })
export const updateCommentStatus = (id, status) => request.put(`/operation/comment/${id}/status`, { params: { status } })
export const deleteComment = (id) => request.delete(`/operation/comment/${id}`)

// ============ 运营管理 - 标签管理 ============
export const getTagPage = (params) => request.get('/operation/tag/page', { params })
export const addTag = (data) => request.post('/operation/tag', data)
export const updateTag = (data) => request.put('/operation/tag', data)
export const deleteTag = (id) => request.delete(`/operation/tag/${id}`)

// ============ 运营管理 - 举报管理 ============
export const getReportPage = (params) => request.get('/operation/report/page', { params })
export const handleReport = (id, data) => request.put(`/operation/report/${id}/handle`, data)

// ============ 运营管理 - 内容管理 ============
export const getContentPage = (params) => request.get('/operation/content/page', { params })
export const addContent = (data) => request.post('/operation/content', data)
export const updateContent = (data) => request.put('/operation/content', data)
export const deleteContent = (id) => request.delete(`/operation/content/${id}`)
export const getContentAggregation = () => request.get('/content/aggregation')

// ============ 运营管理 - 留言管理 ============
export const getFeedbackPage = (params) => request.get('/operation/feedback/page', { params })
export const replyFeedback = (id, data) => request.put(`/operation/feedback/${id}/reply`, data)

// ============ 运营管理 - 搜索记录 ============
export const getSearchLogPage = (params) => request.get('/operation/search-log/page', { params })

// ============ 运营管理 - 消息通知 ============
export const getNotificationPage = (params) => request.get('/operation/notification/page', { params })
export const addNotification = (data) => request.post('/operation/notification', data)
export const updateNotification = (data) => request.put('/operation/notification', data)
export const deleteNotification = (id) => request.delete(`/operation/notification/${id}`)

// ============ 运营管理 - 意见反馈 ============
export const getOpinionPage = (params) => request.get('/operation/opinion/page', { params })
export const replyOpinion = (id, data) => request.put(`/operation/opinion/${id}/reply`, data)

// ============ 运营管理 - 常见问题 ============
export const getFaqPage = (params) => request.get('/operation/faq/page', { params })
export const addFaq = (data) => request.post('/operation/faq', data)
export const updateFaq = (data) => request.put('/operation/faq', data)
export const deleteFaq = (id) => request.delete(`/operation/faq/${id}`)

// ============ 运营管理 - 帮助中心 ============
export const getHelpPage = (params) => request.get('/operation/help/page', { params })
export const addHelp = (data) => request.post('/operation/help', data)
export const updateHelp = (data) => request.put('/operation/help', data)
export const deleteHelp = (id) => request.delete(`/operation/help/${id}`)


// ============ 交易管理 - 支付配置 ============
export const getPaymentConfigPage = (params) => request.get('/trade/payment-config/page', { params })
export const addPaymentConfig = (data) => request.post('/trade/payment-config', data)
export const updatePaymentConfig = (data) => request.put('/trade/payment-config', data)
export const deletePaymentConfig = (id) => request.delete(`/trade/payment-config/${id}`)

// ============ 交易管理 - 物流配送 ============
export const getDeliveryPage = (params) => request.get('/trade/delivery/page', { params })
export const updateDeliveryStatus = (id, status) => request.put(`/trade/delivery/${id}/status`, { params: { status } })

// ============ 交易管理 - 财务记录 ============
export const getFinancePage = (params) => request.get('/trade/finance/page', { params })

// ============ 系统管理 - 系统配置 ============
export const getSystemConfigPage = (params) => request.get('/system/system-config/page', { params })
export const addSystemConfig = (data) => request.post('/system/system-config', data)
export const updateSystemConfig = (data) => request.put('/system/system-config', data)
export const deleteSystemConfig = (id) => request.delete(`/system/system-config/${id}`)

// ============ 系统管理 - 短信配置 ============
export const getSmsConfigPage = (params) => request.get('/system/sms-config/page', { params })
export const addSmsConfig = (data) => request.post('/system/sms-config', data)
export const updateSmsConfig = (data) => request.put('/system/sms-config', data)
export const deleteSmsConfig = (id) => request.delete(`/system/sms-config/${id}`)

// ============ 系统管理 - 关于我们 ============
export const getAbout = () => request.get('/system/about')

// ============ 系统管理 - 服务协议 ============
export const getAgreementPage = (params) => request.get('/system/agreement/page', { params })
export const addAgreement = (data) => request.post('/system/agreement', data)
export const updateAgreement = (data) => request.put('/system/agreement', data)
export const deleteAgreement = (id) => request.delete(`/system/agreement/${id}`)

// ============ 用户管理 - 用户报告 ============
export const getUserReportPage = (params) => request.get('/user/report/page', { params })
export const getUserReportById = (id) => request.get(`/user/report/${id}`)

// ============ 数据分析模块 ============
export const getUserAnalysisPage = () => request.get('/data/user-analysis/page')
export const getTradeAnalysisPage = () => request.get('/data/trade-analysis/page')
export const getServiceAnalysisPage = () => request.get('/data/service-analysis/page')
export const getProductAnalysisPage = () => request.get('/data/product-analysis/page')
export const getRevenuePage = () => request.get('/data/revenue/page')
export const getPerformancePage = () => request.get('/data/performance/page')
export const getExportPage = (params) => request.get('/data/export/page', { params })
export const getTrackingPage = (params) => request.get('/data/tracking/page', { params })
export const getDictPage = (params) => request.get('/data/dict/page', { params })
export const getActionLogPage = (params) => request.get('/data/action-log/page', { params })
export const getDataDashboard = () => request.get('/data/dashboard')
export const getUserAgeAnalysis = () => request.get('/data/user-age-analysis')
export const getUserGenderAnalysis = () => request.get('/data/user-gender-analysis')
export const getRepurchaseAnalysis = () => request.get('/data/repurchase-analysis')
export const getEvaluationAnalysis = () => request.get('/data/evaluation-analysis')
export const getTradePayMethod = () => request.get('/data/trade-analysis/pay-method')
export const getTradeHourly = () => request.get('/data/trade-analysis/hourly')
export const getWorkerRank = () => request.get('/data/service-analysis/worker-rank')
export const getAreaRank = () => request.get('/data/service-analysis/area-rank')
// ============ 用户管理 - 会话 ============
export const getConversationPage = (params) => request.get('/user/conversation/page', { params })
export const getConversationMessages = (id) => request.get(`/user/conversation/${id}/messages`)
export const sendConversationMessage = (id, data) => request.post(`/user/conversation/${id}/messages`, data)
export const deleteConversation = (id) => request.delete(`/user/conversation/${id}`)

// ============ 用户管理 - 成长值规则 ============
export const getGrowthRulePage = (params) => request.get('/user/growth-rule/page', { params })
export const saveGrowthRule = (data) => request.post('/user/growth-rule', data)
export const updateGrowthRule = (data) => request.put('/user/growth-rule', data)
export const updateGrowthRuleStatus = (id, status) => request.put(`/user/growth-rule/${id}/status`, { params: { status } })
export const deleteGrowthRule = (id) => request.delete(`/user/growth-rule/${id}`)

// ============ 商品管理 - 服务项目管理 ============
export const getServiceProjectPage = (params) => request.get('/product/service-project/page', { params })
export const saveServiceProject = (data) => request.post('/product/service-project', data)
export const updateServiceProject = (data) => request.put('/product/service-project', data)
export const updateServiceProjectStatus = (id, status) => request.put(`/product/service-project/${id}/status`, { params: { status } })
export const deleteServiceProject = (id) => request.delete(`/product/service-project/${id}`)

// ============ 运营管理 - 活动字段 ============
export const getActivityFieldPage = (params) => request.get('/operation/activity-field/page', { params })
export const saveActivityField = (data) => request.post('/operation/activity-field', data)
export const updateActivityField = (data) => request.put('/operation/activity-field', data)
export const updateActivityFieldStatus = (id, status) => request.put(`/operation/activity-field/${id}/status`, { params: { status } })
export const deleteActivityField = (id) => request.delete(`/operation/activity-field/${id}`)

// ============ 系统管理 - 药品单位 ============
export const getMedicineUnitPage = (params) => request.get('/system/medicine-unit/page', { params })
export const saveMedicineUnit = (data) => request.post('/system/medicine-unit', data)
export const updateMedicineUnit = (data) => request.put('/system/medicine-unit', data)
export const updateMedicineUnitStatus = (id, status) => request.put(`/system/medicine-unit/${id}/status`, { params: { status } })
export const deleteMedicineUnit = (id) => request.delete(`/system/medicine-unit/${id}`)

// ============ 系统管理 - 缓存监控 ============
export const getCacheStats = () => request.get('/sys/cache/stats')

export const getUserSocialAnalysis = () => request.get('/data/user-social-analysis')
