package me.nabdev.ecliptic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.PooledBlockPosition;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.ecliptic.utils.RaycastUtils;

public class Raycaster implements RaycastUtils.IRaycaster {

    private static final int MAX_DISTANCE = 100;
    private final Ray ray = new Ray();
    private final Array<BlockPosition> toVisit = new Array<>();
    private final Vector3 workingPos = new Vector3();

    private final Queue<BlockPosition> blockQueue = new Queue<>();
    private static final Array<BlockPosition> positionsToFree = new Array<>();
    private final Vector3 mouseCoords = new Vector3();
    private final Vector3 mouseCoords2 = new Vector3();
    private final BoundingBox tmpBoundingBox = new BoundingBox();
    private final Array<BoundingBox> tmpBoundingBoxes = new Array<>(BoundingBox.class);
    private final Vector3 normal = new Vector3();

    private  final Vector3 intersection = new Vector3();
    static Pool<BlockPosition> positionPool = new Pool<>() {
        protected BlockPosition newObject() {
            PooledBlockPosition<BlockPosition> p = new PooledBlockPosition<>(Raycaster.positionPool, null, 0, 0, 0);
            Raycaster.positionsToFree.add(p);
            return p;
        }
    };

    private void addBlockToQueue(Zone zone, BlockPosition bp, int dx, int dy, int dz) {
        BlockPosition step = bp.getOffsetBlockPos(positionPool, zone, dx, dy, dz);
        if (step != null && !toVisit.contains(step, false)) {
            BlockState block = bp.getBlockState();
            if (block != null) {
                block.getBoundingBox(tmpBoundingBox, step);
                if (Intersector.intersectRayBounds(ray, tmpBoundingBox, intersection)) {
                    blockQueue.addLast(step);
                    toVisit.add(step);
                }
            }
        }
    }

    private boolean intersectsWithBlock(BlockState block, BlockPosition nextBlockPos) {
        block.getBoundingBox(tmpBoundingBox, nextBlockPos);
        if (!Intersector.intersectRayBounds(ray, tmpBoundingBox, intersection)) {
            return false;
        } else {
            block.getAllBoundingBoxes(tmpBoundingBoxes, nextBlockPos);
            var var3 = tmpBoundingBoxes.iterator();
            BoundingBox bb;
            do {
                if (!var3.hasNext()) {
                    return false;
                }
                bb = var3.next();
            } while(!Intersector.intersectRayBounds(ray, bb, intersection));

            // Calculate the normal vector
            calculateNormal(bb, intersection, normal);
            return true;
        }
    }

    private void calculateNormal(BoundingBox bb, Vector3 intersection, Vector3 normal) {
        float epsilon = 0.001f;
        if (Math.abs(intersection.x - bb.min.x) < epsilon) {
            normal.set(-1, 0, 0);
        } else if (Math.abs(intersection.x - bb.max.x) < epsilon) {
            normal.set(1, 0, 0);
        } else if (Math.abs(intersection.y - bb.min.y) < epsilon) {
            normal.set(0, -1, 0);
        } else if (Math.abs(intersection.y - bb.max.y) < epsilon) {
            normal.set(0, 1, 0);
        } else if (Math.abs(intersection.z - bb.min.z) < epsilon) {
            normal.set(0, 0, -1);
        } else if (Math.abs(intersection.z - bb.max.z) < epsilon) {
            normal.set(0, 0, 1);
        } else {
            normal.set(0, 0, 0); // Default case, should not happen
        }
    }

    @Override
    public RaycastUtils.HitInfo raycast(Player p){
        Camera worldCamera = GameState.IN_GAME.getWorldCamera();
        Zone zone = p.getZone();
                    if (Gdx.input.isCursorCatched()) {
                        this.ray.set(worldCamera.position, worldCamera.direction);
                    } else {
                        this.mouseCoords.set((float) Gdx.input.getX(), (float) Gdx.input.getY(), 0.0F);
                        this.mouseCoords2.set((float) Gdx.input.getX(), (float) Gdx.input.getY(), 1.0F);
                        worldCamera.unproject(this.mouseCoords);
                        worldCamera.unproject(this.mouseCoords2);
                        this.mouseCoords2.sub(this.mouseCoords).nor();
                        this.ray.set(this.mouseCoords, this.mouseCoords2);
                    }


        boolean raycastHit = false;
        BlockPosition hitBlockPos = null;
        BlockPosition lastBlockPosAtPoint = null;
        toVisit.clear();
        blockQueue.clear();
        workingPos.set(ray.origin);
        for(; workingPos.dst(ray.origin) <= MAX_DISTANCE; workingPos.add(ray.direction)) {
            int bx = (int)Math.floor(workingPos.x);
            int by = (int)Math.floor(workingPos.y);
            int bz = (int)Math.floor(workingPos.z);
            int dx = 0;
            int dy = 0;
            int dz = 0;
            if (lastBlockPosAtPoint != null) {
                if (lastBlockPosAtPoint.isAtGlobal(bx, by, bz)) {
                    continue;
                }
                dx = bx - lastBlockPosAtPoint.getGlobalX();
                dy = by - lastBlockPosAtPoint.getGlobalY();
                dz = bz - lastBlockPosAtPoint.getGlobalZ();
            }
            Chunk c = zone.getChunkAtBlock(bx, by, bz);
            if (c == null) {
                continue;
            }
            BlockPosition nextBlockPos = positionPool.obtain();
            nextBlockPos.set(c, bx - c.blockX, by - c.blockY, bz - c.blockZ);
            if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1) {
                if (dx != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, dx, 0, 0);
                }
                if (dy != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, 0, dy, 0);
                }
                if (dz != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, 0, 0, dz);
                }
                if (dx != 0 && dy != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, dx, dy, 0);
                }
                if (dx != 0 && dz != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, dx, 0, dz);
                }
                if (dy != 0 && dz != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, 0, dy, dz);
                }
            }
            if (!toVisit.contains(nextBlockPos, false)) {
                BlockState block = nextBlockPos.getBlockState();
                block.getBoundingBox(tmpBoundingBox, nextBlockPos);
                if (Intersector.intersectRayBounds(ray, tmpBoundingBox, intersection)) {
                    blockQueue.addLast(nextBlockPos);
                    toVisit.add(nextBlockPos);
                } else if (block.canRaycastForReplace()) {
                    tmpBoundingBox.min.set((float)nextBlockPos.getGlobalX(), (float)nextBlockPos.getGlobalY(), (float)nextBlockPos.getGlobalZ());
                    tmpBoundingBox.max.set(tmpBoundingBox.min).add(1.0F, 1.0F, 1.0F);
                    if (Intersector.intersectRayBounds(ray, tmpBoundingBox, intersection)) {
                        blockQueue.addLast(nextBlockPos);
                        toVisit.add(nextBlockPos);
                    }
                }
            }
            label186:
            while(true) {
                BlockState blockState;
                BlockPosition curBlockPos;
                do {
                    if (!blockQueue.notEmpty()) {
                        break label186;
                    }
                    curBlockPos = blockQueue.removeFirst();
                    blockState = curBlockPos.getBlockState();
                } while(!blockState.hasEmptyModel() && !intersectsWithBlock(blockState, curBlockPos));
                if (blockState.canRaycastForBreak()) {
                    hitBlockPos = curBlockPos;
                    raycastHit = true;
                }
                if (hitBlockPos != null){
                    break;
                }
            }
            if (raycastHit) {
                break;
            }
            lastBlockPosAtPoint = nextBlockPos;
        }
        positionPool.freeAll(positionsToFree);
        if (!raycastHit){
            return null;
        }
        return new RaycastUtils.HitInfo(hitBlockPos, normal);
    }
}
