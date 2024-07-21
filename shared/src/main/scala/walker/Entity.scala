package walker

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.UUID

import scalafx.beans.property.ObjectProperty

sealed trait Entity:
  val id: Long

object Entity:
  given JsonValueCodec[Entity] = JsonCodecMaker.make[Entity](CodecMakerConfig.withDiscriminatorFieldName(None))

  val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd, hh:mm a")

  def format(epochMillis: Long): String = formatter.format( toLocalDateTime(epochMillis) )
  def format(localDateTime: LocalDateTime): String = formatter.format(localDateTime)

  def toLocalDateTime(epochMillis: Long): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC)
  def toEpochMillis(localDateTime: LocalDateTime): Long = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli

final case class Account(id: Long = 0,
                         license: String = UUID.randomUUID.toString,
                         emailAddress: String = "",
                         pin: String = Pin.newInstance,
                         activated: Long = Instant.now.toEpochMilli,
                         deactivated: Long = 0) extends Entity derives CanEqual

object Account:
  val empty = Account(license = "", emailAddress = "", pin = "", activated = 0, deactivated = 0)
  given JsonValueCodec[Account] = JsonCodecMaker.make[Account]

final case class Walker(id: Long = 0,
                         accountId: Long,
                         name: String) extends Entity derives CanEqual:
  val nameProperty = ObjectProperty[String](this, "name", name)
  val walker = this

object Walker:
  given JsonValueCodec[Walker] = JsonCodecMaker.make[Walker]
  given walkerOrdering: Ordering[Walker] = Ordering.by[Walker, String](s => s.name)

final case class Session(id: Long = 0,
                         walkerId: Long,
                         weight: Int = 150,
                         weightUnit: String = WeightUnit.lb.toString,
                         distance: Double = 2.0,
                         distanceUnit: String = DistanceUnit.mi.toString,
                         hours: Int = 0,
                         minutes: Int = 40,
                         calories: Int = 150,
                         datetime: Long = Instant.now.toEpochMilli) extends Entity:
  val weightProperty = ObjectProperty[Double](this, "weight", weight)
  val weightUnitProperty = ObjectProperty[String](this, "weightUnit", weightUnit)
  val distanceProperty = ObjectProperty[Double](this, "distance", distance)
  val distanceUnitProperty = ObjectProperty[String](this, "distanceUnit", distanceUnit)
  val hoursProperty = ObjectProperty[Int](this, "hours", hours)
  val minutesProperty = ObjectProperty[Int](this, "minutes", minutes)
  val caloriesProperty = ObjectProperty[Int](this, "calories", calories)
  val datetimeProperty = ObjectProperty[String](this, "datetime", Entity.format(datetime))
  val session = this

  def minutesWalked(): Int = (hours * 60) + minutes

  def caloriesBurned(): Int =
    if weight < 1.0 || minutes < 1 then 0
    else
      val kg = if WeightUnit.lb.toString == weightUnit then WeightUnit.lbsToKgs(weight) else weight.toDouble
      val met = (Session.MET * 3.5 * kg) / 200
      val mins = minutesWalked()
      ( mins * met ).round.toInt

object Session:
  val MET = 6
  given JsonValueCodec[Session] = JsonCodecMaker.make[Session]
  given sessionOrdering: Ordering[Session] = Ordering.by[Session, Long](session => session.datetime).reverse

enum WeightUnit:
  case lb, kg

object WeightUnit:
  def lbsToKgs(lbs: Int): Double = lbs * 0.454
  def kgsToLbs(kgs: Int): Double = kgs * 2.205
  def toList: List[String] = WeightUnit.values.map(wu => wu.toString).toList

enum DistanceUnit:
  case km, mi

object DistanceUnit:
  def toList: List[String] = DistanceUnit.values.map(du => du.toString).toList