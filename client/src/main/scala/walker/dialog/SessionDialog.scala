package walker.dialog

import scalafx.Includes.*
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{ButtonType, CheckBox, ComboBox, Dialog}
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout.Region

import walker.{Client, Context, Entity, LapUnit, Session, Style, WeightUnit}
import walker.control.{CalorieTextField, DateTimeSelector, IntTextField}

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

  val lapsTextField = new IntTextField:
    text = session.laps.toString

  val lapDistanceTextField = new IntTextField:
    text = session.lapDistance.toString

  val lapUnitComboBox = new ComboBox[String]:
  	items = ObservableBuffer.from( LapUnit.toList )
  	value = session.lapUnit.toString
  lapUnitComboBox.prefWidth = 200

  val styleComboBox = new ComboBox[String]:
  	items = ObservableBuffer.from( Style.toList )
  	value = session.style.toString
  styleComboBox.prefWidth = 300

  val kickboardCheckBox = new CheckBox:
    selected = session.kickboard

  val finsCheckBox = new CheckBox:
    selected = session.fins

  val minutesTextField = new IntTextField:
    text = session.minutes.toString

  val secondsTextField = new IntTextField:
    text = session.seconds.toString

  val caloriesTextField = CalorieTextField(session)

  val datetimeSelector = DateTimeSelector( context, Entity.toLocalDateTime(session.datetime) )

  val controls = List[(String, Region)](
    context.labelWeightUnit  -> weightTextField,
    context.labelWeightUnit  -> weightUnitComboBox,
    context.labelLaps        -> lapsTextField,
    context.labelLapDistance -> lapDistanceTextField,
    context.labelLapUnit     -> lapUnitComboBox,
    context.labelStyle       -> styleComboBox,
    context.labelKickboard   -> kickboardCheckBox,
    context.labelFins        -> finsCheckBox,
    context.labelMinutes     -> minutesTextField,
    context.labelSeconds     -> secondsTextField,
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
        laps = lapsTextField.int(session.laps),
        lapDistance = lapDistanceTextField.int(session.lapDistance),
        lapUnit = lapUnitComboBox.value.value,
        style = styleComboBox.value.value,
        kickboard = kickboardCheckBox.selected.value,
        fins = finsCheckBox.selected.value,
        minutes = minutesTextField.int(session.minutes),
        seconds = secondsTextField.int(session.seconds),
        calories = caloriesTextField.int(session.calories),
        datetime = Entity.toEpochMillis(datetimeSelector.value.value)
      )
    else null