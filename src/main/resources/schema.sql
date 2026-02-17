CREATE TABLE IF NOT EXISTS document_asset (
    id VARCHAR(255) PRIMARY KEY,
    filename VARCHAR(1000) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    customer_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    url VARCHAR(2000),
    size BIGINT NOT NULL,
    upload_date TIMESTAMP NOT NULL,
    correlation_id VARCHAR(255)
);
