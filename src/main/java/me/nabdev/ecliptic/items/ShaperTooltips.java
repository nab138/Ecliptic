package me.nabdev.ecliptic.items;

import finalforeach.cosmicreach.items.ItemStack;
import me.nabdev.cosmictooltips.api.ITooltipItem;

import static me.nabdev.ecliptic.utils.ChatHelper.blockPosToString;

public class ShaperTooltips implements ITooltipItem {
    @Override
    public String getItemID() {
        return Shaper.id.toString();
    }

    @Override
    public String getTooltipText(ItemStack itemStack) {
        if (itemStack.getItem() instanceof Shaper s) {
            StringBuilder builder = new StringBuilder();
            builder.append("Current mode: ");
            builder.append(s.mode);
            builder.append("\nSelected material: ");
            builder.append(s.selectedMaterial == null ? "N/A" : s.selectedMaterial.getName());
            builder.append("\n");
            if (s.pos1 == null) {
                builder.append("No positions set");
            } else {
                builder.append("Pos 1: ");
                builder.append(blockPosToString(s.pos1));
                builder.append("\nPos 2: ");
                builder.append(s.pos2 == null ? "N/A" : blockPosToString(s.pos2));
            }
            return builder.toString();
        }
        return "If you are seeing this, :(";
    }
}
