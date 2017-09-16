/*
 * 版权所有.(c)2008-2017. 卡尔科技工作室
 */

package com.carl.wolf.permission.config;

import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jSubjectFactory;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.AbstractShiroWebFilterConfiguration;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Carl
 * @date 2017/9/16
 * @since
 */
@Configuration
public class ShiroConfiguration extends AbstractShiroWebFilterConfiguration {
    @Value("#{ @environment['cas.loginUrl'] ?: null }")
    private String casLoginUrl;
    @Value("#{ @environment['cas.callbackUrl'] ?: null }")
    private String callbackUrl;

    @Bean
    public Realm pac4jRealm() {
        return new Pac4jRealm();
    }

    @Bean
    public Filter casSecurityFilter() {
        SecurityFilter filter = new SecurityFilter();
        filter.setClients("CasClient");
        filter.setConfig(casConfig());
        return filter;
    }

    @Bean
    protected Config casConfig() {
        Config config = new Config();
        Clients clients = new Clients();
        clients.setClients(casClient());
        config.setClients(clients);
        return config;
    }

    @Bean
    public CasConfiguration casConfiguration() {
        CasConfiguration casConfiguration = new CasConfiguration(casLoginUrl);
        return casConfiguration;
    }

    @Bean
    public CasClient casClient() {
        CasClient casClient = new CasClient();
        casClient.setConfiguration(casConfiguration());
        casClient.setCallbackUrl(callbackUrl);
        return casClient;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition definition = new DefaultShiroFilterChainDefinition();
       definition.addPathDefinition( "/user/**","casSecurityFilter");
        definition.addPathDefinition( "/callback", "callbackFilter");
        definition.addPathDefinition("/**", "anon");
        return definition;
    }

    @Bean
    protected SubjectFactory subjectFactory() {
        return new Pac4jSubjectFactory();
    }

    @Bean
    protected ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ((DefaultSecurityManager)securityManager).setSubjectFactory(subjectFactory());
        ShiroFilterFactoryBean filterFactoryBean = super.shiroFilterFactoryBean();
        filterFactoryBean.setSecurityManager(securityManager);
        Map<String, Filter> filters = new HashMap<>();
        filters.put("casSecurityFilter", casSecurityFilter());
        CallbackFilter callbackFilter = new CallbackFilter();
        callbackFilter.setConfig(casConfig());
        filters.put("callbackFilter", callbackFilter);
        filterFactoryBean.setFilters(filters);
        return filterFactoryBean;
    }
}