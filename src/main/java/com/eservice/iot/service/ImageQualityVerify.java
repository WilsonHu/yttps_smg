package com.eservice.iot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.model.Policy;
import com.eservice.iot.model.ResponseModel;
import com.eservice.iot.model.Tag;
import com.eservice.iot.model.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 人脸质量检测
 */
@Component
@Service
public class ImageQualityVerify {
    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;
    public boolean verify(String img) {
        List demo =new ArrayList();
        demo.add(img);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.AUTHORIZATION, tokenService.getToken());
        HttpEntity httpEntity = new HttpEntity<>(JSON.toJSONString(demo), headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/image_quality/verify", httpEntity, String.class);
        String body = responseEntity.getBody();
        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
        if (responseModel != null && responseModel.getResult() != null) {
            List<Verify> verify =  (ArrayList<Verify>)JSON.parseArray(responseModel.getResult(), Verify.class);
            if (verify.get(0).isEye_normal()&&verify.get(0).isMouth_normal()){
                return true;
            }
        }
        return false;
    }
}
