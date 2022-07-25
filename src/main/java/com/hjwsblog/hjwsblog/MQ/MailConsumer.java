package com.hjwsblog.hjwsblog.MQ;

import com.hjwsblog.hjwsblog.entity.messageToSend;
import com.hjwsblog.hjwsblog.service.MailService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MailConsumer {

    @Autowired
    MailService mailService;

    @RabbitListener(bindings =
    @QueueBinding(
            value = @Queue, //临时队列
            exchange = @Exchange(name = "mailExchange",type = "topic"), //指定交换机名称与类型
            key = {"user.*"}
    )
    )
    private void sendEMail(messageToSend message){
        mailService.sendMail(message.getAddress(), message.getSubject(), message.getMainText());
    }
}
