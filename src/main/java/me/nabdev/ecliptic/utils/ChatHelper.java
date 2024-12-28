package me.nabdev.ecliptic.utils;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.chat.Chat;

public class ChatHelper {
    public static String blockPosToString(BlockPosition pos) {
        return "(" + pos.getGlobalX() + ", " + pos.getGlobalY() + ", " + pos.getGlobalZ() + ")";
    }

    public static void sendMsg(String msg) {
        Chat.MAIN_CLIENT_CHAT.addMessage(null, "[Ecliptic] " + msg);
    }
}
