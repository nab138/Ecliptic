package me.nabdev.ecliptic.clientmixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import finalforeach.cosmicreach.entities.PlayerController;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.ecliptic.utils.ControlUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerController.class)
public class PlayerControllerMixin {
    @Inject(method = "updateMovement", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/entities/player/Player;proneCheck(Lfinalforeach/cosmicreach/world/Zone;)V"))
    void updateMovementMixin(Zone zone, CallbackInfo ci) {
        ControlUtils.ctrlPressed = Controls.sprintPressed();
        ControlUtils.xPressed =  Gdx.input.isKeyPressed(Input.Keys.X);
        ControlUtils.yPressed =  Gdx.input.isKeyPressed(Input.Keys.Y);
        ControlUtils.zPressed =  Gdx.input.isKeyPressed(Input.Keys.Z);
    }
}
