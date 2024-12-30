package me.nabdev.ecliptic;

import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientModInitializer;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.gamestates.InGame;
import me.nabdev.ecliptic.utils.RaycastUtils;


@SuppressWarnings("unused")
public class EclipticClient implements ClientModInitializer {
    @Override
    public void onInit(){
        RaycastUtils.setRaycaster(new Raycaster());

        GameSingletons.updateObservers.add((d) ->  RaycastUtils.update(InGame.getLocalPlayer()));
    }
}
