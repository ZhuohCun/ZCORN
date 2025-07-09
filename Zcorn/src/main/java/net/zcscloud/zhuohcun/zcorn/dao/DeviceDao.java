package net.zcscloud.zhuohcun.zcorn.dao;

import net.zcscloud.zhuohcun.zcorn.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Mapper
public interface DeviceDao {

    @Transactional
    @Update("UPDATE `device` SET `cvalue`=#{value} WHERE `id`=#{devid} AND `isdeleted`=0")
    void updateDeviceValue(@Param("devid") int devid,
                           @Param("value") String value);

    @Select("SELECT * FROM `device` WHERE `id`=#{devid} AND `isdeleted`=0 LIMIT 1")
    Device getDevicebyId(@Param("devid") int devid);

    @Select("SELECT `cvalue` FROM `device` WHERE `id`=#{devid} AND `isdeleted`=0")
    int getValue(@Param("devid") int devid);

    @Select("SELECT * FROM device WHERE isdeleted=0 AND isoperator=0")
    List<Device> getAllSensors();

    @Select("SELECT * FROM device WHERE cvalue=\"1\" AND isoperator=1 AND isdeleted=0")
    List<Device> getAllOnOperators();

    @Transactional
    @Update("UPDATE Device SET cvalue=1, turntime=#{now}, isforcedoff=0 WHERE id=#{devid} AND isoperator=1 AND isdeleted=0 AND cvalue=0")
    void setOperatorOn(int devid, long now);

    @Transactional
    @Update("UPDATE Device SET cvalue=0, turntime=#{now}, isforcedoff=#{isforced} WHERE id=#{devid} AND isoperator=1 AND isdeleted=0 AND cvalue>=1")
    void setOperatorOff(int devid, long now, int isforced);

    @Transactional
    @Update("UPDATE Device SET cvalue=0, turntime=#{now}, isforcedoff=#{isforced} WHERE id=#{devid} AND isoperator=1 AND isdeleted=0")
    void setOperatorLocked(int devid, long now, int isforced);

    @Select("SELECT id FROM Device WHERE label=#{label} AND isoperator=1 AND isdeleted=0 AND cvalue=0 AND id$place=#{placeid} AND `type`=#{typeid}")
    List<Integer> getOperatorIdByLabelForOn(int label,int placeid,int typeid);

    @Select("SELECT id FROM Device WHERE label=#{label} AND isoperator=1 AND isdeleted=0 AND cvalue>=1 AND id$place=#{placeid} AND `type`=#{typeid}")
    List<Integer> getOperatorIdByLabelForOff(int label,int placeid,int typeid);

    @Select("SELECT id FROM Device WHERE label=#{label} AND isoperator=1 AND isdeleted=0 AND id$place=#{placeid} AND `type`=#{typeid}")
    List<Integer> getOperatorIdByLabelForLock(int label,int placeid,int typeid);

    @Select("SELECT cvalue FROM Device WHERE label=#{label} AND isoperator=1 AND isdeleted=0 AND id$place=#{placeid} AND `type`=#{typeid} LIMIT 1") //assume that in a specific place, there will be ONLY ONE same device in one specific group
    int getValueByLabel(int label,int placeid,int typeid);

    @Select("SELECT * FROM Device where id$place=#{placeid} AND label=#{label} AND isoperator=0 AND isdeleted=0 AND type=#{sensortype} LIMIT 1")
    Device getSensorByOperatorid(int placeid, int label, int sensortype);

    @Select("SELECT * FROM Device where id$place=#{placeid} AND label=#{label} AND isoperator=1 AND isdeleted=0 AND type=#{sensortype} LIMIT 1")
    Device getOperatorBySensorid(int placeid,int label,int sensortype);
}
