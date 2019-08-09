package com.panda.pay.service.channel.bluepay;

import com.panda.pay.service.channel.PayChannelBaseResponseParams;
import lombok.Getter;
import lombok.Setter;

/** Created by jiangtao on 19-1-27 下午4:00 bluepay放款接口的响应 */
@Getter
@Setter
public class BluePayFintechResponseParams extends PayChannelBaseResponseParams {

  String transactionId;
  String code;
  String transferStatus;
  String description;
}
