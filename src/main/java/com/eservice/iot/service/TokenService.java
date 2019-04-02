package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.ResponseModel;
import com.eservice.iot.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * @author HT
 */
@Service
public class TokenService {

    @Value("${park_base_url}")
    private String PARK_BASE_URL;
    @Value("${park_username}")
    private String PARK_USERNAME;
    @Value("${park_password}")
    private String PARK_PASSWORD;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 为了防止本机服务器时间和钉钉返回token的过期时间不match，
     * 设置一个十分钟的buffer
     */
    public static final Long EXPIRE_BUFFER = 10 * 60 * 1000L;

    private final static Logger logger = LoggerFactory.getLogger(TokenService.class);

    /**
     * 园区登录，成功则返回token，失败返回null
     */
    public String getToken() {
        String token = null;
        HashMap<String, String> postParameters = new HashMap<>();
        postParameters.put("username", PARK_USERNAME);
        postParameters.put("password", Util.getMD5String(PARK_PASSWORD));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity r = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/user/login", r, String.class);
            if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                String body = responseEntity.getBody();
                if (body != null) {
                    ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                    if (responseModel != null && responseModel.getResult() != null) {
                        token = responseModel.getResult();
                    }
                }
            }
        } catch (Exception exception) {
            logger.error("Token update error ==> " + exception.getMessage());
        }

        return token;
    }
}
