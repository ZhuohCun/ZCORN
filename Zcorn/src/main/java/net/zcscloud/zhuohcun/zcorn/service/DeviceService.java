package net.zcscloud.zhuohcun.zcorn.service;

import net.zcscloud.zhuohcun.zcorn.DaoProxy.DeviceProxy;
import net.zcscloud.zhuohcun.zcorn.DaoProxy.PlaceProxy;
import net.zcscloud.zhuohcun.zcorn.entity.*;
import okhttp3.*;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.MultipartFile;

@Service
@EnableScheduling
public class DeviceService{

    @Autowired
    DeviceProxy deviceProxy;


    @Autowired
    PlaceProxy placeProxy;

    public String updateValue(String devid,String value) throws JSONException, IOException {
        synchronized (this) {
            while(true){
                try{
                    deviceProxy.updateDeviceValue(Integer.parseInt(devid),value);
                    return "1";
                }catch (Exception e){
                    return "-1";
                }
            }
        }
    }

    public String getValue(int id) {
        return deviceProxy.getValue(id);
    }

    @Scheduled(fixedRate = 5000) // this method will be performed every 5 seconds, to turn on all necessary operators    <==> while true
    public void turnOnAll() throws JSONException, IOException {
        long currentTimeMillis = System.currentTimeMillis();
        List<Device> devices=deviceProxy.getAllSensors();
        devices.forEach(device->{
            if(device.getType()==1) {   //soil sensor
                Device opera = deviceProxy.getOperatorBySensorid(device.getId(),4);
                long time=opera.getTurntime();
                int isforcedoff=opera.getIsforcedoff();
                if (((device.getCvalue() <= 10 && isforcedoff==0) || (device.getCvalue() <= 10 && isforcedoff==1 && currentTimeMillis-time>=86400000)) && opera.getCvalue()==0) {   //if it is forced off, it won't be turned on for 24 hours
                    while (deviceProxy.getValueByLabel(device.getLabel(), device.getId$place(), 4) == 0) {   //the ID of watering device is 4
                        deviceProxy.setOperatorOn(device.getLabel(), device.getId$place(), 4);
                    }
                    System.out.println("The watering device "+device.getLabel()+" in "+placeProxy.getPlaceNameById(device.getId$place())+" has been turned on.");
                }
            }else if(device.getType()==2){  //temperature sensor
                Device opera = deviceProxy.getOperatorBySensorid(device.getId(),6);
                long time=opera.getTurntime();
                int isforcedoff=opera.getIsforcedoff();
                if (((device.getCvalue() >= 30 && isforcedoff==0) || (device.getCvalue() >= 30 && isforcedoff==1 && currentTimeMillis-time>=86400000)) && opera.getCvalue()==0){   //if it is forced off, it won't be turned on for 24 hours
                    while (deviceProxy.getValueByLabel(device.getLabel(), device.getId$place(), 6) == 0) {   //the ID of water sprayer is 6
                        deviceProxy.setOperatorOn(device.getLabel(), device.getId$place(), 6);
                    }
                    System.out.println("The watering sprayer "+device.getLabel()+" in "+placeProxy.getPlaceNameById(device.getId$place())+" has been turned on.");
                }
            }
        });
    }
    @Scheduled(fixedRate = 5000) // this method will be performed every 5 seconds, to turn off all operators when recovering or overtime    <==> while true
    public void turnOffAll() {
        long currentTimeMillis = System.currentTimeMillis();
        AtomicInteger turned= new AtomicInteger();
        deviceProxy.getAllOnOperators().forEach(device->{
            turned.set(0);
            if(device.getType()==4) {    //watering device
                Device soil=deviceProxy.getSensorByOperatorid(device.getId$place(),1);
                if(soil.getCvalue()>=15) {
                    deviceProxy.setOperatorOff(device.getLabel(), device.getId$place(), 4,0);
                    turned.set(1);
                    System.out.println("Watering device "+device.getLabel()+" in "+placeProxy.getPlaceNameById(device.getId$place())+" has been turned off.");
                }
                if(currentTimeMillis-device.getTurntime()>=3600000 && turned.equals(0)) { //have been turned on for more than 1 hour
                    deviceProxy.setOperatorOff(device.getLabel(), device.getId$place(), 4,1);
                    System.out.println("Watering device "+device.getLabel()+" in "+placeProxy.getPlaceNameById(device.getId$place())+" has been turned off forcedly.");
                }
            }else if(device.getType()==5){    //drug sprayer   only forced off can be performed for drug sprayers
                if(currentTimeMillis-device.getTurntime()>=300000 && device.getIsforcedoff()<2) { //have been turned on for more than 5 minutes
                    deviceProxy.setOperatorOff(device.getLabel(), device.getId$place(), 5,1);
                    System.out.println("Drug Sprayer "+device.getLabel()+" in "+placeProxy.getPlaceNameById(device.getId$place())+" has been turned off forcedly.");
                }
            }else if(device.getType()==6){    //water sprayer
                Device temperature=deviceProxy.getSensorByOperatorid(device.getId$place(),2);
                if(temperature.getCvalue()<=26) {  //temperature less than 27 oc
                    deviceProxy.setOperatorOff(device.getLabel(), device.getId$place(), 6,0);
                    turned.set(1);
                    System.out.println("Water Sprayer "+device.getLabel()+" in "+placeProxy.getPlaceNameById(device.getId$place())+" has been turned off.");
                }
                if(currentTimeMillis-device.getTurntime()>=7200000 && turned.equals(0)) { //have been turned on for more than 2 hours
                    deviceProxy.setOperatorOff(device.getLabel(), device.getId$place(), 6,1);
                    System.out.println("Water Sprayer "+device.getLabel()+" in "+placeProxy.getPlaceNameById(device.getId$place())+" has been turned off forcedly.");
                }
            }
        });
    }
    public void turnDrugSprayerOn(int sensorid) {
        Device sprayer=deviceProxy.getOperatorBySensorid(sensorid,5);
        long currentTimeMillis = System.currentTimeMillis();
        if(sprayer.getCvalue()==0 && sprayer.getIsforcedoff()!=2 && currentTimeMillis-sprayer.getTurntime()>=86400000) {   //drug sprayer can only be turned on once a day
            deviceProxy.setOperatorOn(sprayer.getLabel(), sprayer.getId$place(), 5);
            System.out.println("Drug Sprayer "+sprayer.getLabel()+" in "+placeProxy.getPlaceNameById(sprayer.getId$place())+" has been turned on.");
        } else if (sprayer.getCvalue()==0 && sprayer.getIsforcedoff()==2 && currentTimeMillis-sprayer.getTurntime()>=259200000) {
            deviceProxy.setOperatorOn(sprayer.getLabel(), sprayer.getId$place(), 5);
            System.out.println("Drug Sprayer "+sprayer.getLabel()+" in "+placeProxy.getPlaceNameById(sprayer.getId$place())+" has been turned on.");
        }
    }
    public void lockDrugSprayer(int sensorid) {
        Device sprayer=deviceProxy.getOperatorBySensorid(sensorid,5);
        if (sprayer.getIsforcedoff() <= 1) {
            deviceProxy.LockOperator(sprayer.getLabel(), sprayer.getId$place(), 5,2);
            System.out.println("Drug Sprayer "+sprayer.getLabel()+" in "+placeProxy.getPlaceNameById(sprayer.getId$place())+" has been locked for 3 days.");
        }
    }
    private final OkHttpClient client = new OkHttpClient();
    private String pythonServiceUrl = "http://localhost:8001";   //replace the address of the AI server here

    public PredictionResponse predicthon(MultipartFile file, int topK) throws IOException {       //to recognize whether the corn in the picture is healthy or not
        okhttp3.RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getOriginalFilename(),
                        okhttp3.RequestBody.create(file.getBytes(), MediaType.parse(file.getContentType())))
                .addFormDataPart("model_name", "hon")
                .addFormDataPart("top_k", String.valueOf(topK))
                .build();

        Request request = new Request.Builder()
                .url(pythonServiceUrl + "/predicthon")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                
            }
            String json = response.body().string();
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, PredictionResponse.class);
        }
    }
    public PredictionResponse predictwd(MultipartFile file, int topK) throws IOException {    //to recognize which disease the corn in the picture is infected
        okhttp3.RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getOriginalFilename(),
                        okhttp3.RequestBody.create(file.getBytes(), MediaType.parse(file.getContentType())))
                .addFormDataPart("model_name", "wd")
                .addFormDataPart("top_k", String.valueOf(topK))
                .build();

        Request request = new Request.Builder()
                .url(pythonServiceUrl + "/predictwd")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {

            }
            String json = response.body().string();
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, PredictionResponse.class);
        }
    }
}

