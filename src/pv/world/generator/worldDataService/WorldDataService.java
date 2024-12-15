package pv.world.generator.worldDataService;

import pv.util.position.WorldPosition;
import pv.world.structure.block.Block;

public interface WorldDataService {
    Block getBlockAt(WorldPosition worldPosition);
}