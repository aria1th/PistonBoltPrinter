package aria1th.main.pistonboltbuilder.utils;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
//import net.minecraft.block.FluidBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.world.World;
import aria1th.main.pistonboltbuilder.utils.PistonBoltMain;
import aria1th.main.pistonboltbuilder.utils.PistonBoltMain.ActionYield;

import java.util.LinkedList;
import java.util.List;
import java.util.LinkedHashMap;


public class Actionhandler {
    private final static int actionPerTick = 6;
    private static PistonBoltMain pistonHandler = new PistonBoltMain();
    private static LinkedList<Actionhandler> actionList = new LinkedList<Actionhandler>();
    private static Long pos1 = null;
    private static Long pos2 = null;
    private Long tick;
    private static boolean isOff = true;
    private PistonBoltMain.ActionYield.Action previousAction = null;
    private BlockPos startPos;
    private Direction direction;
    private boolean isStraight;
    private static final int reachDistance = 7;
    private PistonBoltMain.ActionYield actionYield;
    private static MinecraftClient mc = MinecraftClient.getInstance();
    private World clientWorld = mc.world;
    private static Item powerableBlockItem = Items.SMOOTH_QUARTZ; //can change, how?
    private static Item carpetItem = Items.WHITE_CARPET; //can change or set it to null, how?
    private static Item pushableItem = Items.SEA_LANTERN; //can change or set it to null, how?
    public Actionhandler (Long startPos, Direction direction, boolean isStraight, World clientWorld){
        this.tick = 0L;
        this.startPos = BlockPos.fromLong(startPos);
        this.direction = direction;
        this.isStraight = isStraight;
        this.clientWorld = clientWorld;
        mc = MinecraftClient.getInstance();
        this.actionYield = pistonHandler.new ActionYield(direction, startPos, powerableBlockItem, pushableItem, isStraight);
        actionList.add(this);
    }
    public static void tickAll(){
        for (int i = 0; i< actionPerTick; i++){
            for (Actionhandler lv : actionList){
                if (MinecraftClient.getInstance().world.equals(lv.clientWorld)) {lv.tick();}
            }
        }
    }
    public static boolean playerInventorySwitch(Item itemName){
        ItemStack itemStack = itemName.getDefaultStack();
        PlayerInventory playerInventory = mc.player.getInventory();
        int i = playerInventory.getSlotWithStack(itemStack);
        if (i != -1) {
            if (PlayerInventory.isValidHotbarIndex(i)) {
                playerInventory.selectedSlot = i;
            } else {
                mc.interactionManager.pickFromInventory(i);
            }
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(playerInventory.selectedSlot));
            return true;
        }
        return false;
    }
    public static boolean placeBlockCarpet(MinecraftClient mc, BlockPos pos, Direction facing, Item item) {
        if (!mc.world.getBlockState(pos).isAir()) {breakBlock(mc, pos);} //assure there's no block there, but can't deal with fluids, maybe slime or torch spam?
        if (playerInventorySwitch(item)) {
            Vec3d hitVec = new Vec3d(pos.getX() + 2 + (facing.getId() * 2), pos.getY(), pos.getZ());
            Hand hand = Hand.MAIN_HAND;
            BlockHitResult hitResult = new BlockHitResult(hitVec, facing, pos, false);
            mc.interactionManager.interactBlock(mc.player, mc.world, hand, hitResult);
            return true;
        }
        else {
            return false;
        }
    }
    public static boolean breakBlock(MinecraftClient mc, BlockPos pos){
        switchTool(mc, pos);
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
        return true;
    }
    private static boolean isWithinReach(BlockPos blockPos){
        BlockPos playerBlockPos = MinecraftClient.getInstance().player.getBlockPos();
        return playerBlockPos.getSquaredDistance(blockPos)< reachDistance * reachDistance;
    }
    public static void registerStartPos(Long blockPos){
        pos1 = blockPos;
        if (pos1 != null && pos2 != null){
            generateNew();
            pos1 = null;
            pos2 = null;
        }
    }
    public static void registerEndPos(Long blockPos){
        pos2 = blockPos;
        if (pos1 != null && pos2 != null){
            generateNew();
            pos1 = null;
            pos2 = null;
        }
    }
    public static void generateNew(){
        System.out.println(pos1);
        System.out.println(pos2);
        if (pos1 == pos2){ //reset
            pos1 = null;
            pos2 = null;
            return;
        }
        boolean isStraight = false;
        Direction direction;
        BlockPos relativePos = BlockPos.fromLong(pos2).subtract(BlockPos.fromLong(pos1));
        int a = relativePos.getX();
        int b = relativePos.getZ();
        if (a == 0 || b == 0){
            isStraight = true;
            if (a>0){
                direction = Direction.EAST;
            }
            else if (a<0){
                direction= Direction.WEST;
            }
            else if (b>0){
                direction = Direction.SOUTH;
            }
            else {
                direction = Direction.NORTH;
            }
        }
        else {
            isStraight = false;
            if (a*b>0 && a>0){
                direction = Direction.EAST;
            }
            else if (a*b>0){
                direction = Direction.WEST;
            }
            else if (a*b<0 && a>0){
                direction = Direction.NORTH;
            }
            else {
                direction = Direction.SOUTH;
            }
        }
        mc = MinecraftClient.getInstance();
        new Actionhandler(pos1, direction, isStraight, mc.world);
    }
    public void tick(){
        if (isOff) {return;}
        if (this.previousAction != null && !checkAction(this.previousAction)) {
            processAction(this.previousAction);
            return;
        }
        PistonBoltMain.ActionYield.Action action = this.actionYield.getNext();
        processAction(action);
        this.previousAction = action;
    }
    public static void toggleOnOff(){
        isOff = !isOff;
    }
    private boolean checkAction(PistonBoltMain.ActionYield.Action action){
        boolean shouldBreak = action.ShouldBreak;
        Long longPos = action.blockPos;
        Block block = Block.getBlockFromItem(action.itemMap.get(action.itemType));
        if (shouldBreak) {
            return this.mc.world.getBlockState(BlockPos.fromLong(longPos)).isAir();
        }
        else{
            return this.mc.world.getBlockState(BlockPos.fromLong(longPos)).isOf(block);
        }
    }
    private boolean processAction(PistonBoltMain.ActionYield.Action action){
        boolean shouldBreak = action.ShouldBreak;
        Long longPos = action.blockPos;
        Block block = Block.getBlockFromItem(action.itemMap.get(action.itemType));
        Direction direction = Direction.byId(action.relativeDirection);
        if (!isWithinReach(BlockPos.fromLong(longPos))) {
            return false;
        }
        if (shouldBreak){
            return breakBlock(this.mc, BlockPos.fromLong(longPos));
        }
        else {
            return placeBlockCarpet(mc, BlockPos.fromLong(longPos), direction, action.itemMap.get(action.itemType));
        }
    }
    public static void switchTool(MinecraftClient mc, BlockPos pos) {
        //from printer.breaker and bedrockbreaker class
        int bestSlotId = getBestItemSlotIdToMineBlock(mc, pos);
        PlayerInventory inv = mc.player.getInventory();
        if (mc.player.getInventory().selectedSlot != bestSlotId) {
            mc.player.getInventory().selectedSlot = bestSlotId;
        }
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(inv.selectedSlot));
    }
    private static float getBlockBreakingSpeed(BlockState block, MinecraftClient mc, int slotId) {
        float f = ((ItemStack)mc.player.getInventory().main.get(slotId)).getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getEfficiency(mc.player);
            ItemStack itemStack = mc.player.getInventory().getMainHandStack();
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }
        return f;
    }
    public static int getBestItemSlotIdToMineBlock(MinecraftClient mc, BlockPos blockToMine) {
        int bestSlot = 0;
        float bestSpeed = 0;
        BlockState state = mc.world.getBlockState(blockToMine);
        for (int i = 8; i >= 0; i--) {
            float speed = getBlockBreakingSpeed(state, mc, i);
            if ((speed > bestSpeed && speed > 1.0F)
                    || (speed >= bestSpeed && !mc.player.getInventory().getStack(i).isDamageable())) {
                bestSlot = i;
                bestSpeed = speed;
            }
        }
        return bestSlot;
    }
}