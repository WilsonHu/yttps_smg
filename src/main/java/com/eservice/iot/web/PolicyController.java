package com.eservice.iot.web;

import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.Policy;
import com.eservice.iot.service.PolicyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * Class Description: xxx
 *
 * @author Wilson Hu
 * @date 2018/08/21.
 */
@RestController
@RequestMapping("/policy")
public class PolicyController {
    @Resource
    private PolicyService policyService;

    @GetMapping("/list")
    public Result list() {
        if (policyService != null) {
            ArrayList<String> policyNameList = new ArrayList<>();
            for (Policy policy: policyService.getAllPolicy()) {
                policyNameList.add(policy.getName());
            }
            return ResultGenerator.genSuccessResult(policyNameList);
        } else {
            return ResultGenerator.genSuccessResult(new ArrayList<>());
        }
    }

}
