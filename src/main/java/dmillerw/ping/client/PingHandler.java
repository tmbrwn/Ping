package dmillerw.ping.client;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import dmillerw.ping.data.PingType;
import dmillerw.ping.data.PingWrapper;
import dmillerw.ping.helper.PingRenderHelper;
import dmillerw.ping.network.packet.ServerBroadcastPing;
import dmillerw.ping.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * @author dmillerw
 */
public class PingHandler {

    public static final PingHandler INSTANCE = new PingHandler();

    public static final ResourceLocation TEXTURE = new ResourceLocation("ping:textures/ping.png");

    public static void register() {
        FMLCommonHandler.instance().bus().register(INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    private List<PingWrapper> activePings = new ArrayList<PingWrapper>();

    public void onPingPacket(ServerBroadcastPing packet) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer.getDistance(packet.ping.x, packet.ping.y, packet.ping.z) <= ClientProxy.pingAcceptDistance) {
            if (ClientProxy.sound) {
                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("ping:bloop"), 1.0F));
            }
            packet.ping.timer = ClientProxy.pingDuration;
            activePings.add(packet.ping);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity renderEntity = mc.getRenderViewEntity();
        double interpX = renderEntity.prevPosX + (renderEntity.posX - renderEntity.prevPosX) * event.partialTicks;
        double interpY = renderEntity.prevPosY + (renderEntity.posY - renderEntity.prevPosY) * event.partialTicks;
        double interpZ = renderEntity.prevPosZ + (renderEntity.posZ - renderEntity.prevPosZ) * event.partialTicks;

        Frustum camera = new Frustum();
        camera.setPosition(interpX, interpY, interpZ);

        for (PingWrapper ping : activePings) {
            double px = ping.x + 0.5 - interpX;
            double py = ping.y + 0.5 - interpY;
            double pz = ping.z + 0.5 - interpZ;

            if (camera.isBoundingBoxInFrustum(ping.getAABB())) {
                ping.isOffscreen = false;
                if (ClientProxy.blockOverlay) {
                    renderPingOverlay(ping.x - TileEntityRendererDispatcher.staticPlayerX, ping.y - TileEntityRendererDispatcher.staticPlayerY, ping.z - TileEntityRendererDispatcher.staticPlayerZ, ping);
                }
                renderPing(px, py, pz, renderEntity, ping);
            } else {
                ping.isOffscreen = true;
                translatePingCoordinates(px, py, pz, ping);
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            for (PingWrapper ping : activePings) {
                if (!ping.isOffscreen) {
                    continue;
                }

                int width = mc.displayWidth;
                int height = mc.displayHeight;

                int x1 = -(width / 2) + 32;
                int y1 = -(height / 2) + 32;
                int x2 = (width / 2) - 32;
                int y2 = (height / 2) - 32;

                double pingX = ping.screenX;
                double pingY = ping.screenY;

                pingX -= width / 2;
                pingY -= height / 2;

                double angle = Math.atan2(pingY, pingX);
                angle += (Math.toRadians(90));
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double m = cos / sin;

                if (cos > 0){
                    pingX = y2 / m;
                    pingY = y2;
                } else {
                    pingX = y1 / m;
                    pingY = y1;
                }

                if (pingX > x2) {
                    pingX = x2;
                    pingY = x2 * m;
                } else if (pingX < x1) {
                    pingX = x1;
                    pingY = x1 * m;
                }

                pingX += width / 2;
                pingY += height / 2;

                GL11.glPushMatrix();

                Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

                WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();

                worldrenderer.setTranslation(pingX / 2, pingY / 2, 0);

                float min = -8;
                float max =  8;

                // Background
                worldrenderer.startDrawingQuads();
                worldrenderer.setColorOpaque_I(ping.color);
                worldrenderer.addVertexWithUV(min, max, 0, PingType.BACKGROUND.minU, PingType.BACKGROUND.maxV);
                worldrenderer.addVertexWithUV(max, max, 0, PingType.BACKGROUND.maxU, PingType.BACKGROUND.maxV);
                worldrenderer.addVertexWithUV(max, min, 0, PingType.BACKGROUND.maxU, PingType.BACKGROUND.minV);
                worldrenderer.addVertexWithUV(min, min, 0, PingType.BACKGROUND.minU, PingType.BACKGROUND.minV);
                Tessellator.getInstance().draw();

                // Icon
                worldrenderer.setColorOpaque_F(1, 1, 1);
                worldrenderer.startDrawingQuads();
                worldrenderer.addVertexWithUV(min, max, 0, ping.type.minU, ping.type.maxV);
                worldrenderer.addVertexWithUV(max, max, 0, ping.type.maxU, ping.type.maxV);
                worldrenderer.addVertexWithUV(max, min, 0, ping.type.maxU, ping.type.minV);
                worldrenderer.addVertexWithUV(min, min, 0, ping.type.minU, ping.type.minV);
                Tessellator.getInstance().draw();

                worldrenderer.setTranslation(0, 0, 0);

                GL11.glPopMatrix();
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Iterator<PingWrapper> iterator = activePings.iterator();
        while (iterator.hasNext()) {
            PingWrapper pingWrapper = iterator.next();
            if (pingWrapper.animationTimer > 0) {
                pingWrapper.animationTimer -= 5;
            }
            pingWrapper.timer--;

            if (pingWrapper.timer <= 0) {
                iterator.remove();
            }
        }
    }

    public void renderPing(double px, double py, double pz, Entity renderEntity, PingWrapper ping) {
        GL11.glPushMatrix();

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glTranslated(px, py, pz);

        GL11.glRotatef(-renderEntity.rotationYaw, 0, 1, 0);
        GL11.glRotatef(renderEntity.rotationPitch, 1, 0, 0);
        GL11.glRotated(180, 0, 0, 1);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();

        float min = -0.25F - (0.25F * (float)ping.animationTimer / 20F);
        float max =  0.25F + (0.25F * (float)ping.animationTimer / 20F);

        // Background
        worldrenderer.startDrawingQuads();
        worldrenderer.setColorOpaque_I(ping.color);
        worldrenderer.addVertexWithUV(min, max, 0, PingType.BACKGROUND.minU, PingType.BACKGROUND.maxV);
        worldrenderer.addVertexWithUV(max, max, 0, PingType.BACKGROUND.maxU, PingType.BACKGROUND.maxV);
        worldrenderer.addVertexWithUV(max, min, 0, PingType.BACKGROUND.maxU, PingType.BACKGROUND.minV);
        worldrenderer.addVertexWithUV(min, min, 0, PingType.BACKGROUND.minU, PingType.BACKGROUND.minV);
        Tessellator.getInstance().draw();

        // Icon
        worldrenderer.setColorOpaque_F(1, 1, 1);
        worldrenderer.startDrawingQuads();
        worldrenderer.addVertexWithUV(min, max, 0, ping.type.minU, ping.type.maxV);
        worldrenderer.addVertexWithUV(max, max, 0, ping.type.maxU, ping.type.maxV);
        worldrenderer.addVertexWithUV(max, min, 0, ping.type.maxU, ping.type.minV);
        worldrenderer.addVertexWithUV(min, min, 0, ping.type.minU, ping.type.minV);
        Tessellator.getInstance().draw();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }

    public void renderPingOverlay(double x, double y, double z, PingWrapper ping) {
        Minecraft mc = Minecraft.getMinecraft();
        
        TextureAtlasSprite icon = mc.getRenderItem().getItemModelMesher().getParticleIcon(Item.getItemFromBlock(Blocks.stained_glass));

        float padding = 0F + (0.20F * (float)ping.animationTimer / (float)20);
        float box = 1 + padding + padding;

        int alpha = ping.type == PingType.ALERT ? (int)(100 * (1 + Math.sin(mc.theWorld.getTotalWorldTime()))) : 25;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator.getInstance().getWorldRenderer().setTranslation(x + 0.5, y + 0.5, z + 0.5);

        PingRenderHelper.drawBlockOverlay(box, box, box, icon, ping.color, 150 + alpha);

        Tessellator.getInstance().getWorldRenderer().setTranslation(0, 0, 0);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    private void translatePingCoordinates(double px, double py, double pz, PingWrapper ping) {
        FloatBuffer screenCoords = BufferUtils.createFloatBuffer(4);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);


        if (GLU.gluProject((float)px, (float)py, (float)pz, modelview, projection, viewport, screenCoords)) {
            ping.screenX = screenCoords.get(0);
            ping.screenY = screenCoords.get(1);
            //TODO Rotation sometimes fucks this up
        }
    }
}
