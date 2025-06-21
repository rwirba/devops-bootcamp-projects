pipeline {
  agent { label 'infra-build-node' }

  environment {
    AWS_DEFAULT_REGION = 'us-east-1'  
  }

  stages {
    stage('Install Required Packages') {
      steps {
        sh '''
          sudo apt update
          sudo apt install python3-pip -y
          pip3 install boto3 botocore
          ansible-galaxy collection install amazon.aws
        '''
      }
    }

    stage('Provision Infrastructure') {
      steps {
        sshagent(credentials: ['jenkins-ssh-key']) {
            withCredentials([usernamePassword(
                credentialsId: 'jenkins-ec2-access',
                usernameVariable: 'AWS_ACCESS_KEY_ID',
                passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                )]) {
                sh '''
                   # Debug AWS environment
                   echo "=== AWS DEBUG INFO ==="
                   echo "AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID:0:4}...${AWS_ACCESS_KEY_ID: -4}"
                   echo "AWS_DEFAULT_REGION: $AWS_DEFAULT_REGION"
                   env | grep AWS
    
                   # Debug Python/Ansible environment
                   echo "=== PYTHON PATHS ==="
                   python3 -c "import boto3; print(boto3.__version__); print(boto3.Session().get_credentials().access_key)"
                   ansible --version
    
                   # Test AWS connectivity directly
                   echo "=== AWS API TEST ==="
                   AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
                   aws sts get-caller-identity --region us-east-1
    
                   # Test inventory generation
                   echo "=== INVENTORY TEST ==="
                   AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
                   ansible-inventory -i inventory/prod/aws_ec2.yml --list --export
    
                   # Now run playbook
                  ansible-playbook -i inventory/prod/aws_ec2.yml playbooks/site.yml -vvv
                '''
                sh '''
                    export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
                    export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
                    ansible-playbook -i inventory/prod/aws_ec2.yml playbooks/site.yml -vv
                '''
            }    
        }
      }
    }
  }

  post {
    always {
        cleanWs()
    }
    success {
      echo 'Infrastructure provisioned successfully.'
    }
    failure {
      echo 'Build failed. Check logs for errors.'
    }
  }
}
