package me.nabdev.ecliptic.threading;

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
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FillingThread implements Runnable {

        public static Logger LOGGER = LoggerFactory.getLogger("Ecliptic Filling Thread");
        public static FillingThread INSTANCE;
        public static PauseableThread parent;

        static final Queue<Runnable> queuedRunnables = new Queue<>();

        public static void post(AtomicReference<BlockState[][][]> oldBlocksRef, Zone zone, BlockState block, BoundingBox boundingBox, Function<BlockState, Boolean> filter, BiConsumer<Long, Integer> onDone) {
            queuedRunnables.addLast(() -> {
                synchronized (SpatialManipulator.isRunning) {
                    SpatialManipulator.isRunning.set(true);
                    int numBlocks = 0;
                    long startTime = System.nanoTime();
                    synchronized (oldBlocksRef) {
                        int minX = (int) Math.floor(boundingBox.min.x);
                        int minY = (int) Math.floor(boundingBox.min.y);
                        int minZ = (int) Math.floor(boundingBox.min.z);
                        int maxX = (int) Math.ceil(boundingBox.max.x);
                        int maxY = (int) Math.ceil(boundingBox.max.y);
                        int maxZ = (int) Math.ceil(boundingBox.max.z);

                        BlockState[][][] oldBlocks = new BlockState[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];

                            for (int x = minX; x <= maxX; x++) {
                                for (int z = minZ; z <= maxZ; z++) {
                                    for (int y = minY; y <= maxY; y++) {
                                        oldBlocks[x - minX][y - minY][z - minZ] = BlockUtil.getBlockPosAtVec(zone, x, y, z).getBlockState();
                                        if (filter == null || filter.apply(BlockUtil.getBlockPosAtVec(zone, x, y, z).getBlockState())) {
                                            BlockUtil.setBlockAt(zone, block, x, y, z);
                                            numBlocks++;
                                        }
                                    }
                                }
                            }

                        oldBlocksRef.set(oldBlocks);
                    }
                    if (onDone != null) onDone.accept(System.nanoTime() - startTime, numBlocks);
                    SpatialManipulator.isRunning.set(false);

                }
            });

            if (!started) start();
            else parent.onResume();
        }

    public static void post(AtomicReference<BlockState[][][]> oldBlocksRef, Zone zone, BlockState[][][] blocks, BoundingBox boundingBox, Runnable onDone, boolean nullIsAir, boolean fillOldBlocks) {
        queuedRunnables.addLast(() -> {
            synchronized (SpatialManipulator.isRunning) {
                SpatialManipulator.isRunning.set(true);
                synchronized (oldBlocksRef) {
                    int minX = (int) Math.floor(boundingBox.min.x);
                    int minY = (int) Math.floor(boundingBox.min.y);
                    int minZ = (int) Math.floor(boundingBox.min.z);
                    int maxX = (int) Math.ceil(boundingBox.max.x);
                    int maxY = (int) Math.ceil(boundingBox.max.y);
                    int maxZ = (int) Math.ceil(boundingBox.max.z);

                    BlockState[][][] oldBlocks = new BlockState[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];

                    for (int x = minX; x <= maxX; x++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            for (int y = minY; y <= maxY; y++) {
                                if(fillOldBlocks) oldBlocks[x - minX][y - minY][z - minZ] = BlockUtil.getBlockPosAtVec(zone, x, y, z).getBlockState();
                                BlockState block = blocks[x - minX][y - minY][z - minZ];
                                if (block == null) {
                                    if (nullIsAir) block = Block.AIR.getDefaultBlockState();
                                    else continue;
                                }
                                BlockUtil.setBlockAt(zone, block, x, y, z);
                            }
                        }
                    }
                    if(fillOldBlocks) oldBlocksRef.set(oldBlocks);
                }
                if (onDone != null) onDone.run();
                SpatialManipulator.isRunning.set(false);
            }
        });

        if (!started) start();
        else parent.onResume();
    }

    private static final AtomicReference<BlockState[][][]> EMPTY = new AtomicReference<>(null);
    public static void post(Zone zone, BlockState[][][] blocks, BoundingBox boundingBox, Runnable onDone, boolean nullIsAir) {
        post(EMPTY, zone, blocks, boundingBox, onDone, nullIsAir, false);
    }

    public static void post(AtomicReference<BlockState[][][]> oldBlocksRef, Zone zone, BlockState[][][] blocks, BoundingBox boundingBox, Runnable onDone, boolean nullIsAir) {
        post(oldBlocksRef, zone, blocks, boundingBox, onDone, nullIsAir, true);
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
