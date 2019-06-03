package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.AccessVisitor;
import com.eservice.iot.model.ResponseModel;
import com.eservice.iot.model.VisitRecord;
import com.eservice.iot.model.device.Device;
import com.eservice.iot.util.RecordDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class visitorRecord {

    private static boolean isDelvisitorList = false;
    private final static Logger logger = LoggerFactory.getLogger(visitorRecord.class);
    /**
     * 访客抓拍列表
     */
    private static List<RecordDate> visitorList = new ArrayList<>();
    private String token;
    @Value("${smg_base_url}")
    private String SMG_BASE_URL;
    private TokenService tokenService;
    @Value("${park_base_url}")
    private String PARK_BASE_URL;
    @Autowired
    private RestTemplate restTemplate;
    @Resource
    private DeviceService deviceService;

    @Scheduled(initialDelay = 5000, fixedRate = 1000 * 5)
    public void querySignInVisitorTime() {
        Date date = new Date();
        Long endTime = date.getTime();
        date.setTime(date.getTime() - 60 * 5 * 1000);
        Long startTime = date.getTime();
        querySignInVisitor(startTime / 1000, endTime / 1000);
    }

    @Scheduled(initialDelay = 5000, fixedRate = 1000 * 60 * 10)
    private void timeRemoveRecord() {
        isDelvisitorList = true;
        Date date = new Date();
        for (int i = 0; i < visitorList.size(); i++) {
            if ((date.getTime() - visitorList.get(i).getDate().getTime()) / 1000 > 60 * 10) {
                visitorList.remove(i);
            }
        }
        isDelvisitorList = false;
    }

    private void querySignInVisitor(Long startTime, Long endTime) {
        HashMap<String, Object> postParameters = new HashMap<>();
        ///考勤记录查询开始时间
        postParameters.put("start_timestamp", startTime);
        ///考勤记录查询结束时间
        postParameters.put("end_timestamp", endTime);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        if (token == null) {
            token = tokenService.getToken();
        }
        headers.add(HttpHeaders.AUTHORIZATION, token);
        //只获取访客数据
        ArrayList<String> identity = new ArrayList<>();
        identity.add(Constant.VISITOR);
        postParameters.put("identity_list", identity);
        HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/visit_record/query", httpEntity, String.class);
            if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                String body = responseEntity.getBody();
                if (body != null) {
                    ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                    if (responseModel != null && responseModel.getResult() != null) {
                        List<VisitRecord> tempList = JSONArray.parseArray(responseModel.getResult(), VisitRecord.class);
                        if (tempList != null && tempList.size() > 0) {
                            logger.info("抓拍数量" + tempList.size());
                            recover(tempList);
                            sendWx();
                        }
                    }

                }
            }
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                //token失效,重新获取token后再进行数据请求
                token = tokenService.getToken();
                querySignInVisitor(startTime, endTime);
            }
        }

    }

    /**
     * 保证每条数据没有重复，并且数据线程没有多重干涉
     *
     * @param tempList 园区查询的的内容
     */
    private synchronized void recover(List<VisitRecord> tempList) {
        //对数据，按姓名进行排序，确保姓名相同的人在一块
        for (VisitRecord visitRecord : tempList) {
            boolean falt = true;
            //循环所有本地内容，如果有重复则不添加属性，没有则添加属性
            for (int i = 0; i < visitorList.size(); i++) {
                if (visitRecord.getTrack_id().equals(visitorList.get(i).getVisitRecord().getTrack_id())) {
                    falt = false;
                    break;
                }
            }
            //没有重复
            if (falt) {
                RecordDate recordDate = new RecordDate();
                recordDate.setDate(new Date());
                recordDate.setVisitRecord(visitRecord);
                visitorList.add(recordDate);
            }
        }
    }

    public void sendWx() {
        if (isDelvisitorList) {
            return;
        }
    /*for (VisitRecord visitRecord :
                visitorList) {
            boolean falt = true;          //用来判断是否
            String address=null;
            //判断设备ip是否符合抓拍的设备ip
            Condition condition = new Condition(EquipmentFloorRelations.class);
            condition.createCriteria().andEqualTo("ip",visitRecord.getDevice_id());
            List<EquipmentFloorRelations> infos = equipmentFloorRelationsService.findByCondition(condition);
            if (infos != null && infos.size() > 0) {
                //遍历访客所有的tagid
                for (String id :
                        visitRecord.getPerson().getTag_id_list()) {
                    //判断id是否在EquipmentFloorRelations关系中拥有，有则不发除微信模板，没有则发送模板
                    if (infos.get(0).getTagid().equals(id)) {
                        falt = false;
                        address=infos.get(0).getAddress();
                        break;
                    }
                }
            }
            if (falt){
                try {
                    wxService.sendMoveMessage((String)visitRecord.getPerson().getMeta().get("staffId"),(String)visitRecord.getPerson().getMeta().get("visitorId"),address,visitRecord.getPerson().getPerson_information().getName(),String.valueOf(visitRecord.getTimestamp()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/


        //克隆一个新的List集合对象
        List<RecordDate> visitorList = new ArrayList<>(this.visitorList);
        logger.info("visitorList克隆后集合数量：" + visitorList.size());
        //遍历所有通行记录
        for (int i = visitorList.size() - 1; i >= 0; i--) {
            //判断该条记录是否发送过请求
            if (visitorList.get(i).isKey()) {
                RecordDate recordDate = visitorList.get(i);
                //将数据改为不可用,这个数据将只用于判断重复
                recordDate.setKey(false);
                this.visitorList.set(i, recordDate);
                //用于判断通行记录中是否有该设备id，有则不发送请求，没有则发送请求
                boolean isError = true;

                AccessVisitor accessVisitor=new AccessVisitor();
                accessVisitor.setId(visitorList.get(i).getVisitRecord().getPerson().getMeta().get("staffId").toString());
                logger.info("推送邀请函id"+accessVisitor.getId());
                for (Device device:
                        deviceService.getAllDevice()) {
                    if(device.getDevice_id().equals(visitorList.get(i).getVisitRecord().getDevice_id())) {
                        accessVisitor.setAddress(device.getDevice_meta().getLocation());
                        break;
                    }
                }
                accessVisitor.setDate((long)visitorList.get(i).getVisitRecord().getTimestamp()*1000);
                accessVisitor.setPhone(visitorList.get(i).getVisitRecord().getPerson().getPerson_information().getPhone());
                accessVisitor.setName(visitorList.get(i).getVisitRecord().getPerson().getPerson_information().getName());
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
                HttpEntity httpEntity3 = new HttpEntity<>(JSON.toJSONString(accessVisitor), headers);
                ResponseEntity<String> responseEntity3 = restTemplate.postForEntity(SMG_BASE_URL, httpEntity3, String.class);
                logger.warn("SMG_BASE_URL:返回结果"+responseEntity3);
                }



        }
    }
}
