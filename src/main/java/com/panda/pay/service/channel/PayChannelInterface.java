package com.panda.pay.service.channel;

import com.panda.pay.controller.params.UnifyGatherParams;
import com.panda.pay.controller.params.UnifyLoanParams;
import com.panda.pay.framework.CallbackResult;
import java.util.List;

/** Created by jiangtao on 19-1-24 下午8:04 各个渠道对接的统一抽象接口 主要有 收款 放款 查询接口 */
public interface PayChannelInterface {

  /**
   * 收款接口
   *
   * @param params 渠道收款接口需要的请求参数
   * @return 渠道收款接口的响应
   */
  CallbackResult<?> doGather(UnifyGatherParams params);

  /**
   * 放款接口
   *
   * @param params 渠道放款接口需要的请求参数
   * @return 渠道放款接口的响应
   */
  CallbackResult<?> doLoan(UnifyLoanParams params);

  /**
   * 查询接口
   *
   * @param detail 渠道查询接口的详细表信息类
   * @return 渠道查询接口的响应
   */
  CallbackResult<UnifyResponse> doQuery(Object detail);

  /**
   * 返回渠道code
   *
   * @return 渠道code
   */
  String getChannelCode();

  /**
   * 返回本渠道支持的业务codes
   *
   * @return 本渠道支持的业务codes
   */
  List<String> getSupportBizCodes();
}
