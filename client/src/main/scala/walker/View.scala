package walker

import scalafx.geometry.{Insets, Orientation}
import scalafx.scene.Scene
import scalafx.scene.control.SplitPane
import scalafx.scene.layout.{Priority, VBox}

import walker.pane.{SessionsPane, WalkersPane}

final class View(context: Context, model: Model):
  val vbox = new VBox:
    prefWidth = context.windowWidth
    prefHeight = context.windowHeight
    padding = Insets(6)

  val walkersPane = WalkersPane(context, model)
  VBox.setVgrow(walkersPane, Priority.Always)

  val sessionsPane = SessionsPane(context, model)
  VBox.setVgrow(sessionsPane, Priority.Always)

  val splitPane = new SplitPane {
    orientation = Orientation.Horizontal
    items.addAll(walkersPane, sessionsPane)
  }
  splitPane.setDividerPositions(0.32, 0.68)
  VBox.setVgrow(splitPane, Priority.Always)

  vbox.children = List(splitPane)

  val scene = new Scene:
    root = vbox
    stylesheets = List("/style.css")