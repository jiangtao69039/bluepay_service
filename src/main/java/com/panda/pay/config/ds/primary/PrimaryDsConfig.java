package com.panda.pay.config.ds.primary;

import com.panda.pay.config.ds.AbstractDsConfig;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @Title:PrimaryDsConfig @Copyright: Copyright (c) 2016 @Description: <br>
 * @Company: panda-fintech @Created on 2018/6/16下午9:32
 *
 * @miaoxuehui@panda-fintech.com
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
  entityManagerFactoryRef = PrimaryDsConfig.DATASOURCE_NAME + "EntityManagerFactory",
  transactionManagerRef = PrimaryDsConfig.DATASOURCE_NAME + "TransactionManager",
  basePackages = {PrimaryDsConfig.BASE_PACKAGES}
)
public class PrimaryDsConfig extends AbstractDsConfig {
  protected static final String BASE_PACKAGES = "com.panda.pay.ds.primary";
  protected static final String DATASOURCE_NAME = "defaultPrimary";
  protected static final String DATASOURCE_PREFIX = "default.primary";
  protected static final Database DATABASE = Database.POSTGRESQL;
  protected static final String DIALECT =
      "org.hibernate.dialect.PostgreSQLDialect"; // org.hibernate.dialect.MySQL5Dialect

  @Autowired protected JpaProperties jpaProperties;

  /**
   * 数据源配置对象 Primary 表示默认的对象，Autowire可注入，不是默认的得明确名称注入
   *
   * @return
   */
  @Bean
  @Primary
  @ConfigurationProperties(DATASOURCE_PREFIX + ".datasource")
  public DataSourceProperties primaryDataSourceProperties() {
    return new DataSourceProperties();
  }

  /**
   * 数据源对象
   *
   * @return
   */
  @Bean
  @Primary
  @ConfigurationProperties(DATASOURCE_PREFIX + ".datasource")
  public DataSource primaryDataSource() {
    return primaryDataSourceProperties().initializeDataSourceBuilder().build();
  }

  /**
   * 实体管理对象
   *
   * @param builder 由spring注入这个对象，首先根据type注入（多个就取声明@Primary的对象），否则根据name注入
   * @return
   */
  @Bean(name = DATASOURCE_NAME + "EntityManagerFactory")
  @Primary
  public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
      EntityManagerFactoryBuilder builder) {
    DataSource dataSource = primaryDataSource();
    return builder
        .dataSource(dataSource)
        .packages(BASE_PACKAGES)
        .persistenceUnit(DATASOURCE_NAME)
        .properties(getVendorProperties(dataSource))
        .build();
  }

  protected Map<String, String> getVendorProperties(DataSource dataSource) {
    jpaProperties.setDatabase(Database.POSTGRESQL);
    Map<String, String> map = new HashMap<>();
    map.put("hibernate.dialect", DIALECT);
    map.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
    // map.put("hibernate.hbm2ddl.auto","update");
    // map.put("hibernate.physical_naming_strategy","org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
    jpaProperties.setProperties(map);
    return jpaProperties.getHibernateProperties(dataSource);
  }

  /**
   * 事务管理对象
   *
   * @return
   */
  @Bean(name = DATASOURCE_NAME + "TransactionManager")
  @Primary
  public PlatformTransactionManager transactionManager(
      EntityManagerFactory defaultPrimaryEntityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(defaultPrimaryEntityManagerFactory);
    return transactionManager;
  }

  @Bean
  @Primary
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(primaryDataSource());
  }

  @Bean
  @Primary
  public TransactionTemplate transactionTemplate(
      PlatformTransactionManager platformTransactionManager) {
    return new TransactionTemplate(platformTransactionManager);
  }
}
