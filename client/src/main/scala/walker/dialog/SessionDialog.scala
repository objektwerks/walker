package walker.dialog

import scalafx.Includes.*
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ButtonType, CheckBox, ComboBox, Dialog}
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout.Region

import walker.{Client, Context, DistanceUnit, Entity, Session, WeightUnit}
import walker.control.{CalorieTextField, DateTimeSelector, DoubleTextField, IntTextField}

final class SessionDialog(context: Context, session: Session) extends Dialog[Session]:
  initOwner(Client.stage)
  title = context.windowTitle
  headerText = context.dialogSession

  val weightTextField = new IntTextField:
    text = session.weight.toString
  
  val weightUnitComboBox = new ComboBox[String]:
  	items = ObservableBuffer.from( WeightUnit.toList )
  	value = session.weightUnit.toString
  weightUnitComboBox.prefWidth = 200

  val distanceTextField = new DoubleTextField:
    text = session.distance.toString

  val distanceUnitComboBox = new ComboBox[String]:
  	items = ObservableBuffer.from( DistanceUnit.toList )
  	value = session.distanceUnit.toString
  distanceUnitComboBox.prefWidth = 150

  val hoursTextField = new IntTextField:
    text = session.hours.toString

  val minutesTextField = new IntTextField:
    text = session.minutes.toString

  val caloriesTextField = CalorieTextField(session)

  val datetimeSelector = DateTimeSelector( context, Entity.toLocalDateTime(session.datetime) )

  val controls = List[(String, Region)](
    context.labelWeightUnit  -> weightTextField,
    context.labelWeightUnit  -> weightUnitComboBox,
    context.labelDistance    -> distanceTextField,
    context.labelLapUnit     -> distanceUnitComboBox,
    context.labelHours       -> hoursTextField,
    context.labelMinutes     -> minutesTextField,
    context.labelCalories    -> caloriesTextField,
    context.labelDatetime    -> datetimeSelector
  )
  dialogPane().content = ControlGridPane(controls)

  val saveButtonType = new ButtonType(context.buttonSave, ButtonData.OKDone)
  dialogPane().buttonTypes = List(saveButtonType, ButtonType.Cancel)

  resultConverter = dialogButton =>
    if dialogButton == saveButtonType then
      session.copy(
        weight = weightTextField.int(session.weight),
        weightUnit = weightUnitComboBox.value.value,
        distance = distanceTextField.double(session.distance),
        distanceUnit = distanceUnitComboBox.value.value,
        hours = hoursTextField.int(session.hours),
        minutes = minutesTextField.int(session.minutes),
        calories = caloriesTextField.int(session.calories),
        datetime = Entity.toEpochMillis(datetimeSelector.value.value)
      )
    else null