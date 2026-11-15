package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.entity.Goods;
import com.example.entity.Orders;
import com.example.exception.CustomException;
import com.example.mapper.OrdersMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业务处理
 **/
@Service
public class OrdersService {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private GoodsService goodsService;
    /*** 新增*/
    @Transactional
    public void add(Orders orders) {
        orders.setOrderNo(IdUtil.fastSimpleUUID());  // 唯一的订单编号
        orders.setTime(DateUtil.now());
        orders.setStatus("待支付");
        // 扣减库存
        Goods goods = goodsService.selectById(orders.getGoodsId());
        if (goods == null) {
            throw new CustomException("商品不存在");
        }
        int store = goods.getStore() - orders.getNum();
        if (store < 0) {
            throw new CustomException("商品库存不足");
        }
        goods.setStore(store);
        goodsService.updateById(goods);
        ordersMapper.insert(orders);
    }

    /**
     * 删除0
     */
    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
    }

    /**
     * 修改状态
     */
    public void updateById(Orders orders) {
        if ("已取消".equals(orders.getStatus())) { //用户取消订单  要返还库存
            Integer goodsId = orders.getGoodsId();
            Goods goods = goodsService.selectById(goodsId);
            if (goods != null) {
                goods.setStore(goods.getStore() + orders.getNum());
                goodsService.updateById(goods);
            }
        }
        ordersMapper.updateById(orders);
    }

    // 批量创建订单
    @Transactional
    public void batchAdd(List<Orders> ordersList) {
        if (ordersList == null || ordersList.isEmpty()) {
            throw new CustomException("订单列表不能为空");
        }

        for (Orders orders : ordersList) {
            add(orders); // 调用已有的添加订单方法
        }
    }

    /**
     * 根据ID查询
     */
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Orders> selectAll(Orders orders) {
        return ordersMapper.selectAll(orders);
    }

    /**
     * 统计店铺待处理订单数量（状态为"待支付"的订单）
     */
    public int countPendingByStoreId(Integer storeId) {
        return ordersMapper.countPendingByStoreId(storeId);
    }

    /**
     * 分页查询（支持多条件）
     */
    public PageInfo<Orders> selectPage(String orderNo, Integer userId,
                                       Integer storeId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> list = ordersMapper.selectPageByCondition(orderNo, userId, storeId);
        return PageInfo.of(list);
    }

}