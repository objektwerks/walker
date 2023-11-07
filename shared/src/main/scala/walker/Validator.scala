package walker

object Validator:
  extension (value: String)
    def isLicense: Boolean = if value.nonEmpty && value.length == 36 then true else false
    def isPin: Boolean = value.length == 7
    def isEmailAddress: Boolean = value.nonEmpty && value.length >= 3 && value.contains("@")
    def isInt(text: String): Boolean = text.matches("\\d+")
    def isDouble(text: String): Boolean = text.matches("\\d{0,7}([\\.]\\d{0,4})?")

  extension (command: Command)
    def isValid: Boolean =
      command match
        case register @ Register(emailAddress) => register.isValid
        case login @ Login(_, _)               => login.isValid
        case deactivate @ Deactivate(_)        => deactivate.isValid
        case reactivate @ Reactivate(_)        => reactivate.isValid
        case listSwimmers @ ListSwimmers(_, _) => listSwimmers.isValid
        case saveSwimmer @ SaveSwimmer(_, _)   => saveSwimmer.isValid
        case listSessions @ ListSessions(_, _) => listSessions.isValid
        case saveSession @ SaveSession(_, _)   => saveSession.isValid
        case addFault @ AddFault(_, _)         => addFault.isValid

  extension (addFault: AddFault)
    def isValid: Boolean = addFault.license.isLicense && addFault.fault.cause.nonEmpty

  extension (register: Register)
    def isValid: Boolean = register.emailAddress.isEmailAddress

  extension (login: Login)
    def isValid: Boolean = login.emailAddress.isEmailAddress && login.pin.isPin

  extension (deactivate: Deactivate)
    def isValid: Boolean = deactivate.license.isLicense

  extension (reactivate: Reactivate)
    def isValid: Boolean = reactivate.license.isLicense

  extension (listSwimmers: ListSwimmers)
    def isValid: Boolean = listSwimmers.license.isLicense && listSwimmers.accountId > 0

  extension (saveSwimmer: SaveSwimmer)
    def isValid: Boolean = saveSwimmer.license.isLicense && saveSwimmer.swimmer.isValid

  extension (listSessions: ListSessions)
    def isValid: Boolean = listSessions.license.isLicense && listSessions.swimmerId > 0

  extension (saveSession: SaveSession)
    def isValid: Boolean = saveSession.license.isLicense && saveSession.session.isValid

  extension  (license: License)
    def isLicense: Boolean = license.license.isLicense

  extension (account: Account)
    def isActivated: Boolean =
      account.id >= 0 &&
      account.license.isLicense &&
      account.emailAddress.isEmailAddress &&
      account.pin.isPin &&
      account.activated > 0 &&
      account.deactivated == 0
    def isDeactivated: Boolean =
      account.id > 0 &&
      account.license.isLicense &&
      account.emailAddress.isEmailAddress &&
      account.pin.isPin &&
      account.activated == 0 &&
      account.deactivated > 0

  extension (swimmer: Swimmer)
    def isValid =
      swimmer.id >= 0 &&
      swimmer.accountId > 0 &&
      swimmer.name.length >= 2

  extension (session: Session)
    def isValid: Boolean =
      session.id >= 0 &&
      session.swimmerId > 0 &&
      session.weight > 0 &&
      session.weightUnit.length == 2 &&
      session.laps > 0 &&
      session.lapDistance > 0 &&
      session.lapUnit.length >= 4 &&
      session.style.nonEmpty &&
      session.minutes > 0 &&
      session.seconds >= 0 &&
      session.calories >= 0 &&
      session.datetime > 0