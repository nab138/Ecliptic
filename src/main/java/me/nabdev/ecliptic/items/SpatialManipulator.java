package me.nabdev.ecliptic.items;

import com.badlogic.gdx.graphics.Color;
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
import me.nabdev.ecliptic.utils.DataTagUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static me.nabdev.ecliptic.utils.ChatHelper.blockPosToString;
import static me.nabdev.ecliptic.utils.ChatHelper.sendMsg;
import static me.nabdev.ecliptic.utils.ControlUtils.ctrlPressed;

public class SpatialManipulator implements IModItem {
    DataTagManifest tagManifest = new DataTagManifest();
    public final static Identifier id = Identifier.of(Constants.MOD_ID, "spatial_manipulator");
    public final DataTagUtils dataTag = new DataTagUtils(this);

    public enum Mode {
        FILL(2, true, new Color(0.9098039216f, 0.2745098039f, 0.2745098039f, 1), new Color(0.7882352941f, 0, 0, 0.25f)),
        REPLACE(2, true, new Color(1, 0.7450980392f, 0.3607843137f, 1), new Color(1, 0.6f, 0, 0.25f)),
        COPY(2, false, new Color(0.5960784314f, 0.2588235294f, 0.9607843137f, 1), new Color(0.431372549f, 0, 0.8784313725f, 0.25f)),
        PASTE(1, false, new Color(0.2352941176f, 0.2352941176f, 1, 1), new Color(0, 0, 1, 0.25f));

        private final int requiredSelections;
        private final boolean requiresMaterial;
        private final Color borderColor;
        private final Color fillColor;

        Mode(int requiredSelections, boolean requiresMaterial, Color borderColor, Color fillColor) {
            this.requiredSelections = requiredSelections;
            this.requiresMaterial = requiresMaterial;
            this.borderColor = borderColor;
            this.fillColor = fillColor;
        }

        public int getRequiredSelections() {
            return requiredSelections;
        }

        public boolean requiresMaterial() {
            return requiresMaterial;
        }

        public Color getBorderColor() {
            return borderColor;
        }

        public Color getFillColor() {
            return fillColor;
        }
    }

    public static class SpatialManipulatorAction implements Action {
        public final Mode mode;
        public final BlockState material;
        public final BoundingBox boundingBox;
        public AtomicReference<BlockState[][][]> oldBlocks = new AtomicReference<>();
        public BlockState replaceBlock;
        public BlockState[][][] clipboard;

        public SpatialManipulatorAction(Mode mode, BlockState material, BoundingBox boundingBox, BlockState[][][] clipboard, BlockState replaceBlock){
            this.mode = mode;
            this.material = material;
            this.boundingBox = new BoundingBox(boundingBox);
            if(mode == Mode.PASTE) this.clipboard = clipboard;
            if(mode == Mode.REPLACE){
                this.replaceBlock = replaceBlock;
            }
        }

        public SpatialManipulatorAction(Mode mode, BlockState material, BoundingBox boundingBox, BlockState[][][] clipboard){
            this.mode = mode;
            this.material = material;
            this.boundingBox = new BoundingBox(boundingBox);
            if(mode == Mode.PASTE) this.clipboard = clipboard;
            if(mode == Mode.REPLACE){
                replaceBlock = BlockSelectionUtil.getBlockLookingAt();
                if(replaceBlock == null){
                    replaceBlock = Block.AIR.getDefaultBlockState();
                }
            }
        }

        public SpatialManipulatorAction(Mode mode, BlockState material, BoundingBox boundingBox){
            if(mode == Mode.PASTE){
                throw new IllegalArgumentException("Cannot create a paste action without clipboard");
            }
            this.mode = mode;
            this.material = material;
            this.boundingBox = new BoundingBox(boundingBox);
            if(mode == Mode.REPLACE){
                replaceBlock = BlockSelectionUtil.getBlockLookingAt();
                if(replaceBlock == null){
                    replaceBlock = Block.AIR.getDefaultBlockState();
                }
            }
        }

        @Override
        public void apply(Zone zone, boolean verbose){
            if(isRunning.get()){
                sendMsg("Please wait until the current action is completed before starting a new one");
                return;
            }
            switch (mode) {
                case FILL:
                    FillingThread.post(oldBlocks, zone, material, boundingBox, null, (t, n) -> {
                        if(verbose) sendMsg("Filled " + n + " block(s) with " + material.getName() + " in " + nanoToSec(t) + "s");
                    });
                    break;
                case REPLACE:
                    FillingThread.post(oldBlocks, zone, material, boundingBox, block -> block.getSaveKey().equals(replaceBlock.getSaveKey()), (t, n) -> {
                        if(verbose) sendMsg("Replaced " + n + " " + replaceBlock.getName() + "(s) with " + material.getName() + " in " + nanoToSec(t) + "s");
                    });
                    break;
                case PASTE:
                    if(this.clipboard == null){
                        sendMsg("Nothing in clipboard to paste");
                        return;
                    }
                    FillingThread.post(oldBlocks, zone, material, boundingBox, block -> false, null);
                    FillingThread.post(zone, this.clipboard, boundingBox, () -> sendMsg("Pasted " + (boundingBox.getWidth() + 1) * (boundingBox.getHeight() + 1) * (boundingBox.getDepth() + 1) + " block(s) from clipboard"), true);
                    break;
                default:
                    sendMsg("Not implemented: " + mode);
            }
        }

        @Override
        public void apply(Zone zone){
            apply(zone, false);
        }

        @Override
        public void undo (Zone zone, Runnable after, Runnable ifFailed){
            if(isRunning.get() || oldBlocks.get() == null){
                sendMsg("Please wait until the current action is completed before undoing it");
                if(ifFailed != null) ifFailed.run();
                return;
            }
            switch (mode) {
                case FILL, REPLACE, PASTE:
                    FillingThread.post(zone, oldBlocks.get(), boundingBox, after, true);
                    break;
                default:
                    sendMsg("Undo not implemented for mode " + mode);
            }
        }

        @Override
        public String getName() {
            return mode.toString();
        }
    }

    public final static float eps = 0.01f;

    public static final AtomicReference<BlockState[][][]> clipboard = new AtomicReference<>();
    public static AtomicBoolean clipboardNeedsRemeshing = new AtomicBoolean(true);
    public static final AtomicBoolean isRunning = new AtomicBoolean(false);

    public SpatialManipulator() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "red_manipulator.png"), Identifier.of(Constants.MOD_ID, "orange_manipulator.png"), Identifier.of(Constants.MOD_ID, "purple_manipulator.png"), Identifier.of(Constants.MOD_ID, "blue_manipulator.png"));
    }

    @Override
    public void clientUse(ItemSlot slot, Player player, BlockPosition targetPlaceBlockPos, BlockPosition targetBreakBlockPos, boolean leftClick) {
        Mode mode = getMode(slot.itemStack);
        BlockState selectedMaterial = dataTag.getSelectedMaterial(slot.itemStack);
        BlockPosition pos1 = dataTag.getPosition(player.getZone(), slot.itemStack, "pos1");
        BlockPosition pos2 = dataTag.getPosition(player.getZone(), slot.itemStack, "pos2");
        BoundingBox boundingBox = getBoundingBox(mode, pos1, pos2, false);

        if(!leftClick){
            if(ctrlPressed) {
                BlockState block = BlockSelectionUtil.getBlockLookingAt();
                if (block != null) {
                    dataTag.setSelectedMaterial(slot.itemStack, block);
                    sendMsg("Selected material: " + block.getName());
                } else {
                    dataTag.setSelectedMaterial(slot.itemStack, Block.AIR.getDefaultBlockState());
                    sendMsg("Selected material: Air");
                }
                return;
            }
            select(player.isSneakIntended, mode, slot.itemStack, BlockSelectionUtil.getBlockPositionLookingAt());
            return;
        }
        if(!player.isSneakIntended){
            setMode(slot.itemStack, Mode.values()[(mode.ordinal() + 1) % Mode.values().length]);
            mode = getMode(slot.itemStack);
            sendMsg("Mode set to " + mode);
            return;
        }
        if((mode.getRequiredSelections() == 1 && pos1 == null) || (mode.getRequiredSelections() == 2 && (pos1 == null || pos2 == null))){
            sendMsg("Please select " + mode.getRequiredSelections() + " position" + (mode.getRequiredSelections() == 1 ? "" : "s") + " before using the spatial manipulator in mode " + mode);
            return;
        }
        if(mode.requiresMaterial() && selectedMaterial == null){
            sendMsg("Please select a material before using the spatial manipulator in mode " + mode);
            return;
        }
        if (mode == Mode.COPY) {
            FillingThread.post(clipboard, player.getZone(), selectedMaterial, boundingBox, (BlockState b) -> false, (t, n) -> {
                sendMsg("Copied " + (int) ((boundingBox.getWidth() + 1) * (boundingBox.getHeight() + 1) * (boundingBox.getDepth() + 1)) + " block(s) to clipboard");
                clipboardNeedsRemeshing.set(true);
            });
            return;
        }
        if(mode == Mode.PASTE && clipboard.get() == null){
            sendMsg("Nothing in clipboard to paste");
            return;
        }
        SpatialManipulatorAction actionToApply = new SpatialManipulatorAction(mode, selectedMaterial, boundingBox, clipboard.get());
        actionToApply.apply(player.getZone(), true);
        TemporalManipulator.addToUndoStack(actionToApply);
    }

    private void select(boolean second, Mode mode, ItemStack stack, BlockPosition pos){
        if(pos == null) {
            return;
        }
        if(mode == Mode.PASTE && second){
            sendMsg("Cannot set second position in paste mode");
            return;
        }
        if(second) setPosition(stack, "pos2", pos.copy());
        else setPosition(stack, "pos1", pos.copy());

        sendMsg("Pos " + (second ? 2 : 1) + " set to " + blockPosToString(pos));
    }

    private BoundingBox getBoundingBox(Mode mode, BlockPosition pos1, BlockPosition pos2, boolean visual){
        if(pos1 == null || (pos2 == null && mode.getRequiredSelections() != 1)){
            return null;
        }
        BlockState[][][] clipboard = SpatialManipulator.clipboard.get();
        if(mode == Mode.PASTE && clipboard != null){
            Vector3 pos1v = blockPositionToVector3(pos1);
            Vector3 pos2v = new Vector3(clipboard.length, clipboard[0].length, clipboard[0][0].length).add(pos1v).sub(1, 1, 1);
            return getBoundingBoxFromCorners(visual, pos1v, pos2v);
        }
        if(mode.getRequiredSelections() == 1){
            Vector3 posV = blockPositionToVector3(pos1);
            if(posV == null) return null;
            if(visual) return new BoundingBox(posV.cpy().add(-eps, -eps, -eps), posV.cpy().add(1 + eps, 1 + eps, 1 + eps));
            return new BoundingBox(posV, posV);
        }

        Vector3 pos1v = blockPositionToVector3(pos1);
        Vector3 pos2v = blockPositionToVector3(pos2);
        return getBoundingBoxFromCorners(visual, pos1v, pos2v);
    }

    public BoundingBox getVisualBoundingBox(ItemStack stack, Zone zone){
        Mode mode = getMode(stack);
        BlockPosition pos1 = dataTag.getPosition(zone, stack, "pos1");
        BlockPosition pos2 = dataTag.getPosition(zone, stack, "pos2");
        return getBoundingBox(mode, pos1, pos2, true);
    }

    private BoundingBox getBoundingBoxFromCorners(boolean visual, Vector3 pos1v, Vector3 pos2v) {
        if(pos1v == null || pos2v == null) return null;
        Vector3 min = new Vector3(Math.min(pos1v.x, pos2v.x), Math.min(pos1v.y, pos2v.y), Math.min(pos1v.z, pos2v.z));
        Vector3 max = new Vector3(Math.max(pos1v.x, pos2v.x), Math.max(pos1v.y, pos2v.y), Math.max(pos1v.z, pos2v.z));
        if(visual) return new BoundingBox(min.cpy().add(-eps, -eps, -eps), max.cpy().add(1 + eps, 1 + eps, 1 + eps));
        return new BoundingBox(min, max);
    }

    public static Vector3 blockPositionToVector3(BlockPosition pos) {
        if(pos.chunk == null) return null;
        return new Vector3((float)pos.getGlobalX(), (float)pos.getGlobalY(), (float)pos.getGlobalZ());
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
        return "Spatial Manipulator";
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

    private void setMode(ItemStack stack, Mode mode){
        dataTag.setString(stack, "mode", mode.toString());
        setCurrentEntry(stack, mode.ordinal());
    }

    public Mode getMode(ItemStack stack){
        String rawMode = dataTag.getString(stack, "mode");
        if(rawMode == null) return Mode.FILL;
        return Mode.valueOf(rawMode);
    }

    private void setPosition(ItemStack stack, String pos, BlockPosition position) {
        dataTag.setPosition(stack, pos, position);
        if(pos.equals("pos1")) clipboardNeedsRemeshing.set(true);
    }

    public static String nanoToSec(long nano) {
        return String.format("%.2f", nano / 1e+9);
    }
}