package com.eservice.iot.model;

import java.util.Date;

public class AccessVisitor {
    private String name;            //访客姓名
    private String phone;           //访客手机号
    private String address;         //访客通行地址、
    private Long date;              //记录刷脸时间
    private String id;              //辨别访客的id

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}

