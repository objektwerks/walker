package walker

import com.typesafe.config.ConfigFactory

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.*
import scala.sys.process.Process

import Validator.*

final class IntegrationTest extends AnyFunSuite with Matchers:
  val exitCode = Process("psql -d walker -f ddl.sql").run().exitValue()
  exitCode shouldBe 0

  val config = ConfigFactory.load("test.conf")

  val cache = Store.cache(config)
  val dataSource = Store.dataSource(config)
  val store = Store(cache, dataSource)
  val emailer = Emailer(config)
  val dispatcher = Dispatcher(store, emailer)

  var testAccount = Account()
  var testWalker = Walker(accountId = 0, name = "")
  var testSession = Session(walkerId = 0)

  test("integration"):
    register
    login

    deactivate
    reactivate

    addWalker
    updateWalker
    listWalkers

    addSession
    updateSession
    listSessions

    fault

  def register: Unit =
    val register = Register(config.getString("email.sender"))
    dispatcher.dispatch(register) match
      case Registered(account) =>
        assert( account.isActivated )
        testAccount = account
      case fault => fail(s"Invalid registered event: $fault")
    
  def login: Unit =
    val login = Login(testAccount.emailAddress, testAccount.pin)
    dispatcher.dispatch(login) match
      case LoggedIn(account) => account shouldBe testAccount
      case fault => fail(s"Invalid loggedin event: $fault")

  def deactivate: Unit =
    val deactivate = Deactivate(testAccount.license)
    dispatcher.dispatch(deactivate) match
      case Deactivated(account) => assert( account.isDeactivated )
      case fault => fail(s"Invalid deactivated event: $fault")

  def reactivate: Unit =
    val reactivate = Reactivate(testAccount.license)
    dispatcher.dispatch(reactivate) match
      case Reactivated(account) => assert( account.isActivated )
      case fault => fail(s"Invalid reactivated event: $fault")

  def addWalker: Unit =
    testWalker = testWalker.copy(accountId = testAccount.id, name = "Fred")
    val saveWalker = SaveWalker(testAccount.license, testWalker)
    dispatcher.dispatch(saveWalker) match
      case WalkerSaved(id) =>
        id should not be 0
        testWalker = testWalker.copy(id = id)
        testSession = testSession.copy(walkerId = id)
      case fault => fail(s"Invalid walker saved event: $fault")

  def updateWalker: Unit =
    testWalker = testWalker.copy(name = "Fred Flintstone")
    val saveWalker = SaveWalker(testAccount.license, testWalker)
    dispatcher.dispatch(saveWalker) match
      case WalkerSaved(id) => id shouldBe testWalker.id
      case fault => fail(s"Invalid walker saved event: $fault")
    
  def listWalkers: Unit =
    val listWalkers = ListWalkers(testAccount.license, testWalker.accountId)
    dispatcher.dispatch(listWalkers) match
      case WalkersListed(walkers) =>
        walkers.length shouldBe 1
        walkers.head shouldBe testWalker
      case fault => fail(s"Invalid walkers listed event: $fault")

  def addSession: Unit =
    val saveSession = SaveSession(testAccount.license, testSession)
    dispatcher.dispatch(saveSession) match
      case SessionSaved(id) =>
        id should not be 0
        testSession = testSession.copy(id = id)
      case fault => fail(s"Invalid session saved event: $fault")

  def updateSession: Unit =
    testSession = testSession.copy(weight = 175)
    val saveSession = SaveSession(testAccount.license, testSession)
    dispatcher.dispatch(saveSession) match
      case SessionSaved(id) => id shouldBe testSession.id
      case fault => fail(s"Invalid session saved event: $fault")

  def listSessions: Unit =
    val listSessions = ListSessions(testAccount.license, testWalker.id)
    dispatcher.dispatch(listSessions) match
      case SessionsListed(sessions) =>
        sessions.length shouldBe 1
        sessions.head shouldBe testSession
      case fault => fail(s"Invalid sessions listed event: $fault")

  def fault: Unit =
    val fault = Fault("error message")
    store.addFault(fault)
    store.listFaults().length shouldBe 1