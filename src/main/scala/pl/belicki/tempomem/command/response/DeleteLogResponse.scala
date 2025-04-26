package pl.belicki.tempomem.command.response

case class DeleteLogResponse(id: Long) extends Response {
  override def message: String = s"Deleted worklog with id: $id."
}
