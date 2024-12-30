package me.nabdev.ecliptic.items;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.items.ItemStack;
import me.nabdev.cosmictooltips.api.ITooltipItem;

public class ConstructorTooltips implements ITooltipItem {
    @Override
    public String getItemID() {
        return Constructor.id.toString();
    }

    @Override
    public String getTooltipText(ItemStack itemStack) {
        if (itemStack.getItem() instanceof Constructor s) {
            BlockState selectedMaterial = s.dataTag.getSelectedMaterial(itemStack);

            return "Current mode: " +
                    s.getMode(itemStack) +
                    "\nFilled: " + s.dataTag.getBoolean(itemStack, "filled") +
                    "\nSelected material: " +
                    (selectedMaterial == null ? "N/A" : selectedMaterial.getName());
        }
        return "If you are seeing this, :(";
    }
}
