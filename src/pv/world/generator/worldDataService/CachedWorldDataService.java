package pv.world.generator.worldDataService;

import pv.settings.ConstantGameSettings;
import pv.util.cache.LRUCache;
import pv.util.position.WorldPosition;
import pv.world.structure.block.Block;

public class CachedWorldDataService implements WorldDataService {
    private final WorldDataService next;
    private final LRUCache<WorldPosition, Block> cache;

    public CachedWorldDataService(WorldDataService next) {
        this.next = next;
        this.cache = new LRUCache<>(ConstantGameSettings.BLOCK_CACHE_SIZE);
    }

    @Override
    public Block getBlockAt(WorldPosition worldPosition) {
        Block block = cache.get(worldPosition);
        if (block == null) {
            block = next.getBlockAt(worldPosition);
            cache.put(worldPosition, block);
        }
        return block;
    }
}