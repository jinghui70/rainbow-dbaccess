package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class DbaConfig {

    @Bean
    public MemoryDba dba() {
        return new MemoryDba();
    }

}
