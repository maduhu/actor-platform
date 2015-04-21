package im.actor.server

import im.actor.server.api.frontend.{ Tcp, Ws }
import akka.actor._
import akka.stream.ActorFlowMaterializer
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.api.rpc.service.auth.AuthServiceImpl
import im.actor.server.api.rpc.service.contacts.ContactsServiceImpl
import im.actor.server.api.rpc.service.groups.GroupsServiceImpl
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl
import im.actor.server.api.rpc.service.sequence.SequenceServiceImpl
import im.actor.server.api.rpc.service.users.UsersServiceImpl
import im.actor.server.api.rpc.service.weak.WeakServiceImpl
import im.actor.server.db.{ DbInit, FlywayInit }
import im.actor.server.presences.PresenceManager
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }
import im.actor.server.session.Session

class ApiKernel extends Bootable with DbInit with FlywayInit {
  val config = ConfigFactory.load()
  val serverConfig = config.getConfig("actor-server")
  val sqlConfig = serverConfig.getConfig("persist.sql")

  implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()

  val ds = initDs(sqlConfig)
  implicit val db = initDb(ds)

  def startup() = {
    val flyway = initFlyway(ds.ds)
    flyway.migrate()

    implicit val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    implicit val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    implicit val presenceManagerRegion = PresenceManager.startRegion()

    val rpcApiService = system.actorOf(RpcApiService.props())
    implicit val sessionRegion = Session.startRegion(
      Some(Session.props(rpcApiService)))

    val services = Seq(
      new AuthServiceImpl,
      new ContactsServiceImpl,
      new MessagingServiceImpl,
      new GroupsServiceImpl,
      new SequenceServiceImpl,
      new WeakServiceImpl,
      new UsersServiceImpl)

    services foreach (rpcApiService ! RpcApiService.AttachService(_))

    Tcp.start(serverConfig, sessionRegion)
    //Ws.start(serverConfig)
  }

  def shutdown() = {
    system.shutdown()
    ds.close()
  }
}
