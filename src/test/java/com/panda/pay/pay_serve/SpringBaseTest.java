package com.panda.pay.pay_serve;

import com.panda.pay.BluepayServiceApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Title:SpringBaseTest @Copyright: Copyright (c) 2016 @Description: <br>
 * @Company: panda-fintech @Created on 2018/6/23上午11:15
 *
 * @miaoxuehui@panda-fintech.com
 */
@SpringBootTest(classes = BluepayServiceApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@Rollback
public abstract class SpringBaseTest {}
