package com.karan.risk.paymentriskengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RiskEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskEngineApplication.class, args);
    }

    @Bean
    public static org.springframework.beans.factory.config.BeanFactoryPostProcessor flywayDependsOnPostProcessor() {
        return beanFactory -> {
            if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
                var bd = beanFactory.getBeanDefinition("entityManagerFactory");
                String[] existing = bd.getDependsOn();
                String[] updated;
                if (existing == null) {
                    updated = new String[]{"flyway"};
                } else {
                    updated = java.util.Arrays.copyOf(existing, existing.length + 1);
                    updated[updated.length - 1] = "flyway";
                }
                bd.setDependsOn(updated);
            }
        };
    }
}
