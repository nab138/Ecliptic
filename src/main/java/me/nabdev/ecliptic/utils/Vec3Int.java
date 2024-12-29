package me.nabdev.ecliptic.utils;

import com.badlogic.gdx.math.Vector3;

public class Vec3Int {
    public int x;
    public int y;
    public int z;

    public Vec3Int(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public Vector3 toVector3() {
        return new Vector3(this.x, this.y, this.z);
    }
}
