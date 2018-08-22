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
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /opt

RUN wget https://downloads.gradle.org/distributions/gradle-2.6-bin.zip && \
    unzip gradle-2.6-bin.zip && \
    rm gradle-2.6-bin.zip

ENV GRADLE_HOME /opt/gradle-2.6

ENV PATH $PATH:$GRADLE_HOME/bin

COPY . /home/gradle/src

WORKDIR /home/gradle/src/jni

RUN make

WORKDIR /home/gradle/src

RUN gradle build -x test

WORKDIR /
RUN mkdir app
RUN cp /home/gradle/src/build/distributions/jble6lowpand.tar app/

WORKDIR /app
RUN tar -xvf jble6lowpand.tar
WORKDIR /app/jble6lowpand

ENV LD_LIBRARY_PATH /app/jble6lowpand:${LD_LIBRARY_PATH}

CMD hciconfig hci0 reset && bin/jble6lowpand -configFile /app/jble6lowpand/jble6lowpand.conf

#RUN [ "cross-build-end" ]