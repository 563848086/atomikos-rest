package com.knowis.ordermanager.config;


import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager",
        basePackages = {"com.knowis.ordermanager"}
)
public class DataSourceConfig {

    @Value("${order.db.server}")
    String dbServer;

    @Value("${order.db.port}")
    String dbPort;

    @Value("${order.db.user}")
    String dbUser;

    @Value("${order.db.password}")
    String dbPassword;

    @Value("${order.db.name}")
    String dbName;

    @Value("${order.db.xadatasourceclassname}")
    String xaDataSourceClassName;

    @Value("${order.db.xauniqueresourcename}")
    String xaUniqueResourceName;

    @Value("${order.db.hibernate.dialect}")
    String hibernateDialect;

    public Map<String, String> jpaProperties() {
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "none");
        jpaProperties.put("hibernate.dialect", hibernateDialect);
        jpaProperties.put("hibernate.show_sql", "true");
        jpaProperties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        jpaProperties.put("javax.persistence.transactionType", "jta");
        jpaProperties.put("hibernate.current_session_context_class", "jta");
        jpaProperties.put("hibernate.transaction.manager_lookup_class", "com.atomikos.icatch.jta.hibernate3.TransactionManagerLookup");

        return jpaProperties;
    }

    @Bean(name = "entityManagerFactoryBuilder")
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(
                new HibernateJpaVendorAdapter(), jpaProperties(), null
        );
    }


    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean getMysqlEntityManager(
            @Qualifier("entityManagerFactoryBuilder") EntityManagerFactoryBuilder entityManagerFactoryBuilder,
            @Qualifier("dataSource") DataSource mysqlDataSource
    ) {
        return entityManagerFactoryBuilder
                .dataSource(mysqlDataSource)
                .packages("com.knowis.ordermanager")
                .persistenceUnit("mysql")
                .properties(jpaProperties())
                .jta(true)
                .build();
    }



    @Bean("dataSource")
    public DataSource dataSource() {

        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setUniqueResourceName(xaUniqueResourceName);
        xaDataSource.setXaDataSourceClassName(xaDataSourceClassName);

        Properties xaProperties = new Properties();
        xaProperties.setProperty("databaseName", dbName);
        xaProperties.setProperty("user", dbUser);
        xaProperties.setProperty("password", dbPassword);
        xaProperties.setProperty("serverName", dbServer);
        xaProperties.setProperty("portNumber", dbPort);
        xaDataSource.setXaProperties(xaProperties);

        xaDataSource.setPoolSize(5);
        xaDataSource.setBorrowConnectionTimeout(100);
        return xaDataSource;
    }

}

