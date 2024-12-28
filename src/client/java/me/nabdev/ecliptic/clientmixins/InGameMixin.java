package me.nabdev.ecliptic.clientmixins;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.ui.UI;
import me.nabdev.ecliptic.items.SpatialManipulator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public abstract class InGameMixin {

    @Shadow private static ShapeRenderer sr;

    @Shadow private static PerspectiveCamera rawWorldCamera;

    @Shadow private static Player localPlayer;

    @Inject(method = "render", at= @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/ui/UI;render()V"))
    public void drawShaperBoundingBox(CallbackInfo ci) {
        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if(selected == null) return;
        if(!(selected.getItem() instanceof SpatialManipulator manipulator)) return;
        BoundingBox bb = manipulator.getVisualBoundingBox(selected, localPlayer.getZone());
        if(bb == null) return;
        if (sr == null) {
            sr = new ShapeRenderer();
        }

        sr.setProjectionMatrix(rawWorldCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(new Color(0, 0, 1, 0.25f));
        sr.box(bb.min.x, bb.min.y, bb.min.z + bb.getDepth(), bb.getWidth(), bb.getHeight(), bb.getDepth());
        sr.end();

        sr.setProjectionMatrix(rawWorldCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.BLUE);
        sr.box(bb.min.x, bb.min.y, bb.min.z + bb.getDepth(), bb.getWidth(), bb.getHeight(), bb.getDepth());
        sr.end();
    }

}