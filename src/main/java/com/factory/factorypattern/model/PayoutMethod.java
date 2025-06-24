package com.factory.factorypattern.model;


/**
 * Enum representing different payout methods
 * Used by Factory to determine processor type
 */
public enum PayoutMethod {
    MOBILE_WALLET("mobile_wallet"),
    BANK_TRANSFER("bank_transfer"),
    CASH_PICKUP("cash_pickup"),
    DIGITAL_WALLET("digital_wallet");

    private final String value;

    PayoutMethod(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public static PayoutMethod fromString(String value){
        for (PayoutMethod method : PayoutMethod.values()){
            if(method.value.equalsIgnoreCase(value)){
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown Payout method"+ value);
    }

}
