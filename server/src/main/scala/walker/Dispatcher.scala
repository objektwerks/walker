package walker

import ox.supervised
import ox.resilience.retry
import ox.scheduling.Schedule

import scala.concurrent.duration.*
import scala.util.Try
import scala.util.control.NonFatal

import Validator.*

final class Dispatcher(store: Store, emailer: Emailer):
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
        try
          supervised:
            retry( Schedule.fixedInterval(100.millis).maxAttempts(1) )(
              if store.isAuthorized(license.license) then Authorized
              else Unauthorized(s"Unauthorized: $command")
            )
        catch
          case NonFatal(error) => Unauthorized(s"Unauthorized: $command, cause: $error")
      case Register(_) | Login(_, _) => Authorized

  private def sendEmail(emailAddress: String, message: String): Boolean =
    val recipients = List(emailAddress)
    emailer.send(recipients, message)

  private def register(emailAddress: String): Event =
    try
      supervised:
        val account = Account(emailAddress = emailAddress)
        val message = s"Your new pin is: ${account.pin}\n\nWelcome aboard!"
        val result = retry( Schedule.fixedInterval(600.millis).maxAttempts(1) )( sendEmail(account.emailAddress, message) )
        if result then
          Registered( store.register(account) )
        else
          throw IllegalArgumentException("Invalid email address.")
    catch
      case NonFatal(error) => Fault(s"Registration failed for: $emailAddress, because: ${error.getMessage}")

  private def login(emailAddress: String, pin: String): Event =
    Try:
      supervised:
        retry( Schedule.fixedInterval(100.millis).maxAttempts(1) )( store.login(emailAddress, pin) )
    .fold(
      error => Fault("Login failed:", error),
      optionalAccount =>
        if optionalAccount.isDefined then LoggedIn(optionalAccount.get)
        else Fault(s"Login failed for email address: $emailAddress and pin: $pin")
    )

  private def deactivateAccount(license: String): Event =
    Try:
      supervised:
        retry( Schedule.fixedInterval(100.millis).maxAttempts(1) )( store.deactivateAccount(license) )
    .fold(
      error => Fault("Deactivate account failed:", error),
      optionalAccount =>
        if optionalAccount.isDefined then Deactivated(optionalAccount.get)
        else Fault(s"Deactivate account failed for license: $license")
    )

  private def reactivateAccount(license: String): Event =
    Try:
      supervised:
        retry( Schedule.fixedInterval(100.millis).maxAttempts(1) )( store.reactivateAccount(license) )
    .fold(
      error => Fault("Reactivate account failed:", error),
      optionalAccount =>
        if optionalAccount.isDefined then Reactivated(optionalAccount.get)
        else Fault(s"Reactivate account failed for license: $license")
    )

  private def listWalkers(accountId: Long): Event =
    try
      WalkersListed(
        supervised:
          retry( Schedule.fixedInterval(100.millis).maxAttempts(1) )(  store.listWalkers(accountId) )
      )
    catch
      case NonFatal(error) => Fault("List walkers failed:", error)

  private def saveWalker(walker: Walker): Event =
    try
      WalkerSaved(
        if walker.id == 0 then retry( Schedule.fixedInterval(100.millis).maxAttempts(1) )( store.addWalker(walker) )
        else retry( Schedule.fixedInterval(100.millis).maxAttempts(1) )( store.updateWalker(walker) )
      )
    catch
      case NonFatal(error) => Fault("Save walker failed:", error)

  private def listSessions(swimmerId: Long): Event =
    try
      SessionsListed(
        supervised:
          retry( Schedule.fixedInterval(100.millis).maxRepeats(1) )( store.listSessions(swimmerId) )
      )
    catch
      case NonFatal(error) => Fault("List sessions failed:", error)

  private def saveSession(session: Session): Event =
    try
      SessionSaved(
        if session.id == 0 then retry( Schedule.fixedInterval(100.millis).maxRepeats(1) )( store.addSession(session) )
        else retry( Schedule.fixedInterval(100.millis).maxRepeats(1) )( store.updateSession(session) )
      )
    catch
      case NonFatal(error) => Fault("Save session failed:", error)

  private def addFault(fault: Fault): Event =
    try
      supervised:
        retry( Schedule.fixedInterval(100.millis).maxRepeats(1) )( store.addFault(fault) )
        FaultAdded()
    catch
      case NonFatal(error) => Fault("Add fault failed:", error)