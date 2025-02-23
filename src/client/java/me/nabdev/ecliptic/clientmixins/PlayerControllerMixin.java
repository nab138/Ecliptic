package me.nabdev.ecliptic.clientmixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.ecliptic.godmode.GodModeItem;
import me.nabdev.ecliptic.items.Constructor;
import me.nabdev.ecliptic.items.SpatialManipulator;
import me.nabdev.ecliptic.utils.ChatHelper;
import me.nabdev.ecliptic.utils.ControlUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(PlayerController.class)
public abstract class PlayerControllerMixin {
    @Shadow
    Player player;
    @Shadow
    Camera playerCam;
    @Shadow
    private transient float startMouseX;
    @Shadow
    private transient float startMouseY;

    @Shadow
    protected abstract void moveCamera();

    @Unique
    private char ecliptic$axis = 'Y';

    @Inject(method = "updateMovement", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/entities/player/Player;proneCheck(Lfinalforeach/cosmicreach/world/Zone;)V"))
    void updateMovementMixin(Zone zone, CallbackInfo ci) {
        ControlUtils.ctrlPressed = Controls.sprintPressed();
        ControlUtils.shiftPressed = Controls.crouchPressed();
        ControlUtils.xPressed = Gdx.input.isKeyPressed(Input.Keys.X);
        ControlUtils.yPressed = Gdx.input.isKeyPressed(Input.Keys.Y);
        ControlUtils.zPressed = Gdx.input.isKeyPressed(Input.Keys.Z);
        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if(selected == null || selected.getItem() == null) return;
        if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
            if(selected.getItem() instanceof Constructor constructor){
                constructor.changeMode(selected);
            } else if(selected.getItem() instanceof SpatialManipulator spatialManipulator){
                spatialManipulator.changeMode(selected);
            }
        }
        if(selected.getItem() instanceof SpatialManipulator manipulator){
            if(manipulator.isPasting(selected)) {
                if(Gdx.input.isKeyJustPressed(Input.Keys.Z)){
                    ecliptic$axis = 'Z';
                    ChatHelper.sendMsg("Rotate/Mirror axis: Z");
                }
                if(Gdx.input.isKeyJustPressed(Input.Keys.X)){
                    ecliptic$axis = 'X';
                    ChatHelper.sendMsg("Rotate/Mirror axis: X");
                }
                if(Gdx.input.isKeyJustPressed(Input.Keys.Y)){
                    ecliptic$axis = 'Y';
                    ChatHelper.sendMsg("Rotate/Mirror axis: Y");
                }
                if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
                    AtomicReference<BlockState[][][]> clipboard = SpatialManipulator.clipboard;
                    clipboard.set(ecliptic$rotate(clipboard.get(), ecliptic$axis));
                    SpatialManipulator.clipboardNeedsRemeshing.set(true);
                }
                if(Gdx.input.isKeyJustPressed(Input.Keys.T)){
                    AtomicReference<BlockState[][][]> clipboard = SpatialManipulator.clipboard;
                    clipboard.set(ecliptic$mirror(clipboard.get(), ecliptic$axis));
                    SpatialManipulator.clipboardNeedsRemeshing.set(true);
                }
            }
        }
    }

    @Redirect(method = "updateMovement", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/settings/Controls;pronePressed()Z"))
    public boolean pronePressedMixin() {
        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if(selected != null && selected.getItem() != null && selected.getItem() instanceof SpatialManipulator manipulator && manipulator.isPasting(selected)) {
            return false;
        }
        if(selected != null && selected.getItem() != null && selected.getItem() instanceof Constructor) {
            return false;
        }
        return Controls.pronePressed();
    }


    @Redirect(method = "moveCamera", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/Input;setCursorCatched(Z)V"))
    public void setCursorCatched(Input instance, boolean b) {
        if (GodModeItem.godMode) {
            player.getEntity().noClip = true;
            player.getEntity().velocity.set(0, 0, 0);
            if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                Gdx.input.setCursorCatched(true);
                float deltaX = (Gdx.input.getX() - this.startMouseX) * 0.2f;
                float deltaY = (this.startMouseY - Gdx.input.getY()) * 0.2f;

                player.getEntity().viewDirection.rotate(Vector3.Y, -deltaX);
                player.getEntity().viewDirection.rotate(playerCam.direction.cpy().crs(playerCam.up).nor(), deltaY);

                this.startMouseX = Gdx.input.getX();
                this.startMouseY = Gdx.input.getY();
            } else {
                Gdx.input.setCursorCatched(false);
            }

            // Freecam controls
            float speed = 10f * Gdx.graphics.getDeltaTime();
            Vector3 direction = new Vector3();

            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                direction.add(playerCam.direction);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                direction.sub(playerCam.direction);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                direction.add(playerCam.direction.cpy().crs(playerCam.up).nor().scl(-1));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                direction.add(playerCam.direction.cpy().crs(playerCam.up).nor());
            }
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                direction.add(playerCam.up);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                direction.sub(playerCam.up);
            }


            // Middle-click drag for panning
            if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
                float deltaX = (Gdx.input.getX() - this.startMouseX) * 0.1f;
                float deltaY = (this.startMouseY - Gdx.input.getY()) * 0.1f;

                direction.add(playerCam.direction.cpy().crs(playerCam.up).nor().scl(-deltaX));
                direction.add(playerCam.up.cpy().scl(-deltaY));
            }

            player.getEntity().setPosition(player.getEntity().getPosition().add(direction.scl(speed)));
            return;
        }
        Gdx.input.setCursorCatched(b);
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void updateMixin(CallbackInfo ci) {
        if (GodModeItem.godMode) {
            ci.cancel();
        }
    }

    @Inject(method = "updateMovement", at = @At("HEAD"), cancellable = true)
    public void updateMovementMixin(CallbackInfo ci) {
        if (GodModeItem.godMode) {
            moveCamera();
            ci.cancel();
        }
    }

    @Unique
    private static BlockState[][][] ecliptic$rotate(BlockState[][][] array, char axis) {
        int width = array.length;       // X-dimension
        int height = array[0].length;   // Y-dimension
        int depth = array[0][0].length; // Z-dimension

        BlockState[][][] rotated;

        switch (axis) {
            case 'X': // Rotate around X-axis (Y ↔ Z)
                rotated = new BlockState[width][depth][height];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        for (int z = 0; z < depth; z++) {
                            rotated[x][z][height - 1 - y] = array[x][y][z];
                        }
                    }
                }
                break;

            case 'Y': // Rotate around Y-axis (X ↔ Z)
                rotated = new BlockState[depth][height][width];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        for (int z = 0; z < depth; z++) {
                            rotated[z][y][width - 1 - x] = array[x][y][z];
                        }
                    }
                }
                break;

            case 'Z': // Rotate around Z-axis (X ↔ Y)
                rotated = new BlockState[height][width][depth];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        for (int z = 0; z < depth; z++) {
                            rotated[y][width - 1 - x][z] = array[x][y][z];
                        }
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }

        return rotated;
    }

    @Unique
    private static BlockState[][][] ecliptic$mirror(BlockState[][][] array, char axis) {
        int width = array.length;       // X-dimension
        int height = array[0].length;   // Y-dimension
        int depth = array[0][0].length; // Z-dimension

        BlockState[][][] mirrored;

        switch (axis) {
            case 'X': // Mirror around X-axis
                mirrored = new BlockState[width][height][depth];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        for (int z = 0; z < depth; z++) {
                            mirrored[width - 1 - x][y][z] = array[x][y][z];
                        }
                    }
                }
                break;

            case 'Y': // Mirror around Y-axis
                mirrored = new BlockState[width][height][depth];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        for (int z = 0; z < depth; z++) {
                            mirrored[x][height - 1 - y][z] = array[x][y][z];
                        }
                    }
                }
                break;

            case 'Z': // Mirror around Z-axis
                mirrored = new BlockState[width][height][depth];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        for (int z = 0; z < depth; z++) {
                            mirrored[x][y][depth - 1 - z] = array[x][y][z];
                        }
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }

        return mirrored;
    }
}
