package dmillerw.ping.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import dmillerw.ping.proxy.ClientProxy;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

/**
 * @author dmillerw
 */
public class PingGuiConfig extends GuiConfig {

    private static List<IConfigElement> getElements() {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        list.addAll((new ConfigElement(ClientProxy.configuration.getCategory("general"))).getChildElements());
        list.addAll((new ConfigElement(ClientProxy.configuration.getCategory("visual"))).getChildElements());
        return list;
    }

    public PingGuiConfig(GuiScreen parent) {
        super(
                parent,
                getElements(),
                "Ping",
                false,
                false,
                GuiConfig.getAbridgedConfigPath(ClientProxy.configuration.toString())
        );
    }
}
