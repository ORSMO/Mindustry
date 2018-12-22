package io.anuke.mindustry.world;

import io.anuke.arc.util.NumberUtils;
import io.anuke.mindustry.type.Item;
import io.anuke.arc.Timers;
import io.anuke.arc.util.Bits;
import static io.anuke.mindustry.Vars.*;

public class ItemBuffer{
    private final float speed;

    private long[] buffer;
    private int index;

    public ItemBuffer(int capacity, float speed){
        this.buffer = new long[capacity];
        this.speed = speed;
    }

    public boolean accepts(){
        return index < buffer.length;
    }

    public void accept(Item item, short data){
        //if(!accepts()) return;
        buffer[index++] = Bits.packLong(NumberUtils.floatToIntBits(Time.time()), Bits.packInt((short) item.id, data));
    }

    public void accept(Item item){
        accept(item, (short) -1);
    }

    public Item poll(){
        if(index > 0){
            long l = buffer[0];
            float time = NumberUtils.intBitsToFloat(Bits.getLeftInt(l));

            if(Time.time() >= time + speed || Time.time() < time){
                return content.item(Bits.getLeftShort(Bits.getRightInt(l)));
            }
        }
        return null;
    }

    public short pollData(){
        if(index > 0){
            long l = buffer[0];
            float time = NumberUtils.intBitsToFloat(Bits.getLeftInt(l));

            if(Time.time() >= time + speed || Time.time() < time){
                return Bits.getRightShort(Bits.getRightInt(l));
            }
        }
        return -1;
    }

    public void remove(){
        System.arraycopy(buffer, 1, buffer, 0, index - 1);
        index--;
    }
}
