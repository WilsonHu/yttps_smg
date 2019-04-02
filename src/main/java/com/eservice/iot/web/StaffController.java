package com.eservice.iot.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.*;
import com.eservice.iot.service.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class Description: xxx
 *
 * @author Wilson Hu
 * @date 2018/08/21.
 */
@RestController
@RequestMapping("/staff")
public class StaffController {
    private final static Logger logger = LoggerFactory.getLogger(StaffController.class);

    @Resource
    private StaffService staffService;
    @Resource
    private TagService tagService;
    @Resource
    private TokenService tokenService;
    @Resource
    private RestTemplate restTemplate;

    private SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
    @Value("${park_base_url}")
    private String PARK_BASE_URL;
    @Value("${similar_number}")
    private int SIMILAR_NUMBER;

    @GetMapping("/deleteByTagName")
    public Result deleteStaffByTagName(@RequestParam String name) {
        if (name == null || "".equals(name)) {
            return ResultGenerator.genFailResult("标签名字不能为空！");
        } else {
            if (tagService == null) {
                return ResultGenerator.genFailResult("标签服务没有启动！");
            } else {
                ArrayList<Tag> allTagList = tagService.getmAllTagList();
                String targetTagId = null;
                for (Tag item : allTagList) {
                    if (item.getTag_name().equals(name)) {
                        targetTagId = item.getTag_id();
                        break;
                    }
                }
                if (targetTagId == null) {
                    return ResultGenerator.genFailResult("找不到标签名字！");
                } else {
                    ArrayList<Staff> allStaffList = staffService.getStaffList();
                    ArrayList<Staff> allDeleteStaffList = new ArrayList<>();
                    for (Staff item : allStaffList) {
                        if (item.getTag_id_list().contains(targetTagId)) {
                            allDeleteStaffList.add(item);
                        }
                    }
                    int deleteCount = 0;
                    String resultStr = "";
                    ArrayList<String> failedList = new ArrayList<>();
                    resultStr += "需删除staff总数：" + allDeleteStaffList.size();
                    for (int i = 0; i < allDeleteStaffList.size(); i++) {
                        if (staffService.deleteStaff(allDeleteStaffList.get(i).getStaffId())) {
                            deleteCount++;
                        } else {
                            failedList.add(allDeleteStaffList.get(i).getPerson_information().getName());
                        }
                    }
                    resultStr += "; 删除成功staff总数：" + deleteCount;
                    resultStr += "; 删除失败staff数：" + failedList.size();
                    resultStr += "; 失败列表：" + failedList.toString();
                    return ResultGenerator.genSuccessResult(resultStr);
                }
            }
        }
    }

    @GetMapping("/excel")
    public Result excel() {
        if (staffService.getStaffList().size() > 0) {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("staff");

            ///设置要导出的文件的名字
            String fileName = formatter2.format(new Date()) + ".xls";
            //新增数据行，并且设置单元格数据
            insertDataInSheet(workbook, sheet, staffService.getStaffList());

            try {
                FileOutputStream out = new FileOutputStream("./" + fileName);
                workbook.write(out);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("Staff数量为0");
        }
    }

    @GetMapping("/similar")
    public Result similar() {
        if (staffService.getStaffList().size() > 0) {
            File homeFolder = new File("staff");
            if (!homeFolder.exists()) {
                homeFolder.mkdirs();
            }
            for (int i = 0; i < staffService.getStaffList().size(); i++) {
                Staff staff = staffService.getStaffList().get(i);
                /*
                 * 子目录组成：姓名_员工号_id (id为人脸平台中的id)
                 */
                String subFolderName = staff.getPerson_information().getName() + "_" + staff.getPerson_information().getId() + "_" + staff.getStaffId();
                File subFolder = new File(homeFolder.getPath() + "/" + subFolderName);
                if (!subFolder.exists()) {
                    subFolder.mkdirs();
                }
                if (tokenService != null) {
                    String token = tokenService.getToken();
                    if (token != null) {
                        if(staff.getFace_list() != null && staff.getFace_list().size() > 0) {
                            logger.info("Staff info：" + JSON.toJSONString(staff));
                            String staffBase64ImgStr = getBase64ImgStr(PARK_BASE_URL + "/image/" + staff.getFace_list().get(0).getFace_image_id());
                            if (staffBase64ImgStr != null) {
                                HashMap<String, Object> postParameters = new HashMap<>();
                                postParameters.put("image_content_base64", staffBase64ImgStr);
                                postParameters.put("threshold", 0);
                                postParameters.put("topk", 10);
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
                                            if (responseModel != null && responseModel.getResult() != null) {
                                                ArrayList<PersonRetrievalResultDTO> tempList = (ArrayList<PersonRetrievalResultDTO>) JSONArray.parseArray(responseModel.getResult(), PersonRetrievalResultDTO.class);
                                                if (tempList != null && tempList.size() > 0) {
                                                    LinkedHashMap<String, PersonRetrievalResultDTO> similarPersons = new LinkedHashMap<>();
                                                    for (PersonRetrievalResultDTO person : tempList) {
                                                        if (similarPersons.get(person.getFace_id()) == null && similarPersons.size() < SIMILAR_NUMBER && !staff.getStaffId().equals(person.getPerson().getPerson_id())) {
                                                            similarPersons.put(person.getFace_id(), person);
                                                            if (person.getScore() > 50) {
                                                                logger.info(staff.getPerson_information().getName() + " : " + person.getPerson().getPerson_information().getName() + " ==> " + person.getScore());
                                                            }
                                                            if(person.getPerson().getFace_list() != null || person.getPerson().getFace_list().size() > 0) {
                                                                saveSimilarStaffImage(subFolder.getPath(), person);
                                                            } else {
                                                                logger.error("姓名：{}, 照片不存在！", person.getPerson().getPerson_information().getName());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (HttpClientErrorException errorException) {
                                    if (errorException.getStatusCode().value() == ResponseCode.TOKEN_INVALID) {
                                        similar();
                                    }
                                }
                            } else {
                                return ResultGenerator.genFailResult("Cannot get staff's base64 image!");
                            }
                        }
                    } else {
                        return ResultGenerator.genFailResult("Token is null!");
                    }
                }
            }
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("Staff数量为0");
        }
    }

    private void insertDataInSheet(HSSFWorkbook wb, HSSFSheet sheet, List<Staff> list) {
        int rowNum = 1;
        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        String[] excelHeaders = {"照片", "姓名", "员工号", "卡号", "标签"};
        //headers表示excel表中第一行的表头
        HSSFRow row3 = sheet.createRow(0);
        //在excel表中添加表头
        for (int i = 0; i < excelHeaders.length; i++) {
            HSSFCell cell = row3.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(excelHeaders[i]);
            cell.setCellValue(text);
        }
        //在表中存放查询到的数据放入对应的列
        int index = 1;
        for (Staff staff : list) {
            if (staff.getFace_list().size() > 0) {
                HSSFRow row = sheet.createRow(rowNum);
                row.setHeight((short) 1000);
                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                String url = PARK_BASE_URL + "/image/" + staff.getFace_list().get(0).getFace_image_id();
                BufferedImage image = null;
                try {
                    image = ImageIO.read(new URL(url));
                    ImageIO.write(image, "jpg", byteArrayOut);
                    //anchor主要用于设置图片的属性
                    HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0, (short) 0, index, (short) 1, index + 1);
                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    patriarch.createPicture(anchor, wb.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
                    index++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //row.createCell(0).setCellValue(staff.getFace_list().get(0).getFace_image_id());
                row.createCell(1).setCellValue(staff.getPerson_information().getName());
                row.createCell(2).setCellValue(staff.getPerson_information().getId());
                if (staff.getCard_numbers() != null) {
                    row.createCell(3).setCellValue(listToString(staff.getCard_numbers()));
                } else {
                    row.createCell(3).setCellValue(staff.getPerson_information().getCard_no());
                }
                row.createCell(4).setCellValue(tagService.tagIdToName(staff.getTag_id_list()));
                rowNum++;
            } else {
                logger.warn("Face ID list is zero: {}", staff.getPerson_information().getName());
            }
        }
    }

    private String listToString(List<String> list) {
        String result = "";
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (i != list.size() - 1) {
                    result += list.get(i) + " | ";
                } else {
                    result += list.get(i);
                }
            }
        }
        return result;
    }

    private String getBase64ImgStr(String url) {
        String result = "";
        try {
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            BufferedImage image = ImageIO.read(new URL(url));
            ImageIO.write(image, "jpg", byteArrayOut);
            BASE64Encoder encoder = new BASE64Encoder();
            result = encoder.encode(byteArrayOut.toByteArray()).replaceAll("\\r\\n", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void saveSimilarStaffImage(String path, PersonRetrievalResultDTO person) {
        String fileName = path + "/" + person.getPerson().getPerson_information().getName() + "_" + person.getPerson().getPerson_information().getId() + "_" + person.getScore() + ".jpg";
        try {
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            BufferedImage image = ImageIO.read(new URL(PARK_BASE_URL + "/image/" + person.getPerson().getFace_list().get(0).getFace_image_id()));
            ImageIO.write(image, "jpg", byteArrayOut);
            DataOutputStream to = new DataOutputStream(new FileOutputStream(fileName));
            byteArrayOut.writeTo(to);
            byteArrayOut.flush();
            byteArrayOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
