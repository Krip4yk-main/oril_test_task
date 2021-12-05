package com.Krip4yk.oril_test_task.exception;

public class CurrencyNotFoundException extends Exception {
    private long currencyId;

    public CurrencyNotFoundException(long currencyId) {
        super(String.format("Currency is not found with id : '%s'", currencyId));
    }
}