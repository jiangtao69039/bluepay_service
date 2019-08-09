package com.panda.pay.constant.enums;

/** Created by jiangtao on 19-1-25 下午2:20 */
public enum StatusEnum {
  BLUEPAY_REQ_PARAMS_ERROR("100001", "bluepay请求参数错误"),
  PAYTRADE_OUT_TRADE_NO_EXIST("100002", "pay_trade表out_trade_no重复"),
  GATHERTRADE_OUT_TRADE_NO_EXIST("100003", "gather_trade表out_trade_no重复"),
  HTTP_REQUEST_ERROR("100004", "http请求错误"),
  GATHER_TRADE_STATUS_PROCESS("process", "收款操作进行中"),

  HTTP_RESPONSE_ERROR("100005", "http响应错误"),
  BLUEPAY_GATHER_REQ_ERROR("100006", "bluepay收款接口业务错误"),
  CHANNEL_CODE_ERROR("100007", "渠道code错误"),
  LOANTRADE_OUT_TRADE_NO_EXIST("100008", "loan_trade表out_trade_no重复"),
  LOAN_TRADE_STATUS_PROCESS("process", "放款操作进行中"),
  AES_ERROR("100009", "AES加密失败"),

  BLUEPAY_LOAN_REQ_ERROR("100010", "bluepay放款接口业务错误"),
  PAYTRADE_NOT_FOUND("100011", "pay_trade表记录未找到"),
  GATHERTRADE_OR_LOANTRADE_NOT_FOUND("100012", "详细表记录未找到"),
  CHANNELIMPL_NOT_FOUND("100013", "渠道实现类未找到"),
  BLUEPAY_BIZ_NOT_FOUND("100014", "未找到bluepay业务"),
  CURRENCY_NOT_MATCH("100015", "currency不匹配"),
  PRICE_NOT_MATCH("100016", "currency不匹配"),
  BLUEPAY_GATHER_CALLBACK_ERROR("100017", "bluepay异步回调标识本次交易失败"),
  NOTIFY_BIZ_ERROR("100018", "通知业务系统失败"),
  NOT_SUPPORT_NOTIFY_TYPE("100019", "不支持的通知类型,请使用http或sqs"),
  GET_NOTIFY_BIZ_PARAMS_ERROR("100020", "从bluepay回调参数生成业务系统的回调参数失败"),
  ;

  private String code;
  private String msg;

  private StatusEnum(String code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public String getCode() {
    return code;
  }

  public String getMsg() {
    return msg;
  }
}
