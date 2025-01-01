package me.nabdev.ecliptic.clientmixins;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.ui.UIObject;
import me.nabdev.ecliptic.godmode.GodModeItem;
import me.nabdev.ecliptic.godmode.GodModeUI;
import me.nabdev.ecliptic.items.Constructor;
import me.nabdev.ecliptic.items.SpatialManipulator;
import me.nabdev.ecliptic.utils.BoundingBoxUtils;
import me.nabdev.ecliptic.godmode.GodMode;
import me.nabdev.ecliptic.utils.InGameAccessor;
import me.nabdev.ecliptic.utils.MeshingUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InGame.class)
public abstract class InGameMixin extends GameState implements InGameAccessor {
    @Unique
    private static ShapeRenderer ecliptic$sr2;

    @Shadow
    private static PerspectiveCamera rawWorldCamera;

    @Shadow
    private static Player localPlayer;

    @Unique
    List<MeshingUtils.MeshData> ecliptic$clipboardMeshes;

    @Unique
    List<MeshingUtils.MeshData> ecliptic$previewMeshes;


    @Inject(method = "create", at = @At("HEAD"))
    public void create(CallbackInfo ci) {
        GodModeUI.createButtons(this.uiObjects);
    }

    @Inject(method = "switchAwayTo", at = @At("TAIL"))
    public void switchAwayTo(CallbackInfo ci) {
        GodModeUI.createButtons(this.uiObjects);
    }

    @Inject(method = "unloadWorld", at = @At("HEAD"))
    private void exitWorld(CallbackInfo ci) {
        GodModeItem.godMode = false;
    }


    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/ui/UI;render()V"))
    public void renderEclipticUI(CallbackInfo ci) {
        if (ecliptic$sr2 == null) {
            ecliptic$sr2 = new ShapeRenderer();
        }


        ItemStack selected = UI.hotbar.getSelectedItemStack();

        for (UIObject uiObject : GodMode.godModeUIObjects) {

            if (GodModeItem.godMode && !GodMode.waitingToSelectMat && !GodMode.waitingToSelectReplaceMat)
                uiObject.show();
            else uiObject.hide();
        }

        if (GodModeItem.godMode || (selected != null && selected.getItem() instanceof GodModeItem)) {
            GodMode.renderGodMode(localPlayer);
            return;
        }
        if (selected == null) return;
        if (selected.getItem() instanceof Constructor constructor) {
            Constructor.PreviewData preview = constructor.getPreview(selected);
            if (preview == null) return;
            BlockState[][][] clipboard = preview.preview();
            if (clipboard == null) return;

            ecliptic$previewMeshes = MeshingUtils.createMeshesFromBlocks(clipboard, preview.position());
            for (MeshingUtils.MeshData data : ecliptic$previewMeshes) {
                data.mesh().render(null, rawWorldCamera, data.matrix());
            }
            return;
        }
        if (!(selected.getItem() instanceof SpatialManipulator manipulator)) return;
        BoundingBox bb = manipulator.getVisualBoundingBox(selected, localPlayer.getZone());
        if (bb == null) return;

        SpatialManipulator.Mode mode = manipulator.getMode(selected);

        if (mode == SpatialManipulator.Mode.PASTE) {
            if (SpatialManipulator.clipboardNeedsRemeshing.get()) {
                Threads.runOnMainThread(() -> {
                    Vector3 pos1 = manipulator.dataTag.getPosition(selected, "pos1").toVector3();
                    BlockState[][][] clipboard = SpatialManipulator.clipboard.get();
                    if (clipboard == null) return;
                    ecliptic$clipboardMeshes = MeshingUtils.createMeshesFromBlocks(clipboard, pos1);
                });
                SpatialManipulator.clipboardNeedsRemeshing.set(false);
            }
            if (ecliptic$clipboardMeshes == null) return;
            for (MeshingUtils.MeshData data : ecliptic$clipboardMeshes) {
                data.mesh().render(null, rawWorldCamera, data.matrix());
            }
        }

        BoundingBoxUtils.drawBB(ecliptic$sr2, bb, mode.getFillColor(), mode.getBorderColor());
    }


    @Unique
    @Override
    public Viewport ecliptic$getViewport() {
        return this.uiViewport;
    }
}