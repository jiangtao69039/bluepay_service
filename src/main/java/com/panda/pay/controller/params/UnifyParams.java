package com.panda.pay.controller.params;

import com.alibaba.fastjson.JSONObject;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Created by jiangtao on 19-1-27 下午4:51 */
@Getter
@Setter
public abstract class UnifyParams {
  @NotNull
  // 外部系统传递的唯一标识
  protected String outTradeNo;

  @NotNull
  // 渠道code
  protected String channelCode;

  @NotNull
  // 渠道业务code
  protected String channelBizCode;

  // 具体渠道的特有参数
  protected JSONObject channelSpecialParams;

  // 扩展参数
  protected JSONObject extend;

  // protected Boolean testFlag=Boolean.TRUE;//是否为测试环境,默认true
}
