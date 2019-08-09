/*
package com.panda.pay.service.third.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

*/
/** Created by jiangtao on 19-2-13 下午4:54 *//*

                                            @Service("sqsService")
                                            public class SqsServiceImpl {

                                              @Value("${thirdparty.aws.sqs.accesskeyId}")
                                              private String aws_sqs_accesskey_id;

                                              @Value("${thirdparty.aws.sqs.secretKey}")
                                              private String aws_sqs_secret_key;

                                              @Value("${thirdparty.aws.sqs.region}")
                                              private String aws_sqs_region;

                                              private AmazonSQS sqs;

                                              public SqsServiceImpl(
                                                  @Value("${thirdparty.aws.sqs.accesskeyId}") String aws_sqs_accesskey_id,
                                                  @Value("${thirdparty.aws.sqs.secretKey}") String aws_sqs_secret_key,
                                                  @Value("${thirdparty.aws.sqs.region}") String aws_sqs_region) {

                                                this.aws_sqs_accesskey_id = aws_sqs_accesskey_id;
                                                this.aws_sqs_secret_key = aws_sqs_secret_key;
                                                this.aws_sqs_region = aws_sqs_region;

                                                if (StringUtils.isNotBlank(aws_sqs_secret_key)) {
                                                  // 测试环境
                                                  BasicAWSCredentials awsCreds =
                                                      new BasicAWSCredentials(aws_sqs_accesskey_id, aws_sqs_secret_key);
                                                  sqs =
                                                      AmazonSQSClientBuilder.standard()
                                                          .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                                                          .withRegion(Regions.fromName(aws_sqs_region))
                                                          .build();
                                                } else {
                                                  // EC2
                                                  System.setProperty("aws.region", aws_sqs_region);
                                                  sqs =
                                                      AmazonSQSClientBuilder.standard()
                                                          .withCredentials(new InstanceProfileCredentialsProvider(false))
                                                          .withRegion(Regions.fromName(aws_sqs_region))
                                                          .build();
                                                }
                                              }

                                              public void sendMessage(String queue, String msg) {
                                                if (msg == null) return;

                                                sqs.sendMessage(queue, msg);
                                              }

                                              public List<Message> receiveMessages(String queue, Integer number) {
                                                if (number <= 0) return new ArrayList<>();

                                                return sqs.receiveMessage(
                                                        new ReceiveMessageRequest(queue)
                                                            .withMaxNumberOfMessages(number)
                                                            .withWaitTimeSeconds(20))
                                                    .getMessages();
                                              }

                                              public void deleteMessage(String queue, Message msg) {
                                                if (msg == null) return;
                                                sqs.deleteMessage(queue, msg.getReceiptHandle());
                                              }
                                            }
                                            */
