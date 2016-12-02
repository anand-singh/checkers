package checkers.userinterface.piece

import checkers.consts._
import checkers.core.Board
import checkers.util.Easing
import japgolly.scalajs.react._

object JumpingPieceAnimation {

  case class Props(piece: Occupant,
                   fromSquare: Int,
                   toSquare: Int,
                   progress: Double,
                   rotationDegrees: Double = 0)

  class JumpingPieceAnimationBackend($: BackendScope[Props, Unit]) {
    def render(props: Props) = {
      val t = Easing.easeInOutQuart(props.progress)
      val ptA = Board.squareCenter(props.fromSquare)
      val ptB = Board.squareCenter(props.toSquare)

      val x0 = ptA.x
      val x = x0 + t * (ptB.x - x0)

      val y0 = ptA.y
      val y = y0 + t * (ptB.y - y0)

      val height = {
        val z = 2 * t - 1
        (1 - (z * z)) * 0.3
      }

      val physicalPieceProps = PhysicalPieceProps.default.copy(piece = props.piece,
        scale = 1 + height,
        x = x,
        y = y,
        rotationDegrees = props.rotationDegrees)

      val physicalPiece = PhysicalPiece.apply(physicalPieceProps)

      physicalPiece
    }
  }


  val component = ReactComponentB[Props]("JumpingPieceAnimation")
    .renderBackend[JumpingPieceAnimationBackend]
    .shouldComponentUpdateCB { case ShouldComponentUpdate(scope, nextProps, _) =>
      val result = scope.props != nextProps
      CallbackTo.pure(result)
    }
    .build

  def apply(props: Props) = component(props)

}