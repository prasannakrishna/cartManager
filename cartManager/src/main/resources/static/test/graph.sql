CREATE TABLE nodes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    char_value CHAR(1) NOT NULL UNIQUE
);

CREATE TABLE edges (
    from_node INT,
    to_node INT,
    PRIMARY KEY (from_node, to_node),
    FOREIGN KEY (from_node) REFERENCES nodes(id),
    FOREIGN KEY (to_node) REFERENCES nodes(id)
);


CREATE TABLE community_keys (
    id INT AUTO_INCREMENT PRIMARY KEY,
    community_id INT,
    key VARCHAR(255) NOT NULL,
    height INT NOT NULL
);


