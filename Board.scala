import scala.collection.mutable
import scala.util.Random

// Represents a position on the board
case class Position(x: Int, y: Int)

// Defines the properties of a cell on the board
case class Cell(position: Position, isBomb: Boolean, revealed: Boolean = false, flagged: Boolean = false, number: Int = 0) {
  def reveal: Cell = copy(revealed = true)
  def toggleFlag: Cell = if (!revealed) copy(flagged = !flagged) else this
}

// Represents the Minesweeper board
class Board(val size: Int, val bombs: Int, val grid: Vector[Vector[Cell]]) {
  private var _gameOver: Boolean = false

  def gameOver: Boolean = _gameOver

  // Sets the game over status
  def setGameOver(state: Boolean): Board = {
    _gameOver = state
    this
  }

  // Updates the state of a cell
  def updateCell(x: Int, y: Int, newCell: Cell): Board =
    new Board(size, bombs, grid.updated(x, grid(x).updated(y, newCell)))

  // Reveals a cell and handles the game logic for bomb exposure or expansion
  def revealCell(position: Position): (Board, Boolean) = {
    val cell = grid(position.x)(position.y)
    if (!cell.revealed && !cell.flagged) {
      val revealedCell = cell.reveal
      val newBoard = updateCell(position.x, position.y, revealedCell)
      if (revealedCell.isBomb) (newBoard.setGameOver(true), true)
      else if (revealedCell.number == 0) newBoard.expandZeroFields(position) else (newBoard, false)
    } else (this, false)
  }

  // Expands zero-valued fields when no adjacent bombs are present
  def expandZeroFields(position: Position): (Board, Boolean) = {
    val toReveal = mutable.Queue(position)
    val seen = mutable.Set(position)
    var currentBoard = this

    while (toReveal.nonEmpty) {
      val current = toReveal.dequeue()
      getAdjacentPositions(current).foreach { pos =>
        if (!seen.contains(pos) && !grid(pos.x)(pos.y).revealed) {
          seen += pos
          val adjCell = grid(pos.x)(pos.y)
          val updatedBoard = currentBoard.updateCell(pos.x, pos.y, adjCell.reveal)
          if (adjCell.number == 0 && !adjCell.isBomb) toReveal.enqueue(pos)
          currentBoard = updatedBoard
        }
      }
    }
    (currentBoard, false)
  }

  // Returns the cell at a given position if within bounds
  def getCellAt(position: Position): Option[Cell] =
    if (position.x >= 0 && position.x < size && position.y >= 0 && position.y < size)
      Some(grid(position.x)(position.y))
    else None

  // Gets positions adjacent to a given cell
  def getAdjacentPositions(position: Position): Seq[Position] = {
    (for {
      dx <- -1 to 1
      dy <- -1 to 1 if !(dx == 0 && dy == 0)
      nx = position.x + dx
      ny = position.y + dy if nx >= 0 && nx < size && ny >= 0 && ny < size
    } yield Position(nx, ny)).toList
  }

  // Flags or unflags a cell
  def flagCell(position: Position): Board = {
    val cell = grid(position.x)(position.y)
    if (!cell.revealed) updateCell(position.x, position.y, cell.toggleFlag) else this
  }

  // Checks if the game has been won
  def checkWin: Boolean = grid.flatten.forall(cell => (!cell.isBomb && cell.revealed) || (cell.isBomb && !cell.revealed))
}

// Companion object to create a new game board
object Board {
  def apply(size: Int, bombs: Int): Board = {
    val positions = (for { x <- 0 until size; y <- 0 until size } yield Position(x, y)).toList
    val bombPositions = Random.shuffle(positions).take(bombs).toSet
    val cells = positions.map { pos =>
      val isBomb = bombPositions.contains(pos)
      val adjBombs = if (isBomb) 0 else getAdjacentBombs(pos, bombPositions, size)
      Cell(pos, isBomb, number = adjBombs)
    }
    new Board(size, bombs, cells.grouped(size).map(_.toVector).toVector)
  }

  // Counts the bombs adjacent to a given position
  private def getAdjacentBombs(pos: Position, bombPositions: Set[Position], size: Int): Int = {
    (for {
      dx <- -1 to 1
      dy <- -1 to 1 if !(dx == 0 && dy == 0)
      nx = pos.x + dx
      ny = pos.y + dy if nx >= 0 && nx < size && ny >= 0 && ny < size
    } yield Position(nx, ny)).count(bombPositions.contains)
  }
}