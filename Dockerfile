# Dockerfile
FROM tomcat:9.0

# WAR_URL will be provided by CodeBuild (you'll set it in the Console)
ARG WAR_URL

# Minimal tool and clean image
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Remove default apps and add your WAR as ROOT.war
RUN rm -rf /usr/local/tomcat/webapps/* \
 && test -n "$WAR_URL" \
 && echo "Downloading WAR from: $WAR_URL" \
 && curl -fL "$WAR_URL" -o /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
