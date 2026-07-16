ALTER TABLE user_tag ADD COLUMN tag_type VARCHAR(50) COMMENT 'tag type: customer type/health tag' AFTER tag_name;
ALTER TABLE user_tag ADD COLUMN color VARCHAR(50) COMMENT 'color' AFTER tag_type;
ALTER TABLE user_tag ADD INDEX idx_tag_type (tag_type);
