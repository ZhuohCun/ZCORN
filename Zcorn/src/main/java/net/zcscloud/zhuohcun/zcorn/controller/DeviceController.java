package net.zcscloud.zhuohcun.zcorn.controller;
import lombok.Getter;
import net.zcscloud.zhuohcun.zcorn.entity.PredictionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import net.zcscloud.zhuohcun.zcorn.service.DeviceService;
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
    private DeviceService deviceService;


    @PostMapping("/updateValue")
    public String updateValue(String id, String value) throws IOException {
        while (true) {
            try{
                return deviceService.updateValue(id, value);
            } catch (Exception ignored) {

            }
        }
    }

    @GetMapping("/getvalue")
    public String getValue(String id) throws IOException {
        while (true) {
            try{
                return deviceService.getValue(Integer.parseInt(id));
            } catch (Exception ignored) {

            }
        }
    }

    @PostMapping(value = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadAndPredict(@RequestParam("file") MultipartFile file,
                                 @RequestParam("cameraid") String id) throws IOException {
        while(true) {
            try{
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
            } catch (Exception ignored) {

            }
        }
    }
}