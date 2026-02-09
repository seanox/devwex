FROM eclipse-temurin:latest

RUN apt-get update \
    && apt-get install -y ant git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN git clone https://github.com/seanox/devwex.git

# For local development/testing only
# COPY . /opt/devwex/test
# COPY CHANGES /opt/devwex

ARG DEVWEX_HOME=/opt/devwex
ARG DEVWEX_TEST=$DEVWEX_HOME/test

# Compatibility for the primarily Windows-based test
ENV SystemDrive=C:
ENV SystemRoot=C:\\WINDOWS
RUN ln -s $DEVWEX_TEST/ "$DEVWEX_TEST/?"

WORKDIR $DEVWEX_HOME
CMD ["ant", "-f", "/opt/devwex/test/development/build.xml", "test-lts"]

# Usage:
# docker build -t seanox:devwex-test . && docker run seanox:devwex-test
# cls && ant compile -f development\build.xml && docker build -t seanox:devwex-test . && docker run seanox:devwex-test

# For Docker image development only
# CMD [ "sh", "-c", "tail -f /dev/null" ]
