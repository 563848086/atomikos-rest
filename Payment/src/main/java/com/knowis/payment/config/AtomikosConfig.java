package com.knowis.payment.config;


import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;




@Configuration
@EnableTransactionManagement
public class AtomikosConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor(Environment environment) {
        return beanFactory -> {
            String restPortUrl = environment.getProperty("com.atomikos.icatch.rest_port_url");
            String logBaseName = environment.getProperty("com.atomikos.icatch.log_base_name");
            String tmUniqueName = environment.getProperty("com.atomikos.icatch.tm_unique_name");
            String logBaseDir = environment.getProperty("com.atomikos.icatch.log_base_dir");
            long defaultJtaTimeout = environment.getProperty("com.atomikos.icatch.default_jta_timeout", Long.class);
            long maxTimeout = environment.getProperty("com.atomikos.icatch.max_timeout", Long.class);

            com.atomikos.icatch.config.Configuration.getConfigProperties().setProperty("com.atomikos.icatch.rest_port_url", restPortUrl);
            com.atomikos.icatch.config.Configuration.getConfigProperties().setProperty("com.atomikos.icatch.log_base_name", logBaseName);
            com.atomikos.icatch.config.Configuration.getConfigProperties().setProperty("com.atomikos.icatch.tm_unique_name", tmUniqueName);
            com.atomikos.icatch.config.Configuration.getConfigProperties().setProperty("com.atomikos.icatch.log_base_dir", logBaseDir);
            com.atomikos.icatch.config.Configuration.getConfigProperties().setProperty("com.atomikos.icatch.default_jta_timeout", String.valueOf(defaultJtaTimeout));
            com.atomikos.icatch.config.Configuration.getConfigProperties().setProperty("com.atomikos.icatch.max_timeout", String.valueOf(maxTimeout));
        };
    }
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setShowSql(true);
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        return hibernateJpaVendorAdapter;
    }

    @Bean(name = "userTransaction")
    public UserTransaction userTransaction() throws Throwable {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(100000);
        return userTransactionImp;
    }

    @Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
    public TransactionManager atomikosTransactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);

        AtomikosJtaPlatform.transactionManager = userTransactionManager;

        return userTransactionManager;
    }

    @Bean(name = "transactionManager")
    @DependsOn({"userTransaction", "atomikosTransactionManager"})
    public PlatformTransactionManager transactionManager() throws Throwable {
        UserTransaction userTransaction = userTransaction();

        AtomikosJtaPlatform.transaction = userTransaction;

        TransactionManager atomikosTransactionManager = atomikosTransactionManager();
        return new JtaTransactionManager(userTransaction, atomikosTransactionManager);
    }

}

