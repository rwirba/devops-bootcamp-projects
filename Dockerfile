# Dockerfile
FROM tomcat:9.0-jdk17-temurin

# Clean default apps and drop in your WAR as ROOT
RUN rm -rf /usr/local/tomcat/webapps/*
COPY target/ezlearn.war /usr/local/tomcat/webapps/ROOT.war

# Optional: healthcheck (Tomcat up)
HEALTHCHECK --interval=10s --timeout=3s --retries=30 \
  CMD curl -fsS http://localhost:8080/ || exit 1
