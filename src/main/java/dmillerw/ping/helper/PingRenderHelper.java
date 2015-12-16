package dmillerw.ping.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

/**
 * @author dmillerw
 */
public class PingRenderHelper {

    public static void drawBlockOverlay(float width, float height, float length, TextureAtlasSprite icon, int color, int alpha) {
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();

        worldrenderer.startDrawingQuads();

        worldrenderer.setColorRGBA_I(color, alpha);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        worldrenderer.setBrightness(Integer.MAX_VALUE);

        // TOP
        worldrenderer.addVertexWithUV(-(width / 2), (height / 2), -(length / 2), icon.getMinU(), icon.getMinV());
        worldrenderer.addVertexWithUV( (width / 2), (height / 2), -(length / 2), icon.getMaxU(), icon.getMinV());
        worldrenderer.addVertexWithUV( (width / 2), (height / 2),  (length / 2), icon.getMaxU(), icon.getMaxV());
        worldrenderer.addVertexWithUV(-(width / 2), (height / 2),  (length / 2), icon.getMinU(), icon.getMaxV());

        // BOTTOM
        worldrenderer.addVertexWithUV(-(width / 2), -(height / 2),  (length / 2), icon.getMinU(), icon.getMaxV());
        worldrenderer.addVertexWithUV( (width / 2), -(height / 2),  (length / 2), icon.getMaxU(), icon.getMaxV());
        worldrenderer.addVertexWithUV( (width / 2), -(height / 2), -(length / 2), icon.getMaxU(), icon.getMinV());
        worldrenderer.addVertexWithUV(-(width / 2), -(height / 2), -(length / 2), icon.getMinU(), icon.getMinV());

        // NORTH
        worldrenderer.addVertexWithUV(-(width / 2),  (height / 2),  (length / 2), icon.getMinU(), icon.getMaxV());
        worldrenderer.addVertexWithUV( (width / 2),  (height / 2),  (length / 2), icon.getMaxU(), icon.getMaxV());
        worldrenderer.addVertexWithUV( (width / 2), -(height / 2),  (length / 2), icon.getMaxU(), icon.getMinV());
        worldrenderer.addVertexWithUV(-(width / 2), -(height / 2),  (length / 2), icon.getMinU(), icon.getMinV());

        // SOUTH
        worldrenderer.addVertexWithUV(-(width / 2), -(height / 2), -(length / 2), icon.getMinU(), icon.getMinV());
        worldrenderer.addVertexWithUV( (width / 2), -(height / 2), -(length / 2), icon.getMaxU(), icon.getMinV());
        worldrenderer.addVertexWithUV( (width / 2),  (height / 2), -(length / 2), icon.getMaxU(), icon.getMaxV());
        worldrenderer.addVertexWithUV(-(width / 2),  (height / 2), -(length / 2), icon.getMinU(), icon.getMaxV());

        // EAST
        worldrenderer.addVertexWithUV(-(width / 2),  (height / 2), -(length / 2), icon.getMinU(), icon.getMaxV());
        worldrenderer.addVertexWithUV(-(width / 2),  (height / 2),  (length / 2), icon.getMaxU(), icon.getMaxV());
        worldrenderer.addVertexWithUV(-(width / 2), -(height / 2),  (length / 2), icon.getMaxU(), icon.getMinV());
        worldrenderer.addVertexWithUV(-(width / 2), -(height / 2), -(length / 2), icon.getMinU(), icon.getMinV());

        // WEST
        worldrenderer.addVertexWithUV( (width / 2), -(height / 2), -(length / 2), icon.getMinU(), icon.getMinV());
        worldrenderer.addVertexWithUV( (width / 2), -(height / 2),  (length / 2), icon.getMaxU(), icon.getMinV());
        worldrenderer.addVertexWithUV( (width / 2),  (height / 2),  (length / 2), icon.getMaxU(), icon.getMaxV());
        worldrenderer.addVertexWithUV( (width / 2),  (height / 2), -(length / 2), icon.getMinU(), icon.getMaxV());

        Tessellator.getInstance().draw();
    }
}
