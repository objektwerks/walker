package walker.dialog

import scalafx.Includes.*
import scalafx.scene.layout.Region
import scalafx.scene.control.{ButtonType, Dialog, TextField}
import scalafx.scene.control.ButtonBar.ButtonData

import walker.{Client, Context, Walker}

final class WalkerDialog(context: Context, walker: Walker) extends Dialog[Walker]:
  initOwner(Client.stage)
  title = context.windowTitle
  headerText = context.dialogWalker

  val nameTextField = new TextField:
    text = walker.name

  val controls = List[(String, Region)](
    context.labelName -> nameTextField
  )
  dialogPane().content = ControlGridPane(controls)

  val saveButtonType = new ButtonType(context.buttonSave, ButtonData.OKDone)
  dialogPane().buttonTypes = List(saveButtonType, ButtonType.Cancel)

  resultConverter = dialogButton =>
    if dialogButton == saveButtonType then
      walker.copy(
        name = nameTextField.text.value
      )
    else null