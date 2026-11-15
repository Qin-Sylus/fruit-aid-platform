package com.example.service;

import com.example.entity.Cart;
import com.example.entity.Goods;
import com.example.exception.CustomException;
import com.example.mapper.CartMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartService {
    @Resource
    private CartMapper cartMapper;

    @Resource
    private GoodsService goodsService;

    // 添加商品到购物车
    public void add(Cart cart) {
        Goods goods = goodsService.selectById(cart.getGoodsId());
        if (goods == null) {
            throw new CustomException("商品不存在");
        }
        // 检查库存
        if (cart.getNum() > goods.getStore()) {
            throw new CustomException("商品库存不足");
        }
        // 检查是否已在购物车
        Cart existCart = cartMapper.selectByUserAndGoods(cart.getUserId(), cart.getGoodsId());
        if (existCart != null) {
            // 已存在则更新数量
            existCart.setNum(existCart.getNum() + cart.getNum());
            cartMapper.updateById(existCart);
            return;
        }
        // 设置商品信息（此处现在类型匹配，无报错）
        cart.setGoodsName(goods.getName());
        cart.setGoodsImg(goods.getImg());
        cart.setPrice(goods.getPrice());
        cart.setUnit(goods.getUnit());
        cartMapper.insert(cart);
    }

    // 其他方法不变...
    public void deleteById(Integer id) {
        cartMapper.deleteById(id);
    }

    public void clearByUserId(Integer userId) {
        cartMapper.deleteByUserId(userId);
    }

    public void updateNum(Integer id, Integer num) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null) {
            throw new CustomException("购物车商品不存在");
        }

        Goods goods = goodsService.selectById(cart.getGoodsId());
        if (num > goods.getStore()) {
            throw new CustomException("商品库存不足");
        }

        cart.setNum(num);
        cartMapper.updateById(cart);
    }

    public List<Cart> getByUserId(Integer userId) {
        return cartMapper.selectByUserId(userId);
    }
}