package com.linkflywind.gameserver.yingsanzhangserver.listen;

import com.linkflywind.gameserver.core.action.DispatcherAction;
import com.linkflywind.gameserver.core.TransferData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class RedisListen implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DispatcherAction dispatcherAction;

    private final RedisTemplate redisTemplate;


    @Value("${logicserver.name}")
    private String listenName;

    @Autowired
    public RedisListen(DispatcherAction dispatcherAction, RedisTemplate redisTemplate) {
        this.dispatcherAction = dispatcherAction;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void onMessage(Message message, byte[] bytes) {
        doMessageTask(message,bytes);
    }

    @Async
    public void doMessageTask(Message message, byte[] bytes){
        TransferData transferData = (TransferData) Objects.requireNonNull(this.redisTemplate.getDefaultSerializer()).deserialize(message.getBody());

        dispatcherAction.createAction(transferData.getProtocol()).ifPresent(p-> {
            try {
                p.requestAction(transferData);
            } catch (IOException e) {
                logger.error("action error",e);
            }
        });
    }
}