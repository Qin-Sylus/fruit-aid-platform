// @/api/charity.js
import request from '@/utils/request'

// 活动相关接口 - 全部使用真实API调用
export const getActivities = (params) => {
    console.log('调用真实API - 获取活动列表')
    return request({
        url: '/charity/activities',
        method: 'get',
        params
    })
}

export const getActivityDetail = (id) => {
    console.log('调用真实API - 获取活动详情')
    return request({
        url: `/charity/activity/${id}`,
        method: 'get'
    })
}

export const addActivity = (data) => {
    console.log('调用真实API - 添加活动')
    return request({
        url: '/charity/activity',
        method: 'post',
        data
    })
}

// 报名相关
export const applyActivity = (data) => {
    return request({
        url: '/charity/apply',
        method: 'post',
        data
    })
}

export const getApplies = (activityId) => {
    return request({
        url: `/charity/apply/${activityId}`,
        method: 'get'
    })
}

export const auditApply = (id, data) => {
    return request({
        url: `/charity/apply/audit/${id}`,
        method: 'post',
        data
    })
}

// 点赞收藏
export const likeActivity = (activityId) => {
    return request({
        url: `/charity/like/${activityId}`,
        method: 'post'
    })
}

export const collectActivity = (activityId) => {
    return request({
        url: `/charity/collect/${activityId}`,
        method: 'post'
    })
}

// 评论
export const getComments = (activityId, params) => {
    return request({
        url: `/charity/comments/${activityId}`,
        method: 'get',
        params
    })
}

export const addComment = (data) => {
    return request({
        url: '/charity/comment',
        method: 'post',
        data
    })
}

// 公益订单
export const createCharityOrder = (data) => {
    return request({
        url: '/charity/order',
        method: 'post',
        data
    })
}

export const payOrder = (orderNo) => {
    return request({
        url: `/charity/order/pay/${orderNo}`,
        method: 'post'
    })
}

// 用户相关
export const getUserActivities = () => {
    return request({
        url: '/charity/user/activities',
        method: 'get'
    })
}

export const getStoreActivities = (storeId) => {
    return request({
        url: '/charity/store/activities',
        method: 'get',
        params: { storeId }
    })
}

// 更新活动
export const updateActivity = (id, data) => {
    console.log('调用真实API - 更新活动')
    return request({
        url: `/charity/activities/${id}`,
        method: 'put',
        data
    })
}

// 删除活动
export const deleteActivity = (id) => {
    console.log('调用真实API - 删除活动')
    return request({
        url: `/charity/activities/${id}`,
        method: 'delete'
    })
}

// 商品相关
export const getGoodsList = () => {
    return request({
        url: '/goods',
        method: 'get'
    })
}