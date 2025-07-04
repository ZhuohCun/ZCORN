package net.zcscloud.zhuohcun.zcorn.DaoProxy;

import net.zcscloud.zhuohcun.zcorn.dao.DeviceDao;
import net.zcscloud.zhuohcun.zcorn.entity.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class DeviceProxy{
    @Autowired
    private DeviceDao deviceDao;

    public void updateDeviceValue(int devid, String value){
        deviceDao.updateDeviceValue(devid, value);
    }



    public Device getDevicebyId(int devid) {
        return deviceDao.getDevicebyId(devid);
    }

    public String getValue(int devid) {
        return String.valueOf(deviceDao.getValue(devid));
    }

    public List<Device> getAllSensors() {
        return deviceDao.getAllSensors();
    }
    public List<Device> getAllOnOperators() {
        return deviceDao.getAllOnOperators();
    }
    public void setOperatorOn(int label,int placeid,int typeid){
        List<Integer> deviceids = deviceDao.getOperatorIdByLabelForOn(label,placeid,typeid);
        long currentTimeMillis = System.currentTimeMillis();
        deviceids.forEach(deviceid->{
            deviceDao.setOperatorOn(deviceid,currentTimeMillis);
        });
    }
    public void setOperatorOff(int label,int placeid,int typeid,int isforced){
        List<Integer> deviceids = deviceDao.getOperatorIdByLabelForOff(label,placeid,typeid);
        long currentTimeMillis = System.currentTimeMillis();
        deviceids.forEach(deviceid->{
            deviceDao.setOperatorOff(deviceid,currentTimeMillis,isforced);
        });
    }
    public void LockOperator(int label,int placeid,int typeid,int isforced){
        List<Integer> deviceids = deviceDao.getOperatorIdByLabelForLock(label,placeid,typeid);
        long currentTimeMillis = System.currentTimeMillis();
        deviceids.forEach(deviceid->{
            deviceDao.setOperatorLocked(deviceid,currentTimeMillis,isforced);
        });
    }
    public Device getSensorByOperatorid(int id,int sensortype){
        int placeid;
        int label;
        Device dev = deviceDao.getDevicebyId(id);
        placeid=dev.getId$place();
        label=dev.getLabel();
        return deviceDao.getSensorByOperatorid(placeid,label,sensortype);
    }
    public int getValueByLabel(int label, int placeid, int typeid){
        return deviceDao.getValueByLabel(label,placeid,typeid);
    }
    public Device getOperatorBySensorid(int id,int operatortype) {
        int placeid;
        int label;
        Device dev = deviceDao.getDevicebyId(id);
        placeid=dev.getId$place();
        label=dev.getLabel();
        return deviceDao.getOperatorBySensorid(placeid,label,operatortype);
    }
}
