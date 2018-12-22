package io.anuke.mindustry.type;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.mindustry.content.Weapons;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.game.UnlockableContent;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.ui.ContentDisplay;
import io.anuke.arc.graphics.Draw;
import io.anuke.arc.scene.ui.layout.Table;
import io.anuke.arc.util.Bundles;

public class Mech extends UnlockableContent{
    public final String name;
    public final String description;

    public boolean flying;
    public float speed = 1.1f;
    public float maxSpeed = 10f;
    public float boostSpeed = 0.75f;
    public float drag = 0.4f;
    public float mass = 1f;
    public float shake = 0f;
    public float armor = 1f;

    public float hitsize = 6f;
    public float cellTrnsY = 0f;
    public float mineSpeed = 1f;
    public int drillPower = -1;
    public float carryWeight = 10f;
    public float buildPower = 1f;
    public Color trailColor = Palette.boostFrom;
    public Color trailColorTo = Palette.boostTo;
    public int itemCapacity = 30;
    public boolean turnCursor = true;
    public boolean canHeal = false;

    public float weaponOffsetX, weaponOffsetY;
    public Weapon weapon = Weapons.blaster;

    public TextureRegion baseRegion, legRegion, region, iconRegion;

    public Mech(String name, boolean flying){
        this.flying = flying;
        this.name = name;
        this.description = Core.bundle.get("mech." + name + ".description");
    }

    public String localizedName(){
        return Core.bundle.get("mech." + name + ".name");
    }

    public void updateAlt(Player player){}

    public void draw(Player player){}

    public float getExtraArmor(Player player){
        return 0f;
    }

    public float spreadX(Player player){
        return 0f;
    }

    public float getRotationAlpha(Player player){return 1f;}

    public boolean canShoot(Player player){
        return true;
    }

    public void onLand(Player player){}

    @Override
    public void displayInfo(Table table){
        ContentDisplay.displayMech(table, this);
    }

    @Override
    public TextureRegion getContentIcon(){
        return iconRegion;
    }

    @Override
    public String getContentName(){
        return name;
    }

    @Override
    public ContentType getContentType(){
        return ContentType.mech;
    }

    @Override
    public void load(){
        if(!flying){
            legRegion = Core.atlas.find(name + "-leg");
            baseRegion = Core.atlas.find(name + "-base");
        }

        region = Core.atlas.find(name);
        iconRegion = Core.atlas.find("mech-icon-" + name);
    }

    @Override
    public String toString(){
        return localizedName();
    }
}
