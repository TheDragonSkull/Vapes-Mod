package net.thedragonskull.vapemod.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.custom.VapeCatalog;
import net.thedragonskull.vapemod.block.custom.VapeExpositor;
import net.thedragonskull.vapemod.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, VapeMod.MOD_ID);

    public static final RegistryObject<Block> VAPE_EXPOSITOR = registerBlock("vape_expositor",
            () -> new VapeExpositor(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> VAPE_CATALOG = registerBlock("vape_catalog",
            () -> new VapeCatalog(BlockBehaviour.Properties.of().instabreak().mapColor(MapColor.TERRACOTTA_YELLOW)
                    .sound(SoundType.CHISELED_BOOKSHELF).noOcclusion().ignitedByLava()));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
