package com.eservice.iot.util;

import com.eservice.iot.model.VisitRecord;

import java.util.Date;

public class RecordDate {
    private VisitRecord visitRecord;
    private Date date;
    private boolean isKey=true;

    public boolean isKey() {
        return isKey;
    }

    public void setKey(boolean key) {
        isKey = key;
    }

    public VisitRecord getVisitRecord() {
        return visitRecord;
    }

    public void setVisitRecord(VisitRecord visitRecord) {
        this.visitRecord = visitRecord;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
