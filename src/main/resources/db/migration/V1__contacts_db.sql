CREATE schema IF NOT EXISTS contacts_db;

CREATE TABLE IF NOT EXISTS contacts_db.users
(
    id                bigserial NOT NULL,
    login             character varying NOT NULL UNIQUE,
    password          character varying NOT NULL,
    role              character varying NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS contacts_db.contacts
(
    id               bigserial NOT NULL,
    name             character varying NOT NULL,
    user_id          bigint,
    UNIQUE (name, user_id),
    PRIMARY KEY (id),
    CONSTRAINT fk_contacts
        FOREIGN KEY(user_id)
            REFERENCES contacts_db.users(id)
);

CREATE TABLE IF NOT EXISTS contacts_db.contact_phone_numbers
(
    id               bigserial NOT NULL,
    contact_id       bigint,
    phone_number     character varying NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_phones
        FOREIGN KEY(contact_id)
            REFERENCES contacts_db.contacts(id)
);

CREATE TABLE IF NOT EXISTS contacts_db.contact_emails
(
    id               bigserial NOT NULL,
    contact_id       bigint,
    email            character varying NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_emails
        FOREIGN KEY(contact_id)
            REFERENCES contacts_db.contacts(id)
);