FROM openjdk:8-jdk
RUN apt-get update && apt-get install -y \
	maven
WORKDIR /build
COPY . .
RUN mvn package

FROM openjdk:8-jre-alpine
RUN apk add --no-cache \
	curl \
	python \
	ffmpeg
	
COPY --from=0 /build/piper-server/target/piper-server-0.0.1-SNAPSHOT.jar /app/piper.jar

WORKDIR /piper

ENTRYPOINT []
CMD ["java", "-Xmx1g", "-jar", "-Djava.security.egd=file:/dev/./urandom", "/app/piper.jar"]
