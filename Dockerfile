FROM gradle:jdk8 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test

FROM resin/raspberry-pi-openjdk:8-jre

ENV LD_LIBRARY_PATH /usr/local/nvidia/lib:/usr/local/nvidia/lib64:${LD_LIBRARY_PATH}

EXPOSE 8080

COPY --from=builder /home/gradle/src/build/distributions/jble6lowpand.tar /app/
COPY ./jni /app/jni/

# Install dependencies
RUN apt-get update && apt-get install -y \
    vim \
    gcc \
    build-essential \
    libglib2.0-dev \
    bluez \
    libbluetooth-dev \
    git \
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

#RUN cd /app/jni && make

#RUN cp /app/jni/libs/arm32/libble6lowpand.so /app/jnilibs

WORKDIR /app
RUN tar -xvf jble6lowpand.tar
WORKDIR /app/jble6lowpand

ENV LD_LIBRARY_PATH /app/jble6lowpand:${LD_LIBRARY_PATH}

CMD bin/jble6lowpand -configFile /app/jble6lowpand/jble6lowpand.conf