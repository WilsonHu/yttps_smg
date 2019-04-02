package com.eservice.iot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * 员工modal
 *
 * @author HT
 */
public class Staff {
    @JsonProperty("tag_id_list")
    private List<String> tag_id_list;
    @JsonProperty("upload_time")
    private Integer uploadTime;
    @JsonProperty("person_information")
    private PersonInformation person_information;
    @JsonProperty("face_list")
    private List<FaceListBean> face_list;
    @JsonProperty("identity")
    private String identity;
    @JsonProperty("meta")
    private Map meta;
    @JsonProperty("scene_image_id")
    private String scene_image_id;
    @JsonProperty("staff_id")
    private String staffId;
    @JsonProperty("card_numbers")
    private List<String> card_numbers;
    @JsonProperty("face_image_content")
    private String face_image_content;
    @JsonProperty("face_image_content_list")
    private List<String> face_image_content_list;

    public List<String> getTag_id_list() {
        return tag_id_list;
    }

    public void setTag_id_list(List<String> tag_id_list) {
        this.tag_id_list = tag_id_list;
    }

    public Integer getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Integer uploadTime) {
        this.uploadTime = uploadTime;
    }

    public PersonInformation getPerson_information() {
        return person_information;
    }

    public void setPerson_information(PersonInformation person_information) {
        this.person_information = person_information;
    }

    public List<FaceListBean> getFace_list() {
        return face_list;
    }

    public void setFace_list(List<FaceListBean> face_list) {
        this.face_list = face_list;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Map getMeta() {
        return meta;
    }

    public void setMeta(Map meta) {
        this.meta = meta;
    }

    public String getScene_image_id() {
        return scene_image_id;
    }

    public void setScene_image_id(String scene_image_id) {
        this.scene_image_id = scene_image_id;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public List<String> getCard_numbers() {
        return card_numbers;
    }

    public void setCard_numbers(List<String> card_numbers) {
        this.card_numbers = card_numbers;
    }

    public String getFace_image_content() {
        return face_image_content;
    }

    public void setFace_image_content(String face_image_content) {
        this.face_image_content = face_image_content;
    }

    public List<String> getFace_image_content_list() {
        return face_image_content_list;
    }

    public void setFace_image_content_list(List<String> face_image_content_list) {
        this.face_image_content_list = face_image_content_list;
    }

    /**
     * 目前判断相同的条件是人名、电话都不变
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Staff) {
            Staff person = (Staff) obj;
            boolean same = true;
            if(person.person_information.getName() != null) {
                if(!person.person_information.getName().equals(person_information.getName())) {
                    same = false;
                }
            }
            if(same && (person.person_information.getPhone() != null) ) {
                if(!person.person_information.getPhone().equals(person_information.getPhone())) {
                    same = false;
                }
            }
            if(same && (person.person_information.getId() != null) ) {
                if(!person.person_information.getId().equals(person_information.getId())) {
                    same = false;
                }
            }
            return same;
        } else {
            return super.equals(obj);
        }
    }
}