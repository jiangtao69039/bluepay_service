package com.panda.pay.service.channel;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by jiangtao on 19-1-24 下午8:10
 *
 * <p>各个渠道的基本响应 具体某个渠道某个业务的响应类需要继承
 */
@Getter
@Setter
public abstract class PayChannelBaseResponseParams {

  // 接口的http响应码
  protected String httpStatusCode;

  // 请求业务的业务状态码
  protected String bizStatusCode;

  // 请求业务的业务状态说明
  protected String bizStatusMessage;
}
