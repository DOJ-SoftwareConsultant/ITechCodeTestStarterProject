package org.test;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Jpa configuration.
 *
 */
@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
public class JpaRepositoryConfig {
	private final org.h2.Driver driver = new org.h2.Driver();
    private final String databaseURL = "jdbc:h2:tcp://localhost/~/test";
    private final String username = "sa";
    private final String password = "";
    private final Database databaseVendor = Database.H2;

    /**
     * DataSource declaration.
     * @return the datasource
     */
    @Bean
    public DataSource dataSource() {
        DataSource dataSource = new SimpleDriverDataSource(
                  this.driver,this.databaseURL,this.username,this.password);
          return dataSource;
    }

    /**
     * EntityManager factory.
     * @return the entitu manager factory.
     */
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(this.databaseVendor);
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setJpaVendorAdapter(vendorAdapter);
        factoryBean.setPackagesToScan(this.getClass().getPackage().getName());
        factoryBean.setDataSource(this.dataSource());

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    /**
     * The jpa dialect declaration.
     * @return  jpaDialect
     */
    @Bean
    public JpaDialect jpaDialect() {
        return new HibernateJpaDialect();
    }

    /**
     * Transaction manager declaration.
     * @return the transaction manager.
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(this.entityManagerFactory());
        return  txManager;
    }
}