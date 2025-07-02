package net.zcscloud.zhuohcun.zcorn.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import net.zcscloud.zhuohcun.zcorn.DaoProxy.UserProxy;
import net.zcscloud.zhuohcun.zcorn.common.JwtUtil;
import net.zcscloud.zhuohcun.zcorn.entity.User;
import net.zcscloud.zhuohcun.zcorn.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserService{

    @Autowired
    private UserProxy userProxy;

    public String login(String username, String password) {
        User user = userProxy.findByName(username);
        if (user == null) {
            return "-1";
        }
        if (!user.getPassword().equals(password)) {
            return "-1";
        }
        if(user.getIsdeleted()==1){
            return "-2";
        }
        return JwtUtil.generateToken(username,432000000);
    }
    public String verifyToken(String token) {
        try {
            if (JwtUtil.validateToken(token)) {
                return "1";
            }
            return "-1"; 
        } catch (Exception e) {
            return "-2"; 
        }
    }
}