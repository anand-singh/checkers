package checkers.core

sealed trait Play

object Play {
  case object NoPlay extends Play
  case class Move(path: List[Int], proposeDraw: Boolean = false) extends Play
  case object AcceptDraw extends Play

  val empty = NoPlay

  def move(path: List[Int]): Move = Play.Move(path)

  def move(fromSquare: Int, toSquare: Int): Move = Play.Move(fromSquare :: toSquare :: Nil)
}


