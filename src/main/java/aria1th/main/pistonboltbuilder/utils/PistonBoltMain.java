package aria1th.main.pistonboltbuilder.utils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.RailBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.datafixer.fix.ChunkPalettedStorageFix;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.LinkedHashMap;



public class PistonBoltMain {
    public PistonBoltMain(){

    }



    public World world;
    public class ActionYield{
        private final Direction direction;
        private int order;
        private BlockPos blockPos;
        private boolean isStraight = true;
        private Item powerableBlockItem = Items.SMOOTH_STONE; //can change, how?
        private Item pushableItem = Items.SEA_LANTERN; //can change or set it to null, how?
        private LinkedList<Action> currentIterable = new LinkedList<Action>();
        private final LinkedHashMap <Integer, Item> itemMap;
        public ActionYield (Direction direction, Long blockPos, boolean isStraight, boolean isImproved, boolean isBidirectional, boolean encoded){
            this.direction = direction;
            this.blockPos = BlockPos.fromLong(blockPos);
            this.currentIterable = new LinkedList<Action>();
            this.order = 0;
            this.isStraight = isStraight;
            isImproved = Actionhandler.isImproved();
            isBidirectional = Actionhandler.isBidirectional();

            this.itemMap = new LinkedHashMap<Integer,Item>();
            if(isStraight) {
                if(isImproved){
                    Item railItem = Items.RAIL;
                    this.itemMap.put(0, railItem);
                    Item poweredRailItem = Items.POWERED_RAIL;
                    this.itemMap.put(1, poweredRailItem);
                    Item redstoneTorchItem = Items.REDSTONE_TORCH;
                    this.itemMap.put(2, redstoneTorchItem);
                    Item pistonItem = Items.PISTON;
                    this.itemMap.put(3, pistonItem);
                    Item powerableBlockItem = Actionhandler.getPowerableBlockItem();
                    this.itemMap.put(4, powerableBlockItem);
                    Item carpetItem = Actionhandler.getCarpetBlockItem();
                    this.itemMap.put(5, carpetItem);
                    Item conductingItem = Items.REPEATER;
                    if(encoded){conductingItem = Items.COMPARATOR;}
                    this.itemMap.put(6, conductingItem);
                }
                else if(!isImproved) {
                    Item railItem = Items.RAIL;
                    this.itemMap.put(0, railItem);
                    Item redstoneTorchItem = Items.REDSTONE_TORCH;
                    this.itemMap.put(1, redstoneTorchItem);
                    Item conductingItem = Items.REPEATER;
                    if(encoded){conductingItem = Items.COMPARATOR;}
                    this.itemMap.put(2, conductingItem);
                    Item pistonItem = Items.PISTON;
                    this.itemMap.put(3, pistonItem);
                    Item powerableBlockItem = Actionhandler.getPowerableBlockItem();
                    this.itemMap.put(5, powerableBlockItem);
                    Item carpetItem = Actionhandler.getCarpetBlockItem();
                    this.itemMap.put(6, carpetItem);
                }
            }
            else if(!isStraight) {
                Item railItem = Items.RAIL;
                this.itemMap.put(0, railItem);
                Item redstoneTorchItem = Items.REDSTONE_TORCH;
                this.itemMap.put(1, redstoneTorchItem);
                Item conductingItem = Items.REPEATER;
                if(encoded){conductingItem = Items.COMPARATOR;}
                this.itemMap.put(2, conductingItem);
                Item stickyPiston = Items.STICKY_PISTON;
                this.itemMap.put(3, stickyPiston);
                Item dustItem = Items.REDSTONE;
                this.itemMap.put(4, dustItem);
                Item pushableItem = Actionhandler.getPushableBlockItem();
                this.itemMap.put(5, pushableItem);
                Item poweredRailItem = Items.POWERED_RAIL;
                this.itemMap.put(9, poweredRailItem);
            }

        }
        public Action getNext() {
            if (this.currentIterable.isEmpty()){
                if (this.isStraight) {this.generateNewStraight();}
                else {this.generateNewDiagonal();}
            }
            return this.currentIterable.removeFirst();
        }
        private void addAction(BlockPos blockPos, int itemtype, Direction direction, boolean shouldBreak){
            Action action = new Action(blockPos.asLong(), itemtype, direction.getId(), shouldBreak, this.itemMap);
            this.currentIterable.add(action);
        }
        private void generateNewDiagonal(){
            Direction direction = this.direction;
            Direction yClockwise = direction.rotateYClockwise();
            BlockPos blockPos = this.blockPos;
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction), 0, yClockwise, false); //place rail
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise), 0, yClockwise, false);
            this.addAction(blockPos.offset(direction).offset(yClockwise), 0, yClockwise, false);//place rail
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction), 0, yClockwise, true); //break rail
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise), 0, yClockwise, true);//break rail
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction,2), 8, yClockwise, false); //block for pushing
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise,2), 8, yClockwise, false); //block for pushing
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction,3), 4, yClockwise, false); //piston
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise,3), 4, direction, false); //piston
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction,4), 5, yClockwise, false); //powerable
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise,4), 5, yClockwise, false); //powerable
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction,5), 2, direction.getOpposite(), false); //repeater
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise,5), 2, yClockwise.getOpposite(), false);; //repeater
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction,6), 5, yClockwise, false); //powerable
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise,6), 5, yClockwise, false); //powerable
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(direction), 0, yClockwise, false); //place rail
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(yClockwise), 0, yClockwise, false);//place rail
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2), 0, yClockwise, false);//place rail
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(direction), 0, yClockwise, true); //break rail
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(yClockwise), 0, yClockwise, true); //break rail
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(direction,2), 8, yClockwise, false); //block for pushing
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(yClockwise,2), 8, yClockwise, false); //block for pushing
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(direction,3), 4, yClockwise, false); //piston
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(yClockwise,3), 4, direction, false); //piston
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(direction,5), 7, direction, false); //dust
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(yClockwise,5), 7, direction, false); //dust
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction,3).up(), 6, Direction.DOWN, false); //carpet
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise,3).up(), 6, Direction.DOWN, false); //carpet
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction,4).up(), 6, Direction.DOWN, false); //carpet
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise,4).up(), 6, Direction.DOWN, false); //carpet
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(direction,6).up(), 6, Direction.DOWN, false); //carpet
            this.addAction(blockPos.offset(direction).offset(yClockwise).offset(yClockwise,6).up(), 6, Direction.DOWN, false); //carpet
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(direction,3).up(), 6, Direction.DOWN, false); //carpet
            this.addAction(blockPos.offset(direction,2).offset(yClockwise,2).offset(yClockwise,3).up(), 6, Direction.DOWN, false); //carpet
            this.blockPos = blockPos.offset(direction,2).offset(yClockwise,2);
        }
        private void generateNewStraight(){
            Direction direction = this.direction;
            Direction yClockwise = direction.rotateYClockwise();
            Direction yCounterClockwise = direction.rotateYCounterclockwise();
            if(Actionhandler.isBidirectional()) {
                this.addAction(this.blockPos.offset(direction, 4).offset(yCounterClockwise), 5, direction, false); //place powerable
                this.addAction(this.blockPos.offset(direction, 4).offset(yClockwise), 5, direction, false); //place powerable
                this.addAction(this.blockPos.offset(direction, 2).offset(yCounterClockwise), 5, direction, false); //place powerable
                this.addAction(this.blockPos.offset(direction, 2).offset(yClockwise), 5, direction, false); //place powerable
                this.addAction(this.blockPos.offset(direction), 5, direction, false); //place powerable
                this.addAction(this.blockPos.offset(direction, 4).offset(yCounterClockwise), 8, direction, false); //place pushable
                this.addAction(this.blockPos.offset(direction, 2).offset(yClockwise), 8, direction, false); //place pushable
                this.addAction(this.blockPos.offset(direction, 4).offset(yClockwise), 10, Direction.UP, false); //place trapdoor
                this.addAction(this.blockPos.offset(direction, 2).offset(yCounterClockwise), 10, Direction.UP, false); //place trapdoor

                this.blockPos = blockPos.offset(direction, 4);
            }
            if (Actionhandler.isImproved()){
                if (true) {
                    this.addAction(this.blockPos.offset(direction), 0, direction, false); //place rail
                    this.addAction(this.blockPos.offset(direction, 2), 0, direction, false); //place rail
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise), 0, direction, false); //place rail
                    this.addAction(this.blockPos.offset(direction, 2), 0, direction, true); //break rail
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise), 0, direction, true); //break rail
                    this.addAction(this.blockPos.offset(direction, 2).offset(yClockwise), 1, Direction.NORTH, false); //place poweredRail
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise), 3, direction, false); //place Piston
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise, 2), 4, direction, false); //place powerableBlock
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise, 2), 4, direction, false); //place powerableBlock
                    this.addAction(this.blockPos.offset(direction, 2).offset(yClockwise, 2), 6, direction.getOpposite(), false); //place repeater
                    this.addAction(this.blockPos.offset(direction, 2).offset(yCounterClockwise, 2), 6, direction.getOpposite(), false); //place repeater
                    this.blockPos = blockPos.offset(direction, 2);

                    this.addAction(this.blockPos.offset(direction), 0, direction, false); //place rail
                    this.addAction(this.blockPos.offset(direction, 2), 0, direction, false); //place rail
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise), 0, direction, false); //place rail
                    this.addAction(this.blockPos.offset(direction, 2), 0, direction, true); //break rail
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise), 0, direction, true); //break rail
                    this.addAction(this.blockPos.offset(direction, 2).offset(yClockwise), 1, direction.rotateYClockwise(), false); //place poweredRail
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise), 3, direction, false); //place Piston
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise, 2), 4, direction, false); //place powerableBlock
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise, 2), 4, direction, false); //place powerableBlock
                    this.addAction(this.blockPos.offset(direction, 2).offset(yClockwise, 2), 6, direction.getOpposite(), false); //place repeater
                    this.addAction(this.blockPos.offset(direction, 2).offset(yCounterClockwise, 2), 6, direction.getOpposite(), false); //place repeater
                    this.blockPos = blockPos.offset(direction, 2);
                }
            }
            if (!Actionhandler.isImproved()) {
                if (direction == Direction.EAST || direction == Direction.SOUTH) {
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise), 0, yClockwise, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(direction), 0, direction, false);//place rail
                    this.addAction(this.blockPos.offset(direction), 0, direction, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise), 0, yClockwise, true);//break rail
                    this.addAction(this.blockPos.offset(direction).offset(direction), 0, direction, true);//break rail
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yCounterClockwise), 0, yCounterClockwise, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(direction), 0, direction, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(direction), 0, direction, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yCounterClockwise), 0, yCounterClockwise, true);//break rail
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(direction), 0, direction, true);//break rail
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise), 3, direction, false); //place piston, facing east
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise), 3, direction, false); //place piston, facing east
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise).offset(yCounterClockwise), 5, direction, false); //place powerableBlock
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise).offset(yClockwise), 5, direction, false); //place powerableBlock
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yCounterClockwise).offset(yCounterClockwise), 2, direction.getOpposite(), false); //place repeater, facing east
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise).offset(yClockwise), 2, direction.getOpposite(), false); //place repeater, facing east
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise).up(), 6, Direction.DOWN, false); //place carpet
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise).up(), 6, Direction.DOWN, false);
                    ; //place carpet
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise).offset(yCounterClockwise).up(), 6, Direction.DOWN, false); //place carpet
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise).offset(yClockwise).up(), 6, Direction.DOWN, false); //place carpet
                    this.blockPos = this.blockPos.offset(direction).offset(direction);
                }//new
                if (direction == Direction.WEST || direction == Direction.NORTH) {
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise), 1, Direction.DOWN, false); //place torch
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise), 0, yClockwise, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(direction), 0, direction, false);//place rail
                    this.addAction(this.blockPos.offset(direction), 0, direction, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise), 0, yClockwise, true);//break rail
                    this.addAction(this.blockPos.offset(direction).offset(direction), 0, direction, true);//break rail
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise), 1, Direction.DOWN, true); //break torch
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise), 1, Direction.DOWN, false); //place torch
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yCounterClockwise), 0, yCounterClockwise, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(direction), 0, direction, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(direction), 0, direction, false);//place rail
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yCounterClockwise), 0, yCounterClockwise, true);//break rail
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(direction), 0, direction, true);//break rail
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise), 1, Direction.DOWN, true); //break torch
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise), 3, direction, false); //place piston
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise), 3, direction, false); //place piston
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise).offset(yCounterClockwise), 5, direction, false); //place powerableBlock
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise).offset(yClockwise), 5, direction, false); //place powerableBlock
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yCounterClockwise).offset(yCounterClockwise), 2, direction.getOpposite(), false); //place repeater
                    this.addAction(this.blockPos.offset(direction).offset(yClockwise).offset(yClockwise), 2, direction.getOpposite(), false); //place repeater
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise).offset(yCounterClockwise).up(), 6, Direction.DOWN, false); //place carpet
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise).offset(yClockwise).up(), 6, Direction.DOWN, false);
                    ; //place carpet
                    this.addAction(this.blockPos.offset(direction).offset(yCounterClockwise).up(), 6, Direction.DOWN, false); //place carpet
                    this.addAction(this.blockPos.offset(direction).offset(direction).offset(yClockwise).up(), 6, Direction.DOWN, false); //place carpet
                    this.blockPos = this.blockPos.offset(direction).offset(direction);//new
                }
            }
        }
        public class Action {
            public boolean ShouldBreak;
            public Long blockPos;
            public int itemType;
            public LinkedHashMap<Integer, Item> itemMap = new LinkedHashMap<Integer, Item>();
            public int relativeDirection = 0;
            public Action (Long blockPos, int itemType, int relativeDirection, boolean ShouldBreak, LinkedHashMap<Integer, Item> itemMap){
                this.blockPos = blockPos;
                this.itemType = itemType;
                this.relativeDirection = relativeDirection;
                this.ShouldBreak = ShouldBreak;
                this.itemMap = itemMap;
            }
            public boolean isSuccess(){
                World world = MinecraftClient.getInstance().world;
                if (this.ShouldBreak) {
                    return world.getBlockState(BlockPos.fromLong(this.blockPos)).isAir();
                }
                else {
                    return world.getBlockState(BlockPos.fromLong(this.blockPos)).isOf(Block.getBlockFromItem(this.itemMap.get(this.itemType)));
                }
            }
            public String toString() {
                BlockPos blockPos = BlockPos.fromLong(this.blockPos);
                return String.format("%d, %d, %d, %s, %s", blockPos.getX() , blockPos.getY(), blockPos.getZ(), this.itemMap.get(this.itemType), Direction.byId(this.relativeDirection));
            }
        }
    }

}
