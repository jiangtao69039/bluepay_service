package com.panda.pay.ds.primary.entity;

import com.panda.pay.config.jpa.MyUserType;
import com.panda.pay.ds.BasePostgresEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/** Created by jiangtao on 19-1-27 下午5:05 放款详细表 */
@Entity
@Getter
@Setter
@TypeDefs({@TypeDef(name = "json", typeClass = MyUserType.class)})
public class LoanTrade extends BasePostgresEntity {
  String transactionCode;
  String outTradeNo;
  String channelCode;
  String channelBizCode;

  Integer price;
  String currency;

  @Column(name = "channel_extend_info", columnDefinition = "json")
  @Type(type = "json")
  String channelExtendInfo;

  String channelCallbackNotifyStatus = "0";
  String tradeStatus;
  String tradeMsg;

  @Column(name = "channel_callback_extend_info", columnDefinition = "json")
  @Type(type = "json")
  String channelCallbackExtendInfo;

  @Column(name = "extend", columnDefinition = "json")
  @Type(type = "json")
  String extend;

  @Column(name = "person_info", columnDefinition = "json")
  @Type(type = "json")
  String personInfo;

  @Column(name = "pass_back", columnDefinition = "json")
  @Type(type = "json")
  String passBack;

  @Column(name = "channel_order_info", columnDefinition = "json")
  @Type(type = "json")
  String channelOrderInfo;

  String bizNotifyUrl;
  String bizNotifyType;
}
