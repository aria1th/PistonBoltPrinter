# PistonBoltPrinter
A fabric mod that automatically builds piston bolts for you!
# How to use
1. Place a slime block and right click it with an empty hand, this will create the starting position. 
2. Place a honey block and right click it with an empty hand, this will set the direction, the piston bolt's direction will be slime block --> honey block, it can go straight or diagonal.
3. Walk in the direction you set and it will start building.
# Other info
Right clicking at nothing with an empty hand will toggle the mod ON/OFF.<br> 
Carpet extra protocol status check and lacked materials will be printed at chat <br>
Currently the mod requires carpet extra's "accurateblockplacement" as it needs to place piston reversed. However, the mod can lazy-place pistons if you go back after. If you use lazy-place on diagonals, you will need to go twice as there are 2 direction of pistons on that one.<br> 
Due to your ping, the server might not allow you to use your fast block placement. The mod currently places blocks at a speed of 120 blocks per second at its maximum.<br> 
## Showcase Videos
Diagonal: https://www.youtube.com/watch?v=idju85Dmu-Q <br>
Straight: https://www.youtube.com/watch?v=7nXhzq2TBV8 <br>

# Materials
Materials are now hardcoded

## Straight
- Efficiency 5 pickaxe
- Rails
- Smooth quartz block
- White carpet
- Repeater
- Pistons
- Redstone Torch (directional)
- Redstone dust.</br>
## Diagonal
- Efficeny 5 pickaxe
- Smooth quartz block
- Sea lanterns
- Rails
- Repeater
- Dust
- Redstone Torch (directional)
- Sticky piston.
# How to build mod
1. Download the source code zip and unzip it
2. Copy the file path and navigate there in command prompt (windows), types cd (file location). ex) cd c:
3. Type /gradlew build. To change something go to the src folder and surf.
