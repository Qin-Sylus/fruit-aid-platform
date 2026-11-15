package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.service.AdminService;
import com.example.service.UserService;
import com.example.utils.JwtUtil; // 导入JwtUtil类（根据实际包路径调整）
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
public class WebController {
    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;
    // 注入JwtUtil实例（关键代码）
    @Resource
    private JwtUtil jwtUtil;
    /*** 默认请求接口*/
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }
    /*** 登录*/
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;
        if ("ADMIN".equals(account.getRole())) {
            ac = adminService.login(account);
        } else {
            ac = userService.login(account);
        }
        // 生成token（此时jwtUtil可正常使用）
        String token = jwtUtil.generateToken(ac.getUsername(), ac.getRole());
        // 存储token
        jwtUtil.saveToken(ac.getUsername(), token);
        // 返回用户信息和token
        Map<String, Object> result = new HashMap<>();
        result.put("user", ac);
        result.put("token", token);
        return Result.success(result);
    }
    /*** 注册*/
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }
    /*** 修改密码*/
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("ADMIN".equals(account.getRole())) {
            adminService.updatePassword(account);
        } else {
            userService.updatePassword(account);
        }
        return Result.success();
    }

}