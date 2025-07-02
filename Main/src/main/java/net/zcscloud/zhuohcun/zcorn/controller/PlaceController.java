package net.zcscloud.zhuohcun.zcorn.controller;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import net.zcscloud.zhuohcun.zcorn.common.JwtUtil;
import net.zcscloud.zhuohcun.zcorn.entity.Place;
import net.zcscloud.zhuohcun.zcorn.service.PlaceService;
import net.zcscloud.zhuohcun.zcorn.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Api(tags = "The Controller of Places")
@RestController
@RequestMapping("/api/place")
@CrossOrigin(origins = "*")
public class PlaceController {
    @Autowired
    private UserService userService;
    @Autowired
    private PlaceService placeService;
}
@Getter
class placeresponsebody{
    private int id;
    private String name;
    private String address;
    private String description;

    public placeresponsebody(int id, String name, String address, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
    }

    public placeresponsebody() {
    }
    public int getId() {
        return id;
    }
    public  String getName() {
        return name;
    }
    public  String getAddress() {
        return address;
    }
    public  String getDescription() {
        return description;
    }
}
