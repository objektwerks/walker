package walker

import com.typesafe.config.Config

import scala.jdk.CollectionConverters.*
import scalafx.scene.image.{Image, ImageView}

final class Context(config: Config):
  val windowTitle = config.getString("window.title")
  val windowWidth = config.getDouble("window.width")
  val windowHeight = config.getDouble("window.height")

  val url = config.getString("url")
  val endpoint = config.getString("endpoint")

  val buttonAdd = config.getString("button.add")
  val buttonEdit = config.getString("button.edit")
  val buttonSave = config.getString("button.save")
  val buttonChart = config.getString("button.chart")
  val buttonFaults = config.getString("button.faults")
  val buttonRegister = config.getString("button.register")
  val buttonLogin = config.getString("button.login")
  val buttonAccount = config.getString("button.account")
  val buttonActivate = config.getString("button.activate")
  val buttonDeactivate = config.getString("button.deactivate")

  val chartMin = config.getString("chart.min")
  val chartMax = config.getString("chart.max")
  val chartAvg = config.getString("chart.avg")
  val chartMonthDay = config.getString("chart.monthDay")

  val dialogRegisterLogin = config.getString("dialog.registerLogin")
  val dialogAccount = config.getString("dialog.account")
  val dialogSwimmer = config.getString("dialog.swimmer")
  val dialogSession = config.getString("dialog.session")
  val dialogCharts = config.getString("dialog.charts")
  val dialogFaults = config.getString("dialog.faults")

  val errorServer = config.getString("error.server")
  val errorRegister = config.getString("error.register")
  val errorLogin = config.getString("error.login")

  val headerName = config.getString("header.name")
  val headerWeight = config.getString("header.weight")
  val headerWeightUnit = config.getString("header.weightUnit")
  val headerDistance = config.getString("header.distance")
  val headerDistanceUnit = config.getString("header.distanceUnit")
  val headerHours = config.getString("header.hours")
  val headerMinutes = config.getString("header.minutes")
  val headerCalories = config.getString("header.calories")
  val headerDatetime = config.getString("header.datetime")
  val headerOccurred = config.getString("header.occurred")
  val headerFault = config.getString("header.fault")

  val labelLicense = config.getString("label.license")
  val labelEmailAddress = config.getString("label.emailAddress")
  val labelPin = config.getString("label.pin")
  val labelActivated = config.getString("label.activated")
  val labelDeactivated = config.getString("label.deactivated")
  val labelSwimmers = config.getString("label.swimmers")
  val labelSessions = config.getString("label.sessions")
  val labelName = config.getString("label.name")
  val labelWeight = config.getString("label.weight")
  val labelWeightUnit = config.getString("label.weightUnit")
  val labelDistance = config.getString("label.distance")
  val labelDistanceUnit = config.getString("label.distanceUnit")
  val labelHours = config.getString("label.hours")
  val labelMinutes = config.getString("label.minutes")
  val labelCalories = config.getString("label.calories")
  val labelDatetime = config.getString("label.datetime")

  val tabWalkers = config.getString("tab.walkers")
  val tabSessions = config.getString("tab.sessions")
  val tabCalories = config.getString("tab.calories")
  val tabWeight = config.getString("tab.weight")
  val tabDistance = config.getString("tab.distance")

  val dateTimeSelectorEllipsis = config.getString("dateTimeSelector.ellipsis")
  val dateTimeSelectorYear = config.getString("dateTimeSelector.year")
  val dateTimeSelectorMonth = config.getString("dateTimeSelector.month")
  val dateTimeSelectorDay = config.getString("dateTimeSelector.day")
  val dateTimeSelectorHour = config.getString("dateTimeSelector.hour")
  val dateTimeSelectorMinute = config.getString("dateTimeSelector.minute")
  val dateTimeSelectorSecond = config.getString("dateTimeSelector.second")
  val dateTimeSelectorClose = config.getString("dateTimeSelector.close")

  def logoImage = loadImageView("/image/logo.png")
  def addImage = loadImageView("/image/add.png")
  def editImage = loadImageView("/image/edit.png")
  def chartImage = loadImageView("/image/chart.png")
  def faultsImage = loadImageView("/image/faults.png")
  def accountImage = loadImageView("/image/account.png")

  def logo = new Image(Image.getClass.getResourceAsStream("/image/logo.png"))

  private def loadImageView(path: String): ImageView = new ImageView:
    image = new Image(Image.getClass.getResourceAsStream(path))
    fitHeight = 25
    fitWidth = 25
    preserveRatio = true
    smooth = true