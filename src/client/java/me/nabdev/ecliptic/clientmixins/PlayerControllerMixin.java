package me.nabdev.ecliptic.clientmixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.ecliptic.godmode.GodModeItem;
import me.nabdev.ecliptic.utils.ControlUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "updateMovement", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/entities/player/Player;proneCheck(Lfinalforeach/cosmicreach/world/Zone;)V"))
    void updateMovementMixin(Zone zone, CallbackInfo ci) {
        ControlUtils.ctrlPressed = Controls.sprintPressed();
        ControlUtils.xPressed = Gdx.input.isKeyPressed(Input.Keys.X);
        ControlUtils.yPressed = Gdx.input.isKeyPressed(Input.Keys.Y);
        ControlUtils.zPressed = Gdx.input.isKeyPressed(Input.Keys.Z);
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
}
