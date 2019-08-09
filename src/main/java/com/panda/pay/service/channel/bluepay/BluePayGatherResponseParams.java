package com.panda.pay.service.channel.bluepay;

import com.panda.pay.service.channel.PayChannelBaseResponseParams;
import lombok.Getter;
import lombok.Setter;

/** Created by jiangtao on 19-1-25 上午11:07 bluepay收款接口的响应 */
@Getter
@Setter
public class BluePayGatherResponseParams extends PayChannelBaseResponseParams {

  String vaFee;
  String isStatic;
  String otcFee;
  String status;
  String paymentCode;
  String description;
}
