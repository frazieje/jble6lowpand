FROM gradle:jdk8 as javabuilder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build -x test

FROM openjdk:8-jdk-slim

EXPOSE 8089

RUN apt-get update && apt-get install -y \
    gcc \
    build-essential \
    libglib2.0-dev \
    bluez \
    libbluetooth-dev \
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

COPY --from=javabuilder /home/gradle/src/build/distributions/jble6lowpand.tar /opt/

WORKDIR /opt

RUN tar -xvf jble6lowpand.tar

RUN rm jble6lowpand.tar

COPY ./jni /opt/jble6lowpand/jni

WORKDIR /opt/jble6lowpand/jni

RUN mkdir libs

RUN make

RUN cp /opt/jble6lowpand/jni/libs/libble6lowpand.so /usr/lib/

WORKDIR /opt/jble6lowpand

RUN rm -rf jni/

CMD bin/jble6lowpand -configFile /opt/jble6lowpand/jble6lowpand.conf