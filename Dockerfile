FROM azul/zulu-openjdk-alpine:11.0.4

RUN mkdir -p /usr/src/bot
WORKDIR /usr/src/bot

COPY build/libs/Harmonizer.jar /usr/src/bot
CMD ["java", "-jar", "Harmonizer.jar"]
