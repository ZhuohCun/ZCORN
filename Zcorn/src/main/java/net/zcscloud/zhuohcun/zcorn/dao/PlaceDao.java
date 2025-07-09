package net.zcscloud.zhuohcun.zcorn.dao;

import net.zcscloud.zhuohcun.zcorn.entity.Place;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import javax.transaction.Transactional;
import java.util.List;

@Mapper
public interface PlaceDao{
    @Select("SELECT name FROM place WHERE id=#{placeid} LIMIT 1")
    String getPlaceNameById(int placeid);
}