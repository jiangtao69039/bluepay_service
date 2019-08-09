package com.panda.pay.dao;

import com.alibaba.fastjson.JSONObject;
import com.panda.pay.ds.primary.entity.PayTrade;
import com.panda.pay.ds.primary.repository.PayTradeRepository;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Created by jiangtao on 19-1-27 上午9:54 对pay_trade表的一些操作 */
@Component
public class PayTradeDao {

  @Autowired PayTradeRepository payTradeRepository;

  /**
   * 判断表中是否有这个out_trade_no
   *
   * @param outTradeNo 业务系统唯一标识
   * @return true 存在 false 不存在
   */
  public boolean isOutTradeNoExist(@NotNull String outTradeNo) {
    return payTradeRepository.isOutTradeNoExist(outTradeNo);
  }

  public PayTrade findByTransactionCode(@NotNull String transactionCode) {
    return payTradeRepository.findByTransactionCode(transactionCode);
  }

  public PayTrade saveNewRecord(
      @NotNull String transactionCode,
      @NotNull String outTradeNo,
      String channelCode,
      String channelBizCode,
      JSONObject channelExtendIno,
      JSONObject extend) {

    PayTrade payTrade = new PayTrade();
    payTrade.setTransactionCode(transactionCode);
    payTrade.setOutTradeNo(outTradeNo);
    payTrade.setChannelCode(channelCode);
    payTrade.setChannelBizCode(channelBizCode);
    payTrade.setChannelExtendInfo(
        Optional.ofNullable(channelExtendIno).map(JSONObject::toString).orElse(null));
    payTrade.setExtend(Optional.ofNullable(extend).map(JSONObject::toString).orElse(null));
    // payTrade.setPersonInfo(Optional.ofNullable(personInfo.toJSONString()).orElse(null));
    return payTradeRepository.save(payTrade);
  }
}
