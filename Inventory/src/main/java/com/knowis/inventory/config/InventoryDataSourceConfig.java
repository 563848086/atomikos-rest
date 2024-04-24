package com.knowis.inventory.config;


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
        entityManagerFactoryRef = "inventoryEntityManagerFactory",
        transactionManagerRef = "transactionManager",
        basePackages = {"com.knowis.inventory"}
)
public class InventoryDataSourceConfig {

    @Value("${inventory.db.server}")
    String dbServer;

    @Value("${inventory.db.port}")
    String dbPort;

    @Value("${inventory.db.user}")
    String dbUser;

    @Value("${inventory.db.password}")
    String dbPassword;

    @Value("${inventory.db.name}")
    String dbName;

    @Value("${inventory.db.xadatasourceclassname}")
    String xaDataSourceClassName;

    @Value("${inventory.db.xauniqueresourcename}")
    String xaUniqueResourceName;

    @Value("${inventory.db.hibernate.dialect}")
    String hibernateDialect;

    public Map<String, String> jpaProperties() {
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.dialect", hibernateDialect);
        jpaProperties.put("hibernate.show_sql", "true");
        jpaProperties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        jpaProperties.put("javax.persistence.transactionType", "jta");
        jpaProperties.put("hibernate.current_session_context_class", "jta");
        jpaProperties.put("hibernate.transaction.manager_lookup_class", "com.atomikos.icatch.jta.hibernate3.TransactionManagerLookup");

        return jpaProperties;
    }

    @Bean(name = "inventoryEntityManagerFactoryBuilder")
    public EntityManagerFactoryBuilder inventoryEntityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(
                new HibernateJpaVendorAdapter(), jpaProperties(), null
        );
    }


    @Bean(name = "inventoryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean getMysqlEntityManager(
            @Qualifier("inventoryEntityManagerFactoryBuilder") EntityManagerFactoryBuilder entityManagerFactoryBuilder,
            @Qualifier("inventoryDataSource") DataSource mysqlDataSource
    ) {
        return entityManagerFactoryBuilder
                .dataSource(mysqlDataSource)
                .packages("com.knowis.inventory")
                .persistenceUnit("mysql")
                .properties(jpaProperties())
                .jta(true)
                .build();
    }



    @Bean("inventoryDataSource")
    public DataSource inventoryDataSource() {

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

