CREATE TABLE IF NOT EXISTS tbl_user (
    id UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT tbl_user_pkey PRIMARY KEY (id),
    CONSTRAINT tbl_user_username_unique UNIQUE (username)
);
