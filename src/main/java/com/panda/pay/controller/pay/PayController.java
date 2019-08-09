package com.panda.pay.controller.pay;

import com.panda.pay.constant.ChannelConstant;
import com.panda.pay.constant.enums.StatusEnum;
import com.panda.pay.controller.ResponseData;
import com.panda.pay.controller.params.UnifyGatherParams;
import com.panda.pay.controller.params.UnifyLoanParams;
import com.panda.pay.controller.params.UnifyQueryParams;
import com.panda.pay.framework.CallbackResult;
import com.panda.pay.service.channel.UnifyResponse;
import com.panda.pay.service.channel.bluepay.BluePayChannelImpl;
import com.panda.pay.service.query.UnifyQueryServiceImpl;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/** Created by jiangtao on 19-1-25 下午12:43 */
@Controller
@RequestMapping("/pay")
public class PayController {

  @Autowired BluePayChannelImpl bluePayChannel;
  @Autowired UnifyQueryServiceImpl unifyQueryService;
  /**
   * 统一收款接口
   *
   * @param params 收款接口的统一参数
   * @return 调用结果
   */
  @ResponseBody
  @RequestMapping(value = "/gather", method = RequestMethod.POST)
  public ResponseData unifyGather(@Valid @RequestBody UnifyGatherParams params) {

    CallbackResult callbackResult =
        CallbackResult.failure(
            "统一收款接口", "渠道code错误", StatusEnum.CHANNEL_CODE_ERROR.getCode(), null, null);
    switch (params.getChannelCode()) {
      case ChannelConstant.BLUEPAY_CHANNEL_CODE:
        callbackResult = bluePayChannel.doGather(params);
        break;
      default:
        break;
    }
    return new ResponseData(callbackResult);
  }

  /**
   * 统一放款接口
   *
   * @param params 放款接口的统一参数
   * @return 调用结果
   */
  @ResponseBody
  @RequestMapping(value = "/loan", method = RequestMethod.POST)
  public ResponseData unifyLoan(@Valid @RequestBody UnifyLoanParams params) {

    CallbackResult callbackResult =
        CallbackResult.failure(
            "统一放款接口", "渠道code错误", StatusEnum.CHANNEL_CODE_ERROR.getCode(), null, null);
    switch (params.getChannelCode()) {
      case ChannelConstant.BLUEPAY_CHANNEL_CODE:
        callbackResult = bluePayChannel.doLoan(params);
        break;
      default:
        break;
    }
    return new ResponseData(callbackResult);
  }

  /**
   * 统一查询
   *
   * @param params 查询接口统一参数,支持放款查询和收款查询
   * @return 调用结果
   */
  @ResponseBody
  @RequestMapping(value = "/query", method = RequestMethod.POST)
  public ResponseData unifyQuery(@Valid @RequestBody UnifyQueryParams params) {
    CallbackResult<UnifyResponse> callbackResult = unifyQueryService.doUnifyQuery(params);
    return new ResponseData(callbackResult);
  }
}
