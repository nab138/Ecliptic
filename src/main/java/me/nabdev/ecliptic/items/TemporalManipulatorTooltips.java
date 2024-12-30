package me.nabdev.ecliptic.items;

import finalforeach.cosmicreach.items.ItemStack;
import me.nabdev.cosmictooltips.api.ITooltipItem;

public class TemporalManipulatorTooltips implements ITooltipItem {
    @Override
    public String getItemID() {
        return TemporalManipulator.id.toString();
    }

    @Override
    public String getTooltipText(ItemStack itemStack) {
        if (itemStack.getItem() instanceof TemporalManipulator) {
            return "Things to undo: " + TemporalManipulator.getUndoLength() +
                    "\nThings to redo: " + TemporalManipulator.getRedoLength();
        }
        return "If you are seeing this, :(";
    }
}
