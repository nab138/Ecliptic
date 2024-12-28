package me.nabdev.ecliptic.items;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.items.ItemStack;
import me.nabdev.cosmictooltips.api.ITooltipItem;
import me.nabdev.ecliptic.utils.Vec3Int;

public class ShaperTooltips implements ITooltipItem {
    @Override
    public String getItemID() {
        return SpatialManipulator.id.toString();
    }

    @Override
    public String getTooltipText(ItemStack itemStack) {
        if (itemStack.getItem() instanceof SpatialManipulator s) {
            BlockState selectedMaterial = s.getSelectedMaterial(itemStack);
            Vec3Int pos1 = s.getPosition(itemStack, "pos1");
            Vec3Int pos2 = s.getPosition(itemStack, "pos2");

            StringBuilder builder = new StringBuilder();
            builder.append("Current mode: ");
            builder.append(s.getMode(itemStack));
            builder.append("\nSelected material: ");
            builder.append(selectedMaterial == null ? "N/A" : selectedMaterial.getName());
            builder.append("\n");
            if (pos1 == null && pos2 == null) {
                builder.append("No positions set");
            } else {
                builder.append("Pos 1: ");
                builder.append(pos1 == null ? "N/A" : pos1);
                builder.append("\nPos 2: ");
                builder.append(pos2 == null ? "N/A" : pos2);
            }
            return builder.toString();
        }
        return "If you are seeing this, :(";
    }
}
