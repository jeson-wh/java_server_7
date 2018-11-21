package com.linkflywind.gameserver.logicserver.action;

import akka.actor.ActorRef;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.linkflywind.gameserver.core.action.BaseAction;
import com.linkflywind.gameserver.core.annotation.Protocol;
import com.linkflywind.gameserver.core.network.websocket.GameWebSocketSession;
import com.linkflywind.gameserver.core.player.Player;
import com.linkflywind.gameserver.core.redisModel.TransferData;
import com.linkflywind.gameserver.core.room.RoomAction;
import com.linkflywind.gameserver.logicserver.player.YingSanZhangPlayer;
import com.linkflywind.gameserver.logicserver.protocolData.request.A1001Request;
import com.linkflywind.gameserver.logicserver.protocolData.response.A1011Response;
import com.linkflywind.gameserver.logicserver.room.YingSanZhangRoomActorManager;
import com.linkflywind.gameserver.logicserver.room.YingSanZhangRoomContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@Protocol(1002)
public class ConnectAction extends BaseAction implements RoomAction<A1001Request, YingSanZhangRoomContext> {

    private final YingSanZhangRoomActorManager roomActorManager;

    @Autowired
    public ConnectAction(RedisTemplate redisTemplate, YingSanZhangRoomActorManager roomActorManager) {
        super(redisTemplate);
        this.roomActorManager = roomActorManager;
    }

    @Override
    public void action(TransferData optionalTransferData) throws IOException {


        String name = optionalTransferData.getGameWebSocketSession().getName();
        GameWebSocketSession gameWebSocketSession = this.valueOperationsByGameWebSocketSession.get(optionalTransferData.getGameWebSocketSession().getName());


        gameWebSocketSession.getRoomNumber().ifPresent(number -> {
                    ActorRef actorRef = roomActorManager.getRoomActorRef(number);
                    actorRef.tell(new A1001Request(name, number), null);
                }
        );
    }

    @Override
    public boolean action(A1001Request message, YingSanZhangRoomContext context) {
        try {

            Optional<Player> optionalPlayer = context.getPlayer(message.getName());
            if (optionalPlayer.isPresent()) {
                Player player = optionalPlayer.get();
                player.setDisConnection(false);
                GameWebSocketSession session = this.valueOperationsByGameWebSocketSession.get(message.getName());
                context.send(new A1011Response(context.deskChip, context.getPlayerList().toArray(new YingSanZhangPlayer[0])),
                        new TransferData(session,
                                context.getServerName(), 1011, Optional.empty()));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }
}