package sbt.io

private[sbt] trait WatchLogger {
  def debug(msg: => Any): Unit
}
private[sbt] object NullWatchLogger extends WatchLogger {
  private def ignoreArg[T](f: => T): Unit = if (false) { f; () } else ()
  override def debug(msg: => Any): Unit = ignoreArg(msg)
}
