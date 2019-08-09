package com.panda.pay.service.channel;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by jiangtao on 19-1-28 上午10:05
 *
 * <p>支付系统的统一返回格式
 */
@Getter
@Setter
public class UnifyResponse {

  // 支付系统的业务类型(目前支持gather收款  loan放款  query_gather  query_loan)
  private String payServeBizType;

  private String transactionCode;

  // private String outTradeNo;

  // 根据不同的业务返回不同的内容
  private JSONObject bizParams;
}
