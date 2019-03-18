package io.github.elbryan.fluentd4log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.fluentd.logger.Config;
import org.fluentd.logger.FluentLogger;
import org.fluentd.logger.sender.ConstantDelayReconnector;
import org.fluentd.logger.sender.ExponentialDelayReconnector;
import org.fluentd.logger.sender.Reconnector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Original contribution from tuxetuxe
 */
public class FluentdAppender extends AppenderSkeleton {

    private static FluentLogger fluentLogger;

    /**
     * The MDC keys (comma separated) that should be added to the log structure.
     * Default: "" (empty string)
     */
    private String mdcKeys = "";

    /**
     * The fluentd tag prefix to be used
     * Default: "" (empty string)
     */
    private String tagPrefix = "";

    /**
     * The fluentd tag to be used in all the log messages sent there.
     * Default: "log"
     */
    private String tag = "log";
    /**
     * The fluentd server host to where to send the log messages.
     * Default: "localhost"
     */
    private String host = "localhost";
    /**
     * The fluentd server port to where to send the log messages.
     * Default: 24224
     */
    private int port = 24224;
    /**
     * The timeout (in milliseconds) to connect to the fluentd server
     * Default: 15000 (15s)
     */
    private int timeout = 15 * 1000;  // 15s
    /**
     * The socket buffer capacity to connect to the fluentd server
     * Default: 1048576 (1Mb)
     */
    private int bufferCapacity = 1024 * 1024; // 1M

    /**
     * Switch from the default Exponential Delay reconnector to a constant delay reconnector
     * Default: false (use the Exponential Delay reconnector)
     */
    private boolean useConstantDelayReconnector = false;

    /**
     * Adds a value with the current hostname. This value key is "host"
     * <p>
     * Default: false
     */
    private boolean addHostname = false;

    @Override
    public void activateOptions() {
        Reconnector reconnector;

        if (useConstantDelayReconnector) {
            reconnector = new ConstantDelayReconnector();
        } else {
            reconnector = new ExponentialDelayReconnector();
        }

        System.setProperty(Config.FLUENT_SENDER_CLASS, ManagedRawSocketSender.class.getName());

        if (host == null || host.isEmpty()) {
            throw new InvalidConfigurationException("Host must be specified and non-empty");
        }

        if (port < 1 || port > 65535) {
            throw new InvalidConfigurationException("Port must be a value in the range [1-65535]");
        }

        try {
            fluentLogger = FluentLogger.getLogger(tagPrefix, host, port, timeout, bufferCapacity, reconnector);
            LogLog.debug("FluentdAppender connected to fluentd! (host=" + host + ", port=" + port + ",timeout=" + timeout + ",bufferCapacity="
                    + bufferCapacity + ",tagPrefix=" + tagPrefix + ")");
        } catch (Exception e) {
            LogLog.warn("FluentdAppender NOT connected to fluentd! (host=" + host + ", port=" + port + ",timeout=" + timeout + ",bufferCapacity="
                    + bufferCapacity + ",tagPrefix=" + tagPrefix + ")");
            fluentLogger = null;
        }
    }

    @Override
    public void close() {
        if (fluentLogger != null) {
            fluentLogger.flush();
            fluentLogger.close();
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(LoggingEvent event) {
        if (fluentLogger == null) {
            // Ups! Something very wrong is going on here! Bail out!
            LogLog.debug("FluentdAppender has no fluentLogger. Please check your configuration and/or logs for errors!");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("message", event.getMessage());
        data.put("loggerClass", event.getFQNOfLoggerClass());
        data.put("level", event.getLevel().toString());
        data.put("locationInformation", event.getLocationInformation().fullInfo);
        data.put("logger", event.getLoggerName());
        data.put("threadName", event.getThreadName());
        if (event.getThrowableStrRep() != null) {
            data.put("throwableInformation", event.getThrowableStrRep());
        }
        if (event.getNDC() != null) {
            data.put("NDC", event.getNDC());
        }
        for (String mdcKey : mdcKeys.split(",")) {
            Object value = event.getMDC(mdcKey);
            if (value != null) {
                data.put(mdcKey, value);
            }
        }

        if (addHostname) {
            try {
                data.put("hostname", InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                LogLog.warn("FluentdAppender is unable to get the current hostname. " +
                        "Please check your configuration or disable the addition of the hostname!");
            }
        }
        fluentLogger.log(tag, data);
    }

    //
    // Getters and Setters
    //

    public FluentLogger getFluentLogger() {
        return fluentLogger;
    }

    public void setFluentLogger(FluentLogger fluentLogger) {
        this.fluentLogger = fluentLogger;
    }

    public String getMdcKeys() {
        return mdcKeys;
    }

    public void setMdcKeys(String mdcKeys) {
        this.mdcKeys = mdcKeys;
    }

    public String getTagPrefix() {
        return tagPrefix;
    }

    public void setTagPrefix(String tagPrefix) {
        this.tagPrefix = tagPrefix;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public void setBufferCapacity(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    public boolean isUseConstantDelayReconnector() {
        return useConstantDelayReconnector;
    }

    public void setUseConstantDelayReconnector(boolean useConstantDelayReconnector) {
        this.useConstantDelayReconnector = useConstantDelayReconnector;
    }

    public boolean isAddHostname() {
        return addHostname;
    }

    public void setAddHostname(boolean addHostname) {
        this.addHostname = addHostname;
    }

}