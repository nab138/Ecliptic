package me.nabdev.ecliptic.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIElement;
import finalforeach.cosmicreach.ui.UIObject;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import me.nabdev.ecliptic.GodModeItem;
import me.nabdev.ecliptic.items.SpatialManipulator;
import me.nabdev.ecliptic.items.TemporalManipulator;

import java.util.ArrayList;
import java.util.List;

import static me.nabdev.ecliptic.utils.ChatHelper.sendMsg;

public class GodMode {
    private static Vector3 pos1 = null;
    private static Vector3 pos2 = null;
    private static ShapeRenderer sr;
    private static BoundingBox visualSelection;
    private static BoundingBox selection;

    private static final Color cursorFill = new Color(1, 1, 1, 0.2f);
    private static final Color selectionFill = new Color(0, 1, 1, 0.25f);

    private static final float eps = 0.02f;

    private static boolean dragging = false;

    private static final Raycaster raycaster = new Raycaster();

    public static boolean waitingToSelectMat = false;
    private static boolean justSelectedMat = false;

    private static BlockState selectedBlock = null;

    public static List<UIObject> godModeUIObjects = new ArrayList<>();

    private static UIElement selectMatBtn;

    public static void createButtons(Array<UIObject> uiObjects){
        float padding = 10.0F;
        float btnWidth = 180.0F;
        float btnHeight = 60.0F;

        UIElement exitButton = new UIElement(padding, -padding, btnWidth, btnHeight) {
            public void onClick() {
                super.onClick();
                GodModeItem.turnOff(InGame.getLocalPlayer());
            }
        };
        exitButton.hAnchor = HorizontalAnchor.LEFT_ALIGNED;
        exitButton.vAnchor = VerticalAnchor.BOTTOM_ALIGNED;
        exitButton.setText("Exit");
        uiObjects.add(exitButton);
        godModeUIObjects.add(exitButton);

        float cumButtonHeight = padding;

        selectMatBtn = new UIElement(padding, cumButtonHeight, btnWidth, btnHeight) {
            public void onClick() {
                super.onClick();
                sendMsg("Right click to select the material");
                GodMode.waitingToSelectMat = true;
            }
        };
        selectMatBtn.hAnchor = HorizontalAnchor.LEFT_ALIGNED;
        selectMatBtn.vAnchor = VerticalAnchor.TOP_ALIGNED;
        selectMatBtn.setText("Select Material");
        uiObjects.add(selectMatBtn);
        godModeUIObjects.add(selectMatBtn);
        cumButtonHeight += btnHeight + padding;

        UIElement fillBtn = new UIElement(padding, cumButtonHeight, btnWidth, btnHeight) {
            public void onClick() {
                super.onClick();
                if(selection == null){
                    sendMsg("Select an area first");
                    return;
                }
                if(selectedBlock == null){
                    sendMsg("Select a material first");
                    return;
                }
                SpatialManipulator.SpatialManipulatorAction action = new SpatialManipulator.SpatialManipulatorAction(SpatialManipulator.Mode.FILL, selectedBlock, selection);
                action.apply(InGame.getLocalPlayer().getZone());
                TemporalManipulator.addToUndoStack(action);
            }
        };
        fillBtn.hAnchor = HorizontalAnchor.LEFT_ALIGNED;
        fillBtn.vAnchor = VerticalAnchor.TOP_ALIGNED;
        fillBtn.setText("Fill");
        uiObjects.add(fillBtn);
        godModeUIObjects.add(fillBtn);
        cumButtonHeight += btnHeight + padding;
    }

    public static void renderGodMode(Player p) {
        if (sr == null) {
            sr = new ShapeRenderer();
        }
        RaycastUtils.HitInfo hitInfo = raycaster.raycast(p);

        if (hitInfo != null) {
            BlockPosition cursorPos = hitInfo.blockPosition();
            if (cursorPos != null) {
                Vector3 selectedPos = floorVector(new Vector3(cursorPos.getGlobalX(), cursorPos.getGlobalY(), cursorPos.getGlobalZ()));
                BoundingBox cursor = new BoundingBox(selectedPos.cpy().sub(eps), selectedPos.cpy().add(1 + eps));
                BoundingBoxUtils.drawBB(sr, cursor, cursorFill, Color.BLACK);

                if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                    if(waitingToSelectMat){
                        waitingToSelectMat = false;
                        justSelectedMat = true;
                        selectedBlock = cursorPos.getBlockState();
                        if(selectedBlock == null) selectedBlock = Block.AIR.getDefaultBlockState();
                        selectMatBtn.setText("Mat: " + selectedBlock.getBlock().getName());
                    } else if(!justSelectedMat) {
                        if (!dragging) {
                            dragging = true;
                            pos1 = selectedPos;
                            pos2 = null;
                            visualSelection = null;
                        } else {
                            pos2 = selectedPos;
                            Vector3 minVec = new Vector3(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.min(pos1.z, pos2.z));
                            Vector3 maxVec = new Vector3(Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y), Math.max(pos1.z, pos2.z));
                            selection = new BoundingBox(minVec, maxVec);
                            visualSelection = new BoundingBox(minVec.sub(eps), maxVec.add(eps + 1));
                        }
                    }
                } else {
                    dragging = false;
                    justSelectedMat = false;
                }
            }
        }


        if (pos1 != null && pos2 != null && visualSelection != null) {
            BoundingBoxUtils.drawBB(sr, visualSelection, selectionFill, Color.BLACK);
        }
    }

    private static Vector3 floorVector(Vector3 vec) {
        return new Vector3((float) Math.floor(vec.x), (float) Math.floor(vec.y), (float) Math.floor(vec.z));
    }
}
