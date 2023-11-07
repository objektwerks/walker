package swimmer.dialog

import scalafx.Includes.*
import scalafx.scene.control.{ButtonType, Dialog, TabPane}
import scalafx.scene.layout.VBox

import swimmer.{Client, Context, Model}
import swimmer.chart.{CaloriesChart, DistanceChart, WeightChart}

final class ChartDialog(context: Context, model: Model) extends Dialog:
  initOwner(Client.stage)
  title = context.windowTitle
  headerText = context.dialogCharts

  val chartsTabPane = new TabPane:
    tabs = List(
      DistanceChart(context, model),
      CaloriesChart(context, model),
      WeightChart(context, model)
    )

  dialogPane().buttonTypes = List(ButtonType.Close)
  dialogPane().content = new VBox:
    spacing = 6
    children = List(chartsTabPane)