package walker

import scala.util.Try
import scala.util.control.NonFatal

import Validator.*

sealed trait Security
case object Authorized extends Security
final case class Unauthorized(cause: String) extends Security

final class Dispatcher(store: Store,
                       emailer: Emailer):
  def dispatch(command: Command): Event =
    command.isValid match
      case false => addFault( Fault(s"Invalid command: $command") )
      case true =>
        isAuthorized(command) match
          case Unauthorized(cause) => addFault( Fault(cause) )
          case Authorized =>
            command match
              case Register(emailAddress)     => register(emailAddress)
              case Login(emailAddress, pin)   => login(emailAddress, pin)
              case Deactivate(license)        => deactivateAccount(license)
              case Reactivate(license)        => reactivateAccount(license)
              case ListWalkers(_, accountId)  => listWalkers(accountId)
              case SaveWalker(_, walker)      => saveWalker(walker)
              case ListSessions(_, walkerId)  => listSessions(walkerId)
              case SaveSession(_, session)    => saveSession(session)
              case AddFault(_, fault)         => addFault(fault)

  private def isAuthorized(command: Command): Security =
    command match
      case license: License =>
        Try {
          if store.isAuthorized(license.license) then Authorized
          else Unauthorized(s"Unauthorized: $command")
        }.recover {
          case NonFatal(error) => Unauthorized(s"Unauthorized: $command, cause: $error")
        }.get
      case Register(_) | Login(_, _) => Authorized

  private def register(emailAddress: String): Event =
    Try {
      val account = Account(emailAddress = emailAddress)
      if store.isEmailAddressUnique(emailAddress) then
        email(account.emailAddress, account.pin)
        Registered( store.register(account) )
      else Fault(s"Registration failed because: $emailAddress is already registered.")
    }.recover { case NonFatal(error) => Fault(s"Registration failed for: $emailAddress, because: ${error.getMessage}") }
     .get

  private def email(emailAddress: String, pin: String): Unit =
    val recipients = List(emailAddress)
    val message = s"Your new pin is: $pin\n\nWelcome aboard!"
    emailer.send(recipients, message)

  private def login(emailAddress: String, pin: String): Event =
    Try { store.login(emailAddress, pin) }.fold(
      error => Fault("Login failed:", error),
      optionalAccount =>
        if optionalAccount.isDefined then LoggedIn(optionalAccount.get)
        else Fault(s"Login failed for email address: $emailAddress and pin: $pin")
    )

  private def deactivateAccount(license: String): Event =
    Try { store.deactivateAccount(license) }.fold(
      error => Fault("Deactivate account failed:", error),
      optionalAccount =>
        if optionalAccount.isDefined then Deactivated(optionalAccount.get)
        else Fault(s"Deactivate account failed for license: $license")
    )

  private def reactivateAccount(license: String): Event =
    Try { store.reactivateAccount(license) }.fold(
      error => Fault("Reactivate account failed:", error),
      optionalAccount =>
        if optionalAccount.isDefined then Reactivated(optionalAccount.get)
        else Fault(s"Reactivate account failed for license: $license")
    )

  private def listWalkers(accountId: Long): Event =
    Try {
      WalkersListed(store.listWalkers(accountId))
    }.recover { case NonFatal(error) => Fault("List walkers failed:", error) }
     .get

  private def saveWalker(walker: Walker): Event =
    Try {
      WalkerSaved(
        if walker.id == 0 then store.addWalker(walker)
        else store.updateWalker(walker)
      )
    }.recover { case NonFatal(error) => Fault("Save walker failed:", error) }
     .get

  private def listSessions(walkerId: Long): Event =
    Try {
      SessionsListed( store.listSessions(walkerId) )
    }.recover { case NonFatal(error) => Fault("List sessions failed:", error) }
     .get

  private def saveSession(session: Session): Event =
    Try {
      SessionSaved(
        if session.id == 0 then store.addSession(session)
        else store.updateSession(session)
      )
    }.recover { case NonFatal(error) => Fault("Save cleaning failed:", error) }
     .get

  private def addFault(fault: Fault): Event =
    Try {
      store.addFault(fault)
      FaultAdded()
    }.recover { case NonFatal(error) => Fault("Add fault failed:", error) }
     .get