package com.eservice.iot.model;


import java.util.List;

public class SurveillancePolicy {

    /**
     * device_id_list : ["string"]
     * enabled : true
     * id : string
     * identity_list : ["STAFF"]
     * name : string
     * tag_id_list : ["string"]
     * threshold : 0
     * type : 0
     * valid_time : {"end_date":"string","end_time":"string","end_timestamp":0,"mode":"0","start_date":"string","start_time":"string","start_timestamp":0,"valid_weekday":[0]}
     */

    private boolean enabled;
    private String id;
    private String name;
    private int threshold;
    private String type;
    private ValidTimeBean valid_time;
    private List<String> device_id_list;
    private List<String> identity_list;
    private List<String> tag_id_list;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ValidTimeBean getValid_time() {
        return valid_time;
    }

    public void setValid_time(ValidTimeBean valid_time) {
        this.valid_time = valid_time;
    }

    public List<String> getDevice_id_list() {
        return device_id_list;
    }

    public void setDevice_id_list(List<String> device_id_list) {
        this.device_id_list = device_id_list;
    }

    public List<String> getIdentity_list() {
        return identity_list;
    }

    public void setIdentity_list(List<String> identity_list) {
        this.identity_list = identity_list;
    }

    public List<String> getTag_id_list() {
        return tag_id_list;
    }

    public void setTag_id_list(List<String> tag_id_list) {
        this.tag_id_list = tag_id_list;
    }

    public static class ValidTimeBean {
        /**
         * end_date : string
         * end_time : string
         * end_timestamp : 0
         * mode : 0
         * start_date : string
         * start_time : string
         * start_timestamp : 0
         * valid_weekday : [0]
         */

        private String end_date;
        private String end_time;
        private int end_timestamp;
        private String mode;
        private String start_date;
        private String start_time;
        private int start_timestamp;
        private List<Integer> valid_weekday;

        public String getEnd_date() {
            return end_date;
        }

        public void setEnd_date(String end_date) {
            this.end_date = end_date;
        }

        public String getEnd_time() {
            return end_time;
        }

        public void setEnd_time(String end_time) {
            this.end_time = end_time;
        }

        public int getEnd_timestamp() {
            return end_timestamp;
        }

        public void setEnd_timestamp(int end_timestamp) {
            this.end_timestamp = end_timestamp;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getStart_date() {
            return start_date;
        }

        public void setStart_date(String start_date) {
            this.start_date = start_date;
        }

        public String getStart_time() {
            return start_time;
        }

        public void setStart_time(String start_time) {
            this.start_time = start_time;
        }

        public int getStart_timestamp() {
            return start_timestamp;
        }

        public void setStart_timestamp(int start_timestamp) {
            this.start_timestamp = start_timestamp;
        }

        public List<Integer> getValid_weekday() {
            return valid_weekday;
        }

        public void setValid_weekday(List<Integer> valid_weekday) {
            this.valid_weekday = valid_weekday;
        }
    }
}
