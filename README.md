# PistonBoltPrinter(WIP)
A fabric mod that automatically builds piston bolts for you!<br> 
[Carpet-mod](https://github.com/gnembon/fabric-carpet) is highly recommended, although it partially works without it.<br>
# How to use
type '.' in chat to get more info<br> 
.pos1 sets first position and determines the origin of the pistonbolt<br> 
.pos2 has to have one block of air between it and pos1. with pos2 you choose the diredtion<br> 
# Other info
Right clicking at nothing with an empty hand will toggle the mod ON/OFF.<br> 
Carpet extra protocol status check and lacked materials will be printed at chat <br>
Currently the mod requires carpet extra's "accurateblockplacement" as it needs to place piston reversed. However, the mod can lazy-place pistons if you go back after. If you use lazy-place on diagonals, you will need to go twice as there are 2 direction of pistons on that one.<br> 
Due to your ping, the server might not allow you to use your fast block placement. The mod currently places blocks at a speed of 120 blocks per second at its maximum.<br> 
## Showcase Videos
Diagonal: https://www.youtube.com/watch?v=idju85Dmu-Q <br>
Straight: https://www.youtube.com/watch?v=7nXhzq2TBV8 <br>

# Materials
Some Materials can be changed.

## Straight
- Efficiency 5 pickaxe
- Rails
- Smooth stone block
- White carpet
- Repeater
- Pistons
- Redstone Torch (directional)<br>
## Diagonal
- Efficeny 5 pickaxe
- Smooth stone block
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
