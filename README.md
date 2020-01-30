# Quarkus-Redis

Playground with showcase for Quarkus with Redis client.

## Prerequisites

* Install redis master with slave reachable under redis://redis.example:30600,redis://redis.example:30610
* Install local docker

## Development
A local Quarkus test server running this project can be started with Maven by executing:

```
mvn clean install quarkus:dev
```

## Get Infos from Service

First you have to start quarkus in dev mode with above mvn command.

### Metrics

```
curl -H"Accept: application/json" localhost:8080/metrics/
curl -H"Accept: application/json" localhost:8080/metrics/base
curl -H"Accept: application/json" localhost:8080/metrics/application
curl -H"Accept: application/json" localhost:8080/metrics/vendor
```

### Health

```
curl localhost:8080/health
curl localhost:8080/health/live
curl localhost:8080/health/ready
```

### Query Service

```
curl localhost:8080/redis
```
### Swagger-UI

Open <http://localhost:8080/swagger-ui/> in Browser.

## Build the different images

### Build jvm based Image

This image can be used for local testing and when startup times of 2 Seconds are ok.

Change into directory containing this Readme.

```
mvn package docker:build -Pjvm
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/redis-jvm .
```

### Build GraalVM based Image

This image is used when libs have to be integrated without quarkus-extension, to analyse the behaviour buy running in GraalVM with agent.

Change into directory containing this Readme.

```
mvn package docker:build -Pgraalvm
docker build -f src/main/docker/Dockerfile.graalvm -t quarkus/redis-graalvm .
```

### Build Linux based Native Image

Change into directory containing this Readme.

```
mvn docker:build -Plinux
docker build -f src/main/docker/Dockerfile.linux.multistage -t quarkus/redis-native .
```

### Build real small distroless Native Image

This image contains only shared objects (libs) and the native binary. Warning you can't ssh into containers created from this image, but they are small.

Change into directory containing this Readme.

```
mvn docker:build -Pdistroless
docker build -f src/main/docker/Dockerfile.distroless.multistage -t quarkus/redis-native-distroless .
```

## Start and measure local Container

All measurements are done on my machine:

```
Thinkpad t570p
4 i7 cores
8 hyperthreads
32 GB RAM
Ubuntu Linux 18.04.1 HWE Kernel 5.0
```

Before running the tests you have to warmup the redis a bit, otherwise the first search will be slow.

### JVM Container

Container listening on localhost 11080

```
time docker run -i -d --rm --name quarkus-redis-jvm --add-host=redis.example:$(getent hosts redis.example | cut -d' ' -f1) -p 11080:8080 quarkus/redis-jvm;time sleep 1.8;time curl localhost:11080/redis
```

#### Rampup (create container from image)

```
real    0m0,682s
user    0m0,051s
sys     0m0,030s
```

#### Wait for service start in container

1.8 Seconds is the minimum value on my machine to ensure the service is really reachable.

```
real    0m1,801s
user    0m0,001s
sys     0m0,000s
```

#### Service execution

First time, nothing is hotspot compiled....

```
real    0m1,173s
user    0m0,017s
sys     0m0,009s
```

Second time, hotspot done some work...

```
real    0m0,169s
user    0m0,017s
sys     0m0,005s
```

#### Memory Footprint

```
  "memory.committedHeap" : 165675008,
  "memory.usedHeap" : 115436072
  "memory.usedNonHeap" : 38549632,
  "memory.committedNonHeap" : 40239104
```

#### Stop and remove container

```
docker stop quarkus-redis-jvm
```

### Native Container

Measure container listening on localhost 9080

```
time docker run -i -d --rm --name quarkus-redis-native --add-host=redis.example:$(getent hosts redis.example | cut -d' ' -f1) -p 9080:8080 quarkus/redis-native;time sleep 0.030;time curl localhost:9080/redis
```

#### Rampup (create container from image)

```
real    0m0,620s
user    0m0,032s
sys     0m0,047s
```

#### Wait for service start in container

```
real    0m0,031s
user    0m0,001s
sys     0m0,000s
```

#### Service execution

```
real    0m0,129s
user    0m0,008s
sys     0m0,000s
```
#### Memory Footprint

```
  "memory.committedHeap" : 158806600,
  "memory.usedHeap" : 120865520
  "memory.usedNonHeap" : 16189616,
  "memory.committedNonHeap" : 16189616
```

#### Stop and remove container

```
docker stop quarkus-redis-native
```

### Conclusion

Rampup time of containers is almost the same. Startup from service which is important for high availability and scaleability in kubernetes/fargate differs by the value of 60 times on quarkus-native being faster in providing the service. Serving the service is nearly equal, after the vm is warmed up with hotspot compilation.

From point of memory view quarkus-native takes the almost the same ammount of heap memory as the quarkus-jvm version. In using non heap memory the quarkus-native version is 2 times better than the quarkus-jvm version.

### Distroless Native Container

Measure container listening on localhost 19080

```
time docker run -i -d --rm --name quarkus-redis-native-distroless --add-host=redis.example:$(getent hosts redis.example | cut -d' ' -f1) -p 19080:8080 quarkus/redis-native-distroless;time sleep 0.030;time curl localhost:19080/redis
```

#### Rampup (create container from image)

```
real    0m0,608s
user    0m0,036s
sys     0m0,018s

```

#### Wait for service start in container

```
real    0m0,031s
user    0m0,001s
sys     0m0,000s
```

#### Service execution

```
real    0m0,087s
user    0m0,004s
sys     0m0,003s
```
#### Memory Footprint

```
  "memory.committedHeap" : 160503504,
  "memory.usedHeap" : 121945832
  "memory.usedNonHeap" : 16180016,
  "memory.committedNonHeap" : 16180016
```

#### Stop and remove container

```
docker stop quarkus-redis-native-distroless
```

### Conclusion

Rampup time of containers is almost the same. Startup from service which is important for high availability and scaleability in kubernetes/fargate differs by the value of 60 times on quarkus-native-distroless being faster in providing the service. Serving the service is nearly equal, after the vm is warmed up with hotspot compilation.

From point of memory view quarkus-native-distroless takes the almost the same ammount of heap memory as the quarkus-jvm version. In using non heap memory the quarkus-native-distroless version is 2 times better than the quarkus-jvm version.

Distroless is mainly for production usage to minimize the attack surface from container point of view.

## Quarkus/GraalVM Expert Usage

The following is not needed when trying out this project. You may need it on your own project with special needs.

### GraalVM container

After manual start inside container the application listens on localhost 7080

```
docker run -it --rm --add-host=redis.example:$(getent hosts redis.example | cut -d' ' -f1) -p 7080:8080 quarkus/redis-graalvm bash
```

inside container

```
java -agentlib:native-image-agent=config-output-dir=/deployments/ -cp /deployments/lib/ -jar /deployments/app.jar
cd /deployment
```

Now call some urls

```
curl localhost:7080/redis
...
```

Especially try to reach code used from libs using reflection and other stuff not supported by quarkus-extensions

And then folder ``/deployment`` will contain the *.json files needed for native compile configuration.

### Native compile

Handling class initialization problems with *--initialize-at-build-time=...* and *--initialize-at-run-time=...* see <https://medium.com/graalvm/updates-on-class-initialization-in-graalvm-native-image-generation-c61faca461f7>


**E.g. Error: No instances of io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeHeapByteBuf are allowed....**

```
Error: No instances of io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeHeapByteBuf are allowed in the image heap as this class should be initialized at image runtime. Object has been initialized by the io.vertx.redis.client.impl.types.BulkType class initializer with a trace: 
    at io.netty.buffer.UnpooledUnsafeHeapByteBuf.<init>(UnpooledUnsafeHeapByteBuf.java:34)
    at io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeHeapByteBuf.<init>(UnpooledByteBufAllocator.java:139)
    at io.netty.buffer.UnpooledByteBufAllocator.newHeapBuffer(UnpooledByteBufAllocator.java:82)
    at io.netty.buffer.AbstractByteBufAllocator.heapBuffer(AbstractByteBufAllocator.java:168)
    at io.netty.buffer.Unpooled.buffer(Unpooled.java:135)
    at io.vertx.core.buffer.impl.BufferImpl.<init>(BufferImpl.java:44)
    at io.vertx.core.buffer.impl.BufferImpl.<init>(BufferImpl.java:52)
    at io.vertx.core.buffer.impl.BufferImpl.<init>(BufferImpl.java:56)
    at io.vertx.core.buffer.impl.BufferFactoryImpl.buffer(BufferFactoryImpl.java:39)
    at io.vertx.core.buffer.Buffer.buffer(Buffer.java:72)
    at io.vertx.redis.client.impl.types.BulkType.<clinit>(BulkType.java:27)
.  To fix the issue mark io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeHeapByteBuf for build-time initialization with --initialize-at-build-time=io.netty.buffer.UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeHeapByteBuf or use the the information from the trace to find the culprit and --initialize-at-run-time=<culprit> to prevent its instantiation.
```