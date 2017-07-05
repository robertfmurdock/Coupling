FROM node:7-slim

RUN echo "deb http://http.debian.net/debian jessie-backports main" | \
    tee --append /etc/apt/sources.list.d/jessie-backports.list > /dev/null \
  && apt-get update \
  && apt-get install -y -t jessie-backports\
       git \
       bzip2 \
       openjdk-8-jre