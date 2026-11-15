package com.example.controller;

import com.example.common.Result;
import com.example.entity.Cart;
import com.example.service.CartService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Resource
    private CartService cartService;

    // 添加到购物车
    @PostMapping("/add")
    public Result add(@RequestBody Cart cart) {
        cartService.add(cart);
        return Result.success();
    }

    // 删除购物车商品
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        cartService.deleteById(id);
        return Result.success();
    }

    // 清空购物车
    @DeleteMapping("/clear/{userId}")
    public Result clearByUserId(@PathVariable Integer userId) {
        cartService.clearByUserId(userId);
        return Result.success();
    }

    // 更新数量
    @PutMapping("/updateNum")
    public Result updateNum(@RequestParam Integer id, @RequestParam Integer num) {
        cartService.updateNum(id, num);
        return Result.success();
    }

    // 获取用户购物车
    @GetMapping("/getByUserId/{userId}")
    public Result getByUserId(@PathVariable Integer userId) {
        List<Cart> list = cartService.getByUserId(userId);
        return Result.success(list);
    }
}