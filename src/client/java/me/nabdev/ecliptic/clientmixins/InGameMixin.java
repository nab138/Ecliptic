package me.nabdev.ecliptic.clientmixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.ui.UI;
import me.nabdev.ecliptic.BlockMesh;
import me.nabdev.ecliptic.MeshData;
import me.nabdev.ecliptic.items.Constructor;
import me.nabdev.ecliptic.items.SpatialManipulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static me.nabdev.ecliptic.items.SpatialManipulator.eps;

@Mixin(InGame.class)
public abstract class InGameMixin {


    @Unique private static ShapeRenderer ecliptic$sr2;

    @Shadow private static PerspectiveCamera rawWorldCamera;

    @Shadow private static Player localPlayer;

    @Unique
    private final static float ecliptic$lineWidth = 5f;

    @Unique
    List<MeshData> ecliptic$clipboardMeshes;

    @Unique
    List<MeshData> ecliptic$previewMeshes;



    @Unique
    private final static int MAX_MESH_SIZE = 64;

    @Inject(method = "render", at= @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/ui/UI;render()V", shift = At.Shift.BEFORE))
    public void drawShaperBoundingBox(CallbackInfo ci) {
        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if (selected == null) return;
        if (selected.getItem() instanceof Constructor constructor){
            Constructor.PreviewData preview = constructor.getPreview(selected);
            if(preview == null) return;
            BlockState[][][] clipboard = preview.preview();
            if(clipboard == null) return;

            ecliptic$previewMeshes = ecliptic$createMeshesFromClipboard(clipboard, preview.position());
            for (MeshData data : ecliptic$previewMeshes) {
                data.mesh().render(null, rawWorldCamera, data.matrix());
            }
            return;
        }
        if (!(selected.getItem() instanceof SpatialManipulator manipulator)) return;
        BoundingBox bb = manipulator.getVisualBoundingBox(selected, localPlayer.getZone());
        if (bb == null) return;
        if (ecliptic$sr2 == null) {
            ecliptic$sr2 = new ShapeRenderer();
        }

        SpatialManipulator.Mode mode = manipulator.getMode(selected);

        if(mode == SpatialManipulator.Mode.PASTE){
            if(manipulator.clipboardNeedsRemeshing.get()){
                Threads.runOnMainThread(() -> {
                    Vector3 pos1 = manipulator.dataTag.getPosition(selected, "pos1").toVector3();
                    BlockState[][][] clipboard = manipulator.clipboard.get();
                    if(clipboard == null) return;
                    ecliptic$clipboardMeshes = ecliptic$createMeshesFromClipboard(clipboard, pos1);
                });
                manipulator.clipboardNeedsRemeshing.set(false);
            }
            if(ecliptic$clipboardMeshes == null) return;
            for (MeshData data : ecliptic$clipboardMeshes) {
                data.mesh().render(null, rawWorldCamera, data.matrix());
            }
        }

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

    @Unique
    private List<MeshData> ecliptic$createMeshesFromClipboard(BlockState[][][] clipboard, Vector3 pos1) {
        List<MeshData> meshes = new ArrayList<>();
        int xSize = clipboard.length;
        int ySize = clipboard[0].length;
        int zSize = clipboard[0][0].length;

        for (int x = 0; x < xSize; x += MAX_MESH_SIZE) {
            for (int y = 0; y < ySize; y += MAX_MESH_SIZE) {
                for (int z = 0; z < zSize; z += MAX_MESH_SIZE) {
                    int xEnd = Math.min(x + MAX_MESH_SIZE, xSize);
                    int yEnd = Math.min(y + MAX_MESH_SIZE, ySize);
                    int zEnd = Math.min(z + MAX_MESH_SIZE, zSize);

                    BlockState[][][] subClipboard = new BlockState[xEnd - x][yEnd - y][zEnd - z];
                    boolean notAir = false;
                    for (int i = x; i < xEnd; i++) {
                        for (int j = y; j < yEnd; j++) {
                            for (int k = z; k < zEnd; k++) {
                                subClipboard[i - x][j - y][k - z] = clipboard[i][j][k];
                                if (!notAir && !clipboard[i][j][k].getSaveKey().equals(Block.AIR.getDefaultBlockState().getSaveKey())) {
                                    notAir = true;
                                    }
                            }
                        }
                    }
                    Vector3 offset = new Vector3(x, y, z);
                    Matrix4 matrix = new Matrix4().translate(pos1).translate(offset).translate(0.5f, 0.5f, 0.5f);
                    BlockMesh mesh = new BlockMesh(new AtomicReference<>(subClipboard));
                    if(notAir) meshes.add(new MeshData(mesh, matrix));
                }
            }
        }
        return meshes;
    }
}