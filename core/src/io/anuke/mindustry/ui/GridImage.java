package io.anuke.mindustry.ui;

import io.anuke.arc.graphics.g2d.Batch;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.graphics.Draw;
import io.anuke.arc.scene.Element;

public class GridImage extends Element{
    private int imageWidth, imageHeight;

    public GridImage(int w, int h){
        this.imageWidth = w;
        this.imageHeight = h;
    }

    public void draw(Batch batch, float alpha){
        TextureRegion blank = Core.atlas.find("white");

        float xspace = (getWidth() / imageWidth);
        float yspace = (getHeight() / imageHeight);
        float s = 1f;

        int minspace = 10;

        int jumpx = (int) (Math.max(minspace, xspace) / xspace);
        int jumpy = (int) (Math.max(minspace, yspace) / yspace);

        for(int x = 0; x <= imageWidth; x += jumpx){
            batch.draw(blank, (int) (getX() + xspace * x - s), getY() - s, 2, getHeight() + (x == imageWidth ? 1 : 0));
        }

        for(int y = 0; y <= imageHeight; y += jumpy){
            batch.draw(blank, getX() - s, (int) (getY() + y * yspace - s), getWidth(), 2);
        }
    }

    public void setImageSize(int w, int h){
        this.imageWidth = w;
        this.imageHeight = h;
    }
}
