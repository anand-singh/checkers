package checkers.benchmark

import checkers.consts._
import checkers.core.tables.{JumpTable, NeighborTable}
import checkers.core.{MoveGenerator, _}
import checkers.util.MoveListPrinter
import org.scalajs.dom
import org.scalajs.dom.window.performance


object MoveGeneratorBenchmarks {
  import com.softwaremill.macwire._

  lazy val rulesSettings = RulesSettings.default

  lazy val neighborTable: NeighborTable = wire[NeighborTable]

  lazy val jumpTable: JumpTable = wire[JumpTable]

  lazy val moveExecutor: MoveExecutor = wire[MoveExecutor]

  lazy val moveGenerator: MoveGenerator = wire[MoveGenerator]

  def testBoard(repetitions: Int)(boardState: BoardStack, turnToMove: Color): Double = {
    val generator = moveGenerator
    val startTime = performance.now()
    var i = repetitions
    while (i > 0) {
      generator.generateMoves(boardState, turnToMove)
      i -= 1
    }

    val endTime = performance.now()
    endTime - startTime
  }

  def test1(): Unit = {
    val generator = moveGenerator
    val board = RulesSettings.initialBoard(rulesSettings)
    val stack = BoardStack.fromBoard(board)
    val t = testBoard(100000)(stack, DARK)
    println(s"test1: $t")

    val darkMoves = generator.generateMoves(stack, DARK)
    var moveStr = MoveListPrinter.moveListToString(darkMoves)
    println(s"dark: $moveStr")

    val lightMoves = generator.generateMoves(stack, LIGHT)
    moveStr = MoveListPrinter.moveListToString(lightMoves)
    println(s"light: $moveStr")

    println("--------------")

    dom.console.log(jumpTable.data)

    dom.console.log(neighborTable.moveNE)
    dom.console.log(neighborTable.moveNW)
    dom.console.log(neighborTable.moveSE)
    dom.console.log(neighborTable.moveSW)

    println("--------------")
  }

}