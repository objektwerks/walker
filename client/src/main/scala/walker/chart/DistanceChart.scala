package walker.chart

import java.time.format.DateTimeFormatter

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Orientation}
import scalafx.Includes.*
import scalafx.scene.chart.{LineChart, XYChart}
import scalafx.scene.control.{ComboBox, Separator, Tab}
import scalafx.scene.layout.{Region, VBox}

import walker.{Context, Entity, Model}
import walker.dialog.ControlGridPane

final case class DistanceXY(xDate: String, yCount: Int)

final class DistanceChart(context: Context, model: Model) extends Tab:
  val sessions = model.observableSessions.reverse
  val dateFormat = DateTimeFormatter.ofPattern("M.dd")
  val minDate = Entity.toLocalDateTime( sessions.map(e => e.datetime).min ).format(dateFormat)
  val maxDate = Entity.toLocalDateTime( sessions.map(e => e.datetime).max ).format(dateFormat)

  closable = false
  text = context.tabDistance
  content = new VBox {
    spacing = 6
    padding = Insets(6)
    children = List(
      buildChart()
    )
  }

  def buildChart(): LineChart[String, Number] =
    val filtered = sessions.map(s => DistanceXY( Entity.toLocalDateTime(s.datetime).format(dateFormat), s.distance.round.toInt) )
    val (chart, series) = LineChartBuilder.build(context = context,
                                                 xLabel = context.chartMonthDay,
                                                 xMinDate = minDate,
                                                 xMaxDate = maxDate,
                                                 yLabel = context.tabDistance,
                                                 yLowerBound = 1,
                                                 yUpperBound = 10,
                                                 yTickUnit = 1,
                                                 yValues = filtered.map(exy => exy.yCount))
    filtered foreach { exy =>
      series.data() += XYChart.Data[String, Number](exy.xDate.format(dateFormat), exy.yCount)
    }
    chart.data = series
    LineChartBuilder.addTooltip(chart)
    chart