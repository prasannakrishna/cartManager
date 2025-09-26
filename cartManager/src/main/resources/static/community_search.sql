CREATE TABLE search_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,        -- Unique identifier for each node
    root_node_id BIGINT NOT NULL,                -- ID of the root node
    link_node_id BIGINT,                         -- ID of the linked child node
    last_node_id BIGINT,                         -- ID of the last node in the path
    is_last_node BOOLEAN NOT NULL DEFAULT FALSE, -- Flag to indicate if this is the last node
    height INT NOT NULL,                         -- Height of the node in the Trie
    node_char CHAR(1) NOT NULL,                 -- Character representing the node

    -- Foreign Key constraints
    FOREIGN KEY (root_node_id) REFERENCES search_key(id) ON DELETE CASCADE,
    FOREIGN KEY (link_node_id) REFERENCES search_key(id) ON DELETE SET NULL,
    FOREIGN KEY (last_node_id) REFERENCES search_key(id) ON DELETE SET NULL
);



CREATE TABLE community_search_key_mapping (
    id BIGINT NOT NULL AUTO_INCREMENT,         -- Primary key for the table
    community_id BIGINT NOT NULL,              -- ID representing the community
    search_key_node_id BIGINT NOT NULL,                 -- Root node ID from the search_key table
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,   -- Date when the record was created
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- Date when the record was last updated
    PRIMARY KEY (id),                          -- Setting the primary key for the table
    KEY idx_community_id (community_id),        -- Index on community_id for faster lookups
    KEY idx_root_node_id (search_key_node_id),        -- Index on root_node_id for faster lookups

    -- Adding foreign key constraints for integrity
    CONSTRAINT fk_community_id FOREIGN KEY (community_id) REFERENCES community(community_id),
    CONSTRAINT fk_root_node_id FOREIGN KEY (search_key_node_id) REFERENCES search_key(id)
);

CREATE TABLE sku_pack_config_org_linking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,       -- Primary Key (ID)
    sku_id BIGINT NOT NULL,                      -- Foreign Key (SKU ID)
    pack_config_id BIGINT NOT NULL,              -- Foreign Key (Pack Config ID)
    org_id BIGINT NOT NULL,                      -- Foreign Key (Organization ID)
    UNIQUE (sku_id, pack_config_id, org_id),    -- Composite unique constraint
    FOREIGN KEY (sku_id) REFERENCES sku(sku_id),            -- FK Reference to SKU Table
    FOREIGN KEY (pack_config_id) REFERENCES packing_configuration(config_id),  -- FK Reference to Packing Configuration
    FOREIGN KEY (org_id) REFERENCES business_org(id)                 -- FK Reference to Business Org Table
);

CREATE TABLE community (
    community_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE hash_key (
    hash_key_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hash_key VARCHAR(255) NOT NULL,
    community_id BIGINT NOT NULL,
    usage_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (community_id) REFERENCES community(community_id) ON DELETE CASCADE
);
