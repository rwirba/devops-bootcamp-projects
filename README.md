# DevOps Bootcamp Projects


# Jenkins Pipeline Setup and Application Deployment

This guide walks you through the full setup of a CI/CD pipeline using Jenkins to build, test, analyze, publish, and deploy a Java web application. This is written for absolute beginners in DevOps.

---

## 📦 Prerequisites

Before you begin, ensure you have:

- A running **Jenkins server** with at least 1 build node (agent).
- Jenkins node label: `infra-build-node`
- Jenkins credentials set up:
  - **SSH Key** to access the deployment server (`ssh-agent-key`)
  - **Username/Password** to push artifacts to Nexus (`nexus-creds`)
- Access to:
  - **SonarQube** (e.g. http://sonarqube.mitechnology.org:9000)
  - **Nexus** (e.g. http://nexus.mitechnology.org:8081)
  - **Tomcat Server** deployed at (e.g. http://tomcat.mitechnology.org:8080)
- A `GitHub` repository with a branch called `app-release`
- Java 17 and Maven installed on the Jenkins agent

---

## 🚀 Step 1: Create Jenkins Pipeline Job

1. Login to Jenkins.
2. Click on **"New Item"**.
3. Name the job `app-deploy`.
4. Choose **"Pipeline"** and click OK.
5. Scroll down to **Pipeline** section:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: Your GitHub repo
   - Branch: `app-release`
   - Script Path: `Jenkinsfile` (assuming it is in the root of the repo)
6. Click **Save**.

---

## 🔧 Jenkinsfile Explained (Step-by-Step)

The Jenkinsfile defines the automated pipeline process. Here's what each stage does:

### 🧠 Agent

```groovy
agent { label 'infra-build-node' }
