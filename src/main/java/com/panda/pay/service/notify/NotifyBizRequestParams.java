package com.panda.pay.service.notify;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by jiangtao on 19-1-29 下午2:52
 *
 * <p>回调业务系统的http请求参数
 */
@Getter
@Setter
public class NotifyBizRequestParams {

  String tradeStatus; // 成功失败
  String tradeMsg;
  String channelCode;
  String channelBizCode;
  String transactionCode;
  String outTradeNo;
  JSONObject passBack;
  JSONObject originChannelCallBack;
}
