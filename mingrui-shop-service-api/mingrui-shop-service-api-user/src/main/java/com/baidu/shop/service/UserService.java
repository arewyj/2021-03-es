package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName UserService
 * @Description: TODO
 * @Author wyj
 * @Date 2021/3/10
 * @Version V1.0
 **/
@Api(tags = "用户接口")
public interface UserService {

    @ApiOperation(value = "用户注册")
    @PostMapping(value = "user/register")
    Result<JSONObject> register(@RequestBody UserDTO userDTO);

    @ApiOperation(value = "校验用户名或手机号唯一")
    @GetMapping(value = "user/check/{value}/{type}")
    Result<List<UserEntity>> checkUserNameOrPhone(@PathVariable(value = "value")String value, @PathVariable(value = "type") Integer type);

    @ApiOperation(value = "发送验证码")
    @PostMapping(value = "user/send")
    Result<JSONObject> send(@RequestBody UserDTO userDTO);


    @ApiOperation(value = "验证短信验证码")
    @GetMapping(value = "user/checkCode")
    Result<JSONObject> checkCode(String phone,String code);

}
