package com.panda.pay.framework;

import java.io.Serializable;
import lombok.Getter;

/** Created by jiangtao on 19-1-25 下午2:00 */
@Getter
public class CallbackResult<T> implements Serializable {

  /** 成功 */
  public static final int SUCCESS = 1;

  /** 失败 */
  public static final int FAILURE = -1;

  /** 事务状态 */
  private int statusCode = SUCCESS;

  private String errCode;
  /** 业务名称 */
  private String bizName;
  /** 业务提示信息 */
  private String bizMsg;

  /** 发生的异常信息 */
  private Throwable throwable;

  /** 与该结果关联的业务主体 */
  private T businessObject;

  private CallbackResult(
      int statusCode, String bizName, String bizMsg, T biz, Throwable t, String errCode) {
    this.statusCode = statusCode;
    this.bizName = bizName;
    this.bizMsg = bizMsg;
    this.businessObject = biz;
    this.throwable = t;
    this.errCode = errCode;
  }

  public static <T> CallbackResult<T> success(String bizName, String bizMsg, T biz) {
    return new CallbackResult<>(SUCCESS, bizName, bizMsg, biz, null, null);
  }

  public static <T> CallbackResult<T> success() {
    return new CallbackResult<>(SUCCESS, null, null, null, null, null);
  }

  public static <T> CallbackResult<T> failure(
      String bizName, String bizMsg, String errCode, T biz, Throwable t) {
    return new CallbackResult<>(FAILURE, bizName, bizMsg, biz, t, errCode);
  }

  public boolean isSuccess() {
    return this.statusCode == SUCCESS;
  }
}
