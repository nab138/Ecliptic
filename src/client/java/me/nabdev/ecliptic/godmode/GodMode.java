package me.nabdev.ecliptic.godmode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.viewport.Viewport;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import me.nabdev.ecliptic.items.SpatialManipulator;
import me.nabdev.ecliptic.items.TemporalManipulator;
import me.nabdev.ecliptic.utils.BoundingBoxUtils;
import me.nabdev.ecliptic.utils.InGameAccessor;
import me.nabdev.ecliptic.utils.RaycastUtils;
import me.nabdev.ecliptic.utils.Raycaster;

import java.util.ArrayList;
import java.util.List;

// TODO: CRButton?
@SuppressWarnings("removal")
public class GodMode {
    static Vector3 pos1 = null;
    static Vector3 pos2 = null;
    private static ShapeRenderer sr;
    private static final BoundingBox visualSelection = new BoundingBox();
    static final BoundingBox selection = new BoundingBox();
    private static final Vector3 min = new Vector3();
    private static final Vector3 max = new Vector3();
    private static final Vector3 visMin = new Vector3();
    private static final Vector3 visMax = new Vector3();

    private static final Color cursorFill = new Color(1, 1, 1, 0.2f);
    private static final Color selectionFill = new Color(0, 1, 1, 0.25f);

    private static final float eps = 0.02f;

    private static boolean dragging = false;
    private static boolean movingSelection = false;
    private static final Vector2 lastMousePos = new Vector2();

    private static final Raycaster raycaster = new Raycaster();

    public static boolean waitingToSelectMat = false;
    public static boolean waitingToSelectReplaceMat = false;
    private static boolean justSelectedMat = false;

    static BlockState selectedBlock = null;

    public static List<finalforeach.cosmicreach.ui.UIElement> godModeUIObjects = new ArrayList<>();

    static finalforeach.cosmicreach.ui.UIElement selectMatBtn;

    private static final Vector3 initialPos1 = new Vector3();
    private static final Vector3 initialPos2 = new Vector3();
    private static final Vector3 dragOffset = new Vector3();

    public record Handle(int index, boolean xMax, boolean yMax, boolean zMax) {
    }

    private static Handle hoveredHandle = null;
    private static boolean grabbingHandle = false;

    private static final Vector2 mouse = new Vector2();

    public static void renderGodMode(Player p) {
        if (sr == null) {
            sr = new ShapeRenderer();
        }
        GameState.IN_GAME.getWorldCamera().update();

        RaycastUtils.HitInfo hitInfo = raycaster.raycast(p);

        boolean overSelection = pos1 != null && pos2 != null && mouseIntersectsWith(visualSelection) && !dragging;
        if (overSelection || movingSelection) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.AllResize);
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
        boolean leftMousePressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if (leftMousePressed) {
            Viewport uiViewport = ((InGameAccessor) GameState.IN_GAME).ecliptic$getViewport();
            mouse.set((float) Gdx.input.getX(), (float) Gdx.input.getY());
            uiViewport.unproject(mouse);
            for (var uiObject : godModeUIObjects) {
                if (uiObject.isHoveredOver(uiViewport, mouse.x, mouse.y)) {
                    leftMousePressed = false;
                }
            }
        }
        if (leftMousePressed && !justSelectedMat && !waitingToSelectMat && !waitingToSelectReplaceMat) {
            if (!dragging && (overSelection || movingSelection || hoveredHandle != null || grabbingHandle)) {
                if (!movingSelection && !grabbingHandle) {
                    if (hoveredHandle != null) {
                        grabbingHandle = true;
                        initialPos1.set(min);
                        initialPos2.set(max);
                    } else {
                        movingSelection = true;
                        initialPos1.set(pos1);
                        initialPos2.set(pos2);
                    }

                    lastMousePos.set(Gdx.input.getX(), Gdx.input.getY());
                } else {
                    Vector2 currentMousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                    float distToCenter = InGame.getLocalPlayer().getEntity().getPosition().dst(initialPos1.cpy().add(initialPos2).scl(0.5f));
                    Vector2 mouseDelta = currentMousePos.cpy().sub(lastMousePos).scl(0.005f * Gdx.graphics.getWidth() * distToCenter, 0.008f * Gdx.graphics.getHeight() * distToCenter);

                    Vector3 start = GameState.IN_GAME.getWorldCamera().unproject(new Vector3(0, 0, 0));
                    Vector3 end = GameState.IN_GAME.getWorldCamera().unproject(new Vector3(mouseDelta.x, mouseDelta.y, 0));
                    dragOffset.set(end).sub(start);

                    if (!grabbingHandle) {
                        pos1.set(initialPos1).add(dragOffset);
                        pos2.set(initialPos2).add(dragOffset);

                        min.set((float) Math.floor(Math.min(pos1.x, pos2.x)), (float) Math.floor(Math.min(pos1.y, pos2.y)), (float) Math.floor(Math.min(pos1.z, pos2.z)));
                        max.set((float) Math.floor(Math.max(pos1.x, pos2.x)), (float) Math.floor(Math.max(pos1.y, pos2.y)), (float) Math.floor(Math.max(pos1.z, pos2.z)));
                    } else {
                        min.set(initialPos1);
                        max.set(initialPos2);
                        if (hoveredHandle.xMax) {
                            max.x = Math.max(min.x, max.x + dragOffset.x);
                        } else {
                            min.x = Math.min(min.x + dragOffset.x, max.x);
                        }
                        if (hoveredHandle.yMax) {
                            max.y = Math.max(min.y, max.y + dragOffset.y);
                        } else {
                            min.y = Math.min(min.y + dragOffset.y, max.y);
                        }
                        if (hoveredHandle.zMax) {
                            max.z = Math.max(min.z, max.z + dragOffset.z);
                        } else {
                            min.z = Math.min(min.z + dragOffset.z, max.z);
                        }
                        min.set((float) Math.floor(min.x), (float) Math.floor(min.y), (float) Math.floor(min.z));
                        max.set((float) Math.floor(max.x), (float) Math.floor(max.y), (float) Math.floor(max.z));
                        pos1.set(min);
                        pos2.set(max);
                    }
                    updateFromMinMax();
                }
            }
        } else {
            movingSelection = false;
            grabbingHandle = false;
        }
        if ((waitingToSelectMat || waitingToSelectReplaceMat) && Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            BlockState tmpSelectedBlock = Block.AIR.getDefaultBlockState();
            updateSelectedMats(tmpSelectedBlock);
        }

        if (hitInfo != null) {
            BlockPosition cursorPos = hitInfo.blockPosition();
            if (cursorPos != null) {
                Vector3 selectedPos = floorVector(new Vector3(cursorPos.getGlobalX(), cursorPos.getGlobalY(), cursorPos.getGlobalZ()));
                BoundingBox cursor = new BoundingBox(selectedPos.cpy().sub(eps), selectedPos.cpy().add(1 + eps));
                BoundingBoxUtils.drawBB(sr, cursor, cursorFill, Color.BLACK);

                if (leftMousePressed) {
                    if (waitingToSelectMat || waitingToSelectReplaceMat) {
                        BlockState tmpSelectedBlock = cursorPos.getBlockState();
                        if (tmpSelectedBlock == null) tmpSelectedBlock = Block.AIR.getDefaultBlockState();
                        updateSelectedMats(tmpSelectedBlock);
                    } else if (!justSelectedMat && !overSelection && !movingSelection && !grabbingHandle) {
                        if (!dragging) {
                            dragging = true;
                            pos1 = selectedPos;
                            pos2 = null;
                        } else {
                            pos2 = selectedPos;
                            min.set(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.min(pos1.z, pos2.z));
                            max.set(Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y), Math.max(pos1.z, pos2.z));
                            updateFromMinMax();
                        }
                    }
                } else {
                    dragging = false;
                    movingSelection = false;
                    justSelectedMat = false;
                }
            }
        }

        renderSelection();
    }

    private static void renderSelection() {
        if (pos1 != null && pos2 != null) {
            Vector3 min = visualSelection.getMin(new Vector3());
            Vector3 max = visualSelection.getMax(new Vector3());

            Vector3[] handles = new Vector3[]{
                    new Vector3(min.x - 0.25f, min.y - 0.25f, min.z - 0.25f),
                    new Vector3(max.x - 0.25f, min.y - 0.25f, min.z - 0.25f),
                    new Vector3(min.x - 0.25f, max.y - 0.25f, min.z - 0.25f),
                    new Vector3(max.x - 0.25f, max.y - 0.25f, min.z - 0.25f),
                    new Vector3(min.x - 0.25f, min.y - 0.25f, max.z - 0.25f),
                    new Vector3(max.x - 0.25f, min.y - 0.25f, max.z - 0.25f),
                    new Vector3(min.x - 0.25f, max.y - 0.25f, max.z - 0.25f),
                    new Vector3(max.x - 0.25f, max.y - 0.25f, max.z - 0.25f)
            };

            boolean[][] maxes = new boolean[][]{
                    {false, false, false},
                    {true, false, false},
                    {false, true, false},
                    {true, true, false},
                    {false, false, true},
                    {true, false, true},
                    {false, true, true},
                    {true, true, true}
            };

            boolean foundHoveredHandle = false;
            for (int i = 0; i < handles.length; i++) {
                Vector3 handle = handles[i];
                BoundingBox handleBB = new BoundingBox(handle, handle.cpy().add(0.5f));
                boolean isAlreadyGrabbing = grabbingHandle && hoveredHandle != null && hoveredHandle.index == i;
                boolean intersects = isAlreadyGrabbing || !(foundHoveredHandle || !mouseIntersectsWith(handleBB));
                BoundingBoxUtils.drawBB(sr, handleBB, intersects ? Color.BLUE : Color.WHITE, Color.BLACK);
                if (!grabbingHandle && intersects) {
                    hoveredHandle = new Handle(i, maxes[i][0], maxes[i][1], maxes[i][2]);
                    foundHoveredHandle = true;
                }
            }
            if (!foundHoveredHandle && !grabbingHandle) {
                hoveredHandle = null;
            }
        }

        BoundingBoxUtils.drawBB(sr, visualSelection, selectionFill, Color.BLACK);
    }

    public static boolean mouseIntersectsWith(BoundingBox bb) {
        Vector3 mouseCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        GameState.IN_GAME.getWorldCamera().unproject(mouseCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0));
        GameState.IN_GAME.getWorldCamera().unproject(mouseCoords.set(Gdx.input.getX(), Gdx.input.getY(), 1));
        Ray ray = GameState.IN_GAME.getWorldCamera().getPickRay(Gdx.input.getX(), Gdx.input.getY());

        return Intersector.intersectRayBounds(ray, bb, null);
    }

    private static Vector3 floorVector(Vector3 vec) {
        return new Vector3((float) Math.floor(vec.x), (float) Math.floor(vec.y), (float) Math.floor(vec.z));
    }

    private static void updateFromMinMax() {
        selection.set(min, max);
        visMin.set(min).sub(eps);
        visMax.set(max).add(eps + 1);
        visualSelection.set(visMin, visMax);
    }

    private static void updateSelectedMats(BlockState tmpSelectedBlock) {
        if (waitingToSelectMat) {
            selectedBlock = tmpSelectedBlock;
            selectMatBtn.setText("Mat: " + selectedBlock.getBlock().getName());
        } else {
            SpatialManipulator.SpatialManipulatorAction action = new SpatialManipulator.SpatialManipulatorAction(SpatialManipulator.Mode.REPLACE, selectedBlock, selection, null, tmpSelectedBlock);
            action.apply(InGame.getLocalPlayer().getZone(), true);
            TemporalManipulator.addToUndoStack(action);
        }
        waitingToSelectMat = false;
        waitingToSelectReplaceMat = false;
        justSelectedMat = true;
    }
}
