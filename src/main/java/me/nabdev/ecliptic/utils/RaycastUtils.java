package me.nabdev.ecliptic.utils;

import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.player.Player;

public class RaycastUtils {

    public record HitInfo(BlockPosition blockPosition, Vector3 normal) {}

    private static BlockPosition currentLookAt;
    private static Vector3 currentNormal;
    private static IRaycaster raycaster;

    public static BlockPosition getCurrentLookAt(){
        return currentLookAt;
    }

    public static Vector3 getCurrentNormal(){
        return currentNormal;
    }

    public static void setRaycaster(IRaycaster raycaster){
        RaycastUtils.raycaster = raycaster;
    }

    public static void update(Player p){
        HitInfo info = raycaster.raycast(p);
        if(info != null){
            currentLookAt = info.blockPosition();
            currentNormal = info.normal();
        }
    }

    public interface IRaycaster {
        HitInfo raycast(Player p);
    }
}
