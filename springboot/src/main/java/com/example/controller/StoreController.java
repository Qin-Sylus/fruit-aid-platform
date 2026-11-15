package com.example.controller;

import com.example.common.Result;
import com.example.entity.Store;
import com.example.service.StoreService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
public class StoreController {

    @Resource
    private StoreService storeService;

    @PostMapping("/add")
    public Result add(@RequestBody Store store) {
        storeService.add(store);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        storeService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody Store store) {
        storeService.updateById(store);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(storeService.selectById(id));
    }

    @GetMapping("/myStore")
    public Result getMyStore(@RequestParam Integer userId) {
        return Result.success(storeService.getByUserId(userId));
    }
}