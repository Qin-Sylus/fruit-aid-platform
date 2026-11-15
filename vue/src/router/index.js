import { createRouter, createWebHistory } from 'vue-router'
import MerchantProfile from '@/views/merchant/MerchantProfile.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 管理端路由
    {
      path: '/',
      component: () => import('@/views/Manager.vue'),
      redirect: '/home',
      children: [
        { path: 'person', component: () => import('@/views/manager/Person.vue') },
        { path: 'home', component: () => import('@/views/manager/Home.vue') },
        { path: 'admin', component: () => import('@/views/manager/Admin.vue') },
        { path: 'notice', component: () => import('@/views/manager/Notice.vue') },
        { path: 'category', component: () => import('@/views/manager/Category.vue') },
        { path: 'goods', component: () => import('@/views/manager/Goods.vue') },
        { path: 'goodsStock', component: () => import('@/views/manager/GoodsStock.vue') },
        { path: 'user', component: () => import('@/views/manager/User.vue') },
        { path: 'buy', component: () => import('@/views/manager/Buy.vue') },
        { path: 'orders', component: () => import('@/views/manager/Orders.vue') },
        { path: 'help', component: () => import('@/views/manager/Help.vue') },
        { path: 'feedback', component: () => import('@/views/manager/Feedback.vue') },
        { path: 'cart', component: () => import('@/views/manager/Cart.vue') },
        { path: 'charity-manager', name: 'CharityManager', component: () => import('@/views/manager/CharityManager.vue'), meta: { title: '慈善活动管理' }},
        { path: 'chat-admin', name: 'ChatAdmin', component: () => import('@/views/manager/ChatAdmin.vue'), meta: { title: '在线客服管理', requiresAuth: true, roles: ['ADMIN'] }}
      ]
    },
    // 商家端路由
    {
      path: '/merchant',
      component: () => import('@/views/merchant/MerchantLayout.vue'),
      redirect: '/merchant/home',
      children: [
        {
          path: 'home',
          name: 'MerchantHome',
          component: () => import('@/views/merchant/MerchantHome.vue'),
          meta: { title: '商家首页' }
        },
        {
          path: 'store-create',
          name: 'MerchantStoreCreate',
          component: () => import('@/views/merchant/MerchantStoreCreate.vue'),
          meta: { title: '创建店铺' }
        },
        {
          path: 'store-manage',
          name: 'MerchantStoreManage',
          component: () => import('@/views/merchant/MerchantStoreManage.vue'),
          meta: { title: '店铺管理' }
        },
        {
          path: 'store-goods',
          name: 'MerchantGoods',
          component: () => import('@/views/merchant/MerchantGoods.vue'),
          meta: { title: '商品管理' }
        },
        {
          path: 'store-orders',
          name: 'MerchantOrders',
          component: () => import('@/views/merchant/MerchantOrders.vue'),
          meta: { title: '订单管理' }
        },
        {
          path: 'statistics',
          name: 'MerchantStatistics',
          component: () => import('@/views/merchant/MerchantStatistics.vue'),
          meta: { title: '销售统计' }
        },
        {
          path: 'profile',
          name: 'MerchantProfile',
          component: MerchantProfile,
          meta: { title: '个人中心' }
        },
        // 新增：商家店铺活动报名
        {
          path: 'store-apply',
          name: 'MerchantStoreApply',
          component: () => import('@/views/merchant/StoreApply.vue'),
          meta: { title: '店铺活动报名' }
        }
      ]
    },

    // 慈善活动路由
    {
      path: '/charity',
      component: () => import('@/views/Manager.vue'), // 使用相同的布局
      redirect: '/charity/activities',
      children: [
        // 活动列表页面
        {
          path: 'activities',
          name: 'CharityActivities',
          component: () => import('@/views/charity/CharityActivityList.vue'),
          meta: { title: '公益活动' }
        },
        // 活动详情页面
        {
          path: 'activity/:id',
          name: 'CharityActivityDetail',
          component: () => import('@/views/charity/CharityActivityDetail.vue'),
          meta: { title: '活动详情' },
          props: true // 将路由参数作为props传递给组件
        }
      ]
    },

    // 公共路由
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { title: '注册' }
    },
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('system-user')
  const user = userStr ? JSON.parse(userStr) : {}

  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title + ' - 水果售卖商城'
  }

  // 如果未登录且不是登录/注册页面，跳转到登录页
  if (!user.id && !['/login', '/register', '/customer-service'].includes(to.path)) {
    next('/login')
    return
  }

  // 根据角色处理路由权限
  if (user.role === 'MERCHANT') {
    // 商家可以访问：商家端、个人中心、慈善活动、用户客服
    if (to.path.startsWith('/merchant') ||
        to.path === '/person' ||
        to.path.startsWith('/charity') ||
        to.path === '/customer-service') {
      next()
    } else {
      next('/merchant/home')
    }
  } else if (user.role === 'ADMIN') {
    // 管理员可以访问：管理端、慈善活动、个人中心
    // 但不能访问商家端
    if (to.path.startsWith('/merchant')) {
      next('/home')
    } else {
      next()
    }
  } else if (user.role === 'USER') {
    // 普通用户可以访问：慈善活动、个人中心、购买、客服等
    // 但不能访问管理端和商家端
    if (to.path === '/' || to.path.startsWith('/merchant') ||
        (to.meta.roles && to.meta.roles.includes('ADMIN'))) {
      next('/customer-service') // 跳转到用户客服页面
    } else {
      next()
    }
  } else {
    // 其他情况
    next()
  }
})

export default router