package me.nabdev.ecliptic;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.SharedQuadIndexData;
import finalforeach.cosmicreach.rendering.blockmodels.BlockModelJson;
import finalforeach.cosmicreach.rendering.entities.EntityModelInstance;
import finalforeach.cosmicreach.rendering.entities.IEntityModel;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.rendering.meshes.GameMesh;
import finalforeach.cosmicreach.rendering.shaders.ChunkShader;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Sky;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class BlockMesh implements IEntityModelInstance {

    GameShader shader;
    AtomicReference<BlockState[][][]> state;
    public boolean needsRemesh = true;


    GameMesh mesh;

    public BlockMesh(AtomicReference<BlockState[][][]> state) {
        this.state = state;

        shader = ChunkShader.DEFAULT_BLOCK_SHADER;
    }

    @Override
    public IEntityModel getModel() {
        return new IEntityModel() {
            @Override
            public IEntityModelInstance getNewModelInstance() {
                EntityModelInstance instance = new EntityModelInstance();
                instance.setEntityModel(this);
                return instance;
            }
        };
    }

    @Override
    public void setTint(float v, float v1, float v2, float v3) {

    }

    Vector3 sunDirection = new Vector3();

    Matrix4 rotTmp = new Matrix4();

    @Override
    public void render(Entity _entity, Camera camera, Matrix4 tmp) {
        rotTmp.idt();
        rotTmp.set(tmp.getRotation(new Quaternion()));
        Sky.currentSky.getSunDirection(sunDirection);
        sunDirection.rot(rotTmp);
        short[] blockLightLevels = new short[8];
        int[] skyLightLevels = new int[8];
        Arrays.fill(blockLightLevels, (short) 4095);

        if (needsRemesh) {
            BlockState airBlockState = Block.AIR.getDefaultBlockState();
            MeshData data = null;
            //state.get().addVertices(data, 0, 0, 0);
            for(int x = 0; x < state.get().length; x++){
                for(int y = 0; y < state.get()[0].length; y++){
                    for(int z = 0; z < state.get()[0][0].length; z++) {
                        BlockState b = state.get()[x][y][z];
                        if(b == null) b = airBlockState;
                        if (b != airBlockState) {
                            BlockState bnx = getBlockState(x - 1, y, z);
                            BlockState bpx = getBlockState(x + 1, y, z);
                            BlockState bny = getBlockState(x, y - 1, z);
                            BlockState bpy = getBlockState(x, y + 1, z);
                            BlockState bnz = getBlockState(x, y, z - 1);
                            BlockState bpz = getBlockState(x, y, z + 1);
                            int completeCullMask = 0;
                            completeCullMask |= bnx == null || !bnx.isPosXFaceOccluding ? 0 : 1;
                            completeCullMask |= bpx == null || !bpx.isNegXFaceOccluding ? 0 : 2;
                            completeCullMask |= bny == null || !bny.isPosYFaceOccluding ? 0 : 4;
                            completeCullMask |= bpy == null || !bpy.isNegYFaceOccluding ? 0 : 8;
                            completeCullMask |= bnz == null || !bnz.isPosZFaceOccluding ? 0 : 16;
                            completeCullMask |= bpz == null || !bpz.isNegZFaceOccluding ? 0 : 32;
                            if (completeCullMask != 63) {
                                int opaqueBitmask = 0;
                                Block block = b.getBlock();
                                boolean cullsSelf = b.cullsSelf();
                                opaqueBitmask |= bnx == null || !bnx.isPosXFaceOccluding && (!cullsSelf || block != bnx.getBlock() || !bnx.isSelfPosXFaceOccluding) ? 0 : 1;
                                opaqueBitmask |= bpx == null || !bpx.isNegXFaceOccluding && (!cullsSelf || block != bpx.getBlock() || !bpx.isSelfNegXFaceOccluding) ? 0 : 2;
                                opaqueBitmask |= bny == null || !bny.isPosYFaceOccluding && (!cullsSelf || block != bny.getBlock() || !bny.isSelfPosYFaceOccluding) ? 0 : 4;
                                opaqueBitmask |= bpy == null || !bpy.isNegYFaceOccluding && (!cullsSelf || block != bpy.getBlock() || !bpy.isSelfNegYFaceOccluding) ? 0 : 8;
                                opaqueBitmask |= bnz == null || !bnz.isPosZFaceOccluding && (!cullsSelf || block != bnz.getBlock() || !bnz.isSelfPosZFaceOccluding) ? 0 : 16;
                                opaqueBitmask |= bpz == null || !bpz.isNegZFaceOccluding && (!cullsSelf || block != bpz.getBlock() || !bpz.isSelfNegZFaceOccluding) ? 0 : 32;
                                BlockState bnxnynz = getBlockState(x - 1, y - 1, z - 1);
                                BlockState bnxny0z = getBlockState(x - 1, y - 1, z);
                                BlockState bnxnypz = getBlockState(x - 1, y - 1, z + 1);
                                BlockState bnx0ynz = getBlockState(x - 1, y, z - 1);
                                BlockState bnx0ypz = getBlockState(x - 1, y, z + 1);
                                BlockState bnxpynz = getBlockState(x - 1, y + 1, z - 1);
                                BlockState bnxpy0z = getBlockState(x - 1, y + 1, z);
                                BlockState bnxpypz = getBlockState(x - 1, y + 1, z + 1);
                                BlockState b0xnynz = getBlockState(x, y - 1, z - 1);
                                BlockState b0xnypz = getBlockState(x, y - 1, z + 1);
                                BlockState b0xpynz = getBlockState(x, y + 1, z - 1);
                                BlockState b0xpypz = getBlockState(x, y + 1, z + 1);
                                BlockState bpxnynz = getBlockState(x + 1, y - 1, z - 1);
                                BlockState bpxny0z = getBlockState(x + 1, y - 1, z);
                                BlockState bpxnypz = getBlockState(x + 1, y - 1, z + 1);
                                BlockState bpx0ynz = getBlockState(x + 1, y, z - 1);
                                BlockState bpx0ypz = getBlockState(x + 1, y, z + 1);
                                BlockState bpxpynz = getBlockState(x + 1, y + 1, z - 1);
                                BlockState bpxpy0z = getBlockState(x + 1, y + 1, z);
                                BlockState bpxpypz = getBlockState(x + 1, y + 1, z + 1);
                                opaqueBitmask |= bnxnynz != null && bnxnynz.isOpaque ? 64 : 0;
                                opaqueBitmask |= bnxny0z != null && bnxny0z.isOpaque ? 128 : 0;
                                opaqueBitmask |= bnxnypz != null && bnxnypz.isOpaque ? 256 : 0;
                                opaqueBitmask |= bnx0ynz != null && bnx0ynz.isOpaque ? 512 : 0;
                                opaqueBitmask |= bnx0ypz != null && bnx0ypz.isOpaque ? 1024 : 0;
                                opaqueBitmask |= bnxpynz != null && bnxpynz.isOpaque ? 2048 : 0;
                                opaqueBitmask |= bnxpy0z != null && bnxpy0z.isOpaque ? 4096 : 0;
                                opaqueBitmask |= bnxpypz != null && bnxpypz.isOpaque ? 8192 : 0;
                                opaqueBitmask |= b0xnynz != null && b0xnynz.isOpaque ? 16384 : 0;
                                opaqueBitmask |= b0xnypz != null && b0xnypz.isOpaque ? '耀' : 0;
                                opaqueBitmask |= b0xpynz != null && b0xpynz.isOpaque ? 65536 : 0;
                                opaqueBitmask |= b0xpypz != null && b0xpypz.isOpaque ? 131072 : 0;
                                opaqueBitmask |= bpxnynz != null && bpxnynz.isOpaque ? 262144 : 0;
                                opaqueBitmask |= bpxny0z != null && bpxny0z.isOpaque ? 524288 : 0;
                                opaqueBitmask |= bpxnypz != null && bpxnypz.isOpaque ? 1048576 : 0;
                                opaqueBitmask |= bpx0ynz != null && bpx0ynz.isOpaque ? 2097152 : 0;
                                opaqueBitmask |= bpx0ypz != null && bpx0ypz.isOpaque ? 4194304 : 0;
                                opaqueBitmask |= bpxpynz != null && bpxpynz.isOpaque ? 8388608 : 0;
                                opaqueBitmask |= bpxpy0z != null && bpxpy0z.isOpaque ? 16777216 : 0;
                                opaqueBitmask |= bpxpypz != null && bpxpypz.isOpaque ? 33554432 : 0;
                                GameShader shader = GameShader.getShaderForBlockState(b);

                                RenderOrder renderOrder = RenderOrder.getRenderOrderForBlockState(b);
                                if(data == null) data = new MeshData(shader, renderOrder);


                                b.addVertices(data, x, y, z, opaqueBitmask, blockLightLevels, skyLightLevels);
                            }
                        }
                    }
                }
            }

            if(data == null) return;
            if (BlockModelJson.useIndices) {
                mesh = data.toIntIndexedMesh(true);
            } else {
                mesh = data.toSharedIndexMesh(true);
                if (mesh != null) {
                    int numIndices = (mesh.getNumVertices() * 6) / 4;
                    SharedQuadIndexData.allowForNumIndices(numIndices, false);
                }
            }
            needsRemesh = false;
        }

        renderBlock(camera, tmp);

    }

    public void renderBlock(Camera camera, Matrix4 tmp) {
        if (mesh != null) {
            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.bind();
            }

            Vector3 batchPos = new Vector3(-.5f, -.5f, -.5f);
            try {
                this.shader.bind(camera);
                this.shader.bindOptionalMatrix4("u_projViewTrans", camera.combined);
//                this.shader.bindOptionalUniform4f("tintColor", Sky.currentSky.currentAmbientColor.cpy());
                this.shader.bindOptionalMatrix4("u_modelMat", tmp);
                this.shader.bindOptionalUniform3f("u_batchPosition", batchPos);
                this.shader.bindOptionalUniform3f("u_sunDirection", sunDirection);

                mesh.bind(this.shader.shader);
                mesh.render(this.shader.shader, GL20.GL_TRIANGLES);
                mesh.unbind(this.shader.shader);

                this.shader.unbind();
            } catch (Exception ignore) {}

            if (!BlockModelJson.useIndices) {
                SharedQuadIndexData.unbind();
            }

        }
    }

    @Override
    public Color getCurrentAmbientColor() {
        return Color.WHITE.cpy();
    }

    @Override
    public void setCurrentAnimation(String s) {

    }

    @Override
    public void setEntityModel(IEntityModel iEntityModel) {

    }

    private BlockState getBlockState(int x, int y, int z) {
        if(x < 0 || y < 0 || z < 0 || x >= state.get().length || y >= state.get()[0].length || z >= state.get()[0][0].length) return null;
        return state.get()[x][y][z];
    }
}
