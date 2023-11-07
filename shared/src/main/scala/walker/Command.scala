package walker

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
  given JsonValueCodec[ListWalkers] = JsonCodecMaker.make[ListWalkers]
  given JsonValueCodec[SaveWalker] = JsonCodecMaker.make[SaveWalker]
  given JsonValueCodec[ListSessions] = JsonCodecMaker.make[ListSessions]
  given JsonValueCodec[SaveSession] = JsonCodecMaker.make[SaveSession]

sealed trait License:
  val license: String

final case class Register(emailAddress: String) extends Command
final case class Login(emailAddress: String, pin: String) extends Command

final case class Deactivate(license: String) extends Command with License
final case class Reactivate(license: String) extends Command with License

final case class ListWalkers(license: String, accountId: Long) extends Command with License
final case class SaveWalker(license: String, walker: Walker) extends Command with License

final case class ListSessions(license: String, walkerId: Long) extends Command with License
final case class SaveSession(license: String, session: Session) extends Command with License

final case class AddFault(license: String, fault: Fault) extends Command with License