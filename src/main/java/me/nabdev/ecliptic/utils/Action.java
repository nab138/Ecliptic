package me.nabdev.ecliptic.utils;

import finalforeach.cosmicreach.world.Zone;

public interface Action {
    void apply(Zone zone, boolean verbose);
    void apply(Zone zone);
    void undo(Zone zone, Runnable after, Runnable ifFailed);
    String getName();
}
