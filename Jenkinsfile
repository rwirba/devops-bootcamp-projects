pipeline {
    agent { label 'infra-build-node' }

    environment {
        SONARQUBE_SERVER = 'SonarQube'
        NEXUS_URL = 'http://nexus.mitechnology.org:8081'
        NEXUS_REPO = 'ezlearn-release'
        DEPLOY_SERVER = 'ubuntu@184.72.200.252'
        DEPLOY_PATH = '/opt/tomcat/webapps'
        WAR_NAME = 'ezlearn.war'
        VERSION = '1.0.0'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Unit Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Checkstyle Analysis') {
            steps {
                sh 'mvn checkstyle:check'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_SERVER}") {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=ezlearn \
                          -Dsonar.host.url=http://sonarqube.mitechnology.org:9000
                    '''
                }
            }
        }

        stage('Package WAR') {
            steps {
                sh '''
                    mvn package
                    cp target/ezlearn-${VERSION}.war target/ezlearn-latest.war
                '''
            }
        }

        stage('Publish to Nexus') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-creds',
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh '''
                        mvn deploy:deploy-file \
                          -DgroupId=com.ezlearn \
                            -DartifactId=ezlearn \
                            -Dversion=\$(date +%Y%m%d%H%M%S) \
                            -Dpackaging=war \
                            -Dfile=target/${WAR_NAME} \
                            -DrepositoryId=ezlearn-release \
                            -Durl=${NEXUS_URL}/repository/${NEXUS_REPO}/ \
                            -DgeneratePom=true \
                            --settings jenkins/settings.xml
                    '''
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sshagent (credentials: ['ssh-agent-key']) {
                    sh "scp target/ezlearn-latest.war ${DEPLOY_SERVER}:${DEPLOY_PATH}/${WAR_NAME}"
                }
            }
        }
    }

    post {
        always {
            script {
                def hasReports = fileExists('target/surefire-reports')
                if (hasReports) {
                    junit 'target/surefire-reports/*.xml'
                } else {
                    echo "No test reports found to archive."
                }
            }
        }
        success {
            echo "✅ Pipeline executed successfully!"
        }
        failure {
            echo "❌ Pipeline failed!"
        }
    }
}
