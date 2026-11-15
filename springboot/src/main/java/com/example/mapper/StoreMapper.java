package com.example.mapper;

import com.example.entity.Store;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface StoreMapper {

    @Insert("insert into store(name, logo, description, user_id, status) " +
            "values(#{name}, #{logo}, #{description}, #{userId}, #{status})")
    int insert(Store store);

    @Delete("delete from store where id = #{id}")
    int deleteById(Integer id);

    @Update("update store set name=#{name}, logo=#{logo}, description=#{description}, " +
            "status=#{status}, update_time=now() where id=#{id}")
    int updateById(Store store);

    @Select("select * from store where id = #{id}")
    Store selectById(Integer id);

    @Select("select * from store where user_id = #{userId}")
    Store selectByUserId(Integer userId);

    @Select("select * from store where name like concat('%',#{name},'%')")
    List<Store> selectByLikeName(String name);

    List<Store> selectAll(Store store);
}