package com.panda.pay.service.query;

import com.panda.pay.constant.ChannelConstant;
import com.panda.pay.constant.enums.StatusEnum;
import com.panda.pay.controller.params.UnifyQueryParams;
import com.panda.pay.dao.GatherTradeDao;
import com.panda.pay.dao.LoanTradeDao;
import com.panda.pay.dao.PayTradeDao;
import com.panda.pay.ds.primary.entity.PayTrade;
import com.panda.pay.framework.CallbackResult;
import com.panda.pay.service.channel.UnifyResponse;
import com.panda.pay.service.channel.bluepay.BluePayChannelImpl;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by jiangtao on 19-1-28 上午10:29
 *
 * <p>提供统一查询功能 可以对接多个渠道的查询 目前实现bluepay对接
 */
@Service("unifyQueryService")
public class UnifyQueryServiceImpl {

  @Autowired PayTradeDao payTradeDao;
  @Autowired GatherTradeDao gatherTradeDao;
  @Autowired LoanTradeDao loanTradeDao;

  @Autowired BluePayChannelImpl bluePayChannel;

  /**
   * 统一查询入口
   *
   * @param params 支付系统统一查询参数
   * @return 查询结果
   */
  public CallbackResult<UnifyResponse> doUnifyQuery(@NotNull UnifyQueryParams params) {

    /** 1.根据transactionCode在总表中查询出基础信息 */
    PayTrade payTrade = payTradeDao.findByTransactionCode(params.getTransactionCode());
    if (payTrade == null) {
      return CallbackResult.failure(
          "统一查询接口", "查询pay_trade表未找到记录", StatusEnum.PAYTRADE_NOT_FOUND.getCode(), null, null);
    }
    /** 2.在详细表中查出具体信息 */
    Object detail = null;
    if (payTrade.getChannelBizCode().contains("gather")) {
      // 收款业务
      detail = gatherTradeDao.findByTransactionCode(payTrade.getTransactionCode());
    }
    if (payTrade.getChannelBizCode().contains("loan")) {
      // 放款业务
      detail = loanTradeDao.findByTransactionCode(payTrade.getTransactionCode());
    }
    if (detail == null) {

      return CallbackResult.failure(
          "统一查询接口",
          "查询详细表未找到记录," + payTrade.getChannelBizCode(),
          StatusEnum.GATHERTRADE_OR_LOANTRADE_NOT_FOUND.getCode(),
          null,
          null);
    }
    /** 3.调用第三方查询接口实时查询,调用渠道实现类查询 */
    switch (payTrade.getChannelCode()) {
      case ChannelConstant.BLUEPAY_CHANNEL_CODE:
        return bluePayChannel.doQuery(detail);
      default:
        return CallbackResult.failure(
            "统一查询接口",
            "渠道路由失败,未找到该渠道实现类," + payTrade.getChannelBizCode(),
            StatusEnum.CHANNELIMPL_NOT_FOUND.getCode(),
            null,
            null);
    }
  }
}
