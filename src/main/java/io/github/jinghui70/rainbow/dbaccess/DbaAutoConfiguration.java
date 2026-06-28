package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGenerator;
import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGeneratorRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
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

@AutoConfiguration(after = {JdbcTemplateAutoConfiguration.class, TransactionAutoConfiguration.class})
@ConditionalOnClass({DataSource.class, JdbcTemplate.class, TransactionTemplate.class})
public class DbaAutoConfiguration implements InitializingBean {

    private final ObjectProvider<ValueGenerator> valueGeneratorProvider;

    public DbaAutoConfiguration(ObjectProvider<ValueGenerator> valueGeneratorProvider) {
        this.valueGeneratorProvider = valueGeneratorProvider;
    }

    @Override
    public void afterPropertiesSet() {
        // 注册 ValueGenerator
        valueGeneratorProvider.orderedStream().forEach(ValueGeneratorRegistry::register);
    }

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
         * 如果是可选的，请使用 ObjectProvider<TransactionTemplate>。
         */
        @Bean
        Dba dba(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
            return new Dba(jdbcTemplate, new TransactionTemplate(transactionManager));
        }
    }
}
