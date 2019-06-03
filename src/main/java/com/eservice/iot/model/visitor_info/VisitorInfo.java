package com.eservice.iot.model.visitor_info;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import javax.persistence.*;

@Table(name = "visitor_info")
public class VisitorInfo {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /**
     * 访客姓名
     */
    @Column(name = "visitor_name")
    private String visitorName;

    /**
     * 被访人
     */
    @Column(name = "visitee_name")
    private String visiteeName;

    /**
     * 访客所属公司
     */
    private String company;

    /**
     * 访客电话
     */
    private String phone;

    /**
     * 来访目的
     */
    @Column(name = "visit_purpose")
    private String visitPurpose;

    /**
     * 访问开始时间
     */
    @Column(name = "visit_start_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date visitStartTime;

    /**
     * 访问结束时间
     */
    @Column(name = "visit_end_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date visitEndTime;

    /**
     * 表示是否被审核, "0"->"未被审核"，"1" ->"审核通过", "3" ->"审核未通过"
     */
    private Integer status;

    /**
     * 记录创建时间
     */
    @Column(name = "create_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    /**
     * 园区员工id
     */
    @Column(name = "staff_id")
    private String staffId;

    /**
     * 用于标识迁出id
     */
    @Column(name = "visitor_id")
    private String visitorId;

    /**
     * 照片base64数据
     */
    @Column(name = "photo_data")
    private String photoData;

    /**
     * 访问设备（楼层）的数据["一楼","二楼"]
     */
    private String floor;

    /**
     * 获取ID
     *
     * @return id - ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置ID
     *
     * @param id ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取访客姓名
     *
     * @return visitor_name - 访客姓名
     */
    public String getVisitorName() {
        return visitorName;
    }

    /**
     * 设置访客姓名
     *
     * @param visitorName 访客姓名
     */
    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    /**
     * 获取被访人
     *
     * @return visitee_name - 被访人
     */
    public String getVisiteeName() {
        return visiteeName;
    }

    /**
     * 设置被访人
     *
     * @param visiteeName 被访人
     */
    public void setVisiteeName(String visiteeName) {
        this.visiteeName = visiteeName;
    }

    /**
     * 获取访客所属公司
     *
     * @return company - 访客所属公司
     */
    public String getCompany() {
        return company;
    }

    /**
     * 设置访客所属公司
     *
     * @param company 访客所属公司
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * 获取访客电话
     *
     * @return phone - 访客电话
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置访客电话
     *
     * @param phone 访客电话
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取来访目的
     *
     * @return visit_purpose - 来访目的
     */
    public String getVisitPurpose() {
        return visitPurpose;
    }

    /**
     * 设置来访目的
     *
     * @param visitPurpose 来访目的
     */
    public void setVisitPurpose(String visitPurpose) {
        this.visitPurpose = visitPurpose;
    }

    /**
     * 获取访问开始时间
     *
     * @return visit_start_time - 访问开始时间
     */
    public Date getVisitStartTime() {
        return visitStartTime;
    }

    /**
     * 设置访问开始时间
     *
     * @param visitStartTime 访问开始时间
     */
    public void setVisitStartTime(Date visitStartTime) {
        this.visitStartTime = visitStartTime;
    }

    /**
     * 获取访问结束时间
     *
     * @return visit_end_time - 访问结束时间
     */
    public Date getVisitEndTime() {
        return visitEndTime;
    }

    /**
     * 设置访问结束时间
     *
     * @param visitEndTime 访问结束时间
     */
    public void setVisitEndTime(Date visitEndTime) {
        this.visitEndTime = visitEndTime;
    }

    /**
     * 获取表示是否被审核, "0"->"未被审核"，"1" ->"审核通过", "3" ->"审核未通过"
     *
     * @return status - 表示是否被审核, "0"->"未被审核"，"1" ->"审核通过", "3" ->"审核未通过"
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置表示是否被审核, "0"->"未被审核"，"1" ->"审核通过", "3" ->"审核未通过"
     *
     * @param status 表示是否被审核, "0"->"未被审核"，"1" ->"审核通过", "3" ->"审核未通过"
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取记录创建时间
     *
     * @return create_time - 记录创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 设置记录创建时间
     *
     * @param createTime 记录创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取更新时间
     *
     * @return update_time - 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取园区员工id
     *
     * @return staff_id - 园区员工id
     */
    public String getStaffId() {
        return staffId;
    }

    /**
     * 设置园区员工id
     *
     * @param staffId 园区员工id
     */
    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    /**
     * 获取用于标识迁出id
     *
     * @return visitor_id - 用于标识迁出id
     */
    public String getVisitorId() {
        return visitorId;
    }

    /**
     * 设置用于标识迁出id
     *
     * @param visitorId 用于标识迁出id
     */
    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    /**
     * 获取照片base64数据
     *
     * @return photo_data - 照片base64数据
     */
    public String getPhotoData() {
        return photoData;
    }

    /**
     * 设置照片base64数据
     *
     * @param photoData 照片base64数据
     */
    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }

    /**
     * 获取访问设备（楼层）的数据["一楼","二楼"]
     *
     * @return floor - 访问设备（楼层）的数据["一楼","二楼"]
     */
    public String getFloor() {
        return floor;
    }

    /**
     * 设置访问设备（楼层）的数据["一楼","二楼"]
     *
     * @param floor 访问设备（楼层）的数据["一楼","二楼"]
     */
    public void setFloor(String floor) {
        this.floor = floor;
    }
}