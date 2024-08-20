package organisation

import cats.*
import na.core.*

import io.circe.*
import io.circe.generic.semiauto.*

import na.*

case class Organisation(pid: Option[PID], name: String)

object Organisation:

  given organisationEncoder: Encoder[Organisation] =
    deriveEncoder[Organisation]

  given organisationDecoder: Decoder[Organisation] =
    deriveDecoder[Organisation]

  given organisationEntity[F[_]](using F: Monad[F]): HasPID[F, Organisation] =
    new HasPID[F, Organisation]:

      def id(organisation: Organisation): F[Option[PID]] =
        F.pure(organisation.pid)

      def withId(organisation: Organisation)(id: PID): F[Organisation] =
        F.pure(organisation.copy(pid = Some(id)))
