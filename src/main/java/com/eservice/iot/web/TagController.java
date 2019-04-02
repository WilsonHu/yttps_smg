package com.eservice.iot.web;

import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.service.TagService;
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
@RequestMapping("/tag")
public class TagController {
    @Resource
    private TagService tagService;

    @PostMapping("/list")
    public Result list(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "0") Integer size) {

        if (tagService != null) {
            return ResultGenerator.genSuccessResult(tagService.getmAllTagList());
        } else {
            return ResultGenerator.genSuccessResult(new ArrayList<>());
        }
    }

    @PostMapping("/add")
    public Result add(@RequestParam String name, @RequestParam String identity) {

        if (tagService != null && name != null && identity != null) {
            if ( tagService.getmAllTagList().size() > 0) {
                for (int i = 0; i <tagService.getmAllTagList().size(); i++) {
                    if(tagService.getmAllTagList().get(i).getTag_name().equals(name)) {
                        return ResultGenerator.genFailResult("Tag名称已存在！");
                    }
                }
            }
            if(tagService.createTag(name, identity.toUpperCase())) {
                return ResultGenerator.genSuccessResult();
            } else {
                return ResultGenerator.genFailResult("创建Tag失败！");
            }
        } else {
            if( name == null || identity == null) {
                return ResultGenerator.genFailResult("Tag名称或identity不能为空！");
            } else {
                return ResultGenerator.genFailResult("Tag服务不存在！");
            }
        }
    }

    @GetMapping("/getTagByName")
    public Result getTagByName(@RequestParam String name) {
        if(tagService.getmAllTagList().size() == 0) {
            return ResultGenerator.genFailResult("不存在");
        } else {
            for (int i = 0; i < tagService.getmAllTagList().size(); i++) {
                if(tagService.getmAllTagList().get(i).getTag_name().equals(name)) {
                    return ResultGenerator.genSuccessResult(tagService.getmAllTagList().get(i));
                }
            }
            return ResultGenerator.genFailResult("不存在");
        }
    }

}
