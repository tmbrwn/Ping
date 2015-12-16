package dmillerw.ping.network.packet;

import dmillerw.ping.client.PingHandler;
import dmillerw.ping.data.PingWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sent from the Server, handled on the Client
 * @author dmillerw
 */
public class ServerBroadcastPing implements IMessage, IMessageHandler<ServerBroadcastPing, IMessage> {

    public PingWrapper ping;

    public ServerBroadcastPing() {

    }

    public ServerBroadcastPing(PingWrapper ping) {
        this.ping = ping;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ping.writeToBuffer(buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ping = PingWrapper.readFromBuffer(buf);
    }

    @Override
    public IMessage onMessage(ServerBroadcastPing message, MessageContext ctx) {
        PingHandler.INSTANCE.onPingPacket(message);
        return null;
    }
}
