package com.eservice.iot.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.*;
import com.eservice.iot.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author HT
 */
@Component
public class StaffService {

    private final static Logger logger = LoggerFactory.getLogger(StaffService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Autowired
    private RestTemplate restTemplate;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Token
     */
    private String token;
    /**
     * 员工列表
     */
    private ArrayList<Staff> staffList = new ArrayList<>();

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TagService tagService;

    @Resource
    private SurveillancePolicyService surveillancePolicyService;

    private ThreadPoolTaskExecutor mExecutor;

    /**
     * 查询开始时间,单位为秒
     */
    private Long queryStartTime = 0L;

    /**
     * VIP员工在大屏再次出现的最小时间：20分钟
     */
    public static final int VIP_SHOW_TIME = 20 * 60;
    /**
     * 需要考勤的设备ID列表
     */
    private static ArrayList<String> YINGBIN_DEVICE_LIST = new ArrayList<>();
    private static ArrayList<String> mMovingList = new ArrayList<>();
    private static ArrayList<VisitRecord> mSendVipList = new ArrayList<>();
    private static ArrayList<String> CURRENT_ATTENDANCE = new ArrayList<>();


    public StaffService() {
        //准备初始数据，此时获取到考勤列表后不去通知钉钉，初始化开始查询时间
        queryStartTime = Util.getDateStartTime().getTime() / 1000;
    }


    /**
     * 每分钟获取一次需要签到的员工信息
     */
    @Scheduled(initialDelay = 3000, fixedRate = 1000)
    public void fetchStaffScheduled() {
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                String url = PARK_BASE_URL + "/staffs?";
                for (String tagId : surveillancePolicyService.getmAttendTagIdList()) {

                    url += "tag_id_list=" + tagId + "&";
                }
                if (surveillancePolicyService.getmAttendTagIdList().size() > 0) {
                    url += "&page=0&size=0";
                } else {
                    url += "page=0&size=0";
                }
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        processStaffResponse(body);
                    } else {
                        fetchStaffScheduled();
                    }
                }
            } catch (HttpClientErrorException exception) {
                if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        fetchStaffScheduled();
                    }
                }
            }
        }
    }

    /**
     * 凌晨1点清除签到记录
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void resetStaffDataScheduled() {

    }

    private void processStaffResponse(String body) {
        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
        if (responseModel != null && responseModel.getResult() != null) {
            ArrayList<Staff> tmpList = (ArrayList<Staff>) JSONArray.parseArray(responseModel.getResult(), Staff.class);
            if (tmpList != null && tmpList.size() != 0) {
                if (!staffList.equals(tmpList)) {
                    logger.info("The number of staff：{} ==> {}", staffList.size(), tmpList.size());
                    staffList = tmpList;
                }
            }
        }
    }

    private ArrayList<Staff> filterAttendanceStaff(List<Staff> list) {
        ArrayList<Staff> resultList = new ArrayList<>();
        int notNeedNum = 0;
        for (Staff item : list) {
            boolean notNeed = false;
            for (String tagId : item.getTag_id_list()) {
                if (tagService.getNotSignInTagIdList().contains(tagId)) {
                    notNeed = true;
                    notNeedNum++;
                }
            }
            if (!notNeed) {
                resultList.add(item);
            }
        }
        return resultList;
    }

    private String getAttencanceId(VisitRecord record) {
        String tagId = null;
        //返回第一个考勤的标签ID
        if (record != null && record.getPerson().getTag_id_list().size() > 0 && tagId == null) {
            for (String id : record.getPerson().getTag_id_list()) {
                if (surveillancePolicyService.getmAttendTagIdList().contains(id)) {
                    tagId = id;
                }
            }
        }
        return tagId;
    }

    public boolean deleteStaff(String id) {
        boolean success = false;
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/staffs/" + id, HttpMethod.DELETE, entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                        if(responseModel != null && responseModel.getRtn() == 0) {
                            success = true;
                        }
                    }
                }
            } catch (HttpClientErrorException exception) {
                if (exception.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    token = tokenService.getToken();
                    if (token != null) {
                        deleteStaff(id);
                    }
                }
            }
        }
        return success;
    }

    private void initExecutor() {
        mExecutor = new ThreadPoolTaskExecutor();
        mExecutor.setCorePoolSize(10);
        mExecutor.setMaxPoolSize(100);
        mExecutor.setThreadNamePrefix("YTTPS-");
        mExecutor.initialize();
    }

    public ArrayList<Staff> getStaffList() {
        return staffList;
    }
}
