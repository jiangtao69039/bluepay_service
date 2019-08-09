-- auto-generated definition
create table biz_notify
(
  id               bigserial                                          not null
    constraint biz_notify_pkey
    primary key,
  transaction_code varchar(128)                                       not null,
  out_trade_no     varchar(128)                                       not null,
  notify_url       varchar(1024)                                      not null,
  notify_type      varchar(32)                                        not null,
  biz_response     json,
  notify_status    varchar(32)                                        not null,
  notify_count     integer default 0                                  not null,
  notify_params    json,
  updated_at       timestamp with time zone default CURRENT_TIMESTAMP not null,
  created_at       timestamp with time zone default CURRENT_TIMESTAMP not null
);

comment on table biz_notify
is '通知业务系统的记录表';

comment on column biz_notify.transaction_code
is '支付系统生成的唯一编号';

comment on column biz_notify.out_trade_no
is '调用方的唯一编号';

comment on column biz_notify.notify_url
is '异步通知地址';

comment on column biz_notify.notify_type
is '异步通知类型 http或sqs';

comment on column biz_notify.biz_response
is '回调地址的响应';

comment on column biz_notify.notify_status
is '是否回调成功,http 200 是成功,其他判定为失败';

comment on column biz_notify.notify_count
is '通知次数';

comment on column biz_notify.notify_params
is '通知的参数';


create unique index biz_notify_id_uindex
  on biz_notify (id);

create unique index biz_notify_transaction_code_uindex
  on biz_notify (transaction_code);





-- auto-generated definition
create table gather_trade
(
  id                             bigserial                                          not null
    constraint gather_trade_pkey
    primary key,
  transaction_code               varchar(128)                                       not null,
  out_trade_no                   varchar(128)                                       not null,
  channel_code                   varchar(64)                                        not null,
  channel_biz_code               varchar(64)                                        not null,
  price                          integer                                            not null,
  currency                       varchar(32)                                        not null,
  nation                         varchar(32)                                        not null,
  channel_extend_info            json,
  receive_price                  integer,
  channel_callback_notify_status varchar(32)                                        not null,
  trade_status                   varchar(32)                                        not null,
  channel_callback_extend_info   json,
  extend                         json,
  updated_at                     timestamp with time zone default CURRENT_TIMESTAMP not null,
  created_at                     timestamp with time zone default CURRENT_TIMESTAMP not null,
  person_info                    json,
  pass_back                      json,
  trade_msg                      varchar(1024),
  channel_order_info             json,
  biz_notify_url                 varchar(1024),
  biz_notify_type                varchar(32)                                        not null
);

comment on table gather_trade
is '收款明细表';

comment on column gather_trade.transaction_code
is '支付系统生成的唯一编号 数字加字母';

comment on column gather_trade.out_trade_no
is '业务系统的唯一编号,与transactionId对应,在支付系统内不可重复';

comment on column gather_trade.channel_code
is '渠道(bluepay)';

comment on column gather_trade.channel_biz_code
is '渠道的某个具体业务(如bluepay的动态VA收款)';

comment on column gather_trade.price
is '交易金额,整数';

comment on column gather_trade.currency
is '币种(IDR) 大写';

comment on column gather_trade.nation
is '国家代码  大写';

comment on column gather_trade.channel_extend_info
is '渠道额外信息';

comment on column gather_trade.receive_price
is '收到的金额';

comment on column gather_trade.channel_callback_notify_status
is '是否收到第三方回调通知
0 1 2 3标识回调次数';

comment on column gather_trade.trade_status
is '标识交易状态(进行中, 成功,失败)';

comment on column gather_trade.channel_callback_extend_info
is '保存回调时的一些信息,多次回调可追加';

comment on column gather_trade.extend
is '额外信息';

comment on column gather_trade.person_info
is '人员信息';

comment on column gather_trade.pass_back
is '回调业务系统时原样返回的参数';

comment on column gather_trade.trade_msg
is '出错时可以保存一些信息';

comment on column gather_trade.channel_order_info
is '渠道下单接口的响应';

comment on column gather_trade.biz_notify_url
is '业务系统传过来的回调通知地址';


create unique index gather_trade_id_uindex
  on gather_trade (id);

create unique index gather_trade_transaction_code_uindex
  on gather_trade (transaction_code);

create unique index gather_trade_out_trade_no_uindex
  on gather_trade (out_trade_no);




-- auto-generated definition
create table loan_trade
(
  id                             bigserial                                          not null
    constraint loan_trade_pkey
    primary key,
  transaction_code               varchar(128)                                       not null,
  out_trade_no                   varchar(128)                                       not null,
  channel_code                   varchar(64)                                        not null,
  channel_biz_code               varchar(64)                                        not null,
  price                          integer                                            not null,
  currency                       varchar(32)                                        not null,
  channel_extend_info            json,
  channel_callback_notify_status varchar(32)                                        not null,
  trade_status                   varchar(32)                                        not null,
  channel_callback_extend_info   json,
  extend                         json,
  person_info                    json,
  updated_at                     timestamp with time zone default CURRENT_TIMESTAMP not null,
  created_at                     timestamp with time zone default CURRENT_TIMESTAMP not null,
  trade_msg                      varchar(1024),
  pass_back                      json,
  channel_order_info             json,
  biz_notify_url                 varchar(1024),
  biz_notify_type                varchar(32)                                        not null
);

comment on table loan_trade
is '放款明细表';

comment on column loan_trade.transaction_code
is '支付系统生成的唯一编号';

comment on column loan_trade.out_trade_no
is '业务系统的唯一编号,与transactionId对应,在支付系统内不可重复';

comment on column loan_trade.channel_code
is '渠道(bluepay)';

comment on column loan_trade.channel_biz_code
is '渠道的某个具体业务(如bluepay的动态VA收款)';

comment on column loan_trade.currency
is '币种(IDR 大写)';

comment on column loan_trade.channel_extend_info
is '渠道特有信息';

comment on column loan_trade.channel_callback_notify_status
is '是否收到第三方的回调通知(0 1 2 3)';

comment on column loan_trade.trade_status
is '交易状态 进行中 成功 失败';

comment on column loan_trade.channel_callback_extend_info
is '渠道回调信息,多次回调可追加';

comment on column loan_trade.extend
is '额外信息';

comment on column loan_trade.person_info
is '人员信息';

comment on column loan_trade.trade_msg
is '出错时一些错误信息';

comment on column loan_trade.biz_notify_url
is '业务系统传来的回调通知地址';


create unique index loan_trade_id_uindex
  on loan_trade (id);

create unique index loan_trade_transaction_code_uindex
  on loan_trade (transaction_code);

create unique index loan_trade_out_trade_no_uindex
  on loan_trade (out_trade_no);




-- auto-generated definition
create table pay_trade
(
  id                  bigserial                                          not null
    constraint pay_trade_pkey
    primary key,
  transaction_code    varchar(128)                                       not null,
  out_trade_no        varchar(128)                                       not null,
  channel_code        varchar(64)                                        not null,
  channel_biz_code    varchar(64)                                        not null,
  channel_extend_info json,
  extend              json,
  updated_at          timestamp with time zone default CURRENT_TIMESTAMP not null,
  created_at          timestamp with time zone default CURRENT_TIMESTAMP not null
);

comment on table pay_trade
is '交易总表 包含收款放款';

comment on column pay_trade.transaction_code
is '支付系统生成的唯一编号';

comment on column pay_trade.out_trade_no
is '业务系统的唯一编号,与transactionId对应,在支付系统内不可重复';

comment on column pay_trade.channel_code
is '渠道code';

comment on column pay_trade.channel_biz_code
is '渠道的某个具体业务(如bluepay的动态VA收款)';

comment on column pay_trade.channel_extend_info
is '渠道特有的额外信息';

comment on column pay_trade.extend
is '额外信息';


create unique index pay_trade_id_uindex
  on pay_trade (id);

create unique index pay_trade_transaction_code_uindex
  on pay_trade (transaction_code);

create unique index pay_trade_out_trade_no_uindex
  on pay_trade (out_trade_no);


