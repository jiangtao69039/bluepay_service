package com.panda.pay.service.channel.bluepay;

import com.panda.pay.service.channel.PayChannelBaseRequestParams;
import lombok.Getter;
import lombok.Setter;

/** Created by jiangtao on 19-1-24 下午8:32 bluepay收款接口的请求参数 */
@Getter
@Setter
public class BluePayGatherRequestParams extends PayChannelBaseRequestParams {

  protected String productId;
  protected Integer price;
  protected static final String promotionId = "1000";
  protected static final String ui = "none";

  // 国家代码
  protected String nation;
  // 手机号
  protected String mobile;
  // atm  otc
  protected String payType;
  // 银行类型。如果 payType=atm，那么bankType 必须等于其中之一：permata, bni, mandiri。如果payType=otc, bankType不用传
  protected String backType;
}
