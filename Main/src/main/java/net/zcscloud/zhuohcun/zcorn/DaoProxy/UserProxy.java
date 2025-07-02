package net.zcscloud.zhuohcun.zcorn.DaoProxy;

import net.zcscloud.zhuohcun.zcorn.dao.UserDao;
import net.zcscloud.zhuohcun.zcorn.entity.User;
import net.zcscloud.zhuohcun.zcorn.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserProxy implements GeneralProxy{  

    @Autowired
    private UserDao userDao;  

    public User findByName(String name){
        return userDao.findByName(name);
    }

    public User getUserbyId(int usid) {
        return userDao.getUserbyId(usid);
    }
}
