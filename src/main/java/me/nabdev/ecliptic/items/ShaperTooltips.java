package me.nabdev.ecliptic.items;

import finalforeach.cosmicreach.items.ItemStack;
import me.nabdev.cosmictooltips.api.ITooltipItem;

public class ShaperTooltips implements ITooltipItem {
    @Override
    public String getItemID() {
        return Shaper.id.toString();
    }

    @Override
    public String getTooltipText(ItemStack itemStack) {
        if (itemStack.getItem() instanceof Shaper s) {
            if (s.pos1 == null) {
                return "Nothing selected";
            }
            return "Pos 1: " + s.pos1 + "\nPos 2: " + (s.pos2 == null ? "N/A" : s.pos2);
        }
        return "If you are seeing this, :(";
    }
}
