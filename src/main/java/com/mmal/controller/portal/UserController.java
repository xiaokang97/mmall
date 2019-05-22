package com.mmal.controller.portal;

import com.mmal.common.Const;
import com.mmal.common.ServerResponse;
import com.mmal.pojo.User;
import com.mmal.service.IUserService;
import org.omg.CORBA.Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import javax.xml.ws.Response;

/**
 * Created by Xiaokang  Shi on 2019/4/28.
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;
    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do" ,method = RequestMethod.POST)
    //ResponseBody 在springMvc中dispacter定了返回值进行Json序列化
    @ResponseBody
    public ServerResponse<User>  Login(String username, String password, HttpSession session){
          ServerResponse<User> response = iUserService.login(username,password);
        if (response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }

        //services -- mybatis -- dao
        return  response;

    }

    @RequestMapping(value = "login.out" ,method = RequestMethod.GET)
    //ResponseBody 在springMvc中dispacter定了返回值进行Json序列化
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
         session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }


    @RequestMapping(value = "register.do" ,method = RequestMethod.GET)
    //ResponseBody 在springMvc中dispacter定了返回值进行Json序列化
    @ResponseBody
    public  ServerResponse<String> register(User user){
         return iUserService.register(user);
    }


    //校验用户名和邮箱是否存在，防止用户利用接口进行恶意调用注册接口，注册时点击下一个输入框的时候调用校验接口，实时返回校验情况
    @RequestMapping(value = "check_valid.do" ,method = RequestMethod.GET)
    //ResponseBody 在springMvc中dispacter定了返回值进行Json序列化
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return iUserService.checkValid(str,type);
    }


    @RequestMapping(value = "get_user_info.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user != null)
        {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登陆，无法获取当前用户信息！");
    }

    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
         return iUserService.selectQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.GET)
    @ResponseBody
    public  ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
      return  iUserService.checkAnswer(username,question,answer);
    }

    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetRoken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetRoken);
    }

    @RequestMapping(value = "reset_password.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorMessage("用户未登陆！");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }


}
