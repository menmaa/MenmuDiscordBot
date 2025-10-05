FROM public.ecr.aws/docker/library/amazoncorretto:24-alpine3.22-jdk
WORKDIR /app
COPY ./build/libs/*.jar ./app.jar
CMD ["java", "-jar", "app.jar"]
