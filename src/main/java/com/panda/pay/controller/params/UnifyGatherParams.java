package com.panda.pay.controller.params;

import com.alibaba.fastjson.JSONObject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Created by jiangtao on 19-1-25 上午11:25 统一收款接口参数类 */
@Getter
@Setter
public class UnifyGatherParams extends UnifyParams {

  @NotNull
  // 价格
  @Min(10000)
  protected Integer price;

  @NotNull
  // 币种
  protected String currency;

  @NotNull
  // 异步回调地址
  protected String notifyUrl;

  @NotNull protected String notifyType; // http sqs

  @NotNull
  // 国家代码,通过国家代码来对应国家区号,如印尼国家代码ID,区号62 中国CN 86
  protected String nation;

  protected String mobile;

  // 具体渠道的特有参数
  // protected JSONObject channelSpecialParams;

  // 回调原样返回
  protected JSONObject passBack;

  // 扩展参数
  /** {"prodFlag":"false"} 默认是测试环境,生成环境需要必传这个扩展参数 */
  // protected JSONObject extend;

  // protected Boolean testFlag=Boolean.TRUE;//是否为测试环境,默认true

}
