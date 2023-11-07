package walker.control

import scalafx.scene.control.Button
import scalafx.scene.layout.HBox

import walker.Session

class CalorieTextField(session: Session) extends HBox:
  val caloriesTextField = new IntTextField:
    text = session.calories.toString

  val calcButton = new Button:
    text = "!"
    onAction = { _ =>
      caloriesTextField.text = session.caloriesBurned().toString
    }

  children = List(caloriesTextField, calcButton)

  def int(default: Int): Int = caloriesTextField.text.value.toIntOption.getOrElse(default)