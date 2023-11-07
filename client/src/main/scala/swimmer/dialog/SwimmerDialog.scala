package swimmer.dialog

import scalafx.Includes.*
import scalafx.scene.layout.Region
import scalafx.scene.control.{ButtonType, Dialog, TextField}
import scalafx.scene.control.ButtonBar.ButtonData

import swimmer.{Client, Context, Swimmer}

final class SwimmerDialog(context: Context, swimmer: Swimmer) extends Dialog[Swimmer]:
  initOwner(Client.stage)
  title = context.windowTitle
  headerText = context.dialogSwimmer

  val nameTextField = new TextField:
    text = swimmer.name

  val controls = List[(String, Region)](
    context.labelName -> nameTextField
  )
  dialogPane().content = ControlGridPane(controls)

  val saveButtonType = new ButtonType(context.buttonSave, ButtonData.OKDone)
  dialogPane().buttonTypes = List(saveButtonType, ButtonType.Cancel)

  resultConverter = dialogButton =>
    if dialogButton == saveButtonType then
      swimmer.copy(
        name = nameTextField.text.value
      )
    else null