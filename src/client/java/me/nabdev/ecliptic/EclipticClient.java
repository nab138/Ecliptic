package me.nabdev.ecliptic;

import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientModInitializer;
import com.github.puzzle.game.items.IModItem;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.ui.UI;
import me.nabdev.ecliptic.items.Constructor;
import me.nabdev.ecliptic.utils.RaycastUtils;
import me.nabdev.ecliptic.utils.Raycaster;


@SuppressWarnings("unused")
public class EclipticClient implements ClientModInitializer {
    @Override
    public void onInit(){
        RaycastUtils.setRaycaster(new Raycaster());
        IModItem.registerItem(new GodModeItem());

        GameSingletons.updateObservers.add((d) -> {
            ItemStack selected = UI.hotbar.getSelectedItemStack();
            if(selected == null || GodModeItem.godMode) return;
            Item item = selected.getItem();
            if(item == null) return;
            if(item instanceof Constructor) {
                RaycastUtils.update(InGame.getLocalPlayer());
            }
        });
    }
}
