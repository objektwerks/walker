package swimmer.control

import java.time.LocalDateTime

import scalafx.beans.property.ObjectProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, Spinner}
import scalafx.scene.layout.{GridPane, HBox, Priority, VBox}
import scalafx.stage.Popup

import swimmer.{Context, Entity}

final class DateTimeSelector(context: Context, localDateTime: LocalDateTime) extends HBox:
  spacing = 3
  padding = Insets(3)

  val value: ObjectProperty[LocalDateTime] = ObjectProperty(localDateTime)

  private val localDateTimeLabel = new Label:
    alignment = Pos.CENTER_LEFT
    text = Entity.format(localDateTime)

  private val localDateTimeButton = new Button:
    text = context.dateTimeSelectorEllipsis
    disable = false
    onAction = { _ => showPopup() }

  children = List(localDateTimeLabel, localDateTimeButton)
  HBox.setHgrow(this, Priority.Always)

  private def showPopup(): Unit =
    val popup = Popup()
    popup.setHideOnEscape(false)
    val popupView = PopupView(context, value.value, popup, popupValue)
    popup.content.addOne(popupView)
    popup.show(popup.ownerWindow.value)

  private def popupValue(popupLocalDateTime: LocalDateTime): Unit =
    value.value = popupLocalDateTime
    localDateTimeLabel.text = Entity.format(popupLocalDateTime)

private final class PopupView(context: Context,
                              localDateTime: LocalDateTime,
                              popup: Popup,
                              popupValue: (LocalDateTime) => Unit) extends VBox: 
  val yearSpinner = Spinner[Int](min = localDateTime.getYear - 1, max = localDateTime.getYear, initialValue = localDateTime.getYear, amountToStepBy = 1)
  val monthSpinner = Spinner[Int](min = 1, max = 12, initialValue = localDateTime.getMonthValue, amountToStepBy = 1)
  val daySpinner = Spinner[Int](min = 1, max = 31, initialValue = localDateTime.getDayOfMonth, amountToStepBy = 1)
  val hourSpinner = Spinner[Int](min = 0, max = 23, initialValue = localDateTime.getHour, amountToStepBy = 1)
  val minuteSpinner = Spinner[Int](min = 0, max = 59, initialValue = localDateTime.getMinute, amountToStepBy = 1)
  val secondSpinner = Spinner[Int](min = 0, max = 59, initialValue = localDateTime.getSecond, amountToStepBy = 1)

  val controls = List[(String, Spinner[Int])](
    context.dateTimeSelectorYear -> yearSpinner,
    context.dateTimeSelectorMonth -> monthSpinner,
    context.dateTimeSelectorDay -> daySpinner,
    context.dateTimeSelectorHour -> hourSpinner,
    context.dateTimeSelectorMinute -> minuteSpinner,
    context.dateTimeSelectorSecond -> secondSpinner
  )

  val selector = buildGridPane(controls)

  val closeButton = new Button:
    alignment = Pos.CENTER
    text = context.dateTimeSelectorClose
    disable = false
    onAction = { _ =>
      popup.hide()
      popupValue( value() )
    }

  children = List(selector, closeButton)
  VBox.setVgrow(this, Priority.Always)

  private def value(): LocalDateTime =
    LocalDateTime
      .of(
        yearSpinner.value.value,
        monthSpinner.value.value,
        daySpinner.value.value,
        hourSpinner.value.value,
        minuteSpinner.value.value,
        secondSpinner.value.value
      )

  private def buildGridPane(controls: List[(String, Spinner[Int])]): GridPane =
    val gridPane = new GridPane:
      hgap = 6
      vgap = 6
      padding = Insets(top = 6, right = 6, bottom = 6, left = 6)
    
    var row = 0
    for ((label, spinner) <- controls)
      val columnLabel = new Label:
        alignment = Pos.CENTER_LEFT
        text = label
      gridPane.add(columnLabel, columnIndex = 0, rowIndex = row)
      gridPane.add(spinner, columnIndex = 1, rowIndex = row)
      row += 1

    gridPane