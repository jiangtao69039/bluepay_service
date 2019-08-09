package com.panda.pay.service.channel;

/**
 * Created by jiangtao on 19-1-24 下午8:08
 *
 * <p>各个渠道的基本请求参数 具体渠道的某个业务参数类需要继承本类
 */
public abstract class PayChannelBaseRequestParams {

  // 支付系统生成的唯一交易号

  protected String transactionCode;
}
