package me.nabdev.ecliptic;

import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.ModInitializer;
import com.github.puzzle.game.items.IModItem;
import me.nabdev.ecliptic.items.Shaper;
import me.nabdev.ecliptic.items.TemporalManipulator;

@SuppressWarnings("unused")
public class Ecliptic implements ModInitializer {
    @Override
    public void onInit() {
        IModItem.registerItem(new Shaper());
        IModItem.registerItem(new TemporalManipulator());
    }
}
