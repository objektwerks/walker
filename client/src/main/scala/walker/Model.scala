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

  val selectedWalkerId = ObjectProperty[Long](0)
  val selectedSessionId = ObjectProperty[Long](0)

  selectedWalkerId.onChange { (_, oldSwimmerId, newSwimmerId) =>
    logger.info(s"*** selected swimmer id onchange event: $oldSwimmerId -> $newSwimmerId")
    shouldBeInFxThread("*** selected swimmer id onchange should be in fx thread.")
    sessions(newSwimmerId)
  }

  val objectAccount = ObjectProperty[Account](Account.empty)
  val observableWalkers = ObservableBuffer[Walker]()
  val observableSessions = ObservableBuffer[Session]()
  val observableFaults = ObservableBuffer[Fault]()

  objectAccount.onChange { (_, oldAccount, newAccount) =>
    logger.info(s"*** object account onchange event: $oldAccount -> $newAccount")
  }

  observableWalkers.onChange { (_, changes) =>
    logger.info(s"*** observable pools onchange event: $changes")
  }

  observableSessions.onChange { (_, changes) =>
    logger.info(s"*** observable cleanings onchange event: $changes")
  }

  def onFetchFault(source: String, fault: Fault): Unit =
    val cause = s"$source - $fault"
    logger.error(s"*** Cause: $cause")
    observableFaults += fault.copy(cause = cause)

  def onFetchFault(source: String, entity: Entity, fault: Fault): Unit =
    val cause = s"$source - $entity - $fault"
    logger.error(s"*** Cause: $cause")
    observableFaults += fault.copy(cause = cause)

  def add(fault: Fault): Unit =
    fetcher.fetch(
      AddFault(objectAccount.get.license, fault),
      (event: Event) => event match
        case fault @ Fault(cause, _) => onFetchFault("Model.add fault", fault)
        case FaultAdded() =>
          observableFaults += fault
          observableFaults.sort()
        case _ => ()
    )

  def register(register: Register): Unit =
    fetcher.fetch(
      register,
      (event: Event) => event match
        case fault @ Fault(_, _) => registered.set(false)
        case Registered(account) => objectAccount.set(account)
        case _ => ()
    )

  def login(login: Login): Unit =
    fetcher.fetch(
      login,
      (event: Event) => event match
        case fault @ Fault(_, _) => loggedin.set(false)
        case LoggedIn(account) =>
          objectAccount.set(account)
          swimmers()
        case _ => ()
    )

  def deactivate(deactivate: Deactivate): Unit =
    fetcher.fetch(
      deactivate,
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchFault("Model.deactivate", fault)
        case Deactivated(account) => objectAccount.set(account)
        case _ => ()
    )

  def reactivate(reactivate: Reactivate): Unit =
    fetcher.fetch(
      reactivate,
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchFault("Model.reactivate", fault)
        case Reactivated(account) => objectAccount.set(account)
        case _ => ()
    )

  def swimmers(): Unit =
    fetcher.fetch(
      ListWalkers(objectAccount.get.license, objectAccount.get.id),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchFault("Model.swimmers", fault)
        case WalkersListed(swimmers) =>
          observableWalkers.clear()
          observableWalkers ++= swimmers
        case _ => ()
    )

  def add(selectedIndex: Int, walker: Walker)(runLast: => Unit): Unit =
    fetcher.fetch(
      SaveWalker(objectAccount.get.license, walker),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchFault("Model.save swimmer", walker, fault)
        case WalkerSaved(id) =>
          observableWalkers += walker.copy(id = id)
          observableWalkers.sort()
          selectedWalkerId.set(id)
          runLast
        case _ => ()
    )

  def update(selectedIndex: Int, walker: Walker)(runLast: => Unit): Unit =
    fetcher.fetch(
      SaveWalker(objectAccount.get.license, walker),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchFault("Model.save swimmer", walker, fault)
        case WalkerSaved(id) =>
          observableWalkers.update(selectedIndex, walker)
          runLast
        case _ => ()
    )

  def sessions(swimmerId: Long): Unit =
    fetcher.fetch(
      ListSessions(objectAccount.get.license, swimmerId),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchFault("Model.sessions", fault)
        case SessionsListed(sessions) =>
          observableSessions.clear()
          observableSessions ++= sessions
        case _ => ()
    )

  def add(selectedIndex: Int, session: Session)(runLast: => Unit): Unit =
    fetcher.fetch(
      SaveSession(objectAccount.get.license, session),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchFault("Model.save session", session, fault)
        case SessionSaved(id) =>
          observableSessions += session.copy(id = id)
          observableSessions.sort()
          selectedSessionId.set(id)
          runLast
        case _ => ()
    )

  def update(selectedIndex: Int, session: Session)(runLast: => Unit): Unit =
    fetcher.fetch(
      SaveSession(objectAccount.get.license, session),
      (event: Event) => event match
        case fault @ Fault(_, _) => onFetchFault("Model.save session", session, fault)
        case SessionSaved(id) =>
          observableSessions.update(selectedIndex, session)
          runLast
        case _ => ()
    )