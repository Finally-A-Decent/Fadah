SET DEFAULT_STORAGE_ENGINE = INNODB;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS collection_boxV2
(
    playerUUID VARCHAR(36) NOT NULL PRIMARY KEY,
    items      TEXT        NOT NULL
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS expired_itemsV2
(
    playerUUID VARCHAR(36) NOT NULL PRIMARY KEY,
    items      TEXT        NOT NULL
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS listings
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    uuid         VARCHAR(36)  NOT NULL,
    ownerUUID    VARCHAR(36)  NOT NULL,
    ownerName    VARCHAR(255) NOT NULL,
    category     VARCHAR(255) NOT NULL,
    creationDate BIGINT       NOT NULL,
    deletionDate BIGINT       NOT NULL,
    price        DOUBLE       NOT NULL,
    tax          DOUBLE       NOT NULL,
    itemStack    TEXT         NOT NULL,
    biddable     BOOLEAN      NOT NULL,
    bids         TEXT         NOT NULL
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS historyV3
(
    playerUUID VARCHAR(36) NOT NULL PRIMARY KEY,
    items      LONGTEXT    NOT NULL
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS watchers
(
    playerUUID VARCHAR(36) NOT NULL PRIMARY KEY,
    watching   TEXT        NOT NULL
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;