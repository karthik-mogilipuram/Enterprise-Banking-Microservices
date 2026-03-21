CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_account_id   UUID NOT NULL,
    dest_account_id     UUID NOT NULL,
    amount              DECIMAL(15,2) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'USD',
    type                VARCHAR(20) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    saga_id             UUID,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP
);

CREATE INDEX idx_payments_source ON payments(source_account_id);
CREATE INDEX idx_payments_dest ON payments(dest_account_id);
CREATE INDEX idx_payments_saga ON payments(saga_id);