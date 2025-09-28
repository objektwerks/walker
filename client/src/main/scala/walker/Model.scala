package walker

import com.typesafe.scalalogging.LazyLogging

import ox.supervised

import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.ObjectProperty

import Fault.given

final class Model(fetcher: Fetcher) extends LazyLogging:
  def assertInFxThread(message: String, suffix: String = " should be in fx thread!"): Unit =
    require(Platform.isFxApplicationThread, message + suffix)
  def assertNotInFxThread(message: String, suffix: String = " should not be in fx thread!"): Unit =
    require(!Platform.isFxApplicationThread, message + suffix)

  val registered = ObjectProperty[Boolean](true)
  val loggedin = ObjectProperty[Boolean](true)

  val selectedWalkerId = ObjectProperty[Long](0)
  val selectedSessionId = ObjectProperty[Long](0)

  selectedWalkerId.onChange { (_, _, newWalkerId) =>
    sessions(newWalkerId)
  }

  val objectAccount = ObjectProperty[Account](Account.empty)
  val observableWalkers = ObservableBuffer[Walker]()
  val observableSessions = ObservableBuffer[Session]()
  val observableFaults = ObservableBuffer[Fault]()

  def onFetchFault(source: String, fault: Fault): Unit =
    val cause = s"$source - $fault"
    logger.error("*** cause: {}", cause)
    observableFaults += fault.copy(cause = cause)

  def onFetchFault(source: String, entity: Entity, fault: Fault): Unit =
    val cause = s"$source - $entity - $fault"
    logger.error("*** cause: {}", cause)
    observableFaults += fault.copy(cause = cause)

  def add(fault: Fault): Unit =
    supervised:
      assertNotInFxThread(s"add fault: $fault")
      fetcher.fetch(
        AddFault(objectAccount.get.license, fault),
        (event: Event) => event match
          case fault @ Fault(cause, _) => onFetchFault("add fault", fault)
          case FaultAdded() =>
            observableFaults += fault
            observableFaults.sort()
          case _ => ()
      )

  def register(register: Register): Unit =
    supervised:
      assertNotInFxThread(s"register: $register")
      fetcher.fetch(
        register,
        (event: Event) => event match
          case _ @ Fault(_, _) => registered.set(false)
          case Registered(account) => objectAccount.set(account)
          case _ => ()
      )

  def login(login: Login): Unit =
    supervised:
      assertNotInFxThread(s"login: $login")
      fetcher.fetch(
        login,
        (event: Event) => event match
          case _ @ Fault(_, _) => loggedin.set(false)
          case LoggedIn(account) =>
            objectAccount.set(account)
            walkers()
          case _ => ()
      )

  def deactivate(deactivate: Deactivate): Unit =
    supervised:
      assertNotInFxThread(s"deactivate: $deactivate")
      fetcher.fetch(
        deactivate,
        (event: Event) => event match
          case fault @ Fault(_, _) => onFetchFault("deactivate", fault)
          case Deactivated(account) => objectAccount.set(account)
          case _ => ()
      )

  def reactivate(reactivate: Reactivate): Unit =
    supervised:
      assertNotInFxThread(s"reactivate: $reactivate")
      fetcher.fetch(
        reactivate,
        (event: Event) => event match
          case fault @ Fault(_, _) => onFetchFault("reactivate", fault)
          case Reactivated(account) => objectAccount.set(account)
          case _ => ()
      )

  def walkers(): Unit =
    supervised:
      assertNotInFxThread("list walkers")
      fetcher.fetch(
        ListWalkers(objectAccount.get.license, objectAccount.get.id),
        (event: Event) => event match
          case fault @ Fault(_, _) => onFetchFault("walkers", fault)
          case WalkersListed(walkers) =>
            observableWalkers.clear()
            observableWalkers ++= walkers
          case _ => ()
      )

  def add(walker: Walker)(runLast: => Unit): Unit =
    supervised:
      assertNotInFxThread(s"add walker: $walker")
      fetcher.fetch(
        SaveWalker(objectAccount.get.license, walker),
        (event: Event) => event match
          case fault @ Fault(_, _) => onFetchFault("add walker", walker, fault)
          case WalkerSaved(id) =>
            observableWalkers.insert(0, walker.copy(id = id))
            observableWalkers.sort()
            selectedWalkerId.set(id)
            runLast
          case _ => ()
      )

  def update(selectedIndex: Int, walker: Walker)(runLast: => Unit): Unit =
    supervised:
      assertNotInFxThread(s"update walker from: $selectedIndex to: $walker")
      fetcher.fetch(
        SaveWalker(objectAccount.get.license, walker),
        (event: Event) => event match
          case fault @ Fault(_, _) => onFetchFault("update walker", walker, fault)
          case WalkerSaved(id) =>
            if selectedIndex > -1 then
              observableWalkers.update(selectedIndex, walker)      
              logger.info(s"Updated walker from: $selectedIndex to: $walker")
              runLast
            else
              logger.error(s"Update of walker from: $selectedIndex to: $walker failed due to invalid index: $selectedIndex")
          case _ => ()
      )

  def sessions(walkerId: Long): Unit =
    supervised:
      assertNotInFxThread(s"list sessions, walker id: $walkerId")
      fetcher.fetch(
        ListSessions(objectAccount.get.license, walkerId),
        (event: Event) => event match
          case fault @ Fault(_, _) => onFetchFault("sessions", fault)
          case SessionsListed(sessions) =>
            observableSessions.clear()
            observableSessions ++= sessions
          case _ => ()
      )

  def add(session: Session)(runLast: => Unit): Unit =
    supervised:
      assertNotInFxThread(s"add session: $session")
      fetcher.fetch(
        SaveSession(objectAccount.get.license, session),
        (event: Event) => event match
          case fault @ Fault(_, _) => onFetchFault("add session", session, fault)
          case SessionSaved(id) =>
            observableSessions.insert(0, session.copy(id = id))
            observableSessions.sort()
            selectedSessionId.set(id)
            logger.info(s"Added session: $session")
            runLast
          case _ => ()
      )

  def update(selectedIndex: Int, session: Session)(runLast: => Unit): Unit =
    supervised:
      assertNotInFxThread(s"update session from: $selectedIndex to: $session")
      fetcher.fetch(
        SaveSession(objectAccount.get.license, session),
        (event: Event) => event match
          case fault @ Fault(_, _) => onFetchFault("update session", session, fault)
          case SessionSaved(id) =>
            if selectedIndex > -1 then
              observableSessions.update(selectedIndex, session)      
              logger.info(s"Updated session from: $selectedIndex to: $session")
              runLast
            else
              logger.error(s"Update of session from: $selectedIndex to: $session failed due to invalid index: $selectedIndex")
          case _ => ()
      )