package com.bhagwat.retail.cart.config;

import com.bhagwat.scm.core.rest.api.ApiClient;
import com.bhagwat.scm.core.rest.api.ApiConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class InventoryApiConfig {

    @Bean("inventoryApiClient")
    public ApiClient inventoryApiClient(WebClient webClient){
        return new ApiClient(webClient);
    }

    @Bean("getInventoryApiConfig")
    @ConfigurationProperties("com.bhagwat.scm.inventory-api.get-inventory")
    public ApiConfig getInventoryConfig(){
        return new ApiConfig();
    }
}
