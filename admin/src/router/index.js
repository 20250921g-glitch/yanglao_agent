import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      // 首页
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'Monitor' }
      },
      {
        path: 'dashboard/booking',
        name: 'BookingBoard',
        component: () => import('@/views/dashboard/booking.vue'),
        meta: { title: '预约看板', icon: 'Calendar' }
      },
      // 健康管理
      {
        path: 'health/elder',
        name: 'Elder',
        component: () => import('@/views/health/elder.vue'),
        meta: { title: '老人档案', icon: 'User' }
      },
      {
        path: 'health/family',
        name: 'Family',
        component: () => import('@/views/health/family.vue'),
        meta: { title: '家属管理', icon: 'UserFilled' }
      },
      {
        path: 'health/record',
        name: 'HealthRecord',
        component: () => import('@/views/health/record.vue'),
        meta: { title: '健康记录', icon: 'DataAnalysis' }
      },
      {
        path: 'health/health-data',
        name: 'HealthData',
        component: () => import('@/views/health/health-data.vue'),
        meta: { title: '健康数据', icon: 'TrendCharts' }
      },
      {
        path: 'health/disease',
        name: 'Disease',
        component: () => import('@/views/health/disease.vue'),
        meta: { title: '疾病宝典', icon: 'FirstAidKit' }
      },
      // 服务管理
      {
        path: 'service/worker',
        name: 'ServiceWorker',
        component: () => import('@/views/service/worker.vue'),
        meta: { title: '服务人员', icon: 'Avatar' }
      },
      {
        path: 'service/worker-tag',
        name: 'ServiceWorkerTag',
        component: () => import('@/views/service/worker-tag.vue'),
        meta: { title: '服务人员标签', icon: 'PriceTag' }
      },
      {
        path: 'service/audit',
        name: 'ServiceAudit',
        component: () => import('@/views/service/audit.vue'),
        meta: { title: '审核管理', icon: 'Tickets' }
      },
      {
        path: 'service/audit-detail/:id',
        name: 'AuditDetail',
        component: () => import('@/views/service/audit-detail.vue'),
        meta: { title: '审核详情', hidden: true }
      },
      {
        path: 'service/worker-add',
        name: 'WorkerAdd',
        component: () => import('@/views/service/worker-add.vue'),
        meta: { title: '新增服务人员', hidden: true }
      },
      {
        path: 'service/worker-detail/:id',
        name: 'WorkerDetail',
        component: () => import('@/views/service/worker-detail.vue'),
        meta: { title: '服务人员详情', hidden: true }
      },
      {
        path: 'service/order',
        name: 'ServiceOrder',
        component: () => import('@/views/service/order.vue'),
        meta: { title: '工单管理', icon: 'Suitcase' }
      },
      {
        path: 'service/order-detail/:id',
        name: 'ServiceOrderDetail',
        component: () => import('@/views/service/order-detail.vue'),
        meta: { title: '工单详情', hidden: true }
      },
      {
        path: 'service/commission',
        name: 'Commission',
        component: () => import('@/views/service/commission.vue'),
        meta: { title: '佣金记录', icon: 'Money' }
      },
      {
        path: 'service/tip',
        name: 'Tip',
        component: () => import('@/views/service/tip.vue'),
        meta: { title: '打赏记录', icon: 'Coin' }
      },
      {
        path: 'service/work-setting',
        name: 'WorkSetting',
        component: () => import('@/views/service/work-setting.vue'),
        meta: { title: '工单设置', icon: 'Setting' }
      },
      // 商品管理
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: 'product/category',
        name: 'ProductCategory',
        component: () => import('@/views/product/category.vue'),
        meta: { title: '商品分类', icon: 'Grid' }
      },
      {
        path: 'product/add',
        name: 'ProductAdd',
        component: () => import('@/views/product/add.vue'),
        meta: { title: '新增商品', hidden: true }
      },
      {
        path: 'product/params',
        name: 'ProductParams',
        component: () => import('@/views/product/params.vue'),
        meta: { title: '参数管理', icon: 'Tools' }
      },
      {
        path: 'product/settings',
        name: 'ProductSettings',
        component: () => import('@/views/product/settings.vue'),
        meta: { title: '通用设置', icon: 'Setting' }
      },
      // 运营管理
      {
        path: 'operation/evaluation',
        name: 'Evaluation',
        component: () => import('@/views/operation/evaluation.vue'),
        meta: { title: '测评管理', icon: 'Document' }
      },
      {
        path: 'operation/food',
        name: 'Food',
        component: () => import('@/views/operation/food.vue'),
        meta: { title: '食物管理', icon: { value: 'Food', theme: 'filled' } }
      },
      {
        path: 'operation/topic',
        name: 'Topic',
        component: () => import('@/views/operation/topic.vue'),
        meta: { title: '话题管理', icon: 'ChatLineSquare' }
      },
      {
        path: 'operation/community',
        name: 'Community',
        component: () => import('@/views/operation/community.vue'),
        meta: { title: '生活圈', icon: 'Share' }
      },
      {
        path: 'operation/dynamic',
        name: 'Dynamic',
        component: () => import('@/views/operation/dynamic.vue'),
        meta: { title: '动态管理', icon: 'ChatLineSquare' }
      },
      {
        path: 'operation/recipe',
        name: 'Recipe',
        component: () => import('@/views/operation/recipe.vue'),
        meta: { title: '食谱管理', icon: 'Cooking' }
      },
      {
        path: 'operation/video',
        name: 'Video',
        component: () => import('@/views/operation/video.vue'),
        meta: { title: '视频管理', icon: 'VideoPlay' }
      },
      {
        path: 'operation/comment',
        name: 'Comment',
        component: () => import('@/views/operation/comment.vue'),
        meta: { title: '评论管理', icon: 'ChatDotSquare' }
      },
      {
        path: 'operation/tag',
        name: 'OpTag',
        component: () => import('@/views/operation/tag.vue'),
        meta: { title: '标签管理', icon: 'PriceTag' }
      },

      {
        path: 'operation/content',
        name: 'OpContent',
        component: () => import('@/views/operation/content.vue'),
        meta: { title: '内容管理', icon: 'Document' }
      },
      {
        path: 'operation/content-aggregation',
        name: 'ContentAggregation',
        component: () => import('@/views/operation/content-aggregation.vue'),
        meta: { title: '内容聚合', icon: 'Files' }
      },


      // 运营活动
      {
        path: 'operation/activity',
        name: 'Activity',
        component: () => import('@/views/operation/activity.vue'),
        meta: { title: '活动管理', icon: 'Present' }
      },
      {
        path: 'operation/activity-registration',
        name: 'ActivityRegistration',
        component: () => import('@/views/operation/activity-registration.vue'),
        meta: { title: '活动报名', icon: 'UserFilled' }
      },
      {
        path: 'operation/health-news',
        name: 'HealthNews',
        component: () => import('@/views/operation/health-news.vue'),
        meta: { title: '健康资讯', icon: 'Document' }
      },
      {
        path: 'operation/banner',
        name: 'Banner',
        component: () => import('@/views/operation/banner.vue'),
        meta: { title: '轮播图', icon: 'Picture' }
      },
      {
        path: 'operation/institution',
        name: 'Institution',
        component: () => import('@/views/operation/institution.vue'),
        meta: { title: '养老机构', icon: 'OfficeBuilding' }
      },




      // 用户管理
      {
        path: 'user/list',
        name: 'AppUser',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户列表', icon: 'User' }
      },
      {
        path: 'user/detail/:id',
        name: 'UserDetail',
        component: () => import('@/views/user/detail.vue'),
        meta: { title: '用户详情', hidden: true }
      },
      {
        path: 'user/add',
        name: 'UserAdd',
        component: () => import('@/views/user/add.vue'),
        meta: { title: '新增用户', hidden: true }
      },
      {
        path: 'user/report',
        name: 'UserReport',
        component: () => import('@/views/user/report.vue'),
        meta: { title: '报告管理', icon: 'Document' }
      },
      {
        path: 'user/tag',
        name: 'UserTag',
        component: () => import('@/views/user/tag.vue'),
        meta: { title: '用户标签', icon: 'PriceTag' }
      },
      {
        path: 'user/level',
        name: 'MemberLevel',
        component: () => import('@/views/user/level.vue'),
        meta: { title: '会员等级', icon: 'Medal' }
      },
      {
        path: 'user/coupon',
        name: 'Coupon',
        component: () => import('@/views/user/coupon.vue'),
        meta: { title: '优惠券', icon: 'Ticket' }
      },
      {
        path: 'user/coupon-detail/:id',
        name: 'CouponDetail',
        component: () => import('@/views/user/coupon-detail.vue'),
        meta: { title: '优惠券详情', hidden: true }
      },
      {
        path: 'user/level-detail/:id',
        name: 'LevelDetail',
        component: () => import('@/views/user/level-detail.vue'),
        meta: { title: '等级详情', hidden: true }
      },
      {
        path: 'user/points',
        name: 'Points',
        component: () => import('@/views/user/points.vue'),
        meta: { title: '积分管理', icon: 'Coin' }
      },
      {
        path: 'user/message',
        name: 'AppMessage',
        component: () => import('@/views/user/message.vue'),
        meta: { title: '消息群发', icon: 'ChatDotRound' }
      },
      // 交易管理
      {
        path: 'trade/product-order',
        name: 'ProductOrder',
        component: () => import('@/views/trade/product-order.vue'),
        meta: { title: '全部订单', icon: 'Tickets' }
      },
      {
        path: 'trade/order-detail/:id',
        name: 'TradeOrderDetail',
        component: () => import('@/views/trade/order-detail.vue'),
        meta: { title: '订单详情', hidden: true }
      },
      {
        path: 'trade/after-sale-detail/:id',
        name: 'AfterSaleDetail',
        component: () => import('@/views/trade/after-sale-detail.vue'),
        meta: { title: '售后详情', hidden: true }
      },
      {
        path: 'trade/refund',
        name: 'Refund',
        component: () => import('@/views/trade/refund.vue'),
        meta: { title: '售后管理', icon: 'Box' }
      },
      {
        path: 'trade/order-evaluation',
        name: 'OrderEvaluation',
        component: () => import('@/views/trade/order-evaluation.vue'),
        meta: { title: '评价管理', icon: 'Star' }
      },
      {
        path: 'trade/withdrawal',
        name: 'Withdrawal',
        component: () => import('@/views/trade/withdrawal.vue'),
        meta: { title: '提现记录', icon: 'Money' }
      },
      {
        path: 'trade/withdrawal-detail/:id',
        name: 'WithdrawalDetail',
        component: () => import('@/views/trade/withdrawal-detail.vue'),
        meta: { title: '提现详情', hidden: true }
      },
      {
        path: 'trade/refund-reason',
        name: 'RefundReason',
        component: () => import('@/views/trade/refund-reason.vue'),
        meta: { title: '退款原因', icon: 'QuestionFilled' }
      },
      // 交易配置
      {
        path: 'trade/payment-config',
        name: 'PaymentConfig',
        component: () => import('@/views/trade/payment-config.vue'),
        meta: { title: '通用设置', icon: 'Setting' }
      },

      // 数据分析
      {
        path: 'data/user-analysis',
        name: 'UserAnalysis',
        component: () => import('@/views/data/user-analysis.vue'),
        meta: { title: '用户概况', icon: 'TrendCharts' }
      },
      {
        path: 'data/user-age-analysis',
        name: 'UserAgeAnalysis',
        component: () => import('@/views/data/user-age-analysis.vue'),
        meta: { title: '年龄分析', icon: 'TrendCharts' }
      },
      {
        path: 'data/user-gender-analysis',
        name: 'UserGenderAnalysis',
        component: () => import('@/views/data/user-gender-analysis.vue'),
        meta: { title: '性别分析', icon: 'TrendCharts' }
      },
      {
        path: 'data/repurchase-analysis',
        name: 'RepurchaseAnalysis',
        component: () => import('@/views/data/repurchase-analysis.vue'),
        meta: { title: '复购分析', icon: 'TrendCharts' }
      },
      {
        path: 'data/evaluation-analysis',
        name: 'EvaluationAnalysis',
        component: () => import('@/views/data/evaluation-analysis.vue'),
        meta: { title: '评价统计', icon: 'TrendCharts' }
      },
      {
        path: 'data/trade-analysis',
        name: 'TradeAnalysis',
        component: () => import('@/views/data/trade-analysis.vue'),
        meta: { title: '交易分析', icon: 'DataAnalysis' }
      },
      {
        path: 'data/service-analysis',
        name: 'ServiceAnalysis',
        component: () => import('@/views/data/service-analysis.vue'),
        meta: { title: '服务分析', icon: 'PieChart' }
      },
      {
        path: 'data/product-analysis',
        name: 'ProductAnalysis',
        component: () => import('@/views/data/product-analysis.vue'),
        meta: { title: '商品分析', icon: 'Histogram' }
      },

      {
        path: 'data/export',
        name: 'DataExport',
        component: () => import('@/views/data/export.vue'),
        meta: { title: '数据导出', icon: 'Download' }
      },


      {
        path: 'data/dict',
        name: 'DataDict',
        component: () => import('@/views/data/dict.vue'),
        meta: { title: '数据字典', icon: 'Collection' }
      },


      // 系统管理
      {
        path: 'system/staff',
        name: 'Staff',
        component: () => import('@/views/system/staff.vue'),
        meta: { title: '员工管理', icon: 'UserFilled' }
      },
      {
        path: 'system/staff-detail/:id',
        name: 'StaffDetail',
        component: () => import('@/views/system/staff-detail.vue'),
        meta: { title: '员工详情', hidden: true }
      },

      {
        path: 'system/profile',
        name: 'Profile',
        component: () => import('@/views/system/profile.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' }
      },
      {
        path: 'system/operation-log',
        name: 'OperationLog',
        component: () => import('@/views/system/operation-log.vue'),
        meta: { title: '操作日志', icon: 'Operation' }
      },
      {
        path: 'system/role',
        name: 'SysRole',
        component: () => import('@/views/system/role.vue'),
        meta: { title: '角色管理', icon: 'Key', requiresAdmin: true }
      },
      {
        path: 'system/user',
        name: 'SysUser',
        component: () => import('@/views/system/user.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAdmin: true }
      },
      {
        path: 'health/elder-detail/:id',
        name: 'ElderDetail',
        component: () => import('@/views/health/elder-detail.vue'),
        meta: { title: '老人档案详情', hidden: true }
      },
      {
        path: 'operation/evaluation-detail/:id',
        name: 'EvaluationDetail',
        component: () => import('@/views/operation/evaluation-detail.vue'),
        meta: { title: '测评详情', hidden: true }
      },
      {
        path: 'system/role-detail/:id',
        name: 'RoleDetail',
        component: () => import('@/views/system/role-detail.vue'),
        meta: { title: '角色详情', hidden: true }
      },
      {
        path: 'system/system-config',
        name: 'SystemConfig',
        component: () => import('@/views/system/system-config.vue'),
        meta: { title: '系统配置', icon: 'Setting' }
      },

      {
        path: 'system/menu',
        name: 'Menu',
        component: () => import('@/views/system/menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu', requiresAdmin: true }
      },

      {
        path: 'system/agreement',
        name: 'Agreement',
        component: () => import('@/views/system/agreement.vue'),
        meta: { title: '协议管理', icon: 'Document' }
      },
      {
        path: 'trade/finance',
        name: 'Finance',
        component: () => import('@/views/trade/finance.vue'),
        meta: { title: '财务明细', icon: 'Coin' }
      },
      {
        path: 'data/performance',
        name: 'Performance',
        component: () => import('@/views/data/performance.vue'),
        meta: { title: '业绩统计', icon: 'Trophy' }
      },

      // ===== 批次3：补齐 8 个缺失页 =====
      {
        path: 'user/conversation',
        name: 'Conversation',
        component: () => import('@/views/user/conversation.vue'),
        meta: { title: '会话', icon: 'ChatDotRound' }
      },
      {
        path: 'user/growth-rule',
        name: 'GrowthRule',
        component: () => import('@/views/user/growth-rule.vue'),
        meta: { title: '成长值规则', icon: 'TrendCharts' }
      },
      {
        path: 'user/point-rule',
        name: 'PointRule',
        component: () => import('@/views/marketing/point-rule.vue'),
        meta: { title: '积分规则', icon: 'Coin' }
      },
      {
        path: 'product/service-project',
        name: 'ServiceProject',
        component: () => import('@/views/product/service-project.vue'),
        meta: { title: '服务项目管理', icon: 'Service' }
      },
      {
        path: 'operation/activity-field',
        name: 'ActivityField',
        component: () => import('@/views/operation/activity-field.vue'),
        meta: { title: '活动字段管理', icon: 'Tickets' }
      },
      {
        path: 'operation/dynamic-preview',
        name: 'DynamicPreview',
        component: () => import('@/views/operation/dynamic-preview.vue'),
        meta: { title: '动态预览', icon: 'View' }
      },
      {
        path: 'data/user-social',
        name: 'UserSocial',
        component: () => import('@/views/data/user-social.vue'),
        meta: { title: '用户社交统计', icon: 'PieChart' }
      },
      {
        path: 'system/medicine-unit',
        name: 'MedicineUnit',
        component: () => import('@/views/system/medicine-unit.vue'),
        meta: { title: '药品单位管理', icon: 'FirstAidKit' }
      },
      {
        path: 'system/reset-password',
        name: 'ResetPassword',
        component: () => import('@/views/system/reset-password.vue'),
        meta: { title: '重置密码', icon: 'Key' }
      },
      {
        path: 'system/cache',
        name: 'CacheMonitor',
        component: () => import('@/views/system/cache.vue'),
        meta: { title: '缓存监控', icon: 'DataLine' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.path === '/login') {
    next()
    return
  }
  
  if (!userStore.token) {
    next('/login')
    return
  }
  
  if (to.meta.requiresAdmin && !userStore.isSuperAdmin) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
