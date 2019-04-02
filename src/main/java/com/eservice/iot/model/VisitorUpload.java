package com.eservice.iot.model;

import java.util.List;

/**
 * Class Description:
 *
 * @author Wilson Hu
 * @date 8/15/2018
 */
public class VisitorUpload {

    /**
     * face_list : [{"face_id":"string","face_image_id":"string","scene_image_id":"string"}]
     * identity : STAFF
     * last_visiting_time : 0
     * meta : {}
     * person_information : {"card_no":"string","company":"string","identity_number":"string","name":"string","phone":"string","visit_end_timestamp":0,"visit_purpose":"0","visit_start_timestamp":0,"visit_time_type":"0","visitee_name":"string"}
     * tag_id_list : ["string"]
     * upload_time : 0
     * visiting_counts : 0
     * visitor_id : string
     */

    private List<String> card_numbers;
    private Object meta;
    private PersonInformation person_information;
    private List<String> tag_id_list;

    public List<String> getCard_numbers() {
        return card_numbers;
    }

    public void setCard_numbers(List<String> card_numbers) {
        this.card_numbers = card_numbers;
    }

    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public PersonInformation getPerson_information() {
        return person_information;
    }

    public void setPerson_information(PersonInformation person_information) {
        this.person_information = person_information;
    }

    public List<String> getTag_id_list() {
        return tag_id_list;
    }

    public void setTag_id_list(List<String> tag_id_list) {
        this.tag_id_list = tag_id_list;
    }

    /**
     * 目前判断相同的条件是人名、电话都不变
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VisitorUpload) {
            VisitorUpload person = (VisitorUpload) obj;
            return (person.getPerson_information().getName().equals(person_information.getName())
                    && person.getPerson_information().getPhone().equals(person_information.getPhone()));
        } else {
            return super.equals(obj);

        }
    }
}
