pipeline {
  agent { label 'infra-build-node' }

  stages {
    stage('Install Required Packages') {
      steps {
        sh '''
          sudo apt update
          sudo apt install python3-pip -y
          pip3 install boto3 botocore
        '''
      }
    }

    stage('Provision Infrastructure') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'jenkins-ec2-access',
          usernameVariable: 'AWS_ACCESS_KEY_ID',
          passwordVariable: 'AWS_SECRET_ACCESS_KEY'
        )]) {
          sh '''
            export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
            export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
            ansible-playbook -i inventory/aws_ec2.yml playbooks/site.yml
          '''
        }
      }
    }
  }

  post {
    success {
      echo 'Infrastructure provisioned successfully.'
    }
    failure {
      echo 'Build failed. Check logs for errors.'
    }
  }
}
