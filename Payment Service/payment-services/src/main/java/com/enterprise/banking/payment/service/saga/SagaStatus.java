package com.enterprise.banking.payment.service.saga;

public enum SagaStatus {
    STARTED,
    COMPLETED,
    FAILED,
    COMPENSATED
}