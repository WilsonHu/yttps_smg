package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.*;
import com.eservice.iot.model.device.Device;
import com.eservice.iot.model.visitor_info.VisitorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Condition;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author HT
 */
@Component
public class VisitorService {

    private final static Logger logger = LoggerFactory.getLogger(VisitorService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Autowired
    private RestTemplate restTemplate;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Resource
    VisitorInfoService visitorInfoService;

    @Resource
    PolicyService policyService;

    @Resource
    DeviceService deviceService;

    /**
     * Token
     */
    private String token;
    /**
     * 访客列表
     */
    private static List<Visitor>  visitorList = new ArrayList<>();


    @Autowired
    private TokenService tokenService;

    /**
     * 每10秒钟获取一次当天访客信息
     */
    @Scheduled(initialDelay = 5000, fixedRate = 1000 * 10)
    public void fetchVisitorListScheduled() {
        if (token == null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/visitors", HttpMethod.GET, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        processVisitorResponse(body);
                    } else {
                        fetchVisitorListScheduled();
                    }
                }
            } catch (HttpClientErrorException errorException) {
                if (errorException.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    fetchVisitorListScheduled();
                }
            }
        }
    }

    private void processVisitorResponse(String body) {
        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
        if (responseModel != null && responseModel.getResult() != null) {
            List<Visitor> tmpList = JSONArray.parseArray(responseModel.getResult(), Visitor.class);
            if (tmpList != null && tmpList.size() > 0) {
                boolean changed = false;
                if (tmpList.size() != visitorList.size()) {
                    changed = true;
                } else {
                    if (!tmpList.equals(visitorList)) {
                        changed = true;
                    }
                }
                if (changed) {
                    logger.info("The number of visitor：{} ==> {}", visitorList.size(), tmpList.size());
                }
                visitorList = tmpList;
            }
        }
    }

    public List<Visitor> getVisitorList() {
        return visitorList;
    }

    @Scheduled(initialDelay = 5000, fixedRate = 1000 * 60)
    public void sendTag() {
        Date now = new Date();
        Condition condition = new Condition(VisitorInfo.class);
        condition.createCriteria().andEqualTo("status", 0);
        List<VisitorInfo> visitorInfo = visitorInfoService.findByCondition(condition);
        //模板
        List listInfo = new ArrayList();
        for (int i = 0; i < visitorInfo.size(); i++) {


            Date date = visitorInfo.get(i).getVisitStartTime();
            //创建Visitor模板
            Visitor visitor = new Visitor();
            //查询所有时间小于1小时的数据
            if (date.getTime() - now.getTime() < 3600000 && date.getTime() - now.getTime() > 0) {
                //将查询到的标签插入到visitor模板中
                List<Policy> policy = policyService.getAllPolicy();
                for (Policy policyInfo :
                        policy) {
                    if (visitorInfo.get(i).getFloor().equals(policyInfo.getName())) {
                        //添加标签
                        visitor.setTag_id_list(policyInfo.getTag_id_list());
                        Map map=new HashMap();
                        map.put("deviceId",policyInfo.getDevice_id_list());
                        visitor.setMeta(map);
                        break;
                    }
                }
                //创建园区访客人员信息模板
                PersonInformation personInformation = new PersonInformation();

                //添加卡号，用于辨别是否为唯一
                List<String> staffId=new ArrayList<>();
                staffId.add(visitorInfo.get(i).getStaffId());
                visitor.setCard_numbers(staffId);
                visitor.setFace_image_content(visitorInfo.get(i).getPhotoData());
                personInformation.setName(visitorInfo.get(i).getVisitorName());
                personInformation.setCheck_out_timestamp(visitorInfo.get(i).getVisitEndTime().getTime());
                personInformation.setCompany(visitorInfo.get(i).getCompany());
                personInformation.setPhone(visitorInfo.get(i).getPhone());
                personInformation.setVisit_purpose("0");
                personInformation.setVisit_start_timestamp(visitorInfo.get(i).getVisitStartTime().getTime());
                personInformation.setVisit_end_timestamp(visitorInfo.get(i).getVisitEndTime().getTime());
                personInformation.setVisit_time_type("0");
                personInformation.setVisitee_name(visitorInfo.get(i).getVisiteeName());
                visitor.setPerson_information(personInformation);
                //添加到list中
                listInfo.add(visitor);
                VisitorInfo visitorAuto=visitorInfo.get(i);
                visitorAuto.setStatus(3);
                visitorAuto.setUpdateTime(new Date());
                visitorInfoService.update(visitorAuto);
            }

        }
        Map<String, List> map = new HashMap<>();
        map.put("visitor_list", listInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, tokenService.getToken());
        if (listInfo.size()>0) {
            HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(map), headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/visitors", httpEntity, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {

                String body = responseEntity.getBody();
                ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);

            }
        }
    }

    @Scheduled(initialDelay = 1000, fixedRate = 1000 * 2)
    public void querySignInVisitorTime() {
        Date date = new Date();
        Long endTime = date.getTime()/1000;
        date.setTime(date.getTime() - 2 * 1000);
        Long startTime = date.getTime()/1000;
        querySignInVisitor(startTime, endTime);
    }


    /**
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 返回设备名等指数
     */
    public void querySignInVisitor(Long startTime, Long endTime) {
        if (visitorList != null) {
            visitorList.clear();
        }
        HashMap<String, Object> postParameters = new HashMap<>();
        ///考勤记录查询开始时间
        postParameters.put("start_timestamp", startTime);
        ///考勤记录查询结束时间
        postParameters.put("end_timestamp", endTime);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, token);
        //只获取访客数据
        ArrayList<String> identity = new ArrayList<>();
        identity.add(Constant.VISITOR);
        HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
        List<AccessRecord> visitRecord;
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/access/record", httpEntity, String.class);
            if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                String body = responseEntity.getBody();
                if (body != null) {
                    ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                    if (responseModel != null && responseModel.getResult() != null) {
                        List<AccessRecord> tempList = JSONArray.parseArray(responseModel.getResult(), AccessRecord.class);
                        if (tempList != null && tempList.size() > 0) {
                            //对数据，按姓名进行排序，确保姓名相同的人在一块
                            Collections.sort(tempList, new Comparator<AccessRecord>() {
                                @Override
                                public int compare(AccessRecord o1, AccessRecord o2) {
                                    return o1.getPerson().getPerson_information().getName().compareTo(o2.getPerson().getPerson_information().getName());
                                }
                            });
                            visitRecord = tempList;
                             recond(visitRecord);
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
     *
     * @param visitRecord 所有通行记录
     * @return
     */
    private List<AccessVisitor> recond(List<AccessRecord> visitRecord) {
        List<AccessVisitor> accessVisitors=new ArrayList<>();
        /**
         * 遍历所有通行记录
         */
        for (AccessRecord visitRecordInfo :
                visitRecord) {
            AccessVisitor accessVisitor=new AccessVisitor();

            boolean status=false;
            for (String info:
                    (List<String>)visitRecordInfo.getPerson().getMeta().get("deviceId")) {
                if(info.equals(visitRecordInfo.getDevice_id())){
                    status=true;
                    break;
                }
            }
            /**
             * 遍历所有对应Device的设备
             */
            for (Device device:
            deviceService.getAllDevice()) {
                if(device.getDevice_id().equals(visitRecordInfo.getDevice_id())) {
                    accessVisitor.setAddress(device.getDevice_meta().getLocation());
                    break;
                }
            }
            //SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                //将时间戳转化为时间格式
                accessVisitor.setDate(new Date().getTime()/1000);

            accessVisitor.setId(visitRecordInfo.getPerson().getCard_numbers().get(0));


            if (status){
                accessVisitor.setName(visitRecordInfo.getPerson().getPerson_information().getName());
                accessVisitor.setPhone(visitRecordInfo.getPerson().getPerson_information().getPhone());
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
                HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(accessVisitor), headers);
                ResponseEntity<String> responseEntity = restTemplate.postForEntity("https://guard-test.smgtech.net/index.php?r=api/third/get", httpEntity, String.class);
                String body= responseEntity.getBody();
            }
        }

        return accessVisitors;
    }

    /**
     * 首先进行入参检查防止出现空指针异常
     * 如果两个参数都为空，则返回true
     * 如果有一项为空，则返回false
     * 接着对第一个list进行遍历，如果某一项第二个list里面没有，则返回false
     * 还要再将两个list反过来比较，因为可能一个list是两一个list的子集
     * 如果成功遍历结束，返回true
     *
     * @param l0
     * @param l1
     * @return
     */
    public static boolean isListEqual(List l0, List l1) {
        if (l0 == l1)
            return true;
        if (l0 == null && l1 == null)
            return true;
        if (l0 == null || l1 == null)
            return false;
        if (l0.size() != l1.size())
            return false;
        for (Object o : l0) {
            if (!l1.contains(o))
                return false;
        }
        for (Object o : l1) {
            if (!l0.contains(o))
                return false;
        }
        return true;
    }
}
