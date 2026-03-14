package com.bhagwat.retail.cart.rest.impl;

import com.bhagwat.retail.cart.rest.InventroryApi;
import com.bhagwat.scm.core.rest.api.ApiClient;
import com.bhagwat.scm.core.rest.api.ApiConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InventoryApiImpl implements InventroryApi {
    private final ApiClient invClient;
    private final ApiConfig invConfig;

    public InventoryApiImpl(@Qualifier("inventoryApiClient") ApiClient invClient, @Qualifier("getInventoryApiConfig") ApiConfig invConfig) {
        this.invClient = invClient;
        this.invConfig = invConfig;
    }

    @Override
    public String getInventory() {
       String s =  invClient.invoke(invConfig, null, Map.of("", ""), null, String.class).getBody();
        return s;
    }
}
