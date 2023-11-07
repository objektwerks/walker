package walker.chart

import java.time.format.DateTimeFormatter

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Orientation}
import scalafx.Includes.*
import scalafx.scene.chart.{LineChart, XYChart}
import scalafx.scene.control.{ComboBox, Separator, Tab}
import scalafx.scene.layout.{Region, VBox}

import walker.{Context, Entity, Model, Style}
import walker.dialog.ControlGridPane

final case class DistanceXY(xDate: String, yCount: Int)

final class DistanceChart(context: Context, model: Model) extends Tab:
  val sessions = model.observableSessions.reverse
  val dateFormat = DateTimeFormatter.ofPattern("M.dd")
  val minDate = Entity.toLocalDateTime( sessions.map(e => e.datetime).min ).format(dateFormat)
  val maxDate = Entity.toLocalDateTime( sessions.map(e => e.datetime).max ).format(dateFormat)

  val styleComboBox = new ComboBox[String]:
  	items = ObservableBuffer.from( Style.toList )
  	value = Style.freestyle.toString
  styleComboBox.prefWidth = 300
  styleComboBox.onAction = { _ => buildChart( Style.valueOf(styleComboBox.value.value) ) }

  val controls = List[(String, Region)](
    context.labelStyle -> styleComboBox
  )

  closable = false
  text = context.tabDistance
  content = new VBox {
    spacing = 6
    padding = Insets(6)
    children = List(
      ControlGridPane(controls),
      Separator(Orientation.Horizontal),
      buildChart(Style.freestyle)
    )
  }

  def buildChart(style: Style): LineChart[String, Number] =
    val filtered = sessions
      .filter(s => s.style == style.toString)
      .map(s => DistanceXY( Entity.toLocalDateTime(s.datetime).format(dateFormat), s.distance()) )
    val (chart, series) = LineChartBuilder.build(context = context,
                                                 xLabel = context.chartMonthDay,
                                                 xMinDate = minDate,
                                                 xMaxDate = maxDate,
                                                 yLabel = context.tabDistance,
                                                 yLowerBound = 100,
                                                 yUpperBound = 10000,
                                                 yTickUnit = 100,
                                                 yValues = filtered.map(exy => exy.yCount))
    filtered foreach { exy =>
      series.data() += XYChart.Data[String, Number](exy.xDate.format(dateFormat), exy.yCount)
    }
    chart.data = series
    LineChartBuilder.addTooltip(chart)
    chart