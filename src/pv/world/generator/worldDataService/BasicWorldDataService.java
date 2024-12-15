package pv.world.generator.worldDataService;

import pv.util.noise.FractionalBrownianNoise;
import pv.util.position.WorldPosition;
import pv.world.structure.block.Block;

import java.util.HashMap;
import java.util.Map;

public class BasicWorldDataService implements WorldDataService {
    public static final int WATER_LEVEL = 4;
    private final Map<String, Block> blocks;
    private final FractionalBrownianNoise worldNoise;

    public BasicWorldDataService(FractionalBrownianNoise worldNoise) {
        this.worldNoise = worldNoise;
        blocks = new HashMap<>();
    }

    public void addBlock(String modName, Block block) {
        blocks.put(modName + ":" + block.getID(), block);
    }

    public Block getBlock(String id) {
        return blocks.get(id);
    }

    @Override
    public Block getBlockAt(WorldPosition position) {
        int height = (int) (
                worldNoise.generate(
                        position.x(), position.z()
                ) * 32
        );
        Block block = null;
        if (position.y() < height) {
            block = getBlock("core:stone_block");
        }
//        } else if (position.y() < WATER_LEVEL) {
//            block = getBlock(
//                    position.y() == height ?
//                            "core:sand_block" :
//                            "core:stone_block"
//            );
//        } else {
//            block = getBlock(
//                    position.y() == height ?
//                            "core:grass_block" :
//                            "core:stone_block"
//            );
//        }
        return block;
    }
}
