package me.nabdev.ecliptic.godmode;

import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.UIObject;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import me.nabdev.ecliptic.items.SpatialManipulator;
import me.nabdev.ecliptic.items.TemporalManipulator;
import me.nabdev.ecliptic.threading.FillingThread;

import static me.nabdev.ecliptic.godmode.GodMode.*;
import static me.nabdev.ecliptic.utils.ChatHelper.sendMsg;

// TODO: CRButton?
@SuppressWarnings("removal")
public class GodModeUI {
    private static final float btnWidth = 180.0F;
    private static final float btnHeight = 60.0F;

    public static void createButtons(Array<UIObject> uiObjects) {
        godModeUIObjects.clear();
        uiObjects.clear();

        float padding = 10.0F;


        addButton(uiObjects, padding, -padding, btnWidth, "Exit", () -> GodModeItem.turnOff(InGame.getLocalPlayer()), HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
        addButton(uiObjects, -padding, -padding, btnWidth / 2, "Redo", () -> TemporalManipulator.redo(InGame.getLocalPlayer()), HorizontalAnchor.RIGHT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);
        addButton(uiObjects, -padding - btnWidth / 2 - padding, -padding, btnWidth / 2, "Undo", () -> TemporalManipulator.undo(InGame.getLocalPlayer()), HorizontalAnchor.RIGHT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED);

        float cumButtonHeight = padding;
        cumButtonHeight = addMaterialSelectionButton(uiObjects, padding, cumButtonHeight);
        cumButtonHeight = addFillButton(uiObjects, padding, cumButtonHeight);
        cumButtonHeight = addReplaceButton(uiObjects, padding, cumButtonHeight);
        addCopyButton(uiObjects, padding, cumButtonHeight);
    }

    private static void addButton(Array<UIObject> uiObjects, float x, float y, float width, String text, Runnable onClick, HorizontalAnchor hAnchor, VerticalAnchor vAnchor) {
        var button = new finalforeach.cosmicreach.ui.UIElement(x, y, width, btnHeight) {
            @Override
            public void onClick() {
                super.onClick();
                onClick.run();
            }
        };
        button.setText(text);
        button.hAnchor = hAnchor;
        button.vAnchor = vAnchor;
        uiObjects.add(button);
        godModeUIObjects.add(button);
    }


    private static float addMaterialSelectionButton(Array<UIObject> uiObjects, float x, float y) {
        selectMatBtn = new finalforeach.cosmicreach.ui.UIElement(x, y, GodModeUI.btnWidth, GodModeUI.btnHeight) {
            @Override
            public void onClick() {
                super.onClick();
                sendMsg("Left click to select the material, or right click for air");
                GodMode.waitingToSelectMat = true;
            }
        };
        if (selectedBlock == null) selectMatBtn.setText("Select Material");
        else selectMatBtn.setText("Mat: " + selectedBlock.getName());
        selectMatBtn.hAnchor = HorizontalAnchor.LEFT_ALIGNED;
        selectMatBtn.vAnchor = VerticalAnchor.TOP_ALIGNED;
        uiObjects.add(selectMatBtn);
        godModeUIObjects.add(selectMatBtn);
        return y + GodModeUI.btnHeight + 10.0f;
    }

    private static float addFillButton(Array<UIObject> uiObjects, float x, float y) {
        addButton(uiObjects, x, y, GodModeUI.btnWidth, "Fill", () -> {
            if (GodMode.pos1 == null || GodMode.pos2 == null) {
                sendMsg("Select an area first");
                return;
            }
            if (GodMode.selectedBlock == null) {
                sendMsg("Select a material first");
                return;
            }
            var action = new SpatialManipulator.SpatialManipulatorAction(SpatialManipulator.Mode.FILL, GodMode.selectedBlock, GodMode.selection);
            action.apply(InGame.getLocalPlayer().getZone(), true);
            TemporalManipulator.addToUndoStack(action);
        }, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.TOP_ALIGNED);
        return y + btnHeight + 10.0f;
    }

    private static float addReplaceButton(Array<UIObject> uiObjects, float x, float y) {
        addButton(uiObjects, x, y, GodModeUI.btnWidth, "Replace", () -> {
            if (GodMode.pos1 == null || GodMode.pos2 == null) {
                sendMsg("Select an area first");
                return;
            }
            if (GodMode.selectedBlock == null) {
                sendMsg("Select a material first");
                return;
            }
            sendMsg("Select the block to replace (right click for air)");
            GodMode.waitingToSelectReplaceMat = true;
        }, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.TOP_ALIGNED);
        return y + btnHeight + 10.0f;
    }

    private static void addCopyButton(Array<UIObject> uiObjects, float x, float y) {
        addButton(uiObjects, x, y, GodModeUI.btnWidth, "Copy", () -> {
            if (GodMode.pos1 == null || GodMode.pos2 == null) {
                sendMsg("Select an area first");
                return;
            }
            FillingThread.post(SpatialManipulator.clipboard, InGame.getLocalPlayer().getZone(), GodMode.selectedBlock, GodMode.selection, (b) -> false, (t, n) -> {
                sendMsg("Copied " + (int) ((GodMode.selection.getWidth() + 1) * (GodMode.selection.getHeight() + 1) * (GodMode.selection.getDepth() + 1)) + " block(s) to clipboard");
                SpatialManipulator.clipboardNeedsRemeshing.set(true);
            });
        }, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.TOP_ALIGNED);
    }
}
