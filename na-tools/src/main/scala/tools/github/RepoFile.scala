package na
package tools
package github

import cats.effect.*
import cats.syntax.all.*
import github4s.algebras.GithubAPIs
import github4s.domain.Commit

import java.time.*

case class RepoFile(organisation: String, repository: String, path: String)(github: GithubAPIs[IO]):

  private def commits: IO[List[Commit]] =
    github
      .repos
      .listCommits(organisation, repository, None, Some(path))
      .flatMap(response => IO.fromEither(response.result))

  private def decodeBase64(base64String: String): String =
    import java.nio.charset.StandardCharsets.UTF_8
    import java.util.Base64
    String(Base64.getMimeDecoder.decode(base64String.trim), UTF_8)

  private def loadFileContents(commitSHA: String): IO[Option[String]] =
    github
      .repos
      .getContents(organisation, repository, path, Some(commitSHA))
      .flatMap(response => IO.fromEither(response.result))
      .map(_.head.content)

  private def fileSHA: IO[String] =
    github
      .repos
      .getContents(organisation, repository, path)
      .flatMap(response => IO.fromEither(response.result))
      .map(x => x.head.sha)

  /** returns the file contents history by commit date */
  def history: IO[Map[LocalDateTime, String]] =
    commits
      .flatMap: commits =>
        commits
          .map: commit =>
            loadFileContents(commit.sha)
              .map: content =>
                ZonedDateTime.parse(commit.date).toLocalDateTime -> content.map(decodeBase64)
          .parSequence
          .map: result =>
            result
              .filterNot((d,a) => a.isEmpty)
              .map((d,a) => d -> a.get)
              .toMap

  /** returns the latest contents */
  def contents: IO[String] =
    for {
      sha <- fileSHA
      str <- github
        .gitData
        .getBlob(organisation, repository, sha)
        .flatMap(response => IO.fromEither(response.result))
        .flatMap(contents => IO.fromOption(contents.content)(sys.error(s"no contents: $organisation/$repository/$path")))
    } yield decodeBase64(str)