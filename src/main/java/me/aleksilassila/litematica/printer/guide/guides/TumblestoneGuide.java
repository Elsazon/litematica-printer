package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.IdentifierUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Cobblemon tumblestone buds (cobblemon:small_budding_tumblestone, small_budding_black_tumblestone,
 * small_budding_sky_tumblestone).
 * <p>
 * Cobblemon has no item for the bud itself: you plant one by using the tumblestone item
 * (cobblemon:tumblestone, black_tumblestone, sky_tumblestone) on the face of the block it sits on.
 * Without this guide the printer asks for the bud block's own item, which is air, so it never places them.
 * Here the printer gets the right item and clicks the face of the block behind the bud (the side opposite
 * its "facing"), the same way amethyst buds are placed.
 */
public class TumblestoneGuide extends Guide {
    private static final String PREFIX = "small_budding_";

    private final Identifier blockId;

    public TumblestoneGuide(SchematicBlockContext context) {
        super(context);
        this.blockId = BlockUtils.getKey(requiredBlock);
    }

    @Override
    protected boolean canExecute() {
        return "cobblemon".equals(blockId.getNamespace())
                && blockId.getPath().startsWith(PREFIX)
                && blockId.getPath().endsWith("tumblestone");
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Item seed = getItem(IdentifierUtils.of("cobblemon", blockId.getPath().substring(PREFIX.length())));
        Direction facing = getFacing();
        if (seed == null || seed == Items.AIR || facing == null) {
            return Result.PASS;
        }
        return Result.success(new Action()
                .setItem(seed)
                .setSides(facing.getOpposite())
                .setRequiresSupport());
    }

    private Direction getFacing() {
        Property<?> property = requiredBlock.getStateDefinition().getProperty("facing");
        if (property == null) {
            return null;
        }
        Object value = requiredState.getValue(property);
        return value instanceof Direction direction ? direction : null;
    }

    private static Item getItem(Identifier itemId) {
        //#if MC > 12101
        return BuiltInRegistries.ITEM.getValue(itemId);
        //#else
        //$$ return BuiltInRegistries.ITEM.get(itemId);
        //#endif
    }
}
