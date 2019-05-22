package com.mmal.common;

/**
 * Created by Xiaokang  Shi on 2019/4/28.
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";//用户名
    public  static  final String EMAIL = "email";
    public  static  final String USERNAME = "username";
    public interface Role{
        int ROLE_CUSTOMER = 0 ;//普通用户
        int ROLE_ADMIN = 1 ;//管理员
    }
}
