package me.nabdev.ecliptic.clientmixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.ui.UI;
import me.nabdev.ecliptic.items.SpatialManipulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.nabdev.ecliptic.items.SpatialManipulator.eps;

@Mixin(InGame.class)
public abstract class InGameMixin {

    @Unique private static ShapeRenderer ecliptic$sr2;

    @Shadow private static PerspectiveCamera rawWorldCamera;

    @Shadow private static Player localPlayer;

    @Unique
    private final static float ecliptic$lineWidth = 5f;


    @Inject(method = "render", at= @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/ui/UI;render()V", shift = At.Shift.BEFORE))
    public void drawShaperBoundingBox(CallbackInfo ci) {
        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if (selected == null) return;
        if (!(selected.getItem() instanceof SpatialManipulator manipulator)) return;
        BoundingBox bb = manipulator.getVisualBoundingBox(selected, localPlayer.getZone());
        if (bb == null) return;
        if (ecliptic$sr2 == null) {
            ecliptic$sr2 = new ShapeRenderer();
        }

        SpatialManipulator.Mode mode = manipulator.getMode(selected);

        ecliptic$sr2.setProjectionMatrix(rawWorldCamera.combined);
        ecliptic$sr2.begin(ShapeRenderer.ShapeType.Filled);
        ecliptic$sr2.setColor(mode.getFillColor());
        ecliptic$sr2.box(bb.min.x, bb.min.y, bb.min.z + bb.getDepth(), bb.getWidth(), bb.getHeight(), bb.getDepth());
        ecliptic$sr2.box(bb.max.x, bb.max.y, bb.max.z - bb.getDepth(), -bb.getWidth(), -bb.getHeight(), -bb.getDepth());
        ecliptic$sr2.end();

        Gdx.gl.glLineWidth(ecliptic$lineWidth);
        ecliptic$sr2.begin(ShapeRenderer.ShapeType.Line);
        ecliptic$sr2.setColor(mode.getBorderColor());
        ecliptic$sr2.box(bb.min.x, bb.min.y, bb.min.z + bb.getDepth(), bb.getWidth(), bb.getHeight(), bb.getDepth());
        ecliptic$sr2.box(bb.min.x + eps * 0.5f, bb.min.y + eps * 0.5f, bb.min.z + bb.getDepth() - eps * 0.5f, bb.getWidth() - eps, bb.getHeight() - eps, bb.getDepth() - eps);
        ecliptic$sr2.end();
        Gdx.gl.glLineWidth(1f);
    }
}