pipeline {
  agent { label 'builder' }

  environment {
    // Injected using Credentials Binding plugin (Username with password)
    AWS_ACCESS_KEY_ID     = credentials('jenkins-ec2-access')
    AWS_SECRET_ACCESS_KEY = credentials('jenkins-ec2-access')
  }

  stages {
    stage('Clone Repo') {
      steps {
        git branch: 'project-1-cicd',
            url: 'https://github.com/rwirba/devops-bootcamp-projects.git',
            credentialsId: 'github-pat' // Optional: only needed if your repo is private
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
          ansible-playbook -i inventory/aws_ec2.yml playbooks/site.yml
        '''
      }
    }

    stage('Deploy Application') {
      steps {
        sh '''
          export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
          export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
          ansible-playbook -i inventory/aws_ec2.yml playbooks/deploy.yml
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
