package net.zcscloud.zhuohcun.zcorn.DaoProxy;

import net.zcscloud.zhuohcun.zcorn.dao.PlaceDao;
import net.zcscloud.zhuohcun.zcorn.entity.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlaceProxy{
    @Autowired
    private PlaceDao placeDao;
    public String getPlaceNameById(int placeid){
        return placeDao.getPlaceNameById(placeid);
    }
}
