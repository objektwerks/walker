package walker.pane

import scalafx.Includes.*
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, SelectionMode, Tab, TabPane, TableColumn, TableView}
import scalafx.scene.layout.{HBox, Priority, VBox}

import walker.{Session, Context, Model}
import walker.dialog.{ChartDialog, SessionDialog}

final class SessionsPane(context: Context, model: Model) extends VBox:
  spacing = 6
  padding = Insets(6)

  val tableView = new TableView[Session]():
    columns ++= List(
      new TableColumn[Session, Double]:
        text = context.headerWeight
        cellValueFactory = _.value.weightProperty
      ,
      new TableColumn[Session, String]:
        text = context.headerWeightUnit
        cellValueFactory = _.value.weightUnitProperty
      ,
      new TableColumn[Session, Double]:
        text = context.headerDistance
        cellValueFactory = _.value.distanceProperty
      ,
      new TableColumn[Session, String]:
        text = context.headerDistanceUnit
        cellValueFactory = _.value.distanceUnitProperty
      ,
      new TableColumn[Session, Int]:
        text = context.headerHours
        cellValueFactory = _.value.hoursProperty
      ,
      new TableColumn[Session, Int]:
        text = context.headerMinutes
        cellValueFactory = _.value.minutesProperty
      ,
      new TableColumn[Session, Int]:
        text = context.headerCalories
        cellValueFactory = _.value.caloriesProperty
      ,
      new TableColumn[Session, String]:
        text = context.headerDatetime
        cellValueFactory = _.value.datetimeProperty
      ,
    )
    items = model.observableSessions

  val addButton = new Button:
    graphic = context.addImage
    text = context.buttonAdd
    disable = true
    onAction = { _ => add() }

  val editButton = new Button:
    graphic = context.editImage
    text = context.buttonEdit
    disable = true
    onAction = { _ => update() }

  val chartButton = new Button:
    graphic = context.chartImage
    text = context.buttonChart
    disable = true
    onAction = { _ => chart() }

  val buttonBar = new HBox:
    spacing = 6
    children = List(addButton, editButton, chartButton)

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

  model.selectedSessionId.onChange { (_, _, _) =>
    addButton.disable = false
    chartButton.disable = false
  }

  tableView.onMouseClicked = { event =>
    if (event.getClickCount == 2 && tableView.selectionModel().getSelectedItem != null) update()
  }

  tableView.selectionModel().selectionModeProperty.value = SelectionMode.Single

  tableView.selectionModel().selectedItemProperty().addListener { (_, _, selectedItem) =>
    // model.update executes a remove and add on items. the remove passes a null selectedItem!
    if selectedItem != null then
      model.selectedSessionId.value = selectedItem.id
      editButton.disable = false
    else editButton.disable = true
  }

  def add(): Unit =
    SessionDialog(context, Session(walkerId = model.selectedWalkerId.value)).showAndWait() match
      case Some(session: Session) => model.add(0, session) {
        tableView.selectionModel().select(0)
      }
      case _ =>

  def update(): Unit =
    if tableView.selectionModel().getSelectedItem != null then
      val selectedIndex = tableView.selectionModel().getSelectedIndex
      val session = tableView.selectionModel().getSelectedItem.session
      SessionDialog(context, session).showAndWait() match
        case Some(session: Session) => model.update(selectedIndex, session){
          tableView.selectionModel().select(selectedIndex)
        }
        case _ =>

  def chart(): Unit = ChartDialog(context, model).showAndWait()