package net.zcscloud.zhuohcun.zcorn.dao;

import net.zcscloud.zhuohcun.zcorn.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import javax.transaction.Transactional;

@Mapper
public interface UserDao{


    @Transactional
    @Select("SELECT * FROM `user` WHERE `id`=#{usid} AND `isdeleted`=0 LIMIT 1")
    User getUserbyId(@Param("usid") int usid);

    @Transactional
    @Select("SELECT * FROM `user` WHERE `username`=#{name} AND `isdeleted`=0 LIMIT 1")
    User findByName(@Param("name") String name);

}
