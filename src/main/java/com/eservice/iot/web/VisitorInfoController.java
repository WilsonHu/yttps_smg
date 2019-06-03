package com.eservice.iot.web;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.*;
import com.eservice.iot.model.visitor_info.VisitorInfo;
import com.eservice.iot.service.*;
import com.eservice.iot.util.Util;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Verify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* Class Description: xxx
* @author Wilson Hu
* @date 2019/03/25.
*/
@RestController
@RequestMapping("/visitor/info")
public class VisitorInfoController {
    private final static Logger logger = LoggerFactory.getLogger(VisitorInfoController.class);
    @Resource
    private VisitorInfoService visitorInfoService;

    @Resource
    private PolicyService policyService;

    @Resource
    private ImageQualityVerify imageQualityVerify;

    @Autowired
    private TokenService tokenService;

    /**
     * Token
     */
    private String token;

    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${smg_base_url}")
    private String SMG_BASE_URL;

    @PostMapping("/add")
    public Result add(@RequestBody @NotNull VisitorInfo visitorInfo) {
        //检查照片质量
        if(visitorInfo.getPhotoData() == null || "".equals(visitorInfo.getPhotoData()))  {
            return ResultGenerator.genFailResult("照片不能为空！");
        }
        visitorInfo.setId(Util.getUUIDForDB());
        try {
            visitorInfoService.save(visitorInfo);
        }catch (Exception ex){
            return ResultGenerator.genFailResult(ex.getMessage());
        }
        return ResultGenerator.genSuccessResult(visitorInfo);
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam String id) {
        visitorInfoService.deleteById(id);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/update")
    public Result update(@RequestBody @NotNull VisitorInfo visitorInfo) {
        visitorInfoService.update(visitorInfo);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/detail")
    public Result detail(@RequestParam @NotNull String id) {
        VisitorInfo visitorInfo = visitorInfoService.findById(id);
        return ResultGenerator.genSuccessResult(visitorInfo);
    }

    @PostMapping("/list")
    public Result list(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size) {
        PageHelper.startPage(page, size);
        List<VisitorInfo> list = visitorInfoService.findAll();
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(pageInfo);
    }

    private boolean verify(String photoData, Long score) {
        boolean result = false;
        if (token == null && tokenService != null) {
            token = tokenService.getToken();
        }
        if (photoData != null) {
            HashMap<String, Object> postParameters = new HashMap<>();
            postParameters.put("image_content_base64", photoData);
            //只查50分以上的，至多1人
            postParameters.put("threshold", score);
            postParameters.put("topk", 1);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add("Authorization", token);
            HttpEntity entity = new HttpEntity<>(JSON.toJSONString(postParameters), headers);
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(PARK_BASE_URL + "/persons/retrieval", entity, String.class);
                if (responseEntity.getStatusCodeValue() == ResponseCode.OK) {
                    String body = responseEntity.getBody();
                    if (body != null) {
                        ResponseModel responseModel = JSONObject.parseObject(body, ResponseModel.class);
                        if (responseModel != null && responseModel.getResult() != null && responseModel.getRtn() == 0) {
                            result = true;
                        } else if(responseModel.getRtn() != 0) {
                            logger.warn("Search failed! message:{}",responseModel.getMessage());
                            ResultGenerator.genFailResult(responseModel.getMessage());
                        }
                    }
                }
            } catch (HttpClientErrorException errorException) {
                if (errorException.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                    verify(photoData,score);
                }
            }
        }
        return result;
    }


}
