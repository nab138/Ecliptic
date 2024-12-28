package me.nabdev.ecliptic.items;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.BlockSelectionUtil;
import com.github.puzzle.game.util.BlockUtil;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.ecliptic.Constants;

import java.util.function.Function;

import static me.nabdev.ecliptic.utils.ChatHelper.blockPosToString;
import static me.nabdev.ecliptic.utils.ChatHelper.sendMsg;

public class Shaper implements IModItem {
    DataTagManifest tagManifest = new DataTagManifest();
    public static Identifier id = Identifier.of(Constants.MOD_ID, "shaper");

    public enum Mode {
        FILL(2, true),
        REPLACE(2, true),
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

    public class Action{
        public final Mode mode;
        public final BlockState material;
        public final BoundingBox boundingBox;
        // store the old blocks for undo
        public BlockState[][][] oldBlocks;
        public BlockState replaceBlock;
        public BlockState[][][] clipboard;

        public Action(Mode mode, BlockState material, BoundingBox boundingBox, BlockState[][][] clipboard){
            this.mode = mode;
            this.material = material;
            if(mode != Mode.PASTE) {
                this.boundingBox = new BoundingBox(boundingBox);
            } else{
                this.clipboard = clipboard;
                Vector3 pos1v = blockPositionToVector3(pos1);
                Vector3 pos2v = new Vector3(clipboard.length, clipboard[0].length, clipboard[0][0].length).add(pos1v).sub(1, 1, 1);
                this.boundingBox = new BoundingBox(pos1v, pos2v);
            }
            if(mode == Mode.REPLACE){
                replaceBlock = BlockSelectionUtil.getBlockLookingAt();
                if(replaceBlock == null){
                    replaceBlock = Block.AIR.getDefaultBlockState();
                }
            }
        }

        public void apply(Zone zone, boolean verbose){
            switch (mode) {
                case FILL:
                    oldBlocks = fill(zone, material, boundingBox, null);
                    if(verbose) sendMsg("Filled with " + selectedMaterial.getName());
                    break;
                case REPLACE:
                    oldBlocks = fill(zone, material, boundingBox, block -> block.getSaveKey().equals(replaceBlock.getSaveKey()));
                    if(verbose) sendMsg("Replaced " + replaceBlock.getName() + " with " + selectedMaterial.getName());
                    break;
                case PASTE:
                    if(this.clipboard == null){
                        sendMsg("Nothing in clipboard to paste");
                        return;
                    }
                    oldBlocks = fill(zone, selectedMaterial, boundingBox, (BlockState b) -> false);
                    fill(zone, this.clipboard, boundingBox);
                    sendMsg("Pasted " + boundingBox.getWidth() * boundingBox.getHeight() * boundingBox.getDepth() + " block(s) from clipboard");
                    break;
                default:
                    sendMsg("Not implemented: " + mode);
            }
        }

        public void apply(Zone zone){
            apply(zone, false);
        }

        public void undo (Zone zone){
            switch (mode) {
                case FILL, REPLACE, PASTE:
                    fill(zone, oldBlocks, boundingBox);
                    break;
                default:
                    sendMsg("Undo not implemented for mode " + mode);
            }
        }
    }

    public BlockPosition pos1;
    public BlockPosition pos2;
    public BoundingBox visualBoundingBox;
    public BoundingBox boundingBox;
    public BlockState selectedMaterial = null;

    private final static float eps = 0.01f;

    Mode mode = Mode.FILL;

    private BlockState[][][] clipboard;

    public Shaper() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "shaper.png"));
    }

    @Override
    public void use(ItemSlot slot, Player player, boolean leftClick) {
        if(leftClick){
            if(player.isSneakIntended) {
                BlockState block = BlockSelectionUtil.getBlockLookingAt();
                if (block != null) {
                    selectedMaterial = block;
                    sendMsg("Selected material: " + block.getName());
                } else {
                    selectedMaterial = Block.AIR.getDefaultBlockState();
                    sendMsg("Selected material: Air");
                }
                return;
            }
            select();
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
        if (mode == Mode.COPY) {
            clipboard = fill(player.getZone(), selectedMaterial, boundingBox, (BlockState b) -> false);
            sendMsg("Copied " + (int) ((boundingBox.getWidth() + 1) * (boundingBox.getHeight() + 1) * (boundingBox.getDepth() + 1)) + " block(s) to clipboard");
            return;
        }
        Action actionToApply = new Action(mode, selectedMaterial, boundingBox, clipboard);
        actionToApply.apply(player.getZone(), true);
        TemporalManipulator.undoStack.push(actionToApply);
        TemporalManipulator.redoStack.clear();
    }

    private void fill(Zone zone, BlockState[][][] blocks, BoundingBox boundingBox) {
        int minX = (int) Math.floor(boundingBox.min.x);
        int minY = (int) Math.floor(boundingBox.min.y);
        int minZ = (int) Math.floor(boundingBox.min.z);
        int maxX = (int) Math.ceil(boundingBox.max.x);
        int maxY = (int) Math.ceil(boundingBox.max.y);
        int maxZ = (int) Math.ceil(boundingBox.max.z);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockState block = blocks[x - minX][y - minY][z - minZ];
                    if(block == null){
                        block = Block.AIR.getDefaultBlockState();
                    }
                    BlockUtil.setBlockAt(zone, block, x, y, z);
                }
            }
        }
    }

    private BlockState[][][] fill(Zone zone, BlockState block, BoundingBox boundingBox, Function<BlockState, Boolean> filter) {
        int minX = (int) Math.floor(boundingBox.min.x);
        int minY = (int) Math.floor(boundingBox.min.y);
        int minZ = (int) Math.floor(boundingBox.min.z);
        int maxX = (int) Math.ceil(boundingBox.max.x);
        int maxY = (int) Math.ceil(boundingBox.max.y);
        int maxZ = (int) Math.ceil(boundingBox.max.z);

        BlockState[][][] oldBlocks = new BlockState[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    oldBlocks[x - minX][y - minY][z - minZ] = BlockUtil.getBlockPosAtVec(zone, x, y, z).getBlockState();
                    if(filter == null || filter.apply(BlockUtil.getBlockPosAtVec(zone, x, y, z).getBlockState()))BlockUtil.setBlockAt(zone, block, x, y, z);
                }
            }
        }
        return oldBlocks;
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