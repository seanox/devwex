# Usage:
# unit tests only:
# docker build --build-arg SERIAL=%random% -t seanox:devwex-test . && docker run seanox:devwex-test
# compile and unit tests (when COPY is used):
# ant compile -f development && docker build --build-arg SERIAL=%random% -t seanox:devwex-test . && docker run seanox:devwex-test

FROM eclipse-temurin:latest

RUN apt-get update \
    && apt-get install -y ant git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Force a clone
ARG SERIAL=0000-0000-0000
RUN echo $SERIAL > /tmp/serial

ARG DEVWEX_HOME=/opt/devwex
ARG DEVWEX_TEST=$DEVWEX_HOME/test

RUN git config --global core.autocrlf true
RUN git clone https://github.com/seanox/devwex.git $DEVWEX_HOME

# For local development/testing only
# COPY . /opt/devwex/test
# COPY CHANGES /opt/devwex

# Compatibility for the primarily Windows-based test
ENV SystemDrive=C:
ENV SystemRoot=C:\\WINDOWS
RUN ln -s $DEVWEX_TEST/ "$DEVWEX_TEST/?"

WORKDIR $DEVWEX_HOME
CMD ["ant", "-f", "/opt/devwex/test/development/build.xml", "test-lts"]

# For Docker image development only
# CMD [ "sh", "-c", "tail -f /dev/null" ]
