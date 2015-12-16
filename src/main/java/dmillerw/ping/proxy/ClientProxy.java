package dmillerw.ping.proxy;

import java.awt.Color;

import dmillerw.ping.client.KeyHandler;
import dmillerw.ping.client.PingHandler;
import dmillerw.ping.client.RenderHandler;
import dmillerw.ping.data.PingType;
import dmillerw.ping.data.PingWrapper;
import dmillerw.ping.helper.RaytraceHelper;
import dmillerw.ping.network.PacketHandler;
import dmillerw.ping.network.packet.ClientSendPing;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * @author dmillerw
 */
public class ClientProxy extends CommonProxy {

    public static int pingR;
    public static int pingG;
    public static int pingB;

    public static boolean blockOverlay;
    public static boolean sound;

    public static double pingAcceptDistance;
    public static int pingDuration;

    public static Configuration configuration;

    public static void sendPing(PingType type) {
        MovingObjectPosition mob = RaytraceHelper.raytrace(Minecraft.getMinecraft().thePlayer, 50);
        if (mob != null && mob.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            sendPing(mob, new Color(ClientProxy.pingR, ClientProxy.pingG, ClientProxy.pingB).getRGB(), type);
        }
    }

    public static void sendPing(MovingObjectPosition mob, int color, PingType type) {
        PacketHandler.INSTANCE.sendToServer(new ClientSendPing(new PingWrapper(mob.getBlockPos().getX(), mob.getBlockPos().getY(), mob.getBlockPos().getZ(), color, type)));
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        PingHandler.register();
        KeyHandler.register();
        RenderHandler.register();

        FMLInterModComms.sendRuntimeMessage(this, "VersionChecker", "addVersionCheck", "https://raw.githubusercontent.com/dmillerw/Ping/master/version.json");

        configuration = new Configuration(event.getSuggestedConfigurationFile());
        configuration.load();
    }

    @Override
    public void syncConfig() {
        Property p_pingR = configuration.get("visual", "red", 100, "Value from 0 - 255");
        Property p_pingG = configuration.get("visual", "green", 0, "Value from 0 - 255");
        Property p_pingB = configuration.get("visual", "blue", 0, "Value from 0 - 255");

        pingR = verify(p_pingR);
        pingG = verify(p_pingG);
        pingB = verify(p_pingB);

        blockOverlay = configuration.get("visual", "blockOverlay", true, "Whether to render a colored overlay on the Pinged block").getBoolean();
        sound = configuration.get("general", "sound", true, "Whether to play a sound when a Ping is received").getBoolean();
        pingAcceptDistance = configuration.get("general", "pingAcceptDistance", 32D, "Maximum distance a Ping can be from you and still be received").getDouble();
        pingDuration = configuration.get("general", "pingDuration", 100, "How long a Ping should remain active before disappearing").getInt();

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    private int verify(Property property) {
        int value = property.getInt();
        if (value < 0) {
            value = 0;
        } else if (value > 255) {
            value = 255;
        }
        return value;
    }
}
