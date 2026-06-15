package io.github.jinghui70.rainbow.dbaccess;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@AutoConfiguration(after = {JdbcTemplateAutoConfiguration.class, TransactionAutoConfiguration.class})
@ConditionalOnClass({DataSource.class, JdbcTemplate.class, TransactionTemplate.class})
public class DbaAutoConfiguration {

    @Configuration(proxyBeanMethods = false)  // 内部类封装，禁用代理优化性能
    @ConditionalOnSingleCandidate(JdbcTemplate.class)
    @ConditionalOnBean(TransactionTemplate.class)
    @ConditionalOnMissingBean(Dba.class)
    public static class DbaConfiguration {
        @Bean
        Dba dba(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
            return new Dba(jdbcTemplate, transactionTemplate);
        }
    }
}
