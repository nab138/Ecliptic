package me.nabdev.ecliptic.items;

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
import me.nabdev.ecliptic.utils.Action;

import java.util.Stack;

import static me.nabdev.ecliptic.utils.ChatHelper.sendMsg;

public class TemporalManipulator implements IModItem {
    DataTagManifest tagManifest = new DataTagManifest();
    public final static Identifier id = Identifier.of(Constants.MOD_ID, "temporal_manipulator");
    private final static int MAX_UNDO_REDO = 50;

    public TemporalManipulator() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "temporal_manipulator.png"));
    }

    private final static Stack<Action> undoStack = new Stack<>();
    private final static Stack<Action> redoStack = new Stack<>();

    public static void addToUndoStack(Action action){
        undoStack.push(action);


        if(undoStack.size() > MAX_UNDO_REDO)
            undoStack.remove(0);
        redoStack.clear();
    }

    public static int getUndoLength(){
        return undoStack.size();
    }

    public static int getRedoLength(){
        return redoStack.size();
    }

    @Override
    public void use(ItemSlot slot, Player player, BlockPosition p, BlockPosition p2, boolean leftClick) {
        if(leftClick) undo(player);
        else redo(player);
    }

    public static void undo(Player player){
        if(undoStack.isEmpty()){
            sendMsg("Nothing to undo");
            return;
        }
        Action actionToUndo = undoStack.pop();
        actionToUndo.undo(player.getZone(), () -> {
            redoStack.push(actionToUndo);
            sendMsg("Undid " + actionToUndo.getName());
        }, () -> undoStack.push(actionToUndo));
    }

    public static void redo(Player player){
        if(redoStack.isEmpty()){
            sendMsg("Nothing to redo");
            return;
        }
        Action actionToRedo = redoStack.pop();
        actionToRedo.apply(player.getZone());
        undoStack.push(actionToRedo);
        sendMsg("Redid " + actionToRedo.getName());
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
        return "Temporal Manipulator";
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