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
        case listWalkers @ ListWalkers(_, _)   => listWalkers.isValid
        case saveSwimmer @ SaveWalker(_, _)    => saveSwimmer.isValid
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

  extension (listWalkers: ListWalkers)
    def isValid: Boolean = listWalkers.license.isLicense && listWalkers.accountId > 0

  extension (saveWalker: SaveWalker)
    def isValid: Boolean = saveWalker.license.isLicense && saveWalker.walker.isValid

  extension (listSessions: ListSessions)
    def isValid: Boolean = listSessions.license.isLicense && listSessions.walkerId > 0

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

  extension (walker: Walker)
    def isValid =
      walker.id >= 0 &&
      walker.accountId > 0 &&
      walker.name.length >= 2

  extension (session: Session)
    def isValid: Boolean =
      session.id >= 0 &&
      session.walkerId > 0 &&
      session.weight > 0 &&
      session.weightUnit.length == 2 &&
      session.distance > 0 &&
      session.distanceUnit.length == 2 &&
      session.hours >= 0 &&
      session.minutes > 0 &&
      session.calories >= 0 &&
      session.datetime > 0