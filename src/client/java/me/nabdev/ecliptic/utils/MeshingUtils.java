package me.nabdev.ecliptic.utils;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MeshingUtils {
    public record MeshData(BlockMesh mesh, Matrix4 matrix) {
    }

    private final static int MAX_MESH_SIZE = 64;

    public static List<MeshData> createMeshesFromBlocks(BlockState[][][] blocks, Vector3 pos1) {
        List<MeshData> meshes = new ArrayList<>();
        int xSize = blocks.length;
        int ySize = blocks[0].length;
        int zSize = blocks[0][0].length;

        for (int x = 0; x < xSize; x += MAX_MESH_SIZE) {
            for (int y = 0; y < ySize; y += MAX_MESH_SIZE) {
                for (int z = 0; z < zSize; z += MAX_MESH_SIZE) {
                    int xEnd = Math.min(x + MAX_MESH_SIZE, xSize);
                    int yEnd = Math.min(y + MAX_MESH_SIZE, ySize);
                    int zEnd = Math.min(z + MAX_MESH_SIZE, zSize);

                    BlockState[][][] subClipboard = new BlockState[xEnd - x][yEnd - y][zEnd - z];
                    boolean notAir = false;
                    for (int i = x; i < xEnd; i++) {
                        for (int j = y; j < yEnd; j++) {
                            for (int k = z; k < zEnd; k++) {
                                subClipboard[i - x][j - y][k - z] = blocks[i][j][k];
                                if (!notAir && (blocks[i][j][k] == null || !blocks[i][j][k].getSaveKey().equals(Block.AIR.getDefaultBlockState().getSaveKey()))) {
                                    notAir = true;
                                }
                            }
                        }
                    }
                    Vector3 offset = new Vector3(x, y, z);
                    Matrix4 matrix = new Matrix4().translate(pos1).translate(offset).translate(0.5f, 0.5f, 0.5f);
                    BlockMesh mesh = new BlockMesh(new AtomicReference<>(subClipboard));
                    if(notAir) meshes.add(new MeshData(mesh, matrix));
                }
            }
        }
        return meshes;
    }
}
