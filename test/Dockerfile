FROM eclipse-temurin:latest

RUN apt-get update \
    && apt-get install -y ant git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ARG START_SCRIPT=/usr/local/bin/start.sh
RUN printf "%b" "#!/bin/sh\n" \
    "set -e\n" \
    "git clone https://github.com/seanox/devwex.git\n" \
    "cd devwex\n" \
    "ant test-lts -f test/development/build.xml\n" > $START_SCRIPT
RUN chmod +x $START_SCRIPT

WORKDIR /opt
CMD ["/usr/local/bin/start.sh"]

# docker build -t seanox:devwex-test . && docker run seanox:devwex-test

# For Docker image development only
# CMD [ "sh", "-c", "tail -f /dev/null" ]
