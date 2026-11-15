package com.example.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Store {
    private Integer id;
    private String name;
    private String logo;
    private String description;
    private Integer userId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public LocalDateTime getUpdateTime() {return updateTime;}

    public void setUpdateTime(LocalDateTime updateTime) {this.updateTime = updateTime;}

    public LocalDateTime getCreateTime() {return createTime;}

    public void setCreateTime(LocalDateTime createTime) {this.createTime = createTime;}

    public Integer getStatus() {return status;}

    public void setStatus(Integer status) {this.status = status;}

    public Integer getUserId() {return userId;}

    public void setUserId(Integer userId) {this.userId = userId;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public String getLogo() {return logo;}

    public void setLogo(String logo) {this.logo = logo;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public Integer getId() {return id;}

    public void setId(Integer id) {this.id = id;}
}