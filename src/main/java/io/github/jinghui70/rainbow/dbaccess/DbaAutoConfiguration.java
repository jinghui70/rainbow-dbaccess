package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGenerator;
import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGeneratorRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
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

/**
 * Spring Boot 自动配置类，用于自动注册 {@link Dba} Bean。
 * <p>
 * 当环境中存在 {@link DataSource}、{@link JdbcTemplate} 和 {@link TransactionTemplate}，
 * 且用户未手动注册 Dba Bean 时，自动创建并注册 Dba 实例。
 *
 * @author lijinghui
 * @see Dba
 */
@AutoConfiguration(after = {JdbcTemplateAutoConfiguration.class, TransactionAutoConfiguration.class})
@ConditionalOnClass({DataSource.class, JdbcTemplate.class, TransactionTemplate.class})
public class DbaAutoConfiguration implements InitializingBean {

    private final ObjectProvider<ValueGenerator> valueGeneratorProvider;

    // 通过构造器注入 ObjectProvider，非常安全
    public DbaAutoConfiguration(ObjectProvider<ValueGenerator> valueGeneratorProvider) {
        this.valueGeneratorProvider = valueGeneratorProvider;
    }

    @Override
    public void afterPropertiesSet() {
        valueGeneratorProvider.orderedStream().forEach(ValueGeneratorRegistry::register);
    }

    /**
     * 内部配置类，实际创建 Dba Bean。
     */
    @Configuration(proxyBeanMethods = false)  // 内部类封装，禁用代理优化性能
    @ConditionalOnSingleCandidate(JdbcTemplate.class)
    @ConditionalOnBean(TransactionTemplate.class)
    @ConditionalOnMissingBean(Dba.class)
    public static class DbaConfiguration {
        /**
         * 创建 {@link Dba} Bean。
         *
         * @param jdbcTemplate        Spring JdbcTemplate
         * @param transactionTemplate Spring TransactionTemplate
         * @return Dba 实例
         */
        @Bean
        Dba dba(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
            return new Dba(jdbcTemplate, transactionTemplate);
        }
    }
}
