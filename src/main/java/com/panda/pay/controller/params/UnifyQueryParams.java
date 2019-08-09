package com.panda.pay.controller.params;

import com.alibaba.fastjson.JSONObject;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Created by jiangtao on 19-1-28 上午10:02 */
@Getter
@Setter
public class UnifyQueryParams {

  // 交易系统生成的唯一编号
  @NotNull private String transactionCode;
  // 扩展参数
  private JSONObject extend;
}
