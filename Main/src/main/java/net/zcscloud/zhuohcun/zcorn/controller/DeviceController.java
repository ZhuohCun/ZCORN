package net.zcscloud.zhuohcun.zcorn.controller;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import net.zcscloud.zhuohcun.zcorn.common.JwtUtil;
import net.zcscloud.zhuohcun.zcorn.entity.PredictionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import net.zcscloud.zhuohcun.zcorn.service.DeviceService;
import net.zcscloud.zhuohcun.zcorn.service.PlaceService;
import net.zcscloud.zhuohcun.zcorn.service.UserService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.annotations.Api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Api(tags = "The Controller of Devices")
@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "*")
public class DeviceController {

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;


    @PostMapping("/updateValue")
    public String updateValue(String id, String token, String value) throws IOException {
        while (true) {
            if ("1".equals(userService.verifyToken(token))) {
                try {
                    return deviceService.updateValue(id, value);
                } catch (Exception e) {
                    return "-1";
                }
            } else {
                return "-2";
            }
        }
    }

    @GetMapping("/getvalue")
    public String getValue(String id, String token) throws IOException {
        while (true) {
            if ("1".equals(userService.verifyToken(token))) {
                try {
                    return deviceService.getValue(Integer.parseInt(id));
                } catch (Exception e) {
                    return "-1";
                }
            } else {
                return "-1";
            }
        }
    }

    @PostMapping(value = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadAndPredict(@RequestParam("file") MultipartFile file,
                                 @RequestParam("cameraid") String id,
                                 @RequestParam("token") String token) throws IOException {
        if ("1".equals(userService.verifyToken(token))) {
            PredictionResponse responsehon = deviceService.predicthon(file, 1);
            if(Objects.equals(responsehon.getPrediction(), "Infected") &&responsehon.getConfidence()>=0.85){
                PredictionResponse responsewd = deviceService.predictwd(file, 1);
                if(Objects.equals(responsewd.getPrediction(), "spray") && responsewd.getConfidence()>=0.85){    //  If the corn is over sprayed, the drug sprayer won't be turned on for 3 days.
                    deviceService.lockDrugSprayer(Integer.parseInt(id));
                }else{
                    if(responsewd.getConfidence()>=0.85){
                        deviceService.turnDrugSprayerOn(Integer.parseInt(id));
                    }
                }
            }
            return "1";
        }else {
            return "-1";
        }
    }
}