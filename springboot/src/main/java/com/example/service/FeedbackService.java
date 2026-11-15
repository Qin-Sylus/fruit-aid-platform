package com.example.service;

import com.example.entity.Feedback;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface FeedbackService {
    
    /*** 新增*/
    void add(Feedback feedback);
    /*** 删除*/
    void deleteById(Integer id);
    /*** 修改*/
    void updateById(Feedback feedback);
    /*** 根据ID查询*/
    Feedback selectById(Integer id);
    /*** 查询所有*/
    List<Feedback> selectAll(Feedback feedback);
    /*** 分页查询*/
    PageInfo<Feedback> selectPage(Feedback feedback, Integer pageNum, Integer pageSize);
}



