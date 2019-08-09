package com.panda.pay.controller.params;

import com.alibaba.fastjson.JSONObject;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Created by jiangtao on 19-1-27 下午4:05 */
@Getter
@Setter
public class UnifyLoanParams extends UnifyParams {

  @NotNull
  // 价格
  @Max(14075000)
  @Min(10000)
  protected Integer price;

  @NotNull
  // 币种
  protected String currency;

  // 收款方国家,印尼"ID"
  // String payeeCountry;

  // 国家区号 中国+86 印尼+62
  protected String nation;

  protected String mobile;

  @NotNull
  // 异步回调地址
  protected String notifyUrl;

  @NotNull protected String notifyType;

  // 具体渠道的特有参数,bluepay{payeeCountry,payeeBankName,payeeName,payeeAccount,payeeMsisdn}
  // protected JSONObject channelSpecialParams;

  // 回调原样返回
  protected JSONObject passBack;
}
