package io.github.jinghui70.rainbow.dbaccess;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * rainbow-dbaccess 自动配置类，负责装配 {@link Dba} Bean。
 * <p>
 * 用户自定义 {@link io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGenerator}
 * 的注册与本配置无关（生成器 Bean 初始化时自行注册到
 * {@link io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGeneratorRegistry}），
 * 因此即使本配置因类路径条件不满足而未生效，生成器注册也不受影响。
 */
@AutoConfiguration(after = {JdbcTemplateAutoConfiguration.class, TransactionAutoConfiguration.class})
@ConditionalOnClass({DataSource.class, JdbcTemplate.class, TransactionTemplate.class})
public class DbaAutoConfiguration {

    /**
     * 内部配置类，实际创建 Dba Bean。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnSingleCandidate(JdbcTemplate.class)
    @ConditionalOnMissingBean(Dba.class)
    public static class DbaConfiguration {

        /**
         * 创建 Dba Bean。
         * 如果 TransactionTemplate 是必须的，直接作为参数即可。
         * 如果是可选的，请使用 ObjectProvider&lt;TransactionTemplate&gt;。
         *
         * @param jdbcTemplate       JDBC 操作模板
         * @param transactionManager 事务管理器
         * @return Dba 实例
         */
        @Bean
        Dba dba(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
            return new Dba(jdbcTemplate, new TransactionTemplate(transactionManager));
        }
    }
}
