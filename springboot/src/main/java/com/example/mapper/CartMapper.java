package com.example.mapper;

import com.example.entity.Cart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface CartMapper {
    int insert(Cart cart);

    @Delete("delete from cart where id = #{id}")
    int deleteById(Integer id);

    @Delete("delete from cart where user_id = #{userId}")
    int deleteByUserId(Integer userId);

    int updateById(Cart cart);

    @Select("select * from cart where id = #{id}")
    Cart selectById(Integer id);

    @Select("select * from cart where user_id = #{userId}")
    List<Cart> selectByUserId(Integer userId);

    @Select("select * from cart where user_id = #{userId} and goods_id = #{goodsId}")
    Cart selectByUserAndGoods(Integer userId, Integer goodsId);
}