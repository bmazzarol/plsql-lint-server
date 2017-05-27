package bmazzarol.plsql

import org.eclipse.xtext.diagnostics.Severity
import org.eclipse.xtext.validation.Issue

/**
  * ASTs.
  */
object AST {

  /**
    * Message severity.
    */
  sealed trait Severity

  case object error extends Severity

  case object warning extends Severity

  case object info extends Severity

  def severity(issue: Issue): Severity = issue.getSeverity match {
    case Severity.ERROR   => error
    case Severity.WARNING => warning
    case _                => info
  }

  /**
    * Range for a lint message.
    */
  type Range = ((Int, Int), (Int, Int))

  def range(issue: Issue): Range =  {
    val ln = Option(issue.getLineNumber).map(_ - 1).getOrElse(1)
    val cn = Option(issue.getColumn).map(_ - 1).getOrElse(1)
    ((ln, cn), (ln, cn + Option(issue.getLength).map(_.toInt).getOrElse(0)))
  }

  /**
    * Models the location of a lint message.
    *
    * @param file     path to the file
    * @param position position of the message in the file
    */
  case class MessageLocation(file: String, position: Range)

  /**
    * Models a lint message.
    *
    * @param severity message severity
    * @param location location of the message in the file
    * @param excerpt  message content
    */
  case class Message(severity: String, location: MessageLocation, excerpt: String)

  def message(path: String, issue: Issue): Option[Message] = Option(issue) match {
    case Some(i) if i.getSeverity != Severity.IGNORE =>
      Some(Message(
        severity = severity(i).toString,
        location = MessageLocation(path, range(i)),
        excerpt = i.getMessage
      ))
    case _                                           => None
  }
}


