-- Run against the existing WuTao database before deploying the cover feature.
-- This script does not reset articles or infer ownership of existing images.
create table if not exists ai_cover_assets (
    id varchar(36) primary key,
    owner_id bigint not null,
    article_id int null,
    object_key varchar(255) not null,
    image_url varchar(255) null,
    status varchar(16) not null,
    expires_at datetime not null,
    retry_at datetime not null default current_timestamp,
    create_time datetime not null default current_timestamp,
    update_time datetime not null default current_timestamp on update current_timestamp,
    unique key uk_ai_cover_object(object_key),
    unique key uk_ai_cover_url(image_url),
    index idx_ai_cover_article(article_id, status),
    index idx_ai_cover_expiry(status, expires_at),
    index idx_ai_cover_retry(status, retry_at)
) engine=InnoDB comment 'AI article cover ownership and cleanup';
