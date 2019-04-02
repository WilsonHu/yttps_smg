package com.eservice.iot.web;
import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.service.StaffService;
import com.eservice.iot.service.VisitorService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
* Class Description: xxx
* @author Wilson Hu
* @date 2018/08/21.
*/
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private StaffService staffService;
    @Resource
    private VisitorService visitorService;

    /**
     * 该值为default值， Android端传入的参数不能为“0”
     */
    private static String ZERO_STRING = "0";


    @GetMapping("/getStaffNum")
    public Result getStaffNum() {
        return ResultGenerator.genSuccessResult(staffService.getStaffList().size());
    }
}
