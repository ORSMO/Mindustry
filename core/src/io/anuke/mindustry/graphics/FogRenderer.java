package io.anuke.mindustry.graphics;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.GL20;
import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.graphics.glutils.FrameBuffer;
import io.anuke.arc.collection.Array;
import io.anuke.arc.util.Disposable;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.game.EventType.TileChangeEvent;
import io.anuke.mindustry.game.EventType.WorldLoadGraphicsEvent;
import io.anuke.mindustry.world.Tile;
import io.anuke.arc.Events;
import io.anuke.arc.Graphics;
import io.anuke.arc.entities.EntityDraw;
import io.anuke.arc.graphics.Draw;
import io.anuke.arc.graphics.Fill;

import java.nio.ByteBuffer;

import static io.anuke.mindustry.Vars.*;

/**Used for rendering fog of war. A framebuffer is used for this.*/
public class FogRenderer implements Disposable{
    private TextureRegion region = new TextureRegion();
    private FrameBuffer buffer;
    private ByteBuffer pixelBuffer;
    private Array<Tile> changeQueue = new Array<>();
    private int shadowPadding;
    private boolean dirty;

    public FogRenderer(){
        Events.on(WorldLoadGraphicsEvent.class, event -> {
            dispose();

            shadowPadding = -1;

            buffer = new FrameBuffer(Format.RGBA8888, world.width(), world.height(), false);
            changeQueue.clear();

            //clear buffer to black
            buffer.begin();
            Graphics.clear(0, 0, 0, 1f);
            buffer.end();

            for(int x = 0; x < world.width(); x++){
                for(int y = 0; y < world.height(); y++){
                    Tile tile = world.tile(x, y);
                    if(tile.getTeam() == players[0].getTeam() && tile.block().synthetic() && tile.block().viewRange > 0){
                        changeQueue.add(tile);
                    }
                }
            }

            pixelBuffer = ByteBuffer.allocateDirect(world.width() * world.height() * 4);
            dirty = true;
        });

        Events.on(TileChangeEvent.class, event -> threads.runGraphics(() -> {
            if(event.tile.getTeam() == players[0].getTeam() && event.tile.block().synthetic() && event.tile.block().viewRange > 0){
                changeQueue.add(event.tile);
            }
        }));
    }

    public void writeFog(){
        if(buffer == null) return;

        buffer.begin();
        pixelBuffer.position(0);
        Core.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Core.gl.glReadPixels(0, 0, world.width(), world.height(), GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixelBuffer);

        pixelBuffer.position(0);
        for(int i = 0; i < world.width() * world.height(); i++){
            int x = i % world.width();
            int y = i / world.width();
            byte r = pixelBuffer.get();
            if(r != 0){
                world.tile(x, y).setVisibility((byte)1);
            }
            pixelBuffer.position(pixelBuffer.position() + 3);
        }
        buffer.end();
    }

    public int getPadding(){
        return -shadowPadding;
    }

    public void draw(){
        if(buffer == null) return;

        float vw = Core.camera.viewportWidth * Core.camera.zoom;
        float vh = Core.camera.viewportHeight * Core.camera.zoom;

        float px = Core.camera.position.x - vw / 2f;
        float py = Core.camera.position.y - vh / 2f;

        float u = (px / tilesize) / buffer.getWidth();
        float v = (py / tilesize) / buffer.getHeight();

        float u2 = ((px + vw) / tilesize) / buffer.getWidth();
        float v2 = ((py + vh) / tilesize) / buffer.getHeight();

        Core.batch.getProjectionMatrix().setToOrtho2D(0, 0, buffer.getWidth() * tilesize, buffer.getHeight() * tilesize);

        Draw.color(Color.WHITE);

        buffer.begin();

        Graphics.beginClip((-shadowPadding), (-shadowPadding), (world.width() + shadowPadding*2), (world.height() + shadowPadding*2));

        Graphics.begin();
        EntityDraw.setClip(false);

        renderer.drawAndInterpolate(playerGroup, player -> !player.isDead() && player.getTeam() == players[0].getTeam(), Unit::drawView);
        renderer.drawAndInterpolate(unitGroups[players[0].getTeam().ordinal()], unit -> !unit.isDead(), Unit::drawView);

        for(Tile tile : changeQueue){
            float viewRange = tile.block().viewRange;
            if(viewRange < 0) continue;
            Fill.circle(tile.drawx(), tile.drawy(), tile.block().viewRange);
        }

        changeQueue.clear();

        if(dirty){
            for(int x = 0; x < world.width(); x++){
                for(int y = 0; y < world.height(); y++){
                    Tile tile = world.tile(x, y);
                    if(tile.discovered()){
                        Fill.rect(tile.worldx(), tile.worldy(), tilesize, tilesize);
                    }
                }
            }
            dirty = false;
        }

        EntityDraw.setClip(true);
        Graphics.end();
        buffer.end();

        Graphics.endClip();

        region.setTexture(buffer.getColorBufferTexture());
        region.setRegion(u, v2, u2, v);

        Core.batch.setProjectionMatrix(Core.camera.combined);
        Graphics.shader(Shaders.fog);
        renderer.pixelSurface.getBuffer().begin();
        Graphics.begin();

        Core.batch.draw(region, px, py, vw, vh);

        Graphics.end();
        renderer.pixelSurface.getBuffer().end();
        Graphics.shader();

        Graphics.setScreen();
        Core.batch.draw(renderer.pixelSurface.texture(), 0, Core.graphics.getHeight(), Core.graphics.getWidth(), -Core.graphics.getHeight());
        Graphics.end();
    }

    public Texture getTexture(){
        return buffer.getColorBufferTexture();
    }

    @Override
    public void dispose(){
        if(buffer != null) buffer.dispose();
    }
}
