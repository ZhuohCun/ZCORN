package net.zcscloud.zhuohcun.zcorn.controller;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.Claims;
import net.zcscloud.zhuohcun.zcorn.DaoProxy.UserProxy;
import net.zcscloud.zhuohcun.zcorn.common.JwtUtil;
import net.zcscloud.zhuohcun.zcorn.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "The Controller of Users")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UserController{
    @Autowired
    private UserService userService;
    @Autowired
    private UserProxy userProxy;

    @PostMapping("/login")
    public String login(String requestusername,String requestpassword) {
        String username = requestusername;
        String password = requestpassword;
        if (username == null) {
            return "-1";
        }
        if (password == null) {
            return "-1";
        }
        return userService.login(username,password);
    }
    @PostMapping("/verifyToken")
    public String verifyToken( String token) {
        if (token != null) {
            return userService.verifyToken(token);
        } else {
            return "-1"; 
        }
    }
}

