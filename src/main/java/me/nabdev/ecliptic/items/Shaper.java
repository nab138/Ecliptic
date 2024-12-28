package me.nabdev.ecliptic.items;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.BlockSelectionUtil;
import com.github.puzzle.game.util.BlockUtil;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.ecliptic.Constants;

import static me.nabdev.ecliptic.utils.ChatHelper.blockPosToString;
import static me.nabdev.ecliptic.utils.ChatHelper.sendMsg;

public class Shaper implements IModItem {
    public enum Mode {
        SELECT(-1, false),
        FILL(2, true),
        MOVE(2, false),
        COPY(2, false),
        PASTE(1, false);

        private final int requiredSelections;
        private final boolean requiresMaterial;

        Mode(int requiredSelections, boolean requiresMaterial) {
            this.requiredSelections = requiredSelections;
            this.requiresMaterial = requiresMaterial;
        }

        public int getRequiredSelections() {
            return requiredSelections;
        }

        public boolean requiresMaterial() {
            return requiresMaterial;
        }
    }

    DataTagManifest tagManifest = new DataTagManifest();
    public static Identifier id = Identifier.of(Constants.MOD_ID, "shaper");

    public BlockPosition pos1;
    public BlockPosition pos2;
    public BoundingBox visualBoundingBox;
    public BoundingBox boundingBox;
    public BlockState selectedMaterial = null;

    private final static float eps = 0.01f;

    Mode mode = Mode.SELECT;

    public Shaper() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "shaper.png"));
    }

    @Override
    public void use(ItemSlot slot, Player player, boolean leftClick) {
        if(leftClick){
            BlockState block = BlockSelectionUtil.getBlockLookingAt();
            if (block != null) {
                selectedMaterial = block;
                sendMsg("Selected material: " + block.getName());
            }
            return;
        }
        if(player.isSneakIntended){
            mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
            updateBoundingBox();
            sendMsg("Mode set to " + mode);
            return;
        }
        if((mode.getRequiredSelections() == 1 && pos1 == null) || (mode.getRequiredSelections() == 2 && (pos1 == null || pos2 == null))){
            sendMsg("Please select " + mode.getRequiredSelections() + " position" + (mode.getRequiredSelections() == 1 ? "" : "s") + " before using the shaper in mode " + mode);
            return;
        }
        if(mode.requiresMaterial() && selectedMaterial == null){
            sendMsg("Please select a material before using the shaper in mode " + mode);
            return;
        }
        switch (mode) {
            case SELECT:
                select();
                break;
            case FILL:
                fill(player.getZone());
                break;
        }
    }

    private void fill(Zone zone){
        for (int x = (int)boundingBox.min.x; x <= boundingBox.max.x; x++) {
            for (int y = (int)boundingBox.min.y; y <= boundingBox.max.y; y++) {
                for (int z = (int)boundingBox.min.z; z <= boundingBox.max.z; z++) {
                    BlockUtil.setBlockAt(zone, selectedMaterial, x, y, z);
                }
            }
        }
        sendMsg("Filled with " + selectedMaterial.getName());
    }

    private void select(){
        BlockPosition pos = BlockSelectionUtil.getBlockPositionLookingAt();
        if(pos == null) {
            return;
        }
        if(pos1 == null || pos2 != null) {
            pos1 = pos.copy();
            pos2 = null;
            updateBoundingBox();
            sendMsg("Pos 1 set to " + blockPosToString(pos1));
        } else {
            pos2 = pos.copy();
            updateBoundingBox();
            sendMsg("Pos 2 set to " + blockPosToString(pos2));
        }
    }

    private void updateBoundingBox(){
        if(pos1 == null || (pos2 == null && mode.getRequiredSelections() != 1)){
            boundingBox = null;
            visualBoundingBox = null;
            return;
        }
        if(mode.getRequiredSelections() == 1){
            Vector3 posv = blockPositionToVector3(pos1);
            boundingBox = new BoundingBox(posv, posv);
            visualBoundingBox = new BoundingBox(posv.cpy().add(-eps, -eps, -eps), posv.cpy().add(1 + eps, 1 + eps, 1 + eps));
            return;
        }
        Vector3 pos1v = blockPositionToVector3(pos1);
        Vector3 pos2v = blockPositionToVector3(pos2);
        Vector3 min = new Vector3(Math.min(pos1v.x, pos2v.x), Math.min(pos1v.y, pos2v.y), Math.min(pos1v.z, pos2v.z));
        Vector3 max = new Vector3(Math.max(pos1v.x, pos2v.x), Math.max(pos1v.y, pos2v.y), Math.max(pos1v.z, pos2v.z));
        boundingBox = new BoundingBox(min, max);
        visualBoundingBox = new BoundingBox(min.cpy().add(-eps, -eps, -eps), max.cpy().add(1 + eps, 1 + eps, 1 + eps));

    }

    public static Vector3 blockPositionToVector3(BlockPosition pos) {
        return new Vector3((float)pos.getGlobalX(), (float)pos.getGlobalY(), (float)pos.getGlobalZ());
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
    public boolean canTargetBlockForBreaking(BlockState blockState) {
        return false;
    }
}