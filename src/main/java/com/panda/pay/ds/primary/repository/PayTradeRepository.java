package com.panda.pay.ds.primary.repository;

import com.panda.pay.ds.primary.entity.PayTrade;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/** Created by jiangtao on 19-1-27 上午10:01 */
public interface PayTradeRepository
    extends PagingAndSortingRepository<PayTrade, Long>, JpaSpecificationExecutor {

  @Query(value = "select count(id)>0 from pay_trade where out_trade_no=?1", nativeQuery = true)
  Boolean isOutTradeNoExist(String outTradeNo);

  PayTrade findByTransactionCode(String t);
}
