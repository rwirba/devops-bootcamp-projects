FROM tomcat:9.0-jdk17-temurin


RUN rm -rf /usr/local/tomcat/webapps/*
COPY target/ezlearn.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
ENV CATALINA_OPTS="$CATALINA_OPTS -Dserver.port=8082"

