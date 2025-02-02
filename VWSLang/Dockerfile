FROM ubuntu:14.04
MAINTAINER Boik Su <lobsiinvok@tdohacker.org>

ENV DEBIAN_FRONTEND noninteractive
ENV HOME /root

WORKDIR /tmp
RUN apt-get update \
  && apt-get install -y build-essential wget \
  && wget http://www.cmake.org/files/v3.5/cmake-3.5.2.tar.gz \
  && tar xf cmake-3.5.2.tar.gz

WORKDIR /tmp/cmake-3.5.2
RUN ./configure \
  && make \
  && make install \
  && apt-get install -y git ninja-build vim flex bison ncurses-dev zlib1g-dev \
  && mkdir /root/work

WORKDIR /root/work
RUN git clone --depth 1 https://github.com/llvm-mirror/llvm.git

WORKDIR /root/work/llvm
RUN (cd tools/; git clone --depth 1 https://github.com/llvm-mirror/clang.git)
RUN (cd projects/; git clone --depth 1 https://github.com/llvm-mirror/compiler-rt.git)
RUN mkdir /root/work/obj

WORKDIR /root/work/obj
RUN cmake -G Ninja -DCMAKE_BUILD_TYPE=Release ../llvm \
  && cmake --build . \
  && cmake --build . --target install

# Clean up APT when done.
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
