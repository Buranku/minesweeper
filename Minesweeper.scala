// Defines the main Minesweeper game logic
class Minesweeper(val size: Int = 10, val bombs: Int = 10) {
  var board: Board = Board(size, bombs)

  // Counts flags near a given position
  def countFlagsNear(position: Position): Int =
    board.getAdjacentPositions(position).count(pos => board.getCellAt(pos).exists(_.flagged))

  // Returns the game over state
  def isGameOver: Boolean = board.gameOver

  // Checks if the player has won the game
  def hasWon: Boolean = board.checkWin

  // Reveals a cell and updates the game state
  def revealCell(pos: Position): Boolean = {
    val (newBoard, gameOver) = board.revealCell(pos)
    board = newBoard
    gameOver
  }

  // Toggles a flag on a cell
  def flagCell(pos: Position): Boolean = {
    val newBoard = board.flagCell(pos)
    board = newBoard
    !board.getCellAt(pos).exists(_.flagged)
  }

  // Resets the game to initial state
  def restart(): Unit = {
    board = Board(size, bombs)
  }

  // Reveals all adjacent cells around a given position
  def revealAdjacentCells(position: Position): Unit = {
    board.getAdjacentPositions(position).foreach { adjacentPos =>
      revealCell(adjacentPos)
    }
  }

  // Reveals only adjacent cells that are safe (not flagged and not revealed) and checks for bombs
  def revealAdjacentSafeCells(position: Position): Boolean = {
    var hitBomb = false
    board.getAdjacentPositions(position).foreach { adjacentPos =>
      val cell = board.getCellAt(adjacentPos)
      if (cell.exists(c => !c.revealed && !c.flagged)) {
        val (newBoard, gameOver) = board.revealCell(adjacentPos)
        board = newBoard
        if (gameOver && cell.get.isBomb) {
          hitBomb = true
        }
      }
    }
    hitBomb
  }
}