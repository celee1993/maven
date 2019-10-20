package com.leyou.sms.mq;

import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@EnableConfigurationProperties(SmsProperties.class)
@Component
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties prop;

    /**
     * 监听需要发短信的通知
     *
     * @param
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue", durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange", type = ExchangeTypes.TOPIC, durable = "true"),
            key = {"sms.verity.code"}
    ))
    public void listenInsertOrUpdate(Map<String, String> msg) {
        if (CollectionUtils.isEmpty(msg)) {
            return;
        }
        String phoneNumber = msg.remove("phone");
        if (StringUtils.isBlank(phoneNumber)) {
            return;
        }
        //进行发短信
        smsUtils.sendSms(phoneNumber, prop.getSignName(), prop.getVerifyCodeTemplate(), JsonUtils.serialize(msg));
    }
}
