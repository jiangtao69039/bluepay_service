package com.panda.pay.controller.params;

import lombok.Getter;
import lombok.Setter;

/** Created by jiangtao on 19-1-28 下午3:38 bluepay回调参数 */
@Getter
@Setter
public class BluePayCallBackParams {

  String cmd;
  String msisdn;
  String operator;
  String indomog;
  String paytype;
  String t_id;
  String bt_id;
  String status;
  String price;
  String interfacetype;
  String currency;
  String productid;
  String encrypt;
}
