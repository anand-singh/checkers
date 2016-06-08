package checkers.components

import checkers.components.board.PhysicalBoard
import checkers.components.piece._
import checkers.game._
import checkers.geometry.Point
import checkers.models
import checkers.models.Animation.HidesStaticPiece
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.scalajs.js

object DynamicScene {

  //  case class Model(playField: PlayField,
  //                   rotationDegrees: Double)
  type Model = models.GameScreenModel

  type Callbacks = PieceCallbacks

  type Props = (Model, Callbacks, SceneContainerContext, Point => Point)


  def testCallback(tag: Int) = Callback {
    println(s"tag $tag")
  }

  object TestPieceEvents extends PieceCallbacks {
    val onMouseDown = (event: PieceMouseEvent) => Some(Callback {
      println(event)
    })
  }

  val component = ReactComponentB[Props]("DynamicScene")
    .render_P { case (model, callbacks, sceneContainerContext, screenToBoard) =>

      val boardRotation = model.getBoardRotation

      val pieceRotation = if(boardRotation != 0) -boardRotation else 0
      val pieceScale = 1.0d

      val piecesToHide = model.animations.foldLeft(Set.empty[Int]) {
        case (res, anim: HidesStaticPiece) => res + anim.hidesPieceAtSquare
        case (res, _) => res
      }

      val squares = model.gameState.board.squares

      val staticPieces = new js.Array[ReactNode]

      Board.allSquares.filterNot(piecesToHide.contains).foreach { squareIndex =>
        val occupant = squares(squareIndex)
        occupant match {
          case piece: Piece =>
            val k = s"sp-$squareIndex"

            val pos = Board.position(squareIndex)
            val pt = PhysicalBoard.positionToPoint(pos)

            val pieceProps = PhysicalPieceProps(
              piece = piece,
              tag = squareIndex,
              x = pt.x,
              y = pt.y,
              scale = pieceScale,
              rotationDegrees = pieceRotation,
              clickable = model.clickableSquares.contains(squareIndex),
              highlighted = model.highlightedSquares.contains(squareIndex),
              screenToBoard = screenToBoard,
              callbacks = callbacks)

            val physicalPiece = PhysicalPiece.apply.withKey(k)(pieceProps)
            staticPieces.push(physicalPiece)
          case _ =>
        }
      }

      val ghostPiece = model.ghostPiece.map { gp =>
        GhostPiece(gp)
      }

      <.svg.g(
        staticPieces,
        ghostPiece
      )

    }.build


  def apply(props: Props) = component(props)


}