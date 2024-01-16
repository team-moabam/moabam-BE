use moabam_dev;

create table admin
(
    id         bigint                                        not null auto_increment,
    nickname   varchar(255)                                  not null unique,
    social_id  varchar(255) unique,
    role       enum ('ADMIN','BLACK','USER') default 'ADMIN' not null,
    created_at datetime(6)                                   not null,
    updated_at datetime(6),
    primary key (id)
);

create table badge
(
    id         bigint                             not null auto_increment,
    member_id  bigint                             not null,
    type       enum ('BIRTH','LEVEL10','LEVEL50') not null,
    created_at datetime(6)                        not null,
    primary key (id)
);

create table bug_history
(
    id          bigint                                           not null auto_increment,
    member_id   bigint                                           not null,
    payment_id  bigint,
    bug_type    enum ('GOLDEN','MORNING','NIGHT')                not null,
    action_type enum ('CHARGE','COUPON','REFUND','REWARD','USE') not null,
    quantity    integer                                          not null,
    created_at  datetime(6)                                      not null,
    updated_at  datetime(6),
    primary key (id)
);

create table certification
(
    id         bigint       not null auto_increment,
    routine_id bigint       not null,
    member_id  bigint       not null,
    image      varchar(255) not null,
    created_at datetime(6)  not null,
    updated_at datetime(6),
    primary key (id)
);

create table coupon
(
    id          bigint                                       not null auto_increment,
    name        varchar(20)                                  not null unique,
    point       integer     default 1                        not null,
    description varchar(50) default '',
    type        enum ('DISCOUNT','GOLDEN','MORNING','NIGHT') not null,
    max_count   integer     default 1                        not null,
    start_at    date                                         not null unique,
    open_at     date                                         not null,
    admin_id    bigint                                       not null,
    created_at  datetime(6)                                  not null,
    updated_at  datetime(6),
    primary key (id)
);

create table coupon_wallet
(
    id         bigint      not null auto_increment,
    member_id  bigint      not null,
    coupon_id  bigint      not null,
    created_at datetime(6) not null,
    updated_at datetime(6),
    primary key (id)
);

create table daily_member_certification
(
    id             bigint      not null auto_increment,
    member_id      bigint      not null,
    room_id        bigint      not null,
    participant_id bigint,
    created_at     datetime(6) not null,
    updated_at     datetime(6),
    primary key (id)
);

create table daily_room_certification
(
    id           bigint not null auto_increment,
    room_id      bigint not null,
    certified_at date   not null,
    primary key (id)
);

create table inventory
(
    id         bigint            not null auto_increment,
    member_id  bigint            not null,
    item_id    bigint            not null,
    is_default bit default false not null,
    created_at datetime(6)       not null,
    updated_at datetime(6),
    primary key (id),
    index idx_member_id (member_id)
);

create table item
(
    id               bigint                   not null auto_increment,
    type             enum ('MORNING','NIGHT') not null,
    category         enum ('SKIN')            not null,
    name             varchar(255)             not null,
    awake_image      varchar(255)             not null,
    sleep_image      varchar(255)             not null,
    bug_price        integer default 0        not null,
    golden_bug_price integer default 0        not null,
    unlock_level     integer default 1        not null,
    created_at       datetime(6)              not null,
    updated_at       datetime(6),
    primary key (id)
);

create table member
(
    id                    bigint                                       not null auto_increment,
    social_id             varchar(255)                                 not null unique,
    nickname              varchar(255) unique,
    intro                 varchar(30),
    profile_image         varchar(255)                                 not null,
    morning_image         varchar(255)                                 not null,
    night_image           varchar(255)                                 not null,
    total_certify_count   bigint                        default 0      not null,
    report_count          integer                       default 0      not null,
    current_morning_count integer                       default 0      not null,
    current_night_count   integer                       default 0      not null,
    morning_bug           integer                       default 0      not null,
    night_bug             integer                       default 0      not null,
    golden_bug            integer                       default 0      not null,
    role                  enum ('ADMIN','BLACK','USER') default 'USER' not null,
    deleted_at            datetime(6),
    created_at            datetime(6)                                  not null,
    updated_at            datetime(6),
    primary key (id)
);

create table participant
(
    id                 bigint      not null auto_increment,
    room_id            bigint,
    member_id          bigint      not null,
    is_manager         bit,
    certify_count      integer,
    deleted_at         datetime(6),
    deleted_room_title varchar(30),
    created_at         datetime(6) not null,
    updated_at         datetime(6),
    primary key (id)
);

create table payment
(
    id               bigint                                                             not null auto_increment,
    member_id        bigint                                                             not null,
    product_id       bigint                                                             not null,
    coupon_wallet_id bigint,
    order_id         varchar(255),
    order_name       varchar(255)                                                       not null,
    total_amount     integer                                                            not null,
    discount_amount  integer                                                            not null,
    payment_key      varchar(255),
    status           enum ('ABORTED','CANCELED','DONE','EXPIRED','IN_PROGRESS','READY') not null,
    created_at       datetime(6)                                                        not null,
    requested_at     datetime(6),
    approved_at      datetime(6),
    primary key (id),
    index idx_order_id (order_id)
);

create table product
(
    id         bigint                     not null auto_increment,
    type       enum ('BUG') default 'BUG' not null,
    name       varchar(255)               not null,
    price      integer                    not null,
    quantity   integer      default 1     not null,
    created_at datetime(6)                not null,
    updated_at datetime(6),
    primary key (id)
);

create table report
(
    id                 bigint      not null auto_increment,
    reporter_id        bigint      not null,
    reported_member_id bigint      not null,
    room_id            bigint,
    certification_id   bigint,
    description        varchar(255),
    created_at         datetime(6) not null,
    updated_at         datetime(6),
    primary key (id)
);

create table room
(
    id                 bigint            not null auto_increment,
    title              varchar(20)       not null,
    password           varchar(8),
    level              integer default 0 not null,
    exp                integer default 0 not null,
    room_type          enum ('MORNING','NIGHT'),
    certify_time       integer           not null,
    current_user_count integer           not null,
    max_user_count     integer           not null,
    announcement       varchar(100),
    room_image         varchar(500),
    manager_nickname   varchar(30),
    deleted_at         datetime(6),
    created_at         datetime(6)       not null,
    updated_at         datetime(6),
    primary key (id),
    FULLTEXT INDEX full_index_title (title) WITH PARSER ngram,
    FULLTEXT INDEX full_index_manager_nickname (manager_nickname) WITH PARSER ngram
);

create table routine
(
    id         bigint      not null auto_increment,
    room_id    bigint,
    content    varchar(20) not null,
    created_at datetime(6) not null,
    updated_at datetime(6),
    primary key (id),
    FULLTEXT INDEX full_index_content (content) WITH PARSER ngram
);

alter table bug_history
    add foreign key (payment_id) references payment (id);

alter table certification
    add foreign key (routine_id) references routine (id);

alter table coupon_wallet
    add foreign key (coupon_id) references coupon (id);

alter table daily_member_certification
    add foreign key (participant_id) references participant (id);

alter table inventory
    add foreign key (item_id) references item (id);

alter table participant
    add foreign key (room_id) references room (id);

alter table payment
    add foreign key (product_id) references product (id);

alter table report
    add foreign key (certification_id) references certification (id);

alter table report
    add foreign key (room_id) references room (id);

alter table routine
    add foreign key (room_id) references room (id);
