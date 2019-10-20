package com.leyou.sms.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsTest {
    @Autowired
    private AmqpTemplate template;

    @Test
    public void testSms() {
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", "15075772269");
        msg.put("code", "520131");
        template.convertAndSend("ly.sms.exchange","sms.verity.code",msg);
    }

}
