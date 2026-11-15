package com.example.service.impl;

import com.example.entity.Feedback;
import com.example.mapper.FeedbackMapper;
import com.example.service.FeedbackService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Resource
    private FeedbackMapper feedbackMapper;
    @Override
    public void add(Feedback feedback) {
        feedback.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        feedback.setStatus("待处理");
        feedbackMapper.insert(feedback);
    }
    @Override
    public void deleteById(Integer id) {
        feedbackMapper.deleteById(id);
    }
    @Override
    public void updateById(Feedback feedback) {
        feedbackMapper.updateById(feedback);
    }
    @Override
    public Feedback selectById(Integer id) {
        return feedbackMapper.selectById(id);
    }
    @Override
    public List<Feedback> selectAll(Feedback feedback) {
        return feedbackMapper.selectAll(feedback);
    }
    @Override
    public PageInfo<Feedback> selectPage(Feedback feedback, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Feedback> list = feedbackMapper.selectPage(feedback);
        return PageInfo.of(list);
    }
}



