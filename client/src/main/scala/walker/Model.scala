package walker

import com.typesafe.scalalogging.LazyLogging

import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.ObjectProperty

import Fault.given

final class Model(fetcher: Fetcher) extends LazyLogging:
  val shouldBeInFxThread = (message: String) => require(Platform.isFxApplicationThread, message)
  val shouldNotBeInFxThread = (message: String) => require(!Platform.isFxApplicationThread, message)

  val registered = ObjectProperty[Boolean](true)
  val loggedin = ObjectProperty[Boolean](true)

  val selectedSwimmerId = ObjectProperty[Long](0)
  val selectedSessionId = ObjectProperty[Long](0)

  selectedSwimmerId.onChange { (_, oldSwimmerId, newSwimmerId) =>
    logger.info(s"*** selected swimmer id onchange event: $oldSwimmerId -> $newSwimmerId")
    shouldBeInFxThread("*** selected swimmer id onchange should be in fx thread.")
    sessions(newSwimmerId)
  }

  val objectAccount = ObjectProperty[Account](Account.empty)
  val observableSwimmers = ObservableBuffer[Swimmer]()
  val observableSessions = ObservableBuffer[Session]()
  val observableFaults = ObservableBuffer[Fault]()

  objectAccount.onChange { (_, oldAccount, newAccount) =>
    logger.info(s"*** object account onchange event: $oldAccount -> $newAccount")
  }

  observableSwimmers.onChange { (_, changes) =>
    logger.info(s"*** observable pools onchange event: $changes")
  }

  observableSessions.onChange { (_, changes) =>
    logger.info(s"*** observable cleanings onchange event: $changes")
  }

  def onFetchAsyncFault(source: String, fault: Fault): Unit =
    val cause = s"$source - $fault"
    logger.error(s"*** Cause: $cause")
    observableFaults += fault.copy(cause = cause)

  def onFetchAsyncFault(source: String, entity: Entity, fault: Fault): Unit =
    val cause = s"$source - $entity - $fault"
    logger.error(s"*** Cause: $cause")
    observableFaults += fault.copy(cause = cause)

  def add(fault: Fault): Unit =
    fetcher.fetchAsync(
      AddFault(objectAccount.get.license, fault),
      (event: Event) => event match
        case fault @ Fault(cause, _) => onFetchAsyncFault("Model.add fault", fault)
        case FaultAdded() =>
          observableFaults += fault
          observableFaults.sort()
        case _ => ()
    )

  def register(register: Register): Unit =
    fetcher.fetchAsync(
      register,
      (event: Event) => event match
        case fault @ Fault(_, _) => registered.set(false)
        case Registered(account) => objectAccount.set(account)
        case _ => ()
    )

  def login(login: Login): Unit =
    fetcher.fetchAsync(
      login,
      (event: Event) => event match
        case fault @ Fault(_, _) => loggedin.set(false)
        case LoggedIn(account) =>
          objectAccount.set(account)
          swimmers()
        case _ => ()
    )

  def deactivate(deactivate: Deactivate): Unit =
    fetcher.fetchAsync(
      deactivate,
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchAsyncFault("Model.deactivate", fault)
        case Deactivated(account) => objectAccount.set(account)
        case _ => ()
    )

  def reactivate(reactivate: Reactivate): Unit =
    fetcher.fetchAsync(
      reactivate,
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchAsyncFault("Model.reactivate", fault)
        case Reactivated(account) => objectAccount.set(account)
        case _ => ()
    )

  def swimmers(): Unit =
    fetcher.fetchAsync(
      ListSwimmers(objectAccount.get.license, objectAccount.get.id),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchAsyncFault("Model.swimmers", fault)
        case SwimmersListed(swimmers) =>
          observableSwimmers.clear()
          observableSwimmers ++= swimmers
        case _ => ()
    )

  def add(selectedIndex: Int, swimmer: Swimmer)(runLast: => Unit): Unit =
    fetcher.fetchAsync(
      SaveSwimmer(objectAccount.get.license, swimmer),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchAsyncFault("Model.save swimmer", swimmer, fault)
        case SwimmerSaved(id) =>
          observableSwimmers += swimmer.copy(id = id)
          observableSwimmers.sort()
          selectedSwimmerId.set(id)
          runLast
        case _ => ()
    )

  def update(selectedIndex: Int, swimmer: Swimmer)(runLast: => Unit): Unit =
    fetcher.fetchAsync(
      SaveSwimmer(objectAccount.get.license, swimmer),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchAsyncFault("Model.save swimmer", swimmer, fault)
        case SwimmerSaved(id) =>
          observableSwimmers.update(selectedIndex, swimmer)
          runLast
        case _ => ()
    )

  def sessions(swimmerId: Long): Unit =
    fetcher.fetchAsync(
      ListSessions(objectAccount.get.license, swimmerId),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchAsyncFault("Model.sessions", fault)
        case SessionsListed(sessions) =>
          observableSessions.clear()
          observableSessions ++= sessions
        case _ => ()
    )

  def add(selectedIndex: Int, session: Session)(runLast: => Unit): Unit =
    fetcher.fetchAsync(
      SaveSession(objectAccount.get.license, session),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchAsyncFault("Model.save session", session, fault)
        case SessionSaved(id) =>
          observableSessions += session.copy(id = id)
          observableSessions.sort()
          selectedSessionId.set(id)
          runLast
        case _ => ()
    )

  def update(selectedIndex: Int, session: Session)(runLast: => Unit): Unit =
    fetcher.fetchAsync(
      SaveSession(objectAccount.get.license, session),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchAsyncFault("Model.save session", session, fault)
        case SessionSaved(id) =>
          observableSessions.update(selectedIndex, session)
          runLast
        case _ => ()
    )