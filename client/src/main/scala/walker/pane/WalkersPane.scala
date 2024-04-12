package walker.pane

import scalafx.Includes.*
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, SelectionMode, Tab, TabPane, TableColumn, TableView}
import scalafx.scene.layout.{HBox, Priority, VBox}

import walker.{Context, Model, Walker}
import walker.dialog.{AccountDialog, FaultsDialog, WalkerDialog, DeactivateReactivate}

final class WalkersPane(context: Context, model: Model) extends VBox:
  spacing = 6
  padding = Insets(6)

  val tableView = new TableView[Walker]():
    columns ++= List(
      new TableColumn[Walker, String]:
        text = context.headerName
        cellValueFactory = _.value.nameProperty
    )
    items = model.observableWalkers

  val addButton = new Button:
    graphic = context.addImage
    text = context.buttonAdd
    disable = false
    onAction = { _ => add() }

  val editButton = new Button:
    graphic = context.editImage
    text = context.buttonEdit
    disable = true
    onAction = { _ => update() }

  val faultsButton = new Button:
    graphic = context.faultsImage
    text = context.buttonFaults
    disable = true
    onAction = { _ => faults() }

  val accountButton = new Button:
    graphic = context.accountImage
    text = context.buttonAccount
    disable = false
    onAction = { _ => account() }

  val buttonBar = new HBox:
    spacing = 6
    children = List(addButton, editButton, faultsButton, accountButton)
  
  val tab = new Tab:
  	text = context.tabWalkers
  	closable = false
  	content = new VBox {
      spacing = 6
      padding = Insets(6)
      children = List(tableView, buttonBar)
    }

  val tabPane = new TabPane:
    tabs = List(tab)

  children = List(tabPane)
  VBox.setVgrow(tableView, Priority.Always)
  VBox.setVgrow(tabPane, Priority.Always)

  model.observableFaults.onChange { (_, _) =>
    faultsButton.disable = false
  }

  tableView.onMouseClicked = { event =>
    if (event.getClickCount == 2 && tableView.selectionModel().getSelectedItem != null) update()
  }

  tableView.selectionModel().selectionModeProperty.value = SelectionMode.Single
  
  tableView.selectionModel().selectedItemProperty().addListener { (_, _, selectedItem) =>
    // model.update executes a remove and add on items. the remove passes a null selectedItem!
    if selectedItem != null then
      model.selectedWalkerId.value = selectedItem.id
      editButton.disable = false
  }

  def add(): Unit =
    WalkerDialog(context, Walker(accountId = model.objectAccount.get.id, name = "")).showAndWait() match
      case Some(walker: Walker) => model.add(0, walker) {
        tableView.selectionModel().select(walker.copy(id = model.selectedWalkerId.value))
      }
      case _ =>

  def update(): Unit =
    val selectedIndex = tableView.selectionModel().getSelectedIndex
    val walker = tableView.selectionModel().getSelectedItem.walker
    WalkerDialog(context, walker).showAndWait() match
      case Some(walker: Walker) => model.update(selectedIndex, walker) {
        tableView.selectionModel().select(selectedIndex)
      }
      case _ =>

  def faults(): Unit = FaultsDialog(context, model).showAndWait() match
    case _ => faultsButton.disable = model.observableFaults.isEmpty

  def account(): Unit = AccountDialog(context, model.objectAccount.get).showAndWait() match
      case Some( DeactivateReactivate( Some(deactivate), None) ) => model.deactivate(deactivate)
      case Some( DeactivateReactivate( None, Some(reactivate) ) ) => model.reactivate(reactivate)
      case _ =>