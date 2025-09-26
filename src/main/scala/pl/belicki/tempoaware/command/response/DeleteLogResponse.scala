package pl.belicki.tempoaware.command.response

case class DeleteLogResponse(id: Long) extends Response {
  override def message: String = s"Deleted worklog with id: $id."
}
