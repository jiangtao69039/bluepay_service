package com.panda.pay.dao;

import com.alibaba.fastjson.JSONObject;
import com.panda.pay.constant.enums.StatusEnum;
import com.panda.pay.ds.primary.entity.GatherTrade;
import com.panda.pay.ds.primary.repository.GatherTradeRepository;
import java.util.Date;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Created by jiangtao on 19-1-27 上午10:56 */
@Component
public class GatherTradeDao {
  @Autowired GatherTradeRepository gatherTradeRepository;

  /**
   * 判断表中是否有这个out_trade_no
   *
   * @param outTradeNo 业务系统唯一标识
   * @return true 存在 false 不存在
   */
  public boolean isOutTradeNoExist(@NotNull String outTradeNo) {
    return gatherTradeRepository.isOutTradeNoExist(outTradeNo);
  }

  public GatherTrade findByTransactionCode(@NotNull String transactionCode) {
    return gatherTradeRepository.findByTransactionCode(transactionCode);
  }
  /**
   * 保存一条gather_trade表记录
   *
   * @param transactionCode 交易系统生成的唯一标识
   * @param outTradeNo 业务系统唯一标识
   * @param channelCode 渠道code
   * @param channelBizCode 渠道业务code
   * @param price 收款价格
   * @param currency 币种
   * @param nation 国家代码
   * @param channelExtendInfo 某个渠道的特有参数信息
   * @param extend 扩展参数
   * @param personInfo 人员信息
   * @return 保存后的实体对象
   */
  public GatherTrade saveNewRecord(
      @NotNull String transactionCode,
      @NotNull String outTradeNo,
      String channelCode,
      String channelBizCode,
      @NotNull Integer price,
      @NotNull String currency,
      @NotNull String nation,
      JSONObject channelExtendInfo,
      JSONObject extend,
      JSONObject personInfo,
      JSONObject passBack,
      String bizNotifyUrl,
      String bizNotifyType) {
    GatherTrade gatherTrade = new GatherTrade();
    gatherTrade.setTransactionCode(transactionCode);
    gatherTrade.setOutTradeNo(outTradeNo);
    gatherTrade.setChannelCode(channelCode);
    gatherTrade.setChannelBizCode(channelBizCode);
    gatherTrade.setPrice(price);
    gatherTrade.setCurrency(currency.toUpperCase());
    gatherTrade.setNation(nation);
    gatherTrade.setChannelExtendInfo(
        Optional.ofNullable(channelExtendInfo).map(JSONObject::toString).orElse(null));
    gatherTrade.setExtend(Optional.ofNullable(extend).map(JSONObject::toString).orElse(null));
    gatherTrade.setPersonInfo(
        Optional.ofNullable(personInfo).map(JSONObject::toString).orElse(null));
    gatherTrade.setPassBack(Optional.ofNullable(passBack).map(JSONObject::toString).orElse(null));
    gatherTrade.setBizNotifyUrl(bizNotifyUrl);
    gatherTrade.setUpdatedAt(new Date());
    gatherTrade.setBizNotifyType(bizNotifyType);

    gatherTrade.setChannelCallbackNotifyStatus("0"); // 0次回调
    gatherTrade.setTradeStatus(StatusEnum.GATHER_TRADE_STATUS_PROCESS.getCode());

    return gatherTradeRepository.save(gatherTrade);
  }

  public GatherTrade updateGatherTrade(GatherTrade gatherTrade) {
    if (gatherTrade.getId() == null) {
      return null;
    }

    gatherTrade.setUpdatedAt(new Date());
    return gatherTradeRepository.save(gatherTrade);
  }
}
