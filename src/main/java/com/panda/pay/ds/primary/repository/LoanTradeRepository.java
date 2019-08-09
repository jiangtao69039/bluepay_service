package com.panda.pay.ds.primary.repository;

import com.panda.pay.ds.primary.entity.LoanTrade;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/** Created by jiangtao on 19-1-27 下午5:15 */
public interface LoanTradeRepository
    extends PagingAndSortingRepository<LoanTrade, Long>, JpaSpecificationExecutor {

  @Query(value = "select count(id)>0 from loan_trade where out_trade_no=?1", nativeQuery = true)
  Boolean isOutTradeNoExist(String outTradeNo);

  LoanTrade findByTransactionCode(String code);
}
