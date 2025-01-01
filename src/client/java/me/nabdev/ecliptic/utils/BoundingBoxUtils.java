package me.nabdev.ecliptic.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.gamestates.GameState;

import static me.nabdev.ecliptic.items.SpatialManipulator.eps;

public class BoundingBoxUtils {
    public static Camera rawWorldCamera = GameState.IN_GAME.getWorldCamera();

    public static void drawBB(ShapeRenderer sr, BoundingBox bb, Color fillColor, Color borderColor) {
        sr.setProjectionMatrix(rawWorldCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(fillColor);
        sr.box(bb.min.x, bb.min.y, bb.min.z + bb.getDepth(), bb.getWidth(), bb.getHeight(), bb.getDepth());
        sr.box(bb.max.x, bb.max.y, bb.max.z - bb.getDepth(), -bb.getWidth(), -bb.getHeight(), -bb.getDepth());
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(borderColor);
        sr.box(bb.min.x, bb.min.y, bb.min.z + bb.getDepth(), bb.getWidth(), bb.getHeight(), bb.getDepth());
        sr.box(bb.min.x + eps * 0.5f, bb.min.y + eps * 0.5f, bb.min.z + bb.getDepth() - eps * 0.5f, bb.getWidth() - eps, bb.getHeight() - eps, bb.getDepth() - eps);
        sr.end();
    }
}
