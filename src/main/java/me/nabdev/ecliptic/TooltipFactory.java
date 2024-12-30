package me.nabdev.ecliptic;

import me.nabdev.cosmictooltips.api.ToolTipFactory;
import me.nabdev.ecliptic.items.ConstructorTooltips;
import me.nabdev.ecliptic.items.SpatialManipulatorTooltips;
import me.nabdev.ecliptic.items.TemporalManipulatorTooltips;

@SuppressWarnings("unused")
public class TooltipFactory extends ToolTipFactory {
    public TooltipFactory(){
        addTooltip(new SpatialManipulatorTooltips());
        addTooltip(new ConstructorTooltips());
        addTooltip(new TemporalManipulatorTooltips());
    }
}
