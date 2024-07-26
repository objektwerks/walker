package walker

import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.zaxxer.hikari.HikariDataSource

import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

import scalikejdbc.*
import scala.concurrent.duration.FiniteDuration

object Store:
  def cache(config: Config): Cache[String, String] =
    Scaffeine()
      .initialCapacity(config.getInt("cache.initialSize"))
      .maximumSize(config.getInt("cache.maxSize"))
      .expireAfterWrite( FiniteDuration( config.getLong("cache.expireAfter"), TimeUnit.HOURS) )
      .build[String, String]()

  def dataSource(config: Config): DataSource =
    val ds = HikariDataSource()
    ds.setDataSourceClassName(config.getString("db.driver"))
    ds.addDataSourceProperty("url", config.getString("db.url"))
    ds.addDataSourceProperty("user", config.getString("db.user"))
    ds.addDataSourceProperty("password", config.getString("db.password"))
    ds

final class Store(cache: Cache[String, String],
                  dataSource: DataSource):
  ConnectionPool.singleton( DataSourceConnectionPool(dataSource) )

  def register(account: Account): Account = addAccount(account)

  def login(email: String, pin: String): Option[Account] =
    DB readOnly { implicit session =>
      sql"select * from account where email_address = $email and pin = $pin"
        .map(rs =>
          Account(
            rs.long("id"),
            rs.string("license"),
            rs.string("email_address"),
            rs.string("pin"),
            rs.long("activated"),
            rs.long("deactivated")
          )
        )
        .single()
    }

  def isEmailAddressUnique(emailAddress: String): Boolean =
    val count = DB readOnly { implicit session =>
      sql"select id from account where email_address = $emailAddress"
        .map(rs => rs.long("id"))
        .single()
    }
    if count.isDefined then false else true

  def isAuthorized(license: String): Boolean =
    cache.getIfPresent(license) match
      case Some(_) => true
      case None =>
        val optionalLicense = DB readOnly { implicit session =>
          sql"select license from account where license = $license"
            .map(rs => rs.string("license"))
            .single()
        }
        if optionalLicense.isDefined then
          cache.put(license, license)
          true
        else false

  def listAccounts(): List[Account] =
    DB readOnly { implicit session =>
      sql"select * from account"
        .map(rs =>
          Account(
            rs.long("id"),
            rs.string("license"),
            rs.string("email_address"),
            rs.string("pin"),
            rs.long("activated"),
            rs.long("deactivated")
          )
        )
        .list()
    }

  def addAccount(account: Account): Account =
    val id = DB localTx { implicit session =>
      sql"""
        insert into account(license, email_address, pin, activated, deactivated)
        values(${account.license}, ${account.emailAddress}, ${account.pin}, ${account.activated}, ${account.deactivated})
      """
      .updateAndReturnGeneratedKey()
    }
    account.copy(id = id)

  def removeAccount(license: String): Unit =
    DB localTx { implicit session =>
      sql"delete account where license = $license"
      .update()
    }
    ()

  def deactivateAccount(license: String): Option[Account] =
    DB localTx { implicit session =>
      val deactivated = sql"update account set deactivated = ${LocalDate.now.toEpochDay}, activated = 0 where license = $license"
      .update()
      if deactivated > 0 then
        sql"select * from account where license = $license"
          .map(rs =>
            Account(
              rs.long("id"),
              rs.string("license"),
              rs.string("email_address"),
              rs.string("pin"),
              rs.long("activated"),
              rs.long("deactivated")
            )
          )
          .single()
      else None
    }

  def reactivateAccount(license: String): Option[Account] =
    DB localTx { implicit session =>
      val activated = sql"update account set activated = ${LocalDate.now.toEpochDay}, deactivated = 0 where license = $license"
      .update()
      if activated > 0 then
        sql"select * from account where license = $license"
          .map(rs =>
            Account(
              rs.long("id"),
              rs.string("license"),
              rs.string("email_address"),
              rs.string("pin"),
              rs.long("activated"),
              rs.long("deactivated")
            )
          )
          .single()
      else None
    }

  def listWalkers(accountId: Long): List[Walker] =
    DB readOnly { implicit session =>
      sql"select * from walker where account_id = $accountId order by name"
        .map(rs =>
          Walker(
            rs.long("id"),
            rs.long("account_id"),
            rs.string("name"), 
          )
        )
        .list()
    }

  def addWalker(walker: Walker): Long =
    DB localTx { implicit session =>
      sql"""
        insert into walker(account_id, name) values(${walker.accountId}, ${walker.name})
        """
        .updateAndReturnGeneratedKey()
    }

  def updateWalker(walker: Walker): Long =
    DB localTx { implicit session =>
      sql"""
        update walker set name = ${walker.name}
        where id = ${walker.id}
        """
        .update()
      walker.id
    }

  def listSessions(walkerId: Long): List[Session] =
    DB readOnly { implicit session =>
      sql"select * from session where walker_id = $walkerId order by datetime desc"
        .map(rs =>
          Session(
            rs.long("id"),
            rs.long("walker_id"),
            rs.int("weight"),
            rs.string("weight_unit"),
            rs.double("distance"),
            rs.string("distance_unit"),
            rs.int("hours"),
            rs.int("minutes"),
            rs.int("calories"),
            rs.long("datetime")
          )
        )
        .list()
    }

  def addSession(sess: Session): Long =
    DB localTx { implicit session =>
      sql"""
        insert into session(walker_id, weight, weight_unit, distance, distance_unit, hours, minutes, calories, datetime)
        values(${sess.walkerId}, ${sess.weight}, ${sess.weightUnit}, ${sess.distance}, ${sess.distanceUnit}, ${sess.hours},
        ${sess.minutes}, ${sess.calories}, ${sess.datetime})
        """
        .updateAndReturnGeneratedKey()
    }

  def updateSession(sess: Session): Long =
    DB localTx { implicit session =>
      sql"""
        update session set weight = ${sess.weight}, weight_unit = ${sess.weightUnit}, distance = ${sess.distance},
        distance_unit = ${sess.distanceUnit}, hours = ${sess.hours}, minutes = ${sess.minutes}, calories = ${sess.calories},
        datetime = ${sess.datetime} where id = ${sess.id}
        """
        .update()
      sess.id
    }

  def listFaults(): List[Fault] =
    DB readOnly { implicit session =>
      sql"select * from fault order by occurred desc"
        .map(rs =>
          Fault(
            rs.string("cause"),
            rs.string("occurred")
          )
        )
        .list()
    }

  def addFault(fault: Fault): Fault =
    DB localTx { implicit session =>
      sql"""
        insert into fault(cause, occurred) values(${fault.cause}, ${fault.occurred})
        """
        .update()
        fault
    }