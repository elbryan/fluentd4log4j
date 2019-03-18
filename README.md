# fluentd4log4j

_A Log4J appender to push log events to a fluentd server._

## How to Use

At the beginning it could be surprising that this project is packaged via SBT (being the codebase mostly in Java). Main
reason is that I am not familiar at all with Maven/POMs. This SBT project comes with the `sbt assembly` plugin configured.

The scala version used is 2.11 as this is compatible with Spark.

### Maven dependency
```
<dependency>
    <groupId>io.github.elbryan</groupId>
    <artifactId>fluentd4log4j</artifactId>
	<version>1.0</version>
</dependency>
```

### SBT dependency
```
libraryDependencies += "io.github.elbryan" % "fluentd4log4j" % "1.0"
```


### Configuration

| property      | default value    | Description  |
| ------------- |------------------| -------------|
| mdcKeys | "" | The MDC keys (comma separated) that should be added to the log structure. |
| tagPrefix | ""| The fluentd tag prefix to be used |
| tag | "log" | The fluentd tag to be used in all the log messages sent there |
| host | "localhost" | The fluentd server host to where to send the log messages. |
| port | 24224 | The fluentd server port to where to send the log messages. |
| timeout | 15000 (15s) | The timeout (in milliseconds) to connect to the fluentd server|
| bufferCapacity | 1048576 (1Mb) | The socket buffer capacity to connect to the fluentd server |
| useConstantDelayReconnector| false | Switch from the default Exponential Delay reconnector to a constant delay reconnector |

### Example
**log4j.properties**

```
log4j.rootLogger=info, fluentd
log4j.appender.fluentd=io.github.elbryan.fluentd4log4j.FluentdAppender
log4j.appender.fluentd.tag=tef-cells-ingestion
log4j.appender.fluentd.host=fluentd.service.1e100.net
log4j.appender.fluentd.port=24224
log4j.appender.fluentd.addHostname=true
log4j.appender.fluentd.mdcKeys=userid,host,whatever
```
**fluentd configuration**

```
<source>
  type forward
  port 24224
</source>

<match ** >
	type stdout
</match>
```

### Assembling the project / fatjars
If you want to generate the fatjar, simply open sbt and issue `assembly`

```
$> sbt
sbt:fluentd4log4j> assembly
```

## License
This is available in the Apache Licence 2.0
http://www.tldrlegal.com/license/apache-license-2.0-(apache-2.0)
