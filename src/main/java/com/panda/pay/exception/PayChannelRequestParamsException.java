package com.panda.pay.exception;

/** Created by jiangtao on 19-1-25 上午11:02 渠道参数错误异常 */
public class PayChannelRequestParamsException extends RuntimeException {

  public PayChannelRequestParamsException(String msg) {
    super(msg);
  }
}
