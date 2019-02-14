package com.xiaomi.infra.galaxy.fds.client;

/**
 * Created by yepeng on 18-7-11.
 */
public class Utils {
  public static Integer getErrorCode(String errorMsg){
    String flag = "status=";
    int starIndex = errorMsg.indexOf(flag);
    Integer statusCode = null;
    if(starIndex < 0){
      return null;
    }
    int index = starIndex + flag.length();
    try {
      statusCode =  Integer.valueOf(errorMsg.substring(index, index + 3));
    }catch (NumberFormatException e){
    }
    return statusCode;
  }
}
