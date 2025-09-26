package com.bhagwat.retail.cart.enums;

public enum CartStatus {
    ACTIVE,
    INACTIVE, // Cart has been checked out or abandoned
    PENDING_CHECKOUT // Optional: for more complex checkout flows
}
