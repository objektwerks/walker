package walker

import scala.util.Try
import scala.util.control.NonFatal

import Validator.*

final class Dispatcher(store: Store, emailer: Emailer):
  def dispatch[E <: Event](command: Command): Event =
    if !command.isValid then store.addFault( Fault(s"Command is invalid: $command") )
    
    isAuthorized(command) match
      case Authorized(isAuthorized) => if !isAuthorized then store.addFault( Fault(s"License is unauthorized: $command") )
      case fault @ Fault(_, _) => store.addFault(fault)
      case _ =>
        
    val event = command match
      case Register(emailAddress)     => register(emailAddress)
      case Login(emailAddress, pin)   => login(emailAddress, pin)
      case Deactivate(license)        => deactivateAccount(license)
      case Reactivate(license)        => reactivateAccount(license)
      case ListSwimmers(_, accountId) => listSwimmers(accountId)
      case SaveSwimmer(_, swimmer)    => saveSwimmer(swimmer)
      case ListSessions(_, swimmerId) => listSessions(swimmerId)
      case SaveSession(_, session)    => saveSession(session)
      case AddFault(_, fault)         => addFault(fault)

    event match
      case fault @ Fault(_, _) => store.addFault(fault)
      case _ => event

  private def isAuthorized(command: Command): Event =
    command match
      case license: License =>
        Try {
          Authorized( store.isAuthorized(license.license) )
        }.recover { case NonFatal(error) => Fault(s"Authorization failed: $error") }
         .get
      case Register(_) | Login(_, _) => Authorized(true)

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
    val message = s"<p>Save this pin: <b>${pin}</b> in a safe place; then delete this email.</p>"
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

  private def listSwimmers(accountId: Long): Event =
    Try {
      SwimmersListed(store.listSwimmers(accountId))
    }.recover { case NonFatal(error) => Fault("List swimmers failed:", error) }
     .get

  private def saveSwimmer(swimmer: Swimmer): Event =
    Try {
      SwimmerSaved(
        if swimmer.id == 0 then store.addSwimmer(swimmer)
        else store.updateSwimmer(swimmer)
      )
    }.recover { case NonFatal(error) => Fault("Save swimmer failed:", error) }
     .get

  private def listSessions(swimmerId: Long): Event =
    Try {
      SessionsListed( store.listSessions(swimmerId) )
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