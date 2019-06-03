package com.eservice.iot.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.Policy;
import com.eservice.iot.model.ResponseModel;
import com.eservice.iot.model.Staff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;


/**
 * @author HT
 */
@Component
public class PolicyService {

    private final static Logger logger = LoggerFactory.getLogger(PolicyService.class);

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;

    private static ArrayList<Policy> mPolicyList = new ArrayList<>();

    /**
     * Token
     */
    private String token;

    /**
     * 每分钟更新通行策略
     */
    @Scheduled(initialDelay = 5000, fixedRate = 1000 * 60)
    public void fetchPolicy() {
        if (tokenService != null) {
            token = tokenService.getToken();
            if(token != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.ACCEPT, "application/json");
                headers.add("Authorization", token);
                HttpEntity entity = new HttpEntity(headers);
                try {
                    ResponseEntity<String> responseEntity = restTemplate.exchange(PARK_BASE_URL + "/access/policy?page=0&size=0", HttpMethod.GET, entity, String.class);
                    if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                        String body = responseEntity.getBody();
                        if (body != null) {
                            ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                            if (responseModel != null && responseModel.getResult() != null) {
                                ArrayList<Policy> tmpList = (ArrayList<Policy>) JSONArray.parseArray(responseModel.getResult(), Policy.class);
                                if (tmpList != null && tmpList.size() > 0) {
                                    mPolicyList = tmpList;
                                }
                            }
                        } else {
                            fetchPolicy();
                        }
                    }
                } catch (HttpClientErrorException errorException) {
                    if (errorException.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                        token = tokenService.getToken();
                        fetchPolicy();
                    }
                }
            } else {
                logger.error("Token is null, fetch policy error!");
            }
        }
    }

    public ArrayList<Policy> getAllPolicy() {
        return mPolicyList;
    }

}
