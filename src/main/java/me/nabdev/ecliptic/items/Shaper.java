package me.nabdev.ecliptic.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.BlockSelectionUtil;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.util.Identifier;
import me.nabdev.ecliptic.Constants;

public class Shaper implements IModItem {

    DataTagManifest tagManifest = new DataTagManifest();
    static Identifier id = Identifier.of(Constants.MOD_ID, "shaper");

    public BlockPosition pos1;
    public BlockPosition pos2;

    public Shaper() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "shaper.png"));
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean isTool() {
        return true;
    }

    @Override
    public float getEffectiveBreakingSpeed(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean isEffectiveBreaking(ItemStack itemStack, BlockState blockState) {
        return true;
    }

    @Override
    public String getName() {
        return "Shaper";
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public DataTagManifest getTagManifest() {
        return tagManifest;
    }

    @Override
    public boolean isCatalogHidden() {
        return false;
    }

    @Override
    public void use(ItemSlot slot, Player player) {
        System.out.println("Shaper used");
        BlockPosition pos = BlockSelectionUtil.getBlockPositionLookingAt();
        if(pos1 == null) {
            pos1 = pos;
        } else if(pos2 == null) {
            pos2 = pos;
        } else {
            pos1 = pos;
            pos2 = null;
        }
    }
}