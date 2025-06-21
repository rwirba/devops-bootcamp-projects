pipeline {
  agent { label 'infra-build-node' }

  environment {
    AWS_DEFAULT_REGION = 'us-east-1'
  }

  stages {
    stage('Install Required Packages') {
      steps {
        sh '''
          sudo apt update -y
          sudo apt install -y python3-pip awscli
          pip3 install --upgrade boto3 botocore
          ansible-galaxy collection install amazon.aws --force
        '''
      }
    }

    stage('Provision Infrastructure') {
      steps {
        sshagent(credentials: ['jenkins-ssh-key']) {
          withCredentials([
            usernamePassword(
              credentialsId: 'jenkins-ec2-access',
              usernameVariable: 'AWS_ACCESS_KEY_ID',
              passwordVariable: 'AWS_SECRET_ACCESS_KEY'
            )
          ]) {
            sh '''
              # Debug AWS environment
              echo "=== ENVIRONMENT ==="
              echo "AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID:0:4}...${AWS_ACCESS_KEY_ID: -4}"
              echo "AWS_DEFAULT_REGION: $AWS_DEFAULT_REGION"
              
              # Verify Python environment
              echo "=== PYTHON ENV ==="
              python3 -c "import boto3; print(f'Boto3: {boto3.__version__}')"
              ansible --version
              
              # Test AWS connectivity
              echo "=== AWS CONNECTION TEST ==="
              aws sts get-caller-identity || { echo "AWS Auth Failed!"; exit 1; }
              
              # Test inventory generation
              echo "=== INVENTORY TEST ==="
              ansible-inventory -i inventory/prod/aws_ec2.yml --list --output inventory.json
              jq . < inventory.json | head -n 20
              
              # Run playbook with debug
              echo "=== EXECUTING PLAYBOOK ==="
              ansible-playbook -i inventory/prod/aws_ec2.yml playbooks/site.yml -vvv
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
      echo 'Infrastructure provisioned successfully.'
    }
    failure {
      echo 'Build failed. Check archived inventory.json for details.'
    }
  }
}
