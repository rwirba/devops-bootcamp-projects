pipeline {
    agent { label 'infra-build-node' }

    environment {
        SONARQUBE_SERVER = 'SonarQube'
        NEXUS_URL = 'http://nexus.mitechnology.org:8081'
        NEXUS_REPO = 'ezlearn-release'
        DEPLOY_SERVER = 'ubuntu@184.72.200.252'
        DEPLOY_PATH = '/opt/tomcat/webapps'
        VERSION = '1.0.0'
        SONAR_JACOCO_REPORT_PATH = 'target/site/jacoco/jacoco.xml'
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=512m'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    extensions: [[$class: 'LocalBranch']],
                    userRemoteConfigs: [[url: 'https://github.com/rwirba/devops-bootcamp-projects.git']]
                ])
            }
        }

        stage('Build & Quality Checks') {
            parallel {
                stage('Build') {
                    steps {
                        sh 'mvn clean compile'
                    }
                }
                stage('Static Analysis') {
                    steps {
                        sh 'mvn checkstyle:checkstyle'
                        recordIssues(
                            tools: [checkStyle(pattern: 'target/checkstyle-result.xml')],
                            qualityGates: [[threshold: 1, type: 'TOTAL_ERROR', unstable: true]]
                        )
                    }
                }
            }
        }

        stage('Test & Coverage') {
            steps {
                sh 'mvn test jacoco:report'
                junit 'target/surefire-reports/**/*.xml'
                archiveArtifacts artifacts: 'target/site/jacoco/**/*.xml'
            }
            post {
                always {
                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        exclusionPattern: 'src/test*'
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_SERVER}") {
                    sh """
                        mvn sonar:sonar \
                          -Dsonar.projectKey=ezlearn \
                          -Dsonar.host.url=http://sonarqube.mitechnology.org:9000 \
                          -Dsonar.qualitygate.wait=true \
                          -Dsonar.coverage.jacoco.xmlReportPaths=${SONAR_JACOCO_REPORT_PATH} \
                          -Dsonar.java.binaries=target/classes \
                          -Dsonar.sources=src/main/java \
                          -Dsonar.tests=src/test/java \
                          -Dsonar.junit.reportPaths=target/surefire-reports
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 15, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build & Deploy Artifact') {
            steps {
                sh 'mvn package'
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }

        stage('Publish to Nexus') {
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: "${NEXUS_URL}",
                    groupId: 'com.ezlearn',
                    version: "${env.BUILD_ID}",
                    repository: "${NEXUS_REPO}",
                    credentialsId: 'nexus-creds',
                    artifacts: [
                        [artifactId: 'ezlearn',
                         classifier: '',
                         file: 'target/ezlearn.war',
                         type: 'war']
                    ]
                )
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sshagent(['ssh-agent-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_SERVER} \
                            "sudo systemctl stop tomcat && \
                             sudo rm -rf ${DEPLOY_PATH}/ROOT*"
                        scp -o StrictHostKeyChecking=no target/ezlearn.war \
                            ${DEPLOY_SERVER}:${DEPLOY_PATH}/ROOT.war
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_SERVER} \
                            "sudo chown tomcat:tomcat ${DEPLOY_PATH}/ROOT.war && \
                             sudo systemctl start tomcat"
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            script {
                def subject = "${env.JOB_NAME} - Build #${env.BUILD_NUMBER} - ${currentBuild.currentResult}"
                def details = """Check console output at ${env.BUILD_URL}console"""
                
                if (currentBuild.resultIsBetterOrEqualTo('SUCCESS')) {
                    emailext(
                        subject: subject,
                        body: details,
                        to: 'dev-team@yourcompany.com',
                        attachLog: true
                    )
                } else {
                    emailext(
                        subject: subject,
                        body: details,
                        to: 'dev-team+alerts@yourcompany.com',
                        attachLog: true
                    )
                }
            }
        }
    }
}