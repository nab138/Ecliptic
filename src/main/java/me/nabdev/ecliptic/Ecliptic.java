package me.nabdev.ecliptic;

import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.ModInitializer;
import com.github.puzzle.game.items.IModItem;
import me.nabdev.ecliptic.items.Shaper;

public class Ecliptic implements ModInitializer {
    @Override
    public void onInit() {
        IModItem.registerItem(new Shaper());
    }
}
