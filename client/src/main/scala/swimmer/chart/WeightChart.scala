package swimmer.chart

import java.time.format.DateTimeFormatter

import scalafx.Includes.*
import scalafx.scene.chart.{LineChart, XYChart}
import scalafx.scene.control.Tab

import swimmer.{Context, Entity, Model}

final case class WeightXY(xDate: String, yCount: Double)

final class WeightChart(context: Context, model: Model) extends Tab:
  val sessions = model.observableSessions.reverse
  val dateFormat = DateTimeFormatter.ofPattern("M.dd")
  val minDate = Entity.toLocalDateTime( sessions.map(e => e.datetime).min ).format(dateFormat)
  val maxDate = Entity.toLocalDateTime( sessions.map(e => e.datetime).max ).format(dateFormat)

  closable = false
  text = context.tabWeight
  content = buildChart()

  def buildChart(): LineChart[String, Number] =
    val filtered = sessions.map(s => WeightXY( Entity.toLocalDateTime(s.datetime).format(dateFormat), s.weight) )
    val (chart, series) = LineChartBuilder.build(context = context,
                                                 xLabel = context.chartMonthDay,
                                                 xMinDate = minDate,
                                                 xMaxDate = maxDate,
                                                 yLabel = context.tabWeight,
                                                 yLowerBound = 50,
                                                 yUpperBound = 400,
                                                 yTickUnit = 50,
                                                 yValues = filtered.map(exy => exy.yCount))
    filtered foreach { exy =>
      series.data() += XYChart.Data[String, Number](exy.xDate.format(dateFormat), exy.yCount)
    }
    chart.data = series
    LineChartBuilder.addTooltip(chart)
    chart