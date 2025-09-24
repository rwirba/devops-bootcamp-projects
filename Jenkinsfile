pipeline {
  agent { label 'infra-build-node' }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  parameters {
    booleanParam(name: 'SKIP_BUILD', defaultValue: true,  description: 'Skip building/pushing ezlearn image (use pre-pushed tag)')
    booleanParam(name: 'BOOTSTRAP_PV', defaultValue: false, description: 'Apply PV/PVC bootstrap (idempotent)')
    string(name: 'INGRESS_CLASS', defaultValue: 'nginx', description: 'Ingress class (traefik or nginx)')
    string(name: 'EZLEARN_HOST',  defaultValue: 'ezlearn-dev.mitechnology.org', description: 'Ingress host for ezlearn app')
    string(name: 'NEXUS_HOST',    defaultValue: 'nexus.mitechnology.org',       description: 'Ingress host for Nexus')
    string(name: 'SONAR_HOST',    defaultValue: 'sonarqube.mitechnology.org',   description: 'Ingress host for SonarQube')

    string(name: 'DOCKER_ORG',    defaultValue: 'mitechllc', description: 'DockerHub org/user that holds ezlearn image')
    string(name: 'EZLEARN_IMAGE', defaultValue: 'ezlearn',   description: 'Image name for ezlearn app')
    string(name: 'EZLEARN_TAG',   defaultValue: 'latest',    description: 'Tag to deploy for ezlearn app')

    string(name: 'K8S_CONTEXT',   defaultValue: '',          description: 'Optional kubectl context (from kubeconfig); leave blank to use current')
  }

  environment {
    CICD_NS = 'ezlearn-cicd-ns'
    DEV_NS  = 'ezlearn-dev-ns'
    CHARTS_DIR = 'charts'
    BOOTSTRAP_DIR = 'k8s/bootstrap'
    // Images for Nexus & Sonar (pulled from DockerHub)
    NEXUS_IMAGE = 'mitechllc/nexus3:latest'
    SONAR_IMAGE = 'mitechllc/sonarqube:community'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Install kubectl & helm if missing') {
      steps {
        sh '''
          set -e
          if ! command -v kubectl >/dev/null 2>&1; then
            curl -sSL -o /usr/local/bin/kubectl https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl
            chmod +x /usr/local/bin/kubectl
          fi
          if ! command -v helm >/dev/null 2>&1; then
            curl -sSL https://get.helm.sh/helm-v3.15.2-linux-amd64.tar.gz | tar -xz
            mv linux-amd64/helm /usr/local/bin/helm && rm -rf linux-amd64
          fi
          helm version && kubectl version --client
        '''
      }
    }

    stage('Kube auth') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig_ezlearn', variable: 'KCFG')]) {
          sh '''
            export KUBECONFIG="$KCFG"
            kubectl version --client
            kubectl get ns
          '''
          script {
            if (params.K8S_CONTEXT?.trim()) {
              sh "KUBECONFIG=$KCFG kubectl config use-context '${params.K8S_CONTEXT}'"
            }
          }
        }
      }
    }

    // stage('(Optional) Build & Push ezlearn image') {
    //   when { expression { !params.SKIP_BUILD } }
    //   steps {
    //     withCredentials([usernamePassword(credentialsId: 'dockerhub_creds', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
    //       sh '''
    //         set -e
    //         IMAGE="${DOCKER_ORG}/${EZLEARN_IMAGE}:${EZLEARN_TAG}"
    //         echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin
    //         # Expect Dockerfile at repo root or adjust path:
    //         docker build -t "$IMAGE" .
    //         docker push "$IMAGE"
    //         docker logout
    //       '''
    //     }
    //   }
    // }

    stage('Bootstrap PV/PVC (once)') {
      when { expression { return params.BOOTSTRAP_PV } }
      steps {
        withCredentials([file(credentialsId: 'kubeconfig_ezlearn', variable: 'KCFG')]) {
          sh '''
            set -e
            export KUBECONFIG="$KCFG"
            kubectl apply -f "${BOOTSTRAP_DIR}/pv-nexus-data.yaml"
            kubectl apply -f "${BOOTSTRAP_DIR}/pv-sonarqube-data.yaml"
            kubectl apply -f "${BOOTSTRAP_DIR}/pv-sonarqube-extensions.yaml"
            kubectl apply -f "${BOOTSTRAP_DIR}/pv-sonarqube-logs.yaml"
            kubectl apply -f "${BOOTSTRAP_DIR}/pv-tomcat-webapps.yaml"

            kubectl apply -f "${BOOTSTRAP_DIR}/pvc-nexus-data.yaml"
            kubectl apply -f "${BOOTSTRAP_DIR}/pvc-sonarqube.yaml"
            kubectl apply -f "${BOOTSTRAP_DIR}/pvc-tomcat-webapps.yaml"

            echo "PVCs in CICD NS:"
            kubectl -n ${CICD_NS} get pvc
            echo "PVCs in DEV NS:"
            kubectl -n ${DEV_NS} get pvc
          '''
        }
      }
    }

    stage('Helm Lint') {
      steps {
        sh '''
          helm lint ${CHARTS_DIR}/ezlearn-chart
          helm lint ${CHARTS_DIR}/nexus-chart
          helm lint ${CHARTS_DIR}/sonarqube-chart
        '''
      }
    }

    stage('Deploy CICD stack (Nexus & SonarQube)') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig_ezlearn', variable: 'KCFG')]) {
          sh '''
            set -e
            export KUBECONFIG="$KCFG"

            # --- Nexus ---
            helm upgrade --install nexus ${CHARTS_DIR}/nexus-chart \
              --namespace ${CICD_NS} --create-namespace \
              --set image.repository=${NEXUS_IMAGE%%:*} \
              --set image.tag=${NEXUS_IMAGE##*:} \
              --set ingress.className="${INGRESS_CLASS}" \
              --set ingress.host="${NEXUS_HOST}" \
              --set persistence.existingClaim="nexus-data-pvc"

            # --- SonarQube ---
            helm upgrade --install sonarqube ${CHARTS_DIR}/sonarqube-chart \
              --namespace ${CICD_NS} --create-namespace \
              --set image.repository=${SONAR_IMAGE%%:*} \
              --set image.tag=${SONAR_IMAGE##*:} \
              --set ingress.className="${INGRESS_CLASS}" \
              --set ingress.host="${SONAR_HOST}" \
              --set persistence.data.existingClaim="sonarqube-data-pvc" \
              --set persistence.extensions.existingClaim="sonarqube-extensions-pvc" \
              --set persistence.logs.existingClaim="sonarqube-logs-pvc"

            kubectl -n ${CICD_NS} get deploy,svc,ingress,pvc
          '''
        }
      }
    }

    stage('Deploy ezlearn App') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig_ezlearn', variable: 'KCFG')]) {
          sh '''
            set -e
            export KUBECONFIG="$KCFG"

            helm upgrade --install ezlearn ${CHARTS_DIR}/ezlearn-chart \
              --namespace ${DEV_NS} --create-namespace \
              --set image.repository=${DOCKER_ORG}/${EZLEARN_IMAGE} \
              --set image.tag=${EZLEARN_TAG} \
              --set ingress.className="${INGRESS_CLASS}" \
              --set ingress.host="${EZLEARN_HOST}" \
              --set persistence.existingClaim="tomcat-webapps-pvc"

            kubectl -n ${DEV_NS} get deploy,svc,ingress,pvc
          '''
        }
      }
    }
  }

  post {
    always {
      sh '''
        echo "----- CICD NS -----"
        kubectl -n ${CICD_NS} get pods
        echo "----- DEV NS -----"
        kubectl -n ${DEV_NS} get pods
      ''' 
    }
  }
}
