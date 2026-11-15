package com.example.mapper;

import com.example.entity.Goods;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作goods相关数据接口
*/
@Mapper
public interface GoodsMapper {

    /**
      * 新增
    */
    int insert(Goods goods);

    /**
      * 删除
    */
    @Delete("delete from goods where id = #{id}")
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Goods goods);

    /**
      * 根据ID查询
    */
    @Select("select * from goods where id = #{id}")
    Goods selectById(Integer id);

    /**
      * 查询所有
    */
    List<Goods> selectAll(Goods goods);

    /**
      * 商家
    */
    @Select("select g.*, s.name as store_name from goods g " +
            "left join store s on g.store_id = s.id " +
            "where g.store_id = #{storeId}")
    List<Goods> selectByStoreId(Integer storeId);

    /**
     * 根据店铺ID查询商品数量（严格匹配前端参数名 storeId）
     * @param storeId 店铺ID
     * @return 商品总数
     */
    @Select("SELECT COUNT(*) FROM goods WHERE store_id = #{storeId}") // 数据库字段是 store_id，与参数对应
    int selectCountByStoreId(@Param("storeId") Integer storeId); // @Param 确保参数名匹配
}