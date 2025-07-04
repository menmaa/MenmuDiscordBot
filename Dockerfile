FROM public.ecr.aws/docker/library/amazoncorretto:18-alpine3.16-jdk
WORKDIR /app
COPY ./build/libs/*.jar ./app.jar
COPY ./config.json ./
CMD ["java", "-jar", "app.jar"]
