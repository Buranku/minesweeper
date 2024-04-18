# Minesweeper

Made with Scala version 3.4.1 and Scala.swing, and built with sbt version 1.9.9.



## Running the game

The easiest way to run the game is to run the Minesweeper jar file located in the same folder as this README

Another way to run the game is to build to use sbt, and then run it. Open up the terminal in this folder, execute the command 'sbt' and then execute the command 'run'.



## Playing the game

The game is played with the mouse and its three buttons, the left click, right click, and middle click.

Left click reveals a cell the cell that you click if the cell is not flagged.

Right click flags or unflags the cell you click.

Middle click can be used to reveal all the adjacent cells as long as the amount of bombs adjacent to the cell is the same as the number of flagged cells adjacent to the cell.