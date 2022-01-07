package aria1th.main.pistonboltbuilder.utils;

import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
//import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;
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
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import aria1th.main.pistonboltbuilder.utils.PistonBoltMain.ActionYield;

import java.util.*;
import java.util.stream.Collectors;


public class Actionhandler {
    private static final int actionsPerTick = 6;
    private static String previousMessage = null;
    private static int waitTime = 0;
    private static boolean carpetAvailable = true;
    private static Long carpetCheckedTick = 0L;
    private static boolean carpetChecked = false;
    private static BlockPos carpetUsedBlockPos = null;
    private static final PistonBoltMain pistonHandler = new PistonBoltMain();
    private static final LinkedList<Actionhandler> actionList = new LinkedList<>();
    private final List<ActionYield.Action> temporaryActionList = new ArrayList<>();
    private final LinkedHashMap<Long, Boolean> temporaryActionListBool = new LinkedHashMap<>();
    private static Long pos1 = null;
    private static Long pos2 = null;
    private static boolean isOff = false;
    private static Long tick = 0L;
    private ActionYield.Action previousAction = null;
    private static final int reachDistance = 5;
    private final ActionYield actionYield;
    private static MinecraftClient mc = MinecraftClient.getInstance();
    private final World clientWorld;
    private static Item powerableBlockItem = Items.SMOOTH_STONE; //can change, now
    private static Item carpetBlockItem = Items.WHITE_CARPET; //can change now
    private static Item pushableBlockItem = Items.SEA_LANTERN; //can change now?
    private static boolean improved = false;
    private static boolean bidirectional = false;
    private static boolean encoded = false;


    private void checkCarpetExtra(){
        if (carpetChecked) {return;}
        if (carpetUsedBlockPos != null && tick > carpetCheckedTick + 45L){
            if (!clientWorld.getBlockState(carpetUsedBlockPos).isOf(Blocks.REPEATER)) {
                carpetUsedBlockPos = null;
                return;
            }
            carpetAvailable = mc.player.getHorizontalFacing().rotateYClockwise() == clientWorld.getBlockState(carpetUsedBlockPos).get(RepeaterBlock.FACING);
            carpetChecked = true;
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Carpet protocol check : %s".format(String.valueOf(carpetAvailable))));
            return;
        }
        else if (carpetUsedBlockPos != null && tick <= carpetCheckedTick + 45L) {
            return;
        }
        Direction direction = mc.player.getHorizontalFacing();
        BlockPos blockPos = mc.player.getBlockPos();
        BlockPos testPos = BlockPos.streamOutwards(blockPos,6,6,6).
                filter(a -> clientWorld.getBlockState(a).isAir()).
                filter(a-> BlockPos.streamOutwards(blockPos,1,1,1).noneMatch(a::equals)).
        filter(a-> RepeaterBlock.hasTopRim(clientWorld, a.down())).findFirst().orElse(null);
        System.out.println(testPos);
        if(testPos == null) {return;}
        if (placeBlockCarpet(mc, testPos, direction.rotateYClockwise(), Items.REPEATER)) {
            carpetCheckedTick = tick;
            carpetUsedBlockPos = testPos;
        }
    }
    public static boolean canPlaceFace(Item item, Direction facing){
        if (carpetAvailable) {return true;}
        if (facing.equals(Direction.UP) || facing.equals(Direction.DOWN)) {return true;}
        if (Block.getBlockFromItem(item) instanceof RepeaterBlock){
            return facing.getOpposite().equals(mc.player.getHorizontalFacing());
        }
        else if (Block.getBlockFromItem(item) instanceof PistonBlock){
            return facing.getOpposite().equals(mc.player.getHorizontalFacing());
        }
        return true;
    }
    public Actionhandler (Long startPos, Direction direction, boolean isStraight, World clientWorld){
        BlockPos startPos1 = BlockPos.fromLong(startPos);
        this.clientWorld = clientWorld;
        mc = MinecraftClient.getInstance();
        this.actionYield = pistonHandler.new ActionYield(direction, startPos, isStraight, isImproved(), isBidirectional(), isEncoded());
        actionList.add(this);
    }

    public static void tickAll(){
        for (int i = 0; i< actionsPerTick; i++){
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
    public static void chatMessage(Text text){
        if (previousMessage!= null && previousMessage.equals(text.getString())) {return;}
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
        previousMessage = text.getString();
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
            chatMessage(Text.of("No item: "+ item));
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
        if (Objects.equals(pos1, pos2)){ //reset
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
    public static String detectExisting(Long longPos){
        BlockPos blockPos = BlockPos.fromLong(longPos);
        Optional<BlockPos> directionPos = BlockPos.streamOutwards(blockPos,1,0,1).
                filter(a -> mc.world.getBlockState(a) != null && mc.world.getBlockState(a).isOf(Blocks.RAIL) && a.asLong() != blockPos.asLong()).findAny();
        if(!directionPos.isPresent()){
            return null;
        }
        BlockPos optionalPos = directionPos.get();
        if (determineIsDiagonal(optionalPos, blockPos)){ //diagonal
            return mc.world.getBlockState(optionalPos).get(RailBlock.SHAPE).asString(); //follow its rail shape
        }
        else { //straight
            HashSet<String> hashSet1 = new HashSet<String>(Arrays.asList(mc.world.getBlockState(optionalPos).get(RailBlock.SHAPE).asString().split("_")));
            HashSet<String> hashSet2 = new HashSet<String>(Arrays.asList(mc.world.getBlockState(blockPos).get(RailBlock.SHAPE).asString().split("_")));
            hashSet1.retainAll(hashSet2);
            if (!hashSet1.isEmpty()){
                return hashSet1.stream().findAny().get();
            }
        }
        return null;
    }
    public static void resumeExisting(Long longPos){
        String direction = detectExisting(longPos);
        System.out.println(direction);
        if (direction == null) {return;}
        String railShape = mc.world.getBlockState(BlockPos.fromLong(longPos)).get(RailBlock.SHAPE).asString();
        BlockPos startPos = BlockPos.fromLong(longPos);
        BlockPos fromLongPos = BlockPos.fromLong(longPos);
        Direction resumeDirection;
        boolean isResumeStraight = true;
        switch (direction) {
            case "west":
                resumeDirection = Direction.WEST;
                if (railShape.equals("north_west")){
                    startPos = fromLongPos.east();
                }
                else {
                    startPos = fromLongPos.offset(Direction.EAST,2);
                }
                break;
            case "east":
                resumeDirection = Direction.EAST;
                if (railShape.equals("south_east")){
                    startPos = fromLongPos.west();
                }
                else {
                    startPos = fromLongPos.offset(Direction.WEST,2);
                }
                break;
            case "north":
                resumeDirection = Direction.NORTH;
                if (railShape.equals("north_east")){
                    startPos = fromLongPos.south();
                }
                else {
                    startPos = fromLongPos.offset(Direction.SOUTH,2);
                }
                break;
            case "south":
                resumeDirection = Direction.SOUTH;
                if (railShape.equals("south_west")){
                    startPos = fromLongPos.north();
                }
                else {
                    startPos = fromLongPos.offset(Direction.NORTH,2);
                }
                break;
            case "south_west":
                resumeDirection = Direction.SOUTH;
                isResumeStraight = false;
                if (mc.world.isAir(fromLongPos.offset(Direction.WEST,4))){
                    startPos = fromLongPos.east(2).north(2);
                }
                else {
                    startPos = fromLongPos.east().north();
                }
                break;
            case "south_east":
                resumeDirection = Direction.EAST;
                isResumeStraight = false;
                if (mc.world.isAir(fromLongPos.offset(Direction.SOUTH,4))){
                    startPos = fromLongPos.west(2).north(2);
                }
                else {
                    startPos = fromLongPos.west().north();
                }
                break;
            case "north_west":
                resumeDirection = Direction.WEST;
                isResumeStraight = false;
                if (mc.world.isAir(fromLongPos.offset(Direction.NORTH,4))){
                    startPos = fromLongPos.east(2).south(2);
                }
                else {
                    startPos = fromLongPos.east().south();
                }
                break;
            case "north_east":
                resumeDirection = Direction.NORTH;
                isResumeStraight = false;
                if (mc.world.isAir(fromLongPos.offset(Direction.EAST,4))){
                    startPos = fromLongPos.west(2).south(2);
                }
                else {
                    startPos = fromLongPos.west().south();
                }
                break;
            default:
                return;
        }
        mc = MinecraftClient.getInstance();
        new Actionhandler(startPos.asLong(), resumeDirection, isResumeStraight, mc.world);
    }
    public static boolean determineIsDiagonal(BlockPos blockPosA,BlockPos blockPosB){
        BlockPos subPos = blockPosA.subtract(blockPosB);
        System.out.println(subPos);
        return subPos.getX()!=0 && subPos.getZ()!=0;
    }
    public void tick(){
        tick++;
        checkCarpetExtra();
        if (isOff || !carpetChecked) {return;}
        this.temporaryActionList.stream().filter(a -> !checkAction(a) && canProcess(a)).limit(1).iterator().forEachRemaining(this::processAction);
        this.temporaryActionList.removeAll(this.temporaryActionList.stream().filter(this::checkAction).collect(Collectors.toList()));
        if (this.previousAction != null && !checkAction(this.previousAction) &&
                canPlaceFace(this.previousAction.itemMap.get(this.previousAction.itemType), Direction.byId(this.previousAction.relativeDirection))) {
            if (canPlaceFace(this.previousAction.itemMap.get(this.previousAction.itemType), Direction.byId(this.previousAction.relativeDirection)) &&
                    this.previousAction.itemType == 5 && clientWorld.getBlockState(BlockPos.fromLong(this.previousAction.blockPos).down()).isAir()){
                temporaryActionList.add(this.previousAction);
            }
            else {
                waitTime ++;
                if (waitTime > 10){
                    waitTime = 0;
                    processAction(this.previousAction);
                }
                return;
            }
        }
        //this.temporaryActionList.forEach(System.out::println);
        ActionYield.Action action = this.actionYield.getNext();
        if(!action.isSuccess()) {
            processAction(action);
        }
        this.previousAction = action;
    }
    public static void toggleOnOff(){
        if(carpetChecked == true){
            isOff = !isOff;
            chatMessage(Text.of("building: " + !isOff));
            System.out.println("building: " + !isOff);
        }
        else{chatMessage(Text.of("somethings wrong with carpet-extra accurate block placement. Make sure '/carpet accurateBlockPlacement' is set to true"));}
    }
    private boolean checkAction(ActionYield.Action action){
        boolean shouldBreak = action.ShouldBreak;
        Long longPos = action.blockPos;
        Block block = Block.getBlockFromItem(action.itemMap.get(action.itemType));
        if (shouldBreak) {
            return mc.world.getBlockState(BlockPos.fromLong(longPos)).isAir();
        }
        else{
            return mc.world.getBlockState(BlockPos.fromLong(longPos)).isOf(block);
        }
    }
    private static boolean canProcess(ActionYield.Action action){
        return action.ShouldBreak || canPlaceFace(action.itemMap.get(action.itemType), Direction.byId(action.relativeDirection)) && isWithinReach(BlockPos.fromLong(action.blockPos));
    }
    private boolean processAction(ActionYield.Action action){
        boolean shouldBreak = action.ShouldBreak;
        Long longPos = action.blockPos;
        Block block = Block.getBlockFromItem(action.itemMap.get(action.itemType));
        Direction direction = Direction.byId(action.relativeDirection);
        //System.out.println(action.itemMap.get(action.itemType));
        if (!isWithinReach(BlockPos.fromLong(longPos))) {
            return false;
        }
        if (shouldBreak){
            this.temporaryActionListBool.remove(action.blockPos);
            return breakBlock(mc, BlockPos.fromLong(longPos));
        }
        else {
            if (canPlaceFace(action.itemMap.get(action.itemType), direction)){
                this.temporaryActionListBool.put(action.blockPos, true);
                return placeBlockCarpet(mc, BlockPos.fromLong(longPos), direction, action.itemMap.get(action.itemType));
            }
            else {
                temporaryActionList.add(action);
                return false;
            }
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


    //getter and setter

    public static Item getPowerableBlockItem() {
        return powerableBlockItem;
    }

    public static void setPowerableBlockItem(String powerableBlockItemid) {
        Actionhandler.powerableBlockItem = Registry.ITEM.get(new Identifier("minecraft", powerableBlockItemid));
    }

    public static Item getCarpetBlockItem() {
        return carpetBlockItem;
    }

    public static void setCarpetBlockItem(String carpetItemid) {
        Actionhandler.carpetBlockItem = Registry.ITEM.get(new Identifier("minecraft", carpetItemid));
    }

    public static Item getPushableBlockItem() {
        return pushableBlockItem;
    }

    public static void setPushableBlockItem(String pushableItemid) {
        Actionhandler.pushableBlockItem = Registry.ITEM.get(new Identifier("minecraft", pushableItemid));
    }

    public static boolean isImproved() {
        return improved;
    }
    public static void toggleImproved() {
        improved = !improved;
    }

    public static boolean isBidirectional() {
        return bidirectional;
    }
    public static void toggleBidirectiona() {
        bidirectional = !bidirectional;
    }

    public static boolean isEncoded() {
        return encoded;
    }
    public static void toggleEncoded() {
        encoded = !encoded;
    }
}
