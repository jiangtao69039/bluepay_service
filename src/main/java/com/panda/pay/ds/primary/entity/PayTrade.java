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

/** Created by jiangtao on 19-1-27 上午9:56 */
@Entity
@TypeDefs({@TypeDef(name = "json", typeClass = MyUserType.class)})
@Getter
@Setter
public class PayTrade extends BasePostgresEntity {

  String transactionCode;
  String outTradeNo;
  String channelCode;
  String channelBizCode;

  @Column(name = "channel_extend_info", columnDefinition = "json")
  @Type(type = "json")
  String channelExtendInfo;

  @Column(name = "extend", columnDefinition = "json")
  @Type(type = "json")
  String extend;
}
