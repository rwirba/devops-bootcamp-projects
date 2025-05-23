pipeline {
  agent { label 'infra-build-node' }

  environment {
    // Injected using Credentials Binding plugin (Username with password)
    AWS_ACCESS_KEY_ID     = credentials('jenkins-ec2-access')
    AWS_SECRET_ACCESS_KEY = credentials('jenkins-ec2-access')
  }

  stages {
    stage('Clone Repo') {
      steps {
        git branch: 'project-1-cicd',
            url: 'https://github.com/rwirba/devops-bootcamp-projects.git'
        }
    }

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
        sh '''
          export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
          export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
          ansible-playbook -i inventory/prod/aws_ec2.yml playbooks/site.yml
        '''
      }
    }
  }
  post {
    success {
      echo '✅ Infrastructure provisioned and application deployed successfully.'
    }
    failure {
      echo '❌ Build failed. Check logs for errors.'
    }
  }
}
