package com.example.service;

import com.example.entity.Store;
import com.example.exception.CustomException;
import com.example.mapper.StoreMapper;
import com.example.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {

    @Resource
    private StoreMapper storeMapper;
    @Resource
    private UserMapper userMapper;

    @Transactional
    public void add(Store store) {
        // 检查用户是否存在
        if (userMapper.selectById(store.getUserId()) == null) {
            throw new CustomException("用户不存在");
        }
        // 检查是否已创建店铺
        if (storeMapper.selectByUserId(store.getUserId()) != null) {
            throw new CustomException("您已创建过店铺");
        }
        store.setStatus(1); // 默认启用状态
        storeMapper.insert(store);
    }

    public void deleteById(Integer id) {
        storeMapper.deleteById(id);
    }

    public void updateById(Store store) {
        storeMapper.updateById(store);
    }

    public Store selectById(Integer id) {
        return storeMapper.selectById(id);
    }

    public Store getByUserId(Integer userId) {
        return storeMapper.selectByUserId(userId);
    }
}