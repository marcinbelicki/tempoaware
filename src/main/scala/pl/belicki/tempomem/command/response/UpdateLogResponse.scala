package pl.belicki.tempomem.command.response

case class UpdateLogResponse(id: Long) extends Response {
  override def message: String = s"Updated worklog with id: $id."
}
