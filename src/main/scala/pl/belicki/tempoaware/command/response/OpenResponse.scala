package pl.belicki.tempoaware.command.response

case object OpenResponse extends Response {
  override val message: String = "Opened the requested asset in the browser."
}
