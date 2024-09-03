package walker

import ox.{IO, supervised}
import ox.resilience.{retry, RetryConfig}

import scala.concurrent.duration.*
import scala.util.Try
import scala.util.control.NonFatal

import Validator.*

final class Dispatcher(store: Store, emailer: Emailer):
  def dispatch(command: Command): Event =
    IO.unsafe:
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

  private def isAuthorized(command: Command)(using IO): Security =
    command match
      case license: License =>
        try
          supervised:
            retry( RetryConfig.delay(1, 100.millis) )(
              if store.isAuthorized(license.license) then Authorized
              else Unauthorized(s"Unauthorized: $command")
            )
        catch
          case NonFatal(error) => Unauthorized(s"Unauthorized: $command, cause: $error")
      case Register(_) | Login(_, _) => Authorized

  private def sendEmail(emailAddress: String, message: String): Unit =
    val recipients = List(emailAddress)
    emailer.send(recipients, message)

  private def register(emailAddress: String)(using IO): Event =
    try
      supervised:
        val account = Account(emailAddress = emailAddress)
        val message = s"Your new pin is: ${account.pin}\n\nWelcome aboard!"
        retry( RetryConfig.delay(1, 600.millis) )( sendEmail(account.emailAddress, message) )
        Registered( store.register(account) )
    catch
      case NonFatal(error) => Fault(s"Registration failed for: $emailAddress, because: ${error.getMessage}")

  private def login(emailAddress: String, pin: String)(using IO): Event =
    Try:
      supervised:
        retry( RetryConfig.delay(1, 100.millis) )( store.login(emailAddress, pin) )
    .fold(
      error => Fault("Login failed:", error),
      optionalAccount =>
        if optionalAccount.isDefined then LoggedIn(optionalAccount.get)
        else Fault(s"Login failed for email address: $emailAddress and pin: $pin")
    )

  private def deactivateAccount(license: String)(using IO): Event =
    Try:
      supervised:
        retry( RetryConfig.delay(1, 100.millis) )( store.deactivateAccount(license) )
    .fold(
      error => Fault("Deactivate account failed:", error),
      optionalAccount =>
        if optionalAccount.isDefined then Deactivated(optionalAccount.get)
        else Fault(s"Deactivate account failed for license: $license")
    )

  private def reactivateAccount(license: String)(using IO): Event =
    Try:
      supervised:
        retry( RetryConfig.delay(1, 100.millis) )( store.reactivateAccount(license) )
    .fold(
      error => Fault("Reactivate account failed:", error),
      optionalAccount =>
        if optionalAccount.isDefined then Reactivated(optionalAccount.get)
        else Fault(s"Reactivate account failed for license: $license")
    )

  private def listWalkers(accountId: Long)(using IO): Event =
    try
      WalkersListed(
        supervised:
          retry( RetryConfig.delay(1, 100.millis) )(  store.listWalkers(accountId) )
      )
    catch
      case NonFatal(error) => Fault("List walkers failed:", error)

  private def saveWalker(walker: Walker)(using IO): Event =
    Try:
      WalkerSaved(
        if walker.id == 0 then retry( RetryConfig.delay(1, 100.millis) )( store.addWalker(walker) )
        else retry( RetryConfig.delay(1, 100.millis) )( store.updateWalker(walker) )
      )
    .recover:
      case NonFatal(error) => Fault("Save walker failed:", error)
    .get

  private def listSessions(swimmerId: Long)(using IO): Event =
    Try:
      SessionsListed(
        supervised:
          retry( RetryConfig.delay(1, 100.millis) )( store.listSessions(swimmerId) )
      )
    .recover:
      case NonFatal(error) => Fault("List sessions failed:", error)
    .get

  private def saveSession(session: Session)(using IO): Event =
    Try:
      SessionSaved(
        if session.id == 0 then retry( RetryConfig.delay(1, 100.millis) )( store.addSession(session) )
        else retry( RetryConfig.delay(1, 100.millis) )( store.updateSession(session) )
      )
    .recover:
      case NonFatal(error) => Fault("Save session failed:", error)
    .get

  private def addFault(fault: Fault)(using IO): Event =
    Try:
      supervised:
        retry( RetryConfig.delay(1, 100.millis) )( store.addFault(fault) )
        FaultAdded()
    .recover:
      case NonFatal(error) => Fault("Add fault failed:", error)
    .get