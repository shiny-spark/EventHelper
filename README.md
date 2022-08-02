# EventHelper
Minecraft Java plugin that allows you to create activators. For example, you can display text or execute a command by clicking a button.

EventHelper is a plugin for Minecraft that allows you to create various activators in the game.
An activator is an object that performs certain actions when it is used.

The following game objects can be activators:
Button, Chest, Door, Lever, Plate, Region

Event types that can trigger actions in an activator:
- ON - Lever
- OFF - Lever
- USE - Lever, Button, Door, Plate
- OPEN - Door, Chest
- CLOSE - Door, Chest
- PUT - Chest
- TAKE - Chest
- ENTER - Region
- LEAVE - Region

Types of actions that can be called when the activator is launched:
- COMMAND - execute any command on behalf of the console
- MESSAGE - send a message to the player
- TP - teleport the player to the exact coordinates
- EFFECT - apply an effect to the player
- KILL - kill a player
- HEALTH - heal the player
- GIVE - give an item to a player
- TAKE - take an item from a player
- ANNOUNCE - display a message to all online players
- FLY - toggle the player's flight state
- META - set the Meta tag to the player
- DELAY - set the delay between the execution of actions
- CONDITION - create a condition for selecting different actions (works in conjunction with Meta)


<h4>
The plugin is still under development.
</h4>
