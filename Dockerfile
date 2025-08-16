# Simple Tomcat runtime that serves your WAR as ROOT on port 8081
FROM tomcat:9.0-jdk17-temurin

ARG WAR_FILE=target/ezlearn.war
ENV CATALINA_HOME=/usr/local/tomcat
ENV HTTP_PORT=8081

# Clean default apps and copy your war
RUN rm -rf $CATALINA_HOME/webapps/*
COPY ${WAR_FILE} $CATALINA_HOME/webapps/ROOT.war

# Switch Tomcat connector to 8081
RUN sed -i "s/Connector port=\"8080\"/Connector port=\"${HTTP_PORT}\"/" \
    $CATALINA_HOME/conf/server.xml

EXPOSE ${HTTP_PORT}
CMD ["catalina.sh", "run"]
