package com.mmal.service.impl;

import com.mmal.common.Const;
import com.mmal.common.ServerResponse;
import com.mmal.common.TokenCache;
import com.mmal.dao.UserMapper;
import com.mmal.pojo.User;
import com.mmal.service.IUserService;
import com.mmal.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import java.util.UUID;


/**
 * Created by Xiaokang  Shi on 2019/4/28.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{

    @Autowired
    private UserMapper  userMapper;

    @Override


    public ServerResponse<User> login(String username, String password) {

        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("用户名不存在!");
        }
        
        // TODO: 2019/4/28 密码登录MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if (user==null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return  ServerResponse.createBySuccess("登录成功！",user);
    }

    //注册
    public ServerResponse<String> register(User user){
        ServerResponse<String> validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败!");
        }
        return ServerResponse.createByErrorMessage("注册成功!");
    }

    //注册验证方法
    public ServerResponse<String> checkValid(String str,String type) {
       if (org.apache.commons.lang3.StringUtils.isNotBlank(type)) {
           if (Const.USERNAME.equals(type)){
               int resultCount = userMapper.checkUsername(str);
               if (resultCount > 0){
                   return ServerResponse.createByErrorMessage("用户名已存在");
               }
           }
           if (Const.EMAIL.equals(type)){
               int resultCount = userMapper.checkEmail(str);
               if (resultCount > 0){
                   return ServerResponse.createByErrorMessage("邮箱已存在");
               }
           }
       }else{
           return ServerResponse.createByErrorMessage("参数错误！");
       }
        return ServerResponse.createBySuccess("校验成功");
    }

    public ServerResponse selectQuestion(String username){
        ServerResponse validReponse = this.checkValid(username,Const.USERNAME);
        if (validReponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在！");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(question))
        {
           return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount>0){
            //验证成功
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if (org.apache.commons.lang3.StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
        ServerResponse validReponse = this.checkValid(username,Const.USERNAME);
        if (validReponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在！");
        }
        String  token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (org.apache.commons.lang3.StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        if (org.apache.commons.lang3.StringUtils.equals(forgetToken,token)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
            if (rowCount > 0){
                return ServerResponse.createBySuccessMessage("密码修改成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("密码修改失败！");
    }

    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
       //防止横向越权，校验一下用户的旧密码，一定要指定是这个用户，查询一个count（1），如果不指定id，那么结果就是true了
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if (resultCount == 0){
          return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount>0){
            ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

}
