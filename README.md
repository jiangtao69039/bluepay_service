# panda pay_serve
# 支付服务



目前对接bluepay渠道,提供统一放款 收款 和 订单查询接口  
结果会异步通知调用方


## 统一收款接口请求说明
说明:支付系统的统一收款业务,调用该接口触发向用户收款,收款结果会异步通知调用方.目前对接bluepay  
请求地址:/{context_path}/pay/gather  
类型:application/json  
* 公共请求参数:  

~~~ 
  price:Integer 收款金额 必填
  currency:String 币种 大写 必填 
  notifyUrl:String 回调通知地址 必填  
  notifyType:String 回调通知类型 http/sqs 必填  
  nation:String 两位国家代码 中国CN 印尼ID 必填
  mobile:String 手机号 不带国家区号 必填  
  passBack:json 回调通知时原样返回 选填
  outTradeNo:String 外部系统传递的唯一标识 必传  
  channelCode:String 使用的渠道(bluepay) 目前必填
  channelBizCode:String 使用的渠道业务(gather_dynamic_VA_bluepay) 目前必填  
  channelSpecialParams:json 渠道特定参数 详情参见下表 必填
  extend:json 扩展参数(生产环境需要传"prodFlag":"true",默认为"false") 选填  
~~~  

* bluepay渠道收款特定参数  channelSpecialParams:json

~~~  
  payType:String 支付类型 支持 atm,otc（便利店） 必传
  bankType:String 银行类型 如果 payType=atm，那么bankType 必须等于其中之一：permata, bni, mandiri。如果payType=otc, bankType不用传 测试环境BNI不可用 必传  
  prductId:String bluepay产品id 2257测试 生产待定 必填
~~~  

* 请求示例:  
~~~ 
  POST /bluepay-service/pay/gather HTTP/1.1
  Host: 47.104.109.123:84
  Content-Type: application/json
  Cache-Control: no-cache
  Postman-Token: c9b3baf5-caa0-4561-3713-780ee9f1cb7a
  
  {
      "notifyType":"http",
      "nation":"ID",
      "price":20000,
      "channelSpecialParams":{
          "payType":"atm",
          "productId":"2257",
          "bankType":"permata"
      },
      "outTradeNo":"2732024fec0d42e082fc9831fe41742d",
      "mobile":"082197662283",
      "notifyUrl":"http://47.104.109.123:84/bluepay-service/callback/testNotify",
      "currency":"IDR",
      "channelBizCode":"gather_dynamic_VA_bluepay",
      "channelCode":"bluepay"
  }
~~~ 



## 统一收款接口响应说明  
响应类型:json  
* 响应公共参数:
~~~
  code:String 10000表示成功 其他为失败
  msg:String 对code的说明
  data:{
   payServeBizType:String 支付系统的业务类型(目前支持gather收款  loan放款  query_gather  query_loan)  
   transactionCode:String 支付系统唯一标识 46位 
   bizParams:json 具体渠道的返回
   }业务信息
~~~

* bluepay渠道收款返回的bizParams:json  

~~~ 
  channelCode:String bluepay
  channelBizCode:String 具体业务code bluepay收款接口固定为gather_dynamic_VA_bluepay
  vaFee:String VA手续费
  otcFee:String  OTC手续费
  isStatic:String 是否静态VA
  status:String 交易状态码
  paymentCode:String  虚拟VA账号
  description
  httpStatusCode:调用bluepay的http结果
  bizStatusCode: bluepay业务状态
  bizStatusMessage: bluepay业务说明
~~~   

* 响应示例: 
~~~ 
  {
      "code": "100000",
      "message": "成功",
      "data": {
          "payServeBizType": "gather",
          "transactionCode": "d1948ed5750f414ebf75c37bbfdae319",
          "bizParams": {
              "bizStatusMessage": "Success",
              "isStatic": "0",
              "otcFee": "0",
              "paymentCode": "8359190216393465",
              "bizStatusCode": "201",
              "vaFee": "0",
              "description": "Success",
              "transactionCode": "d1948ed5750f414ebf75c37bbfdae319",
              "channelBizCode": "gather_dynamic_VA_bluepay",
              "httpStatusCode": "200",
              "status": "201",
              "channelCode": "bluepay"
          }
      }
  }
~~~ 



## 统一放款接口请求说明  
说明:支付系统的统一放款业务,调用该接口向指定银行账户放款,放款结果会异步通知调用方.目前对接bluepay  
请求地址:/{context_path}/pay/loan  
类型:application/json  
* 公共请求参数:  

~~~ 
  price:Integer 收款金额 必填
  currency:String 币种 大写 必填 
  notifyUrl:String 回调通知地址 必填  
  notifyType:String 回调通知类型 http/sqs 必填  
  nation:String 两位国家代码 中国CN 印尼ID 必填
  mobile:String 手机号 不带国家区号 必填  
  passBack:json 回调通知时原样返回 选填
  outTradeNo:String 外部系统传递的唯一标识 必传  
  channelCode:String 使用的渠道(bluepay) 目前必填
  channelBizCode:String 使用的渠道业务(loan_fintech_bluepay) 目前必填  
  channelSpecialParams:json 渠道特定参数 详情参见下表 必填
  extend:json 扩展参数(生产环境需要传"prodFlag":"true",默认为"false") 选填  
~~~  

* bluepay渠道放款特定参数  channelSpecialParams:json

~~~  
  payeeBackName:String 银行名称 必填  
  payeeName:String 收款方名字 必填  
  payeeAccount:String 收款方银行账户(不是卡号) 必填  
  payeeType:String 收款方类型 企业"MERCHANT" 个人"NORMAL" 不传NORMAL  
  prductId:String bluepay产品id 2257测试 生产待定 必填  
~~~  

* 请求示例: 
~~~ 
  POST /bluepay-service/pay/loan HTTP/1.1
  Host: 47.104.109.123:84
  Content-Type: application/json
  Cache-Control: no-cache
  Postman-Token: bc032af2-eab5-616f-a71e-9c013050e76f
  
  {
      "notifyType":"http",
      "nation":"ID",
      "price":20000,
      "channelSpecialParams":{
          "payeeName":"Afif Asrif",
          "payeeBankName":"ARTA GRAHA",
          "productId":"2257",
          "payeeType":"NORMAL",
          "payeeAccount":"1077098557"
      },
      "outTradeNo":"a3eb39f673eb404590e9659a0017aa06",
      "mobile":"082197662283",
      "notifyUrl":"http://47.104.109.123:84/bluepay-service/callback/testNotify",
      "currency":"IDR",
      "channelBizCode":"loan_fintech_bluepay",
      "channelCode":"bluepay"
  }
~~~ 

## 统一放款接口响应说明  
响应类型:json  
* 响应公共参数:
~~~
  code:String 10000表示成功 其他为失败
  msg:String 对code的说明
  data:{
     payServeBizType:String 支付系统的业务类型(目前支持gather收款  loan放款  query_gather  query_loan)  
     transactionCode:String 支付系统唯一标识 46位 
     bizParams:json 具体渠道的返回
     }业务信息
~~~

* bluepay渠道放款返回的bizParams:json  

~~~ 
  transactionCode:String 
  channelCode:String bluepay
  channelBizCode:String loan_fintech_bluepay  
  transactionId:String 与transactionCode一样
  code:String 转账请求状态  
  transferStatus:String 转账状态  
  description:String 描述
~~~  

* 响应示例: 
~~~ 
  {
      "code": "100000",
      "message": "成功",
      "data": {
          "payServeBizType": "loan",
          "transactionCode": "25aa546630ee4b96915e16d577d15fa7",
          "bizParams": {
              "bizStatusMessage": "success",
              "code": "200",
              "transferStatus": "201",
              "bizStatusCode": "201",
              "description": "success",
              "transactionCode": "25aa546630ee4b96915e16d577d15fa7",
              "channelBizCode": "loan_fintech_bluepay",
              "transactionId": "25aa546630ee4b96915e16d577d15fa7",
              "httpStatusCode": "200",
              "channelCode": "bluepay"
          }
      }
  }
~~~ 



## 统一查询接口请求说明 
说明:支付系统统一查询业务,通过transactionCode查询一个订单的状态支持收款查询和放款查询   
请求地址:/{context_path}/pay/query  
类型:application/json   
* 请求参数:  
~~~ 
  transactionCode:String 支付系统返回的唯一编号
  extend:json 目前不用传递
~~~  

* 请求示例: 
~~~ 
  POST /bluepay-service/pay/query HTTP/1.1
  Host: 47.104.109.123:84
  Content-Type: application/json
  Cache-Control: no-cache
  Postman-Token: bd5aab88-0dfe-d24f-a314-d6458e2fed19
  
  {"transactionCode":"25aa546630ee4b96915e16d577d15fa7"}
~~~ 

## 统一查询接口响应说明  
响应类型:json  
* 响应公共参数:
~~~
  code:String 10000表示成功 其他为失败
  msg:String 对code的说明
  data:{
     payServeBizType:String  query_gather或者query_loan  
     transactionCode:String 支付系统唯一标识 46位 
     bizParams:json 具体渠道的返回
     }业务信息
~~~

* 1.bluepay渠道收款VA返回的bizParams:json  

~~~  
  channelCode:String bluepay
  channelBizCode:String query_gather
  result:json{
    innerid:String bluepay方标识符  
    msisdn:String 手机号  
    price:String 金额  
    record_status 交易状态结果,参考错误码说明 
    timestamp:String 交易时间 yyyy-MM-dd HH:mm:ss 
    transactionId:String transactionCode 
  }
  status:String 查询状态,200标识接口正常  
  
  
  响应示例: 
  {
      "code": "100000",
      "message": "成功",
      "data": {
          "payServeBizType": "query_gather",
          "transactionCode": "d1948ed5750f414ebf75c37bbfdae319",
          "bizParams": {
              "result": {
                  "innerid": "2257042521550283190351SmBwdU",
                  "record_status": "200",
                  "price": "20000",
                  "msisdn": "nomsisdn",
                  "transactionId": "d1948ed5750f414ebf75c37bbfdae319",
                  "timestamp": "2019-02-16 10:14:28"
              },
              "channelBizCode": "query_gather_detail_bluepay",
              "status": "200",
              "channelCode": "bluepay"
          }
      }
  }
~~~  

* 2.bluepay渠道放款返回的bizParams:json  

~~~  
  channelCode:String bluepay
  channelBizCode:String query_loan  
  time:String 交易时间 yyyy-MM-dd HH:mm:ss 
  transactionId:String transactionCode 
  transferStatus:String 转账结果,详情见错误码 
  code:String 接口调用200表示接口正常  
  productId:String bluepay产品id  
  
  
  响应示例: 
  {
      "code": "100000",
      "message": "成功",
      "data": {
          "payServeBizType": "query_loan",
          "transactionCode": "25aa546630ee4b96915e16d577d15fa7",
          "bizParams": {
              "transferStatus": "200",
              "code": "200",
              "productId": "2257",
              "time": "2019-02-16 10:21:34",
              "channelBizCode": "query_loan_detail_bluepay",
              "transactionId": "25aa546630ee4b96915e16d577d15fa7",
              "channelCode": "bluepay"
          }
      }
  }
~~~  


# 回调通知  
说明:支付系统当收到第三方的回调时,也会回调通知业务系统.回调方式 采用业务系统下单  
时传递的方式和地址(sqs或http) http收到通知需要返回正常响应,支付系统收到不是200的响应  
 判定为失败. 目前只回调1次  
 类型:http为json  sqs为json格式的字符串
 
 * 收款回调通知:  
 ~~~  
   tradeStatus:String  交易状态:fail失败 process进行中 success成功 
   tradeMsg:String 交易状态说明 
   channelCode:String 渠道code
   channelBizCode:String 渠道业务code
   transactionCode:String 支付系统唯一编号 
   outTradeNo:String 业务系统下单时传递的唯一编号
   passBack:json 业务系统下单时传递的参数,原样返回 
   originChannelCallBack:json 第三方渠道回调的原始参数
   
   
   bluepay渠道的originChannelCallBack说明:
   t_id:String transactionCode
   bt_id:String  BluePay 在交易过程中生成的id,可以作后续对账用
   interfacetype:String 只有一个值bank
   productid:String
   encrypt:String 签名
   price:String 交易金额
   currency:String 币种 IDR
   cmd:String CHG 表示已经发出交易请求，但不确认是否成功；CFM表示返回要确认的交易结果，需通过 status 确认交易是否成功；
   msisdn:String 带国家区号的手机号
   paytype:String 只有一个值 Pre
   operator:String 付费通道，银行通道operator=atm，便利店通道operator=otc
   status:String 状态码,200成功 201进行中 其他失败 
   
   
   
   收款回调通知的http请求示例:(sqs方式传递下面的json格式字符串) 
   
   POST /bluepay-service/pay/loan HTTP/1.1
   Host: 47.104.109.123:84
   Content-Type: application/json
   Cache-Control: no-cache
   Postman-Token: bc032af2-eab5-616f-a71e-9c013050e76f
   
   {
       "tradeStatus":"success",
       "tradeMsg":"交易成功",
       "channelCode":"bluepay",
       "channelBizCode":"gather_dynamic_VA_bluepay",
       "transactionCode":"d1948ed5750f414ebf75c37bbfdae319",
       "outTradeNo":"2732024fec0d42e082fc9831fe41742d",
       "passBack":null,
       "originChannelCallBack":{
           "t_id":"d1948ed5750f414ebf75c37bbfdae319",
           "bt_id":"2257042521550283190351SmBwdU",
           "interfacetype":"bank",
           "productid":"2257",
           "encrypt":"176cd85f5c2148c75842f8da34e09b96",
           "price":"20000",
           "currency":"THB",
           "cmd":"CFM",
           "msisdn":"nomsisdn",
           "paytype":"pre",
           "operator":"atm",
           "status":"200"
       }
   }
     
   
 ~~~  
 
 
 * 放款回调通知:
 ~~~ 
   tradeStatus:String  交易状态:fail失败 process进行中 success成功 
   tradeMsg:String 交易状态说明 
   channelCode:String 渠道code
   channelBizCode:String 渠道业务code
   transactionCode:String 支付系统唯一编号 
   outTradeNo:String 业务系统下单时传递的唯一编号
   passBack:json 业务系统下单时传递的参数,原样返回 
   originChannelCallBack:json 第三方渠道回调的原始参数
      
      
   bluepay渠道的originChannelCallBack说明:
   t_id:String transactionCode
   bt_id:String  BluePay 在交易过程中生成的id,可以作后续对账用
   interfacetype:String cashout为放款 refundcashout为放款被退款
   productid:String
   encrypt:String 签名
   price:String 交易金额
   currency:String 币种 IDR
   cmd:String CHG 表示已经发出交易请求，但不确认是否成功；CFM表示返回要确认的交易结果，需通过 status 确认交易是否成功；
   msisdn:String 带国家区号的手机号,没有会返回nomsisdn
   paytype:String 用户手机号类型 默认pre
   operator:String 选择的银行. /*在收到回调通知后需要先url_decode再参与md5*/
   status:String 状态码,200成功 201进行中 其他失败 
   
   
   
   
   放款回调通知的http请求示例:(sqs方式传递下面的json格式字符串)
   POST /bluepay-service/pay/loan HTTP/1.1
   Host: 47.104.109.123:84
   Content-Type: application/json
   Cache-Control: no-cache
   Postman-Token: bc032af2-eab5-616f-a71e-9c013050e76f  
   
   {
       "tradeStatus":"success",
       "tradeMsg":"交易成功",
       "channelCode":"bluepay",
       "channelBizCode":"loan_fintech_bluepay",
       "transactionCode":"25aa546630ee4b96915e16d577d15fa7",
       "outTradeNo":"a3eb39f673eb404590e9659a0017aa06",
       "passBack":null,
       "originChannelCallBack":{
           "t_id":"25aa546630ee4b96915e16d577d15fa7",
           "bt_id":"2257042141550283650288kdPmZk",
           "interfacetype":"cashout",
           "productid":"2257",
           "encrypt":"b4dadc75b2ac2861a2cab06ec9c1b7bf",
           "price":"20000",
           "currency":"IDR",
           "cmd":"CFM",
           "msisdn":"62082197662283",
           "paytype":"pre",
           "operator":"ARTA GRAHA",
           "status":"200"
       }
   }
      
   
 ~~~ 

