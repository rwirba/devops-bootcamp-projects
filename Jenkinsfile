pipeline {
  agent { label 'infra-build-node' }

  environment {
    AWS_DEFAULT_REGION = 'us-east-1'
    DYNATRACE_API_TOKEN = credentials('DYNATRACE_API_TOKEN')
    DYNATRACE_ENV_URL = 'https://djd77724.live.dynatrace.com'
  }

  stages {
    stage('set up one agaent') {
      steps {
        sshagent(credentials: ['ssh-agent-key']) {
          withCredentials([
            usernamePassword(
              credentialsId: 'jenkins-ec2-access',
              usernameVariable: 'AWS_ACCESS_KEY_ID',
              passwordVariable: 'AWS_SECRET_ACCESS_KEY'
            )
          ]) {
            sh '''
              ansible-playbook -i inventory/aws_ec2.yml install_one_agent.yml
            '''
          }
        }
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'inventory.json', allowEmptyArchive: true
      cleanWs()
    }
    success {
      echo 'Dynatrace provisioned successfully.'
    }
    failure {
      echo 'Build failed. Check archived inventory.json for details.'
    }
  }
}