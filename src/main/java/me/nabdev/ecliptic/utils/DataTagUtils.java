package me.nabdev.ecliptic.utils;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTag;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.items.data.attributes.BooleanDataAttribute;
import com.github.puzzle.game.items.data.attributes.IntDataAttribute;
import com.github.puzzle.game.items.data.attributes.StringDataAttribute;
import com.github.puzzle.game.util.DataTagUtil;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.world.Zone;

public class DataTagUtils {
    private final IModItem item;
    public DataTagUtils(IModItem item){
        this.item = item;
    }
    public String getString(ItemStack stack, String key){
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if(manifest == null) return null;
        if(!manifest.hasTag(key)) return null;
        return (String) manifest.getTag(key).getValue();
    }

    public void setString(ItemStack stack, String key, String mode){
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if(manifest == null) manifest = new DataTagManifest();
        manifest.addTag(new DataTag<>(key, new StringDataAttribute(mode)));
    }

    public BlockState getSelectedMaterial(ItemStack stack){
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if(manifest == null) return null;
        if(!manifest.hasTag("selectedMaterial")) return null;
        return BlockState.getInstance((String) manifest.getTag("selectedMaterial").getValue(), MissingBlockStateResult.MISSING_OBJECT);
    }

    public BlockState getSelectedMaterial(ItemStack stack, BlockState defaultMaterial){
        BlockState material = getSelectedMaterial(stack);
        if(material == null) return defaultMaterial;
        return material;
    }


    public void setSelectedMaterial(ItemStack stack, BlockState material){
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if(manifest == null) manifest = new DataTagManifest();
        manifest.addTag(new DataTag<>("selectedMaterial", new StringDataAttribute(material.getSaveKey())));
    }

    public Vec3Int getPosition(ItemStack stack, String pos){
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if (manifest == null) return null;
        if(!item.hasIntProperty(stack, pos + "X")) return null;
        if(!item.hasIntProperty(stack, pos + "Y")) return null;
        if(!item.hasIntProperty(stack, pos + "Z")) return null;

        int x = item.getIntProperty(stack, pos + "X", -1);
        int y = item.getIntProperty(stack, pos + "Y", -1);
        int z = item.getIntProperty(stack, pos + "Z", -1);

        return new Vec3Int(x, y, z);
    }

    public BlockPosition getPosition(Zone zone, ItemStack stack, String pos){
        Vec3Int posv = getPosition(stack, pos);
        if(posv == null) return null;
        return BlockPosition.ofGlobal(zone, posv.x, posv.y, posv.z);
    }

    public void setPosition(ItemStack stack, String pos, BlockPosition position) {
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if (manifest == null) manifest = new DataTagManifest();
        manifest.addTag(new DataTag<>(pos + "X", new IntDataAttribute(position.getGlobalX())));
        manifest.addTag(new DataTag<>(pos + "Y", new IntDataAttribute(position.getGlobalY())));
        manifest.addTag(new DataTag<>(pos + "Z", new IntDataAttribute(position.getGlobalZ())));
    }

    public void setInt(ItemStack stack, String key, int value){
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if(manifest == null) manifest = new DataTagManifest();
        manifest.addTag(new DataTag<>(key, new IntDataAttribute(value)));
    }

    public void setBoolean(ItemStack stack, String key, boolean value){
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if(manifest == null) manifest = new DataTagManifest();
        manifest.addTag(new DataTag<>(key, new BooleanDataAttribute(value)));
    }

    public boolean getBoolean(ItemStack stack, String key, boolean defaultVal){
        DataTagManifest manifest = DataTagUtil.getManifestFromStack(stack);
        if(manifest == null) return defaultVal;
        if(!manifest.hasTag(key)) return defaultVal;
        return (boolean) manifest.getTag(key).getValue();
    }

    public boolean getBoolean(ItemStack stack, String key){
        return getBoolean(stack, key, false);
    }
}
