package gg.moonflower.pollen.core;

import gg.moonflower.pollen.api.block.PollinatedLiquidBlock;
import gg.moonflower.pollen.api.datagen.provider.tags.PollinatedFluidTagsProvider;
import gg.moonflower.pollen.api.item.BucketItemBase;
import gg.moonflower.pollen.api.platform.Platform;
import gg.moonflower.pollen.api.registry.FluidBehaviorRegistry;
import gg.moonflower.pollen.api.registry.PollinatedBlockRegistry;
import gg.moonflower.pollen.api.registry.PollinatedFluidRegistry;
import gg.moonflower.pollen.api.registry.PollinatedRegistry;
import gg.moonflower.pollen.api.registry.resource.TagRegistry;
import gg.moonflower.pollen.api.resource.condition.PollinatedResourceCondition;
import gg.moonflower.pollen.api.util.PollinatedModContainer;
import gg.moonflower.pollen.core.client.render.DebugPollenFlowerPotRenderer;
import gg.moonflower.pollen.core.test.TestFluid;
import gg.moonflower.pollen.core.test.TestPollenFluidBehavior;
import gg.moonflower.pollen.pinwheel.api.client.render.BlockRendererRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Internal
public class PollenTest {

    private static final PollinatedRegistry<Item> ITEMS = PollinatedRegistry.create(Registry.ITEM, Pollen.MOD_ID);
    private static final PollinatedBlockRegistry BLOCKS = PollinatedRegistry.createBlock(ITEMS);
    private static final PollinatedFluidRegistry FLUIDS = PollinatedRegistry.createFluid(Pollen.MOD_ID);

    public static final Tag.Named<Fluid> TEST_TAG = TagRegistry.bindFluid(new ResourceLocation(Pollen.MOD_ID, "test"));

    public static final Supplier<FlowingFluid> TEST_FLUID = FLUIDS.register("test", TestFluid.Source::new);
    public static final Supplier<FlowingFluid> FLOWING_TEST_FLUID = FLUIDS.register("flowing_test", TestFluid.Flowing::new);
    public static final Supplier<Block> TEST = BLOCKS.register("test", () -> new PollinatedLiquidBlock(TEST_FLUID, BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops()));
    public static final Supplier<Item> TEST_BUCKET = ITEMS.register("test", () -> new BucketItemBase(TEST_FLUID, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(CreativeModeTab.TAB_MISC)));

    static void init() {
    }

    static void onClient() {
        BlockRendererRegistry.register(Blocks.FLOWER_POT, new DebugPollenFlowerPotRenderer());
    }

    static void onCommon() {
        ITEMS.register(Pollen.PLATFORM);
        BLOCKS.register(Pollen.PLATFORM);
        FLUIDS.register(Pollen.PLATFORM);

        FluidBehaviorRegistry.register(TEST_TAG, new TestPollenFluidBehavior());
    }

    static void onClientPost(Platform.ModSetupContext context) {
    }

    static void onCommonPost(Platform.ModSetupContext context) {
    }

    static void onDataInit(Platform.DataSetupContext context) {
        DataGenerator dataGenerator = context.getGenerator();
        PollinatedModContainer container = context.getMod();
        dataGenerator.addProvider(new PollinatedFluidTagsProvider(dataGenerator, container) {
            @Override
            protected void addTags() {
                super.addTags();
                this.tag(TEST_TAG).add(TEST_FLUID.get());
                this.addConditions(TEST_TAG, PollinatedResourceCondition.allModsLoaded("pollen"));
            }
        });
    }
}
