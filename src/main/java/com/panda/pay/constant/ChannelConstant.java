package com.panda.pay.constant;

/** Created by jiangtao on 19-1-24 下午8:41 */
public interface ChannelConstant {

  String BLUEPAY_CHANNEL_CODE = "bluepay";

  /** 收款业务都以gather开头,放款业务都以loan开头 */

  // bluepay动态VA收款
  String BLUEPAY_BIZ_CODE_GATHER_DYNAMIC_VA = "gather_dynamic_VA_bluepay";
  // bluepay静态VA收款
  String BLUEPAY_BIZ_CODE_GATHER_STATIC_VA = "gather_static_VA_bluepay";

  // bluepay放款业务
  String BLUEPAY_BIZ_CODE_FINTECH_LOAN = "loan_fintech_bluepay";

  // 查询bluepay的收款详情
  String BLUEPAY_BIZ_QUERY_GATHER_DETAIL = "query_gather_detail_bluepay";
  // 查询bluepay的收款详情
  String BLUEPAY_BIZ_QUERY_LOAN_DETAIL = "query_loan_detail_bluepay";

  String SUCCESS = "success";
  String VAILD_FAIL = "vaild_fail";
  int SLEEP_TIME = 100;
  int REDIS_RETRY_COUNT = 20;
}
