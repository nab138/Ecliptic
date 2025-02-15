package me.nabdev.ecliptic.godmode;

import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.util.Identifier;
import me.nabdev.ecliptic.Constants;

public class GodModeItem implements IModItem {
    DataTagManifest tagManifest = new DataTagManifest();
    public final static Identifier id = Identifier.of(Constants.MOD_ID, "god_mode");

    public static boolean godMode = false;
    private static final Vector3 originalPos = new Vector3();
    private static boolean wasNoClip = false;

    public GodModeItem() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "godmode.png"));
    }

    @Override
    public void clientUse(ItemSlot slot, Player p, BlockPosition p1, BlockPosition p2) {
        if (!godMode) {
            originalPos.set(p.getPosition());
            wasNoClip = p.getEntity().noClip;
        }

        godMode = true;
    }

    public static void turnOff(Player p) {
        p.getEntity().setPosition(originalPos);
        p.getEntity().noClip = wasNoClip;
        godMode = false;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean isTool() {
        return false;
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
        return "God Mode";
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
    public boolean canTargetBlockForBreaking(BlockState blockState) {
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canMergeWith(Item item) {
        return false;
    }

    @Override
    public boolean canMergeWithSwapGroup(Item item) {
        return false;
    }
}
