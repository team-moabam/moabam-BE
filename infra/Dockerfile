FROM amazoncorretto:17

ARG SPRING_ACTIVE_PROFILES
ENV SPRING_ACTIVE_PROFILES ${SPRING_ACTIVE_PROFILES}

COPY ../build/libs/moabam-server-0.0.1-SNAPSHOT.jar moabam.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=${SPRING_ACTIVE_PROFILES}", "/moabam.jar"]
