FROM gradle:jdk8 as javabuilder

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build -x test

FROM resin/raspberry-pi-openjdk:8-jdk

#RUN [ "cross-build-start" ]

ENV LD_LIBRARY_PATH /usr/local/nvidia/lib:/usr/local/nvidia/lib64:${LD_LIBRARY_PATH}

EXPOSE 8080

# Install dependencies
RUN apt-get update && apt-get install -y \
    vim \
    gcc \
    build-essential \
    libglib2.0-dev \
    bluez \
    libbluetooth-dev \
    wget \
    unzip \
    git \
    radvd \
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

COPY --from=javabuilder /home/gradle/src/build/distributions/jble6lowpand.tar /app/

COPY ./jni /app/jni

WORKDIR /app/jni

RUN mkdir libs

RUN make

WORKDIR /app

RUN tar -xvf jble6lowpand.tar

RUN cp /app/jni/libs/libble6lowpand.so /app/jble6lowpand/

WORKDIR /app/jble6lowpand

ENV LD_LIBRARY_PATH /app/jble6lowpand:${LD_LIBRARY_PATH}

COPY ./entrypoint.sh /app/jble6lowpand/entrypoint.sh

RUN chmod 777 /app/jble6lowpand/etrypoint.sh

ENTRYPOINT /app/jble6lowpand/entrypoint.sh

CMD /usr/sbin/radvd && \
    hciconfig hci0 reset && \
    bin/jble6lowpand -configFile /app/jble6lowpand/jble6lowpand.conf

#RUN [ "cross-build-end" ]