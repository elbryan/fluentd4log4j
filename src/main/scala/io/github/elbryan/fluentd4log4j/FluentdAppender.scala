package io.github.elbryan.fluentd4log4j

import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.helpers.LogLog
import org.apache.log4j.spi.LoggingEvent
import org.fluentd.logger.FluentLogger
import org.fluentd.logger.sender.ExponentialDelayReconnector
import java.net.InetAddress

import scala.collection.mutable

class FluentdAppender() extends AppenderSkeleton {
  /**
    * The MDC keys (comma separated) that should be added to the log structure.
    * <b>Default:</b> none
    */
  private var _mdcKeys: String = ""
  def mdcKeys: String = _mdcKeys
  def mdcKeys_= (value: String): Unit = _mdcKeys = value

  /**
    * The fluentd tag prefix to be used
    * <b>Default:</b> "" (empty string)
    */
  private var _tagPrefix: String = ""
  def tagPrefix: String = _tagPrefix
  def tagPrefix_= (value: String): Unit = _tagPrefix = value

  /**
    * The fluentd tag to be used in all the log messages sent there.
    * <b>Default:</b> "log"
    */
  private var _tag: String = "log"
  def tag: String = _tag
  def tag_= (value: String): Unit = _tag = value

  /**
    * The fluentd server host to where to send the log messages.
    * <b>Default:</b> "localhost"
    */
  private var _host: String = "localhost"
  def host: String = _host
  def host_= (value: String): Unit = _host = value

  /**
    * The fluentd server port to where to send the log messages.
    * <b>Default:</b> 24224
    */
  private var _port: Int = 24224
  def port: Int = _port
  def port_= (value: Int): Unit = _port = value

  /**
    * The timeout (in milliseconds) to connect to the fluentd server
    * <b>Default:</b> 15000 (15s)
    */
  private var _timeout: Int = 15 * 1000 // 15s
  def timeout: Int = _timeout
  def timeout_= (value: Int): Unit = _timeout = value

  /**
    * The socket buffer capacity to connect to the fluentd server
    * <b>Default:</b> 1048576 (1Mb)
    */
  private var _bufferCapacity: Int = 1024 * 1024 // 1M
  def bufferCapacity: Int = _bufferCapacity
  def bufferCapacity_= (value: Int): Unit = _bufferCapacity = value

  /**
    * Adds a value with the current hostname. This value key is "hostname"
    *
    * <b>Default:</b> false
    */
  private var _addHostname: Boolean = false
  def addHostname: Boolean = _addHostname
  def addHostname_= (value: Boolean): Unit = _addHostname = value

  private var logger : Option[FluentLogger] = None

  override def activateOptions(): Unit = {
    if (host == null || host.isEmpty) {
      throw new InvalidConfigurationException("Host must be specified and non-empty")
    }

    if (port < 1 || port > 65535) {
      throw new InvalidConfigurationException("Port must be a value in the range [1-65535]")
    }

    try {

      logger = Some(FluentLogger.getLogger(tagPrefix, host, port, timeout, bufferCapacity, new ExponentialDelayReconnector))
      LogLog.debug(s"FluentdAppender connected successfully to fluentd using the following configuration" +
        s"parameters (host=$host, port=$port,timeout=$timeout,bufferCapacity=$bufferCapacity,tagPrefix=$tagPrefix)")

    } catch {
      case e: Exception => LogLog.error(s"FluentdAppender cannot connect to fluentd using the following configuration" +
        s"parameters (host=$host, port=$port,timeout=$timeout,bufferCapacity=$bufferCapacity,tagPrefix=$tagPrefix)", e)
    }

  }

  override def append(event: LoggingEvent): Unit = {
    logger match {
      case Some(fluentLogger) => {
        val data = mutable.HashMap[String, Object]()

        data.put("message", event.getMessage)
        data.put("loggerClass", event.getFQNOfLoggerClass)
        data.put("level", event.getLevel.toString)
        data.put("locationInformation", event.getLocationInformation.fullInfo)
        data.put("logger", event.getLoggerName)
        data.put("threadName", event.getThreadName)

        if (event.getThrowableStrRep != null) {
          data.put("throwableInformation", event.getThrowableStrRep)
        }

        if (event.getNDC != null) {
          data.put("NDC", event.getNDC)
        }

        if (mdcKeys.nonEmpty) {
          mdcKeys.split(",").foreach {
            mdcKey => {
              val value = event.getMDC(mdcKey)
              if (value != null) {
                data.put(mdcKey, value)
              } else {
                LogLog.warn(s"Cannot retrieve MDC value for key $mdcKey")
              }
            }
          }
        }

        if (addHostname) {
          try {
            data.put("hostname", InetAddress.getLocalHost.getHostName)
          }catch {
            case e: Exception =>
              LogLog.warn("FluentdAppender is unable to get the current hostname." +
                "Please check your configuration and/or disable the addition of the hostname!", e)
          }
        }

        import collection.JavaConverters._
        fluentLogger.log(tag, data.asJava)
      }
      case None => {
        // Odd enough if we get to this point
        LogLog.error("FluentdAppender has no fluentlogger. Please check your configuration and/or logs for errors!")
      }
    }
  }

  override def close(): Unit = {
    logger.foreach {
      fluentLogger => {
        fluentLogger.flush()
        fluentLogger.close()
      }
    }
  }

  override def requiresLayout(): Boolean = false
}