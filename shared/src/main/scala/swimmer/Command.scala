package swimmer

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

sealed trait Command

object Command:
  given JsonValueCodec[Command] = JsonCodecMaker.make[Command]
  given JsonValueCodec[License] = JsonCodecMaker.make[License]
  given JsonValueCodec[Register] = JsonCodecMaker.make[Register]
  given JsonValueCodec[Login] = JsonCodecMaker.make[Login]
  given JsonValueCodec[Deactivate] = JsonCodecMaker.make[Deactivate]
  given JsonValueCodec[Reactivate] = JsonCodecMaker.make[Reactivate]
  given JsonValueCodec[ListSwimmers] = JsonCodecMaker.make[ListSwimmers]
  given JsonValueCodec[SaveSwimmer] = JsonCodecMaker.make[SaveSwimmer]
  given JsonValueCodec[ListSessions] = JsonCodecMaker.make[ListSessions]
  given JsonValueCodec[SaveSession] = JsonCodecMaker.make[SaveSession]

sealed trait License:
  val license: String

final case class Register(emailAddress: String) extends Command
final case class Login(emailAddress: String, pin: String) extends Command

final case class Deactivate(license: String) extends Command with License
final case class Reactivate(license: String) extends Command with License

final case class ListSwimmers(license: String, accountId: Long) extends Command with License
final case class SaveSwimmer(license: String, swimmer: Swimmer) extends Command with License

final case class ListSessions(license: String, swimmerId: Long) extends Command with License
final case class SaveSession(license: String, session: Session) extends Command with License

final case class AddFault(license: String, fault: Fault) extends Command with License