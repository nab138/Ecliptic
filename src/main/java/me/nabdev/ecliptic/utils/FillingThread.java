package me.nabdev.ecliptic.utils;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.PauseableThread;
import com.badlogic.gdx.utils.Queue;
import com.github.puzzle.game.util.BlockUtil;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.ecliptic.items.SpatialManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class FillingThread implements Runnable {

        public static Logger LOGGER = LoggerFactory.getLogger("Ecliptic Filling Thread");
        public static FillingThread INSTANCE;
        public static PauseableThread parent;

        static final Queue<Runnable> queuedRunnables = new Queue<>();

        public static void post(AtomicReference<BlockState[][][]> oldBlocksRef, Zone zone, BlockState block, BoundingBox boundingBox, Function<BlockState, Boolean> filter, Consumer<Long> onDone) {
            queuedRunnables.addLast(() -> {
                synchronized (SpatialManipulator.isRunning) {
                    SpatialManipulator.isRunning.set(true);
                    long startTime = System.nanoTime();
                    synchronized (oldBlocksRef) {
                        int minX = (int) Math.floor(boundingBox.min.x);
                        int minY = (int) Math.floor(boundingBox.min.y);
                        int minZ = (int) Math.floor(boundingBox.min.z);
                        int maxX = (int) Math.ceil(boundingBox.max.x);
                        int maxY = (int) Math.ceil(boundingBox.max.y);
                        int maxZ = (int) Math.ceil(boundingBox.max.z);

                        BlockState[][][] oldBlocks = new BlockState[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];

                        int xSize = maxX - minX + 1;
                        int ySize = maxY - minY + 1;
                        int zSize = maxZ - minZ + 1;
                        if(ySize < xSize && ySize < zSize) {
                            for (int x = minX; x <= maxX; x++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    for (int y = minY; y <= maxY; y++) {
                                        oldBlocks[x - minX][y - minY][z - minZ] = BlockUtil.getBlockPosAtVec(zone, x, y, z).getBlockState();
                                        applyFill(zone, block, filter, x, y, z);
                                    }
                                }
                            }
                        } else if (xSize < ySize && xSize < zSize) {
                            for (int y = minY; y <= maxY; y++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    for (int x = minX; x <= maxX; x++) {
                                        oldBlocks[x - minX][y - minY][z - minZ] = BlockUtil.getBlockPosAtVec(zone, x, y, z).getBlockState();
                                        applyFill(zone, block, filter, x, y, z);
                                    }
                                }
                            }
                        } else {
                            for (int x = minX; x <= maxX; x++) {
                                for (int y = minY; y <= maxY; y++) {
                                    for (int z = minZ; z <= maxZ; z++) {
                                        oldBlocks[x - minX][y - minY][z - minZ] = BlockUtil.getBlockPosAtVec(zone, x, y, z).getBlockState();
                                        applyFill(zone, block, filter, x, y, z);
                                    }
                                }
                            }
                        }
                        oldBlocksRef.set(oldBlocks);
                    }
                    if (onDone != null) onDone.accept(System.nanoTime() - startTime);
                    SpatialManipulator.isRunning.set(false);
                }
            });

            if (!started) start();
            else parent.onResume();
        }

        private static void applyFill(Zone zone, BlockState block, Function<BlockState, Boolean> filter, int x, int y, int z) {
            if (filter == null || filter.apply(BlockUtil.getBlockPosAtVec(zone, x, y, z).getBlockState()))
                BlockUtil.setBlockAt(zone, block, x, y, z);
        }

    public static void post(Zone zone, BlockState[][][] blocks, BoundingBox boundingBox, Runnable onDone) {
        queuedRunnables.addLast(() -> {
            synchronized (SpatialManipulator.isRunning) {
                SpatialManipulator.isRunning.set(true);
                synchronized (zone) {
                    int minX = (int) Math.floor(boundingBox.min.x);
                    int minY = (int) Math.floor(boundingBox.min.y);
                    int minZ = (int) Math.floor(boundingBox.min.z);
                    int maxX = (int) Math.ceil(boundingBox.max.x);
                    int maxY = (int) Math.ceil(boundingBox.max.y);
                    int maxZ = (int) Math.ceil(boundingBox.max.z);

                    int xSize = maxX - minX + 1;
                    int ySize = maxY - minY + 1;
                    int zSize = maxZ - minZ + 1;
                    if(ySize < xSize && ySize < zSize) {
                        for (int x = minX; x <= maxX; x++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                for (int y = minY; y <= maxY; y++) {
                                    applyFill(blocks, minX, minY, minZ, zone, x, y, z);
                                }
                            }
                        }
                    } else if (xSize < ySize && xSize < zSize) {
                        for (int y = minY; y <= maxY; y++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    for (int x = minX; x <= maxX; x++) {
                                    applyFill(blocks, minX, minY, minZ, zone, x, y, z);
                                }
                            }
                        }
                    } else {
                        for (int x = minX; x <= maxX; x++) {
                            for (int y = minY; y <= maxY; y++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    applyFill(blocks, minX, minY, minZ, zone, x, y, z);
                                }
                            }
                        }
                    }
                }
                if (onDone != null) onDone.run();
                SpatialManipulator.isRunning.set(false);
            }
        });

        if (!started) start();
        else parent.onResume();
    }

    private static void applyFill(BlockState[][][] blocks, int minX, int minY, int minZ, Zone zone, int x, int y, int z) {
        BlockState block = blocks[x - minX][y - minY][z - minZ];
        if (block == null) {
            block = Block.AIR.getDefaultBlockState();
        }
        BlockUtil.setBlockAt(zone, block, x, y, z);
    }


        public FillingThread() {
            INSTANCE = this;
            parent = (PauseableThread) ThreadHelper.getThread("filling");
        }

        @Override
        public void run() {
            synchronized (queuedRunnables) {
                while (!queuedRunnables.isEmpty()) {
                    Runnable runnable = queuedRunnables.removeFirst();
                    if (runnable != null)
                        runnable.run();
                    else
                        LOGGER.warn("Uh oh, A null runnable was found on the `FillingThread`");
                }
            }
            parent.onPause();
        }

        public static boolean started = false;

        public static void clear() {
            synchronized (queuedRunnables) {
                queuedRunnables.clear();
            }

            parent.onPause();
        }

        public static void pause() {
            parent.onPause();
        }

        public static void resume() {
            parent.onResume();
        }

        public static void init() {
            parent = ThreadHelper.createPauseable("filling", new FillingThread());
        }

        public static PauseableThread start() {
            started = true;
            if (parent == null) {
                throw new RuntimeException("Call `init()` on the `FillingThread` first.");
            }
//        try {
            parent.start();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
            return parent;
        }
}

