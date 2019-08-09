package com.panda.pay.dao;

import com.alibaba.fastjson.JSONObject;
import com.panda.pay.constant.enums.StatusEnum;
import com.panda.pay.ds.primary.entity.LoanTrade;
import com.panda.pay.ds.primary.repository.LoanTradeRepository;
import java.util.Date;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Created by jiangtao on 19-1-27 下午5:16 */
@Component
public class LoanTradeDao {

  @Autowired LoanTradeRepository loanTradeRepository;

  /**
   * 判断表中是否有这个out_trade_no
   *
   * @param outTradeNo 业务系统唯一标识
   * @return true 存在 false 不存在
   */
  public boolean isOutTradeNoExist(@NotNull String outTradeNo) {
    return loanTradeRepository.isOutTradeNoExist(outTradeNo);
  }

  public LoanTrade findByTransactionCode(@NotNull String transactionCode) {
    return loanTradeRepository.findByTransactionCode(transactionCode);
  }

  public LoanTrade saveNewRecord(
      @NotNull String transactionCode,
      @NotNull String outTradeNo,
      String channelCode,
      String channelBizCode,
      @NotNull Integer price,
      @NotNull String currency,
      JSONObject channelExtendInfo,
      JSONObject extend,
      JSONObject personInfo,
      JSONObject passBack,
      String bizNotifyUrl,
      String notifyType) {
    LoanTrade loanTrade = new LoanTrade();
    loanTrade.setTransactionCode(transactionCode);
    loanTrade.setOutTradeNo(outTradeNo);
    loanTrade.setChannelCode(channelCode);
    loanTrade.setChannelBizCode(channelBizCode);
    loanTrade.setPrice(price);
    loanTrade.setCurrency(currency.toUpperCase());
    loanTrade.setChannelExtendInfo(
        Optional.ofNullable(channelExtendInfo).map(JSONObject::toString).orElse(null));
    loanTrade.setExtend(Optional.ofNullable(extend).map(JSONObject::toString).orElse(null));
    loanTrade.setPersonInfo(Optional.ofNullable(personInfo).map(JSONObject::toString).orElse(null));
    loanTrade.setPassBack(Optional.ofNullable(passBack).map(JSONObject::toString).orElse(null));
    loanTrade.setBizNotifyUrl(bizNotifyUrl);
    loanTrade.setUpdatedAt(new Date());
    loanTrade.setBizNotifyType(notifyType);

    loanTrade.setChannelCallbackNotifyStatus("0"); // 0次回调
    loanTrade.setTradeStatus(StatusEnum.LOAN_TRADE_STATUS_PROCESS.getCode());

    return loanTradeRepository.save(loanTrade);
  }

  public LoanTrade updateGatherTrade(LoanTrade loanTrade) {
    if (loanTrade.getId() == null) {
      return null;
    }

    loanTrade.setUpdatedAt(new Date());
    return loanTradeRepository.save(loanTrade);
  }
}
