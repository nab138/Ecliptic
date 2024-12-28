package me.nabdev.ecliptic.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.util.Identifier;
import me.nabdev.ecliptic.Constants;

import java.util.Stack;

import static me.nabdev.ecliptic.utils.ChatHelper.sendMsg;

public class TemporalManipulator implements IModItem {
    DataTagManifest tagManifest = new DataTagManifest();
    public static Identifier id = Identifier.of(Constants.MOD_ID, "temporal_manipulator");

    public TemporalManipulator() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "temporal_manipulator.png"));
    }

    public final static Stack<Shaper.Action> undoStack = new Stack<>();
    public final static Stack<Shaper.Action> redoStack = new Stack<>();

    @Override
    public void use(ItemSlot slot, Player player, boolean leftClick) {
        if(leftClick){
            if(undoStack.isEmpty()){
                sendMsg("Nothing to undo");
                return;
            }
            Shaper.Action action = undoStack.pop();
            action.undo(player.getZone());
            redoStack.push(action);
            sendMsg("Undid " + action.mode);
            return;
        }


        if(redoStack.isEmpty()){
            sendMsg("Nothing to redo");
            return;
        }
        Shaper.Action actionToRedo = redoStack.pop();
        actionToRedo.apply(player.getZone());
        undoStack.push(actionToRedo);
        sendMsg("Redid " + actionToRedo.mode);
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
}