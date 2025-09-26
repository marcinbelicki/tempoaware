package pl.belicki.tempoaware.command.response

case class LogResponse(id: Long) extends Response {
  override def message: String = s"Added worklog with id: $id."
}
