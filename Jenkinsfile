pipeline {
  agent { label 'infra-build-node' }

  environment {
    SONARQUBE_SERVER = 'SonarQube'
    NEXUS_URL        = 'http://nexus.mitechnology.org:8081'
    NEXUS_REPO       = 'ezlearn-release'
    VERSION          = '1.0.0'

    APP_IMAGE        = 'ezlearn-app'     // change to your org/image if you wish
    CONTAINER_NAME   = 'ezlearn-app'
    APP_PORT_HOST    = '8888'                   // external port you want
    APP_PORT_CONT    = '8080'                   // Tomcat internal port
    DOCKERHUB_IMAGE  = 'mitechllc'
  }

  options {
    skipDefaultCheckout() 
    disableConcurrentBuilds()
  }
  

  stages {
    stage('Workspace Cleanup') {
      steps {
        // If you have the Workspace Cleanup plugin, use cleanWs(); otherwise deleteDir()
        script {
          try { cleanWs() } catch (err) { deleteDir() }
        }
      }
    }
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build') {
      steps { sh 'mvn -B clean compile' }
    }

    stage('Unit Test') {
      steps { sh 'mvn -B test' }
      post { always { junit 'target/surefire-reports/*.xml' } }
    }

    stage('Checkstyle Analysis') {
      steps { sh 'mvn -B checkstyle:check' }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv("${SONARQUBE_SERVER}") {
          sh '''
            mvn -B sonar:sonar \
              -Dsonar.projectKey=ezlearn \
              -Dsonar.host.url=http://sonarqube.mitechnology.org:9000
          '''
        }
      }
    }

    stage('Package WAR') {
      steps {
        sh '''
          mvn -B package
          cp target/ezlearn-1.0.0.war target/ezlearn.war
        '''
      }
    }

    stage('Publish to Nexus (WAR)') {
      steps {
        script {
          def ts = sh(script: "date +%Y%m%d%H%M%S", returnStdout: true).trim()
          def warName = "ezlearn-${ts}.war"
          sh "cp target/ezlearn.war target/${warName}"

          withCredentials([usernamePassword(
            credentialsId: 'nexus-creds',
            usernameVariable: 'NEXUS_USER',
            passwordVariable: 'NEXUS_PASS'
          )]) {
            sh """
              mvn -B deploy:deploy-file \
                -DgroupId=com.ezlearn \
                -DartifactId=ezlearn \
                -Dversion=${ts} \
                -Dpackaging=war \
                -Dfile=target/${warName} \
                -DrepositoryId=ezlearn-release \
                -Durl=${NEXUS_URL}/repository/${NEXUS_REPO}/ \
                -DgeneratePom=true \
                --settings jenkins/settings.xml
            """
          }
          env.BUILD_TS = ts   // keep for tagging if you want
        }
      }
    }

    stage('Build App Image') {
      steps {
        script {
          // Use timestamp tag for traceability; also tag 'latest'
          def tag = env.BUILD_TS ?: sh(script: "date +%Y%m%d%H%M%S", returnStdout: true).trim()
          env.APP_TAG = tag
          sh """
            docker build -t ${APP_IMAGE}:${APP_TAG} .
            docker tag ${APP_IMAGE}:${APP_TAG} ${APP_IMAGE}:latest
          """
        }
      }
    }
    stage('Push Image to Docker Hub') {
        steps {
            script {
            if (env.APP_IMAGE != env.APP_IMAGE.toLowerCase()) {
                error "APP_IMAGE must be lowercase (current: ${env.APP_IMAGE})"
            }
            if (env.DOCKERHUB_IMAGE != env.DOCKERHUB_IMAGE.toLowerCase()) {
                error "DOCKERHUB_IMAGE must be lowercase (current: ${env.DOCKERHUB_IMAGE})"
            }
            }

            withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
            usernameVariable: 'HUB_USER', passwordVariable: 'HUB_PASS')]) {

            sh """
                echo "\$HUB_PASS" | docker login -u "\$HUB_USER" --password-stdin

                docker tag ${APP_IMAGE}:${APP_TAG} ${DOCKERHUB_IMAGE}/${APP_IMAGE}:latest
                

                
                docker push ${DOCKERHUB_IMAGE}/${APP_IMAGE}:latest

                docker logout || true
          """
        }
      }
    }
    stage('Deploy (Recreate Container)') {
        steps {
            sh '''#!/usr/bin/env bash
                set -euo pipefail

                # Stop and remove the existing container if it's running
                docker rm -f ezlearn-app >/dev/null 2>&1 || true

                # Run the new container with the same name and port
                docker run -d --restart=unless-stopped --name ezlearn-app \
                    -p 8888:8080 \
                    ezlearn-app:${APP_TAG}

                CONTAINER_IP="$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' ezlearn-app)"
                echo "Container IP: $CONTAINER_IP (checking http://$CONTAINER_IP:8080/)"

                # Wait up to ~60s for app inside the container
                for i in {1..30}; do
                    if curl -sS -o /dev/null "http://$CONTAINER_IP:8080/"; then
                    echo "✅ App reachable at http://$CONTAINER_IP:8080/"
                    exit 0
                    fi
                    sleep 2
                done

                echo "❌ Health check failed (no response on container IP/port)"
                docker logs ezlearn-app || true
                exit 1
            '''
            }
        }
    }
    post {
    // Optional: also clean after the build if you installed the plugin
    always {
      script {
        try { cleanWs() } catch (err) { /* plugin not installed; ignore */ }
      }
      sh 'docker image prune -f || true'
    }
    success { echo "✅ Deployed ${APP_IMAGE}:${APP_TAG} to '${CONTAINER_NAME}' on port ${APP_PORT_HOST}" }
    failure { echo "❌ Pipeline failed" }
  }
}
  
