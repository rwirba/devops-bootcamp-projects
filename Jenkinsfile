pipeline {
  agent { label 'infra-build-node' }

  environment {
    SONARQUBE_SERVER = 'SonarQube'
    NEXUS_URL        = 'http://nexus.mitechnology.org:8081'
    NEXUS_REPO       = 'ezlearn-release'
    VERSION          = '1.0.0'

    // Container deployment settings (same Docker host where Jenkins/agent run)
    APP_IMAGE        = 'ezlearn/tomcat-app'     // change to your org/image if you wish
    CONTAINER_NAME   = 'ezlearn-app'
    APP_PORT_HOST    = '8888'                   // external port you want
    APP_PORT_CONT    = '8082'                   // Tomcat internal port
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

    // Optional: push image to a registry (uncomment & configure if you have docker.kihhuf.org)
    // stage('Push Image') {
    //   steps {
    //     withCredentials([usernamePassword(credentialsId: 'nexus-docker-creds',
    //       usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
    //       sh '''
    //         echo "$REG_PASS" | docker login docker.kihhuf.org -u "$REG_USER" --password-stdin
    //         docker tag ${APP_IMAGE}:${APP_TAG} docker.kihhuf.org/${APP_IMAGE}:${APP_TAG}
    //         docker tag ${APP_IMAGE}:latest docker.kihhuf.org/${APP_IMAGE}:latest
    //         docker push docker.kihhuf.org/${APP_IMAGE}:${APP_TAG}
    //         docker push docker.kihhuf.org/${APP_IMAGE}:latest
    //       '''
    //     }
    //   }
    // }

    stage('Deploy (Recreate Container)') {
      steps {
        sh '''
          set -euo pipefail

          # Stop & remove prior container (if any)
          docker rm -f ${CONTAINER_NAME} >/dev/null 2>&1 || true

          # Run new version with host:8888 -> container:8082
          docker run -d --restart=unless-stopped --name ${CONTAINER_NAME} \
            -p ${APP_PORT_HOST}:${APP_PORT_CONT} \
            ${APP_IMAGE}:${APP_TAG}

          # Health check (up to 60s)
          for i in {1..30}; do
            if curl -fsS http://localhost:${APP_PORT_HOST}/ >/dev/null; then
              echo "✅ App healthy at http://localhost:${APP_PORT_HOST}/"
              exit 0
            fi
            sleep 2
          done

          echo "❌ Health check failed"
          docker logs ${CONTAINER_NAME} || true
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
