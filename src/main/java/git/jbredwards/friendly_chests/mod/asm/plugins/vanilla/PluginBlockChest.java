package git.jbredwards.friendly_chests.mod.asm.plugins.vanilla;

import git.jbredwards.fluidlogged_api.api.asm.IASMPlugin;
import git.jbredwards.friendly_chests.api.ChestType;
import git.jbredwards.friendly_chests.api.IChestMatchable;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public final class PluginBlockChest implements IASMPlugin
{
    @Override
    public boolean isMethodValid(@Nonnull MethodNode method, boolean obfuscated) { return method.name.equals(obfuscated ? "" : "neighborChanged"); }

    @Override
    public boolean transform(@Nonnull InsnList instructions, @Nonnull MethodNode method, @Nonnull AbstractInsnNode insn, boolean obfuscated, int index) {
        /*
         * neighborChanged: (changes are around line 430)
         * Old code:
         * super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
         *
         * New code:
         * //update state here when a neighbor disappears
         * super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
         * Hooks.neighborChanged(this, state, worldIn, pos);
         */
        if(insn.getOpcode() == INVOKESPECIAL) {
            final InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ALOAD, 1));
            list.add(new VarInsnNode(ALOAD, 2));
            list.add(new VarInsnNode(ALOAD, 3));
            list.add(genMethodNode("neighborChanged", "(Lnet/minecraft/block/BlockChest;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"));
            instructions.insert(insn, list);
            return true;
        }

        return false;
    }

    @Override
    public boolean transformClass(@Nonnull ClassNode classNode, boolean obfuscated) {
        classNode.interfaces.add("git/jbredwards/friendly_chests/api/IChestMatchable");
        classNode.methods.removeIf(method -> method.name.equals(obfuscated ? "" : "canPlaceBlockAt"));
        /*
         * getBoundingBox:
         * New code:
         * public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
         * {
         *     return Hooks.getBoundingBox(state);
         * }
         */
        overrideMethod(classNode, method -> method.name.equals(obfuscated ? "" : "getBoundingBox"),
            "getBoundingBox", "(Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/util/math/AxisAlignedBB;",
                generator -> generator.visitVarInsn(ALOAD, 1));
        /*
         * onBlockAdded:
         * New code:
         * public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
         * {
         *     Hooks.onBlockAdded(this, worldIn, pos, state);
         * }
         */
        overrideMethod(classNode, method -> method.name.equals(obfuscated ? "" : "onBlockAdded"),
            "onBlockAdded", "(Lnet/minecraft/block/BlockChest;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V", generator -> {
                generator.visitVarInsn(ALOAD, 0);
                generator.visitVarInsn(ALOAD, 1);
                generator.visitVarInsn(ALOAD, 2);
                generator.visitVarInsn(ALOAD, 3);
            }
        );
        /*
         * getStateForPlacement:
         * New code:
         * public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
         * {
         *     return Hooks.getStateForPlacement(this, world, pos, facing, placer);
         * }
         */
        overrideMethod(classNode, method -> method.name.equals(obfuscated ? "" : "getStateForPlacement"),
            "getStateForPlacement", "(Lnet/minecraft/block/BlockChest;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/block/state/IBlockState;", generator -> {
                generator.visitVarInsn(ALOAD, 0);
                generator.visitVarInsn(ALOAD, 1);
                generator.visitVarInsn(ALOAD, 2);
                generator.visitVarInsn(ALOAD, 3);
                generator.visitVarInsn(ALOAD, 8);
            }
        );
        /*
         * onBlockPlacedBy:
         * New code:
         * public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
         * {
         *     Hooks.onBlockPlacedBy(worldIn, pos, stack);
         * }
         */
        overrideMethod(classNode, method -> method.name.equals(obfuscated ? "" : "onBlockPlacedBy"),
            "onBlockPlacedBy", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V", generator -> {
                generator.visitVarInsn(ALOAD, 1);
                generator.visitVarInsn(ALOAD, 2);
                generator.visitVarInsn(ALOAD, 5);
            }
        );
        /*
         * getContainer:
         * New code:
         * @Nullable
         * public ILockableContainer getContainer(World worldIn, BlockPos pos, boolean allowBlocking)
         * {
         *     return Hooks.getContainer(this, worldIn, pos, allowBlocking);
         * }
         */
        overrideMethod(classNode, method -> method.name.equals(obfuscated? "" : "getContainer"),
            "getContainer", "(Lnet/minecraft/block/BlockChest;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)Lnet/minecraft/world/ILockableContainer;", generator -> {
                generator.visitVarInsn(ALOAD, 0);
                generator.visitVarInsn(ALOAD, 1);
                generator.visitVarInsn(ALOAD, 2);
                generator.visitVarInsn(ILOAD, 3);
            }
        );
        /*
         * getStateFromMeta:
         * New code:
         * public IBlockState getStateFromMeta(int meta)
         * {
         *     return Hooks.getStateFromMeta(this, meta);
         * }
         */
        overrideMethod(classNode, method -> method.name.equals(obfuscated? "" : "getStateFromMeta"),
            "getStateFromMeta", "(Lnet/minecraft/block/BlockChest;I)Lnet/minecraft/block/state/IBlockState;", generator -> {
                generator.visitVarInsn(ALOAD, 0);
                generator.visitVarInsn(ILOAD, 1);
            }
        );
        /*
         * getMetaFromState:
         * New code:
         * public int getMetaFromState(IBlockState state)
         * {
         *     return Hooks.getMetaFromState(state);
         * }
         */
        overrideMethod(classNode, method -> method.name.equals(obfuscated? "" : "getMetaFromState"),
            "getMetaFromState", "(Lnet/minecraft/block/state/IBlockState;)I",
                generator -> generator.visitVarInsn(ALOAD, 1));
        /*
         * createBlockState:
         * New code:
         * public BlockStateContainer createBlockState()
         * {
         *     return Hooks.createBlockState(this);
         * }
         */
        overrideMethod(classNode, method -> method.name.equals(obfuscated? "" : "createBlockState"),
            "createBlockState", "(Lnet/minecraft/block/BlockChest;)Lnet/minecraft/block/state/BlockStateContainer;",
                generator -> generator.visitVarInsn(ALOAD, 0));

        return true;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        @Nonnull
        public static BlockStateContainer createBlockState(@Nonnull BlockChest block) {
            return new BlockStateContainer.Builder(block).add(BlockChest.FACING, ChestType.TYPE).build();
        }

        @Nonnull
        public static AxisAlignedBB getBoundingBox(@Nonnull IBlockState state) {
            if(state.getValue(ChestType.TYPE) == ChestType.SINGLE) return BlockChest.NOT_CONNECTED_AABB;
            switch(ChestType.getDirectionToAttached(state)) {
                case NORTH: return BlockChest.NORTH_CHEST_AABB;
                case SOUTH: return BlockChest.SOUTH_CHEST_AABB;
                case WEST: return BlockChest.WEST_CHEST_AABB;
                case EAST: return BlockChest.EAST_CHEST_AABB;
            }

            //should never pass
            return BlockChest.NOT_CONNECTED_AABB;
        }

        @Nullable
        public static ILockableContainer getContainer(@Nonnull BlockChest block, @Nonnull World world, @Nonnull BlockPos pos, boolean allowBlocking) {
            final TileEntity tile = world.getTileEntity(pos);
            if(!(tile instanceof TileEntityChest) || !allowBlocking && block.isBlocked(world, pos))
                return null;

            ILockableContainer container = (ILockableContainer)tile;
            final IBlockState state = world.getBlockState(pos);

            final ChestType type = state.getValue(ChestType.TYPE);
            if(type == ChestType.SINGLE) return container;

            final BlockPos neighborPos = pos.offset(ChestType.getDirectionToAttached(state));
            if(!allowBlocking && block.isBlocked(world, neighborPos)) return null;

            final ILockableContainer neighbor = (ILockableContainer)world.getTileEntity(neighborPos);
            if(neighbor == null) return null;

            return new InventoryLargeChest("container.chestDouble",
                    type == ChestType.RIGHT ? neighbor : container,
                    type == ChestType.LEFT ? neighbor : container);
        }

        public static int getMetaFromState(@Nonnull IBlockState state) {
            return state.getValue(BlockChest.FACING).getHorizontalIndex()
                    | state.getValue(ChestType.TYPE).ordinal() << 2;
        }

        @Nonnull
        public static IBlockState getStateFromMeta(@Nonnull BlockChest block, int meta) {
            return block.getDefaultState()
                    .withProperty(BlockChest.FACING, EnumFacing.byHorizontalIndex(meta & 3))
                    .withProperty(ChestType.TYPE, ChestType.fromOrdinal(meta >> 2));
        }

        @Nonnull
        public static IBlockState getStateForPlacement(@Nonnull BlockChest block, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, @Nonnull EntityLivingBase placer) {
            final boolean isSneaking = placer.isSneaking();
            EnumFacing facing = placer.getHorizontalFacing().getOpposite();
            ChestType type = ChestType.SINGLE;

            if(side.getAxis().isHorizontal() && isSneaking) {
                final EnumFacing sideToAttach = getDirectionToAttach(block, world, pos, side.getOpposite());
                if(sideToAttach != null && sideToAttach.getAxis() != side.getAxis()) {
                    facing = sideToAttach;
                    type = sideToAttach.rotateYCCW() == side.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
                }
            }

            if(type == ChestType.SINGLE && !isSneaking) {
                if(facing == getDirectionToAttach(block, world, pos, facing.rotateY())) type = ChestType.LEFT;
                else if(facing == getDirectionToAttach(block, world, pos, facing.rotateYCCW())) type = ChestType.RIGHT;
            }

            return block.getDefaultState().withProperty(BlockChest.FACING, facing).withProperty(ChestType.TYPE, type);
        }

        public static void neighborChanged(@Nonnull BlockChest block, @Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
            if(state.getValue(ChestType.TYPE) != ChestType.SINGLE) {
                final BlockPos otherPos = pos.offset(ChestType.getDirectionToAttached(state));
                final IBlockState other = world.getBlockState(otherPos);
                if(!IChestMatchable.chestMatches(block, world, state, pos, other, otherPos))
                    world.setBlockState(pos, state.withProperty(ChestType.TYPE, ChestType.SINGLE));
            }
        }

        public static void onBlockAdded(@Nonnull BlockChest block, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
            final ChestType type = state.getValue(ChestType.TYPE);
            if(type != ChestType.SINGLE) {
                final BlockPos offset = pos.offset(ChestType.getDirectionToAttached(state));
                final IBlockState attachedTo = world.getBlockState(offset);

                //update neighbor chest
                if(IChestMatchable.chestMatches(block, world, state, pos, attachedTo, offset)
                && attachedTo.getValue(ChestType.TYPE) == ChestType.SINGLE)
                    world.setBlockState(offset, attachedTo
                            .withProperty(BlockChest.FACING, state.getValue(BlockChest.FACING))
                            .withProperty(ChestType.TYPE, type.getOpposite()));

                //update this chest
                else world.setBlockState(pos, state.withProperty(ChestType.TYPE, ChestType.SINGLE));
            }
        }

        public static void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ItemStack stack) {
            if(stack.hasDisplayName()) {
                final @Nullable TileEntity tile = world.getTileEntity(pos);
                if(tile instanceof TileEntityChest) ((TileEntityChest)tile).setCustomName(stack.getDisplayName());
            }
        }

        //helper
        @Nullable
        public static EnumFacing getDirectionToAttach(@Nonnull BlockChest block, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing) {
            final IBlockState state = world.getBlockState(pos.offset(facing));
            return IChestMatchable.chestMatches(block, world, block.getDefaultState(), pos, state, pos.offset(facing))
                    && state.getValue(ChestType.TYPE) == ChestType.SINGLE
                            ? state.getValue(BlockChest.FACING) : null;
        }
    }
}