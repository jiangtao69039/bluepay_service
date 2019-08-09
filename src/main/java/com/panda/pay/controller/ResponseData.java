package com.panda.pay.controller;

import com.panda.pay.framework.CallbackResult;
import lombok.Getter;
import lombok.Setter;

/** Created by jiangtao on 19-1-27 下午2:58 */
@Getter
@Setter
public class ResponseData {

  private String code;
  private String message;
  private Object data;

  public ResponseData(String code, String message, Object data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public ResponseData(CallbackResult callbackResult) {
    if (callbackResult.isSuccess()) {
      this.code = "100000";
      this.message = "成功";
      this.data = callbackResult.getBusinessObject();
    } else {
      this.code = callbackResult.getErrCode();
      this.message = callbackResult.getBizName() + "," + callbackResult.getBizMsg();
      this.data = callbackResult.getBusinessObject();
    }
  }

  public static ResponseData unifyException(String msg) {
    return new ResponseData("2000000", msg, null);
  }
}
