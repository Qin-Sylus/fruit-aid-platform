package com.example.mapper;

import com.example.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作orders相关数据接口
*/
public interface OrdersMapper {

    /**
      * 新增
    */
    int insert(Orders orders);

    /**
      * 删除
    */
    @Delete("delete from orders where id = #{id}")
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Orders orders);

    /**
      * 根据ID查询
    */
    @Select("select * from orders where id = #{id}")
    Orders selectById(Integer id);

    /**
      * 查询所有
    */
    List<Orders> selectAll(Orders orders);

    /**
     * 按店铺ID查询订单（关联商品表）
     */
    @Select("select o.*, g.name as goods_name, g.img as goods_img, " +
            "u.name as user_name from orders o " +
            "left join goods g on o.goods_id = g.id " +
            "left join user u on o.user_id = u.id " +
            "where g.store_id = #{storeId}")
    List<Orders> selectByStoreId(Integer storeId);

    // OrdersMapper.java
    int countPendingByStoreId(@Param("storeId") Integer storeId);

    /**
     * 分页查询（支持店铺筛选）
     */
    @Select("<script>" +
            "select o.*, g.name as goods_name, g.img as goods_img, " +
            "u.name as user_name from orders o " +
            "left join goods g on o.goods_id = g.id " +
            "left join user u on o.user_id = u.id " +
            "<where>" +
            "<if test='orderNo != null'>o.order_no like concat('%',#{orderNo},'%')</if>" +
            "<if test='userId != null'>o.user_id = #{userId}</if>" +
            "<if test='storeId != null'>g.store_id = #{storeId}</if>" +
            "</where>" +
            "</script>")
    List<Orders> selectPageByCondition(@Param("orderNo") String orderNo,
                                       @Param("userId") Integer userId,
                                       @Param("storeId") Integer storeId);
}