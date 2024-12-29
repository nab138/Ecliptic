package me.nabdev.ecliptic;

import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.ModInitializer;
import com.github.puzzle.game.items.IModItem;
import me.nabdev.ecliptic.items.SpatialManipulator;
import me.nabdev.ecliptic.items.TemporalManipulator;
import me.nabdev.ecliptic.utils.FillingThread;

@SuppressWarnings("unused")
public class Ecliptic implements ModInitializer {
    @Override
    public void onInit() {
        IModItem.registerItem(new SpatialManipulator());
        IModItem.registerItem(new TemporalManipulator());
        FillingThread.init();
    }
}
