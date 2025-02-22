package me.nabdev.ecliptic.items;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.BlockSelectionUtil;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.ecliptic.Constants;
import me.nabdev.ecliptic.threading.FillingThread;
import me.nabdev.ecliptic.utils.Action;
import me.nabdev.ecliptic.utils.ControlUtils;
import me.nabdev.ecliptic.utils.DataTagUtils;
import me.nabdev.ecliptic.utils.RaycastUtils;

import java.util.concurrent.atomic.AtomicReference;

import static me.nabdev.ecliptic.items.SpatialManipulator.isRunning;
import static me.nabdev.ecliptic.utils.ChatHelper.sendMsg;
import static me.nabdev.ecliptic.utils.ControlUtils.ctrlPressed;

public class Constructor implements IModItem {
    DataTagManifest tagManifest = new DataTagManifest();
    DataTagUtils dataTag = new DataTagUtils(this);
    public final static Identifier id = Identifier.of(Constants.MOD_ID, "constructor");

    public Constructor() {
        addTexture(IModItem.MODEL_2_5D_ITEM,
                Identifier.of(Constants.MOD_ID, "box_filled_constructor.png"),
                Identifier.of(Constants.MOD_ID, "box_hollow_constructor.png"),
                Identifier.of(Constants.MOD_ID, "sphere_filled_constructor.png"),
                Identifier.of(Constants.MOD_ID, "sphere_hollow_constructor.png"),
                Identifier.of(Constants.MOD_ID, "cylinder_filled_constructor.png"),
                Identifier.of(Constants.MOD_ID, "cylinder_hollow_constructor.png"));
    }

    public enum Mode {
        BOX,
        SPHERE,
        CYLINDER,
    }

    public static class ConstructorAction implements Action {
        private final Mode mode;
        private final PreviewData preview;
        private final AtomicReference<BlockState[][][]> oldBlocksRef = new AtomicReference<>();
        private final BoundingBox box;

        public ConstructorAction(Mode mode, PreviewData preview) {
            this.mode = mode;
            this.preview = preview;
            if(preview == null) {
                box = null;
                return;
            }
            BlockState[][][] blocks = preview.preview;
            Vector3 pos = preview.position;
            box = new BoundingBox(pos, new Vector3(pos.x + blocks.length - 1, pos.y + blocks[0].length - 1, pos.z + blocks[0][0].length - 1));
        }

        @Override
        public void apply(Zone zone, boolean verbose) {
            if(isRunning.get()) {
                sendMsg("Please wait until the current action is completed before starting a new one");
                return;
            }
            if (preview == null || box == null) return;
            BlockState[][][] blocks = preview.preview;
            FillingThread.post(oldBlocksRef, zone, blocks, box, () -> {
                if (verbose) {
                    sendMsg("Filled " + blocks.length * blocks[0].length * blocks[0][0].length + " blocks");
                }
            }, false);
        }

        @Override
        public void apply(Zone zone) {
            apply(zone, false);
        }

        @Override
        public void undo(Zone zone, Runnable after, Runnable ifFailed) {
            if(isRunning.get() || oldBlocksRef.get() == null){
                sendMsg("Please wait until the current action is completed before undoing it");
                if(ifFailed != null) ifFailed.run();
                return;
            }
            BlockState[][][] blocks = oldBlocksRef.get();
            FillingThread.post(zone, blocks, box, after, false);
        }

        @Override
        public String getName() {
            return mode.toString();
        }
    }

    public record PreviewData(BlockState[][][] preview, Vector3 position) {}

    @Override
    public void clientUse(ItemSlot slot, Player player, BlockPosition targetPlaceBlockPos, BlockPosition targetBreakBlockPos, boolean leftClick) {
        Mode mode = getMode(slot.getItemStack());

        if(ControlUtils.xPressed){
            int add = leftClick ? -1 : 1;
            dataTag.setInt(slot.getItemStack(), "xScale", Math.max(getIntProperty(slot.getItemStack(), "xScale", 3) + add, 1));
            return;
        }
        if(ControlUtils.yPressed){
            int add = leftClick ? -1 : 1;
            dataTag.setInt(slot.getItemStack(), "yScale", Math.max(getIntProperty(slot.getItemStack(), "yScale", 3) + add, 1));
            return;
        }
        if(ControlUtils.zPressed){
            int add = leftClick ? -1 : 1;
            dataTag.setInt(slot.getItemStack(), "zScale", Math.max(getIntProperty(slot.getItemStack(), "zScale", 3) + add, 1));
            return;
        }

        if(leftClick){
            if(!player.isSneakIntended){
                setMode(slot.getItemStack(), Mode.values()[(mode.ordinal() + 1) % Mode.values().length]);
                mode = getMode(slot.getItemStack());
                sendMsg("Mode set to " + mode);
                sendMsg("Warning: Only box mode is implemented!");
                return;
            }
            PreviewData preview = getPreview(slot.getItemStack());
            if(preview == null){
                sendMsg("Not implemented yet!");
                return;
            }
            ConstructorAction action = new ConstructorAction(mode, preview);
            action.apply(player.getZone(), true);
            TemporalManipulator.addToUndoStack(action);


        } else {
            if (ctrlPressed) {
                BlockState block = BlockSelectionUtil.getBlockLookingAt();
                if (block != null) {
                    dataTag.setSelectedMaterial(slot.getItemStack(), block);
                    sendMsg("Selected material: " + block.getName());
                } else {
                    dataTag.setSelectedMaterial(slot.getItemStack(), Block.AIR.getDefaultBlockState());
                    sendMsg("Selected material: Air");
                    return;
                }
            }
            toggleFilled(slot.getItemStack());
            sendMsg("Switched to " + (getFilled(slot.getItemStack()) ? "filled" : "hollow") + " mode");
        }
    }

    public PreviewData getPreview(ItemStack stack){
        Mode mode = getMode(stack);
        boolean filled = getFilled(stack);
        return switch (mode) {
            case SPHERE -> filled ? getSphere() : getHollowSphere();
            case CYLINDER -> filled ? getCylinder() : getHollowCylinder();
            case BOX -> filled ? getBox(stack) : getHollowBox(stack);
        };
    }

    private PreviewData getSphere(){
        return null;
    }

    private PreviewData getHollowSphere(){
        return null;
    }

    private PreviewData getCylinder(){
        return null;
    }

    private PreviewData getHollowCylinder(){
        return null;
    }

    private PreviewData getBox(ItemStack stack) {
        BlockPosition pos1 = RaycastUtils.getCurrentLookAt();
        Vector3 normal = RaycastUtils.getCurrentNormal();

        int xSize = getIntProperty(stack, "xScale", 3);
        int ySize = getIntProperty(stack, "yScale", 3);
        int zSize = getIntProperty(stack, "zScale", 3);

        if (pos1 == null) return null;

        Vector3 adjustedPos = new Vector3(pos1.getGlobalX(), pos1.getGlobalY(), pos1.getGlobalZ());

        if (normal.x > 0) {
            adjustedPos.x += 1;
        } else if (normal.x < 0) {
            adjustedPos.x -= xSize;
        } else {
            adjustedPos.x -= (float) Math.floor((xSize - 1) / 2.0f);
        }

        if (normal.y > 0) {
            adjustedPos.y += 1;
        } else if (normal.y < 0) {
            adjustedPos.y -= ySize;
        } else {
            adjustedPos.y -= (float) Math.floor((ySize - 1) / 2.0f);
        }

        if (normal.z > 0) {
            adjustedPos.z += 1;
        } else if (normal.z < 0) {
            adjustedPos.z -= zSize;
        } else {
            adjustedPos.z -= (float) Math.floor((zSize - 1) / 2.0f);
        }

        BlockState[][][] blocks = new BlockState[xSize][ySize][zSize];
        BlockState selectedMaterial = dataTag.getSelectedMaterial(stack);
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                for (int z = 0; z < zSize; z++) {
                    blocks[x][y][z] = selectedMaterial;
                }
            }
        }
        return new PreviewData(blocks, adjustedPos);
    }

    private PreviewData getHollowBox(ItemStack stack){
        PreviewData box = getBox(stack);
        if(box == null) return null;
        BlockState[][][] blocks = box.preview;
        for (int x = 1; x < blocks.length - 1; x++) {
            for (int y = 1; y < blocks[0].length - 1; y++) {
                for (int z = 1; z < blocks[0][0].length - 1; z++) {
                    blocks[x][y][z] = null;
                }
            }
        }
        return new PreviewData(blocks, box.position);
    }

    private void setMode(ItemStack itemStack, Mode mode) {
        dataTag.setString(itemStack, "mode", mode.toString());
        setCurrentEntry(itemStack, mode.ordinal() * 2 + (getFilled(itemStack) ? 0 : 1));
    }

    public Mode getMode(ItemStack itemStack) {
        String rawMode = dataTag.getString(itemStack, "mode");
        if(rawMode == null) return Mode.BOX;
        return Mode.valueOf(rawMode);
    }

    public boolean getFilled(ItemStack itemStack) {
        return dataTag.getBoolean(itemStack, "filled", true);
    }

    public void toggleFilled(ItemStack itemStack) {
        dataTag.setBoolean(itemStack, "filled", !getFilled(itemStack));
        setCurrentEntry(itemStack, getMode(itemStack).ordinal() * 2 + (getFilled(itemStack) ? 0 : 1));
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
        return "Constructor";
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
