FROM amazoncorretto:17

ARG SPRING_ACTIVE_PROFILES
ENV SPRING_ACTIVE_PROFILES ${SPRING_ACTIVE_PROFILES}

COPY build/libs/moabam-server-0.0.1-SNAPSHOT.jar moabam.jar
COPY src/main/resources/config/agent.java/ agent.java

ENTRYPOINT ["java", "-javaagent:agent.java/scouter.agent.jar", "-Dsocuter.config=agent.java/conf/scouter.conf", "-jar", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=${SPRING_ACTIVE_PROFILES}", "/moabam.jar"]
