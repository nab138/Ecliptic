package me.nabdev.ecliptic;

import me.nabdev.cosmictooltips.api.ToolTipFactory;
import me.nabdev.ecliptic.items.ShaperTooltips;

public class TooltipFactory extends ToolTipFactory {
    public TooltipFactory(){
        addTooltip(new ShaperTooltips());
    }
}
