package com.example.entity;

import java.math.BigDecimal; // 导入BigDecimal

public class Cart {
    private Integer id;
    private Integer userId;
    private Integer goodsId;
    private Integer num;
    private String goodsName;
    private String goodsImg;
    private BigDecimal price;
    private String unit;

    // 其他字段的getter/setter不变，仅更新price相关
    public BigDecimal getPrice() {return price;}
    public void setPrice(BigDecimal price) {this.price = price;}
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
    public Integer getNum() { return num; }
    public void setNum(Integer num) { this.num = num; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public String getGoodsImg() { return goodsImg; }
    public void setGoodsImg(String goodsImg) { this.goodsImg = goodsImg; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}