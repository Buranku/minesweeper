import scala.swing._
import scala.swing.event._
import javax.swing.{SwingUtilities, JButton}

// GUI implementation for the Minesweeper game
object MinesweeperGUI extends SimpleSwingApplication {
  // Define difficulty levels with board dimensions and number of bombs
  val difficulties = Map(
    "Easy" -> (10, 10),
    "Medium" -> (14, 30),
    "Hard" -> (18, 60)
  )

  var currentDifficulty = "Easy"
  var game = new Minesweeper(difficulties(currentDifficulty)._1, difficulties(currentDifficulty)._2)

  // Initialize buttons for the grid
  val buttons: Array[Array[Button]] = Array.ofDim[Button](18, 18)
  val gridPanel = new GridPanel(game.size, game.size) {
    for (x <- 0 until game.size; y <- 0 until game.size) {
      val button = createButton(x, y)
      buttons(x)(y) = button
      contents += button
    }
  }

  // Main window setup
  override def top = new MainFrame {
    title = "Minesweeper"
    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(Action("Restart") { restartGame() })
        contents += new MenuItem(Action("Exit") { sys.exit(0) })
      }
    }
    contents = new BorderPanel {
      layout(gridPanel) = BorderPanel.Position.Center
      layout(createDifficultyPanel()) = BorderPanel.Position.North
    }
    minimumSize = new Dimension(850, 850) // Set minimum size
    size = new Dimension(850, 930)
    centerOnScreen()
    visible = true
  }

  // Creates the panel for selecting difficulty
  def createDifficultyPanel(): BoxPanel = {
    val difficultyPanel = new BoxPanel(Orientation.Horizontal)
    val difficultyGroup = new ButtonGroup()
    difficulties.keys.foreach { difficulty =>
      val radioButton = new RadioButton(difficulty) {
        selected = difficulty == currentDifficulty
        reactions += {
          case ButtonClicked(_) =>
            currentDifficulty = difficulty
            restartGame()
        }
      }
      difficultyGroup.buttons += radioButton
      difficultyPanel.contents += radioButton
    }
    difficultyPanel
  }

  // Restart game with new difficulty settings
  def restartGame(): Unit = {
    val (size, bombs) = difficulties(currentDifficulty)
    game = new Minesweeper(size, bombs)
    updateGridPanel(size)
    updateGrid()
    resetButtonStates()
  }

  // Updates the grid panel based on the game's size
  def updateGridPanel(size: Int): Unit = {
      gridPanel.contents.clear()
      gridPanel.rows = size
      gridPanel.columns = size
      for (x <- 0 until size; y <- 0 until size) {
          val button = createButton(x, y)
          buttons(x)(y) = button
          gridPanel.contents += button
      }
      gridPanel.revalidate()
      gridPanel.repaint()
  }

  // Resets all button states in the grid
  def resetButtonStates(): Unit = {
      for (x <- 0 until game.size; y <- 0 until game.size) {
          if (buttons(x)(y) != null) {
              buttons(x)(y).text = ""
              buttons(x)(y).enabled = true
          }
      }
  }

  // Utility to create buttons for the grid
  def createButton(x: Int, y: Int): Button = new Button {
    preferredSize = new Dimension(40, 40)
    listenTo(mouse.clicks)
    reactions += {
      case e: MouseClicked => handleButtonClick(e, Position(x, y))
    }
  }

  // Handles button clicks for game interactions
  def handleButtonClick(e: MouseClicked, position: Position): Unit = {
    game.board.getCellAt(position).foreach { cell =>
      val javaMouseEvent = e.peer
      if (SwingUtilities.isLeftMouseButton(javaMouseEvent) && !cell.revealed && !cell.flagged) {
        val revealed = game.revealCell(position)
        updateGrid()
        if (revealed && cell.isBomb) showGameOverDialog()
        if (game.hasWon) showWinDialog()  // Check win condition after actions.
      } else if (SwingUtilities.isRightMouseButton(javaMouseEvent) && !cell.revealed) {
        game.flagCell(position)
        updateGrid()
      } else if (SwingUtilities.isMiddleMouseButton(javaMouseEvent) && cell.revealed && cell.number > 0) {
        val flagsAround = game.countFlagsNear(position)
        if (flagsAround == cell.number) {
          val hitBomb = game.revealAdjacentSafeCells(position)
          updateGrid()
          if (hitBomb) showGameOverDialog()  // Check after revealing if a bomb has been hit.
          if (game.hasWon) showWinDialog()  // Check win condition after actions.
        }
      }
    }
  }

  // Update the visual representation of the grid
  def updateGrid(): Unit = {
    for (x <- 0 until game.size; y <- 0 until game.size) {
      game.board.getCellAt(Position(x, y)).foreach { cell =>
        buttons(x)(y).text = if (cell.revealed) {
          if (cell.isBomb) "💣" else cell.number.toString
        } else if (cell.flagged) {
          "🚩"
        } else ""
        buttons(x)(y).enabled = !cell.revealed
      }
    }
    if (game.hasWon) showWinDialog()
  }

  // Displays a dialog when the game is over due to a bomb
  def showGameOverDialog(): Unit = {
    Dialog.showMessage(
      message = "Game Over! You hit a bomb!",
      title = "Game Over",
      messageType = Dialog.Message.Error
    )
    restartGame()
  }

  // Displays a dialog when the player wins the game
  def showWinDialog(): Unit = {
    Dialog.showMessage(
      message = "Congratulations! You Win!",
      title = "Victory!",
      messageType = Dialog.Message.Info
    )
    restartGame()
  }
}