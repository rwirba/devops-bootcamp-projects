# EzLearn-Class CI/CD Bootcamp Project

This project provides a full-scale CI/CD pipeline setup for a Java web application (`ezlearn`) using Jenkins, Ansible, Nexus, SonarQube, and Tomcat. Designed as a hands-on project for bootcamp participants, it enables learners to develop real-world DevOps skills applicable in job environments.

---

## Project Overview

- **Application Name:** `ezlearn`
- **Project Name:** `ezlearn-class`

### Tools and Roles

| Tool         | Role Name            | Purpose                                      |
|--------------|----------------------|----------------------------------------------|
| Java         | `common`             | Installs OpenJDK 11                          |
| Maven        | `maven`              | Installs Maven                               |
| Jenkins      | `jenkins_master`, `jenkins_slave` | CI/CD automation with master/agent setup |
| Ansible      | N/A                  | Configuration management and provisioning    |
| SonarQube    | `sonarqube`          | Static code analysis                         |
| Nexus        | `nexus`              | Artifact repository and versioning           |
| Tomcat       | `tomcat`             | Application server for WAR deployment        |
| Deployment   | `deploy_app`         | Deploys WAR to Tomcat                        |

---

## Folder Structure

```
ezlearn-class/
├── inventory/
│   ├── nonprod/
│   └── prod/
│       └── hosts.ini
├── playbooks/
│   ├── jenkins_install.yml
│   ├── site.yml
│   └── deploy.yml
├── roles/
│   ├── common/
│   ├── maven/
│   ├── jenkins_master/
│   ├── jenkins_slave/
│   ├── sonarqube/
│   ├── nexus/
│   ├── tomcat/
│   └── deploy_app/
```

---

## Step-by-Step Setup Guide

### Phase 1: Provision EC2 Infrastructure

1. **Create AWS Key Pair:**  
   - Download the key file locally.

2. **Launch EC2 Instances (Ubuntu 22.04):**  
   - 1 Jenkins Master  
   - 1 Jenkins Slave  
   - 1 SonarQube  
   - 1 Nexus  
   - 1 Tomcat

3. **Tag Each Instance:**  
   - `Name = jenkins-master`  
   - `Name = jenkins-slave`  
   - `Name = sonarqube`  
   - `Name = nexus`  
   - `Name = tomcat`

4. **Create Security Groups:**  
   - **All Instances:** Allow SSH (Port 22)  
   - **Jenkins:** Port 8080  
   - **SonarQube:** Port 9000  
   - **Nexus:** Port 8081  
   - **Tomcat:** Port 8080 or 8081

5. **Update OS Packages on All Instances:**

   ```bash
   sudo apt update && sudo apt upgrade -y
   ```

---

### Phase 2: Jenkins Slave Setup

1. **Install Java and Ansible on Jenkins Slave:**

   ```bash
   sudo apt install fontconfig openjdk-21-jre -y
   sudo apt install software-properties-common -y
   sudo add-apt-repository --yes --update ppa:ansible/ansible
   sudo apt install ansible -y
   ```

2. **Create Jenkins User (Slave Node):**

   ```bash
   sudo useradd -m -s /bin/bash jenkins
   ```

3. **Enable Passwordless Sudo for Jenkins:**

   ```bash
   sudo visudo
   ```

   Append this line:

   ```
   jenkins ALL=(ALL) NOPASSWD:ALL
   ```

---

### Phase 3: Setup Dynamic Inventory

1. **Create IAM User in AWS Console:**  
   - Name: `jenkins-ec2-access`  
   - Enable programmatic access  
   - Attach policy: `AmazonEC2ReadOnlyAccess`  
   - Save Access Key ID & Secret Access Key

2. **Store AWS Credentials in Jenkins:**  
   - Install: `Credentials Plugin`, `Credentials Binding Plugin`  
   - Jenkins → Manage → Credentials → (Global) → Add  
   - Kind: `Username and Password`  
   - ID: `jenkins-ec2-access`  
   - Username: `<AWS_ACCESS_KEY>`  
   - Password: `<AWS_SECRET_KEY>`

---

### Phase 4: Jenkins Master Setup

1. **Install Jenkins on Jenkins Master:**

   ```bash
   sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
   echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
   sudo apt-get update
   sudo apt-get install jenkins -y
   sudo apt install fontconfig openjdk-21-jre -y
   sudo systemctl enable jenkins
   sudo systemctl start jenkins
   ```

2. **Access Jenkins UI:**  
   - Visit: `http://<jenkins-master-ip>:8080`  
   - Retrieve initial password:

     ```bash
     sudo cat /var/lib/jenkins/secrets/initialAdminPassword
     ```

   - Install suggested plugins and create admin user

---

### Phase 5: Add Jenkins Slave Agent

1. **Add New Node:**  
   - Jenkins → Manage → Nodes → New Node  
   - Name: `infra-build-node`  
   - Type: Permanent Agent  
   - Remote Root: `/home/jenkins`  
   - Launch Method: SSH via key

2. **Add Credentials:**  
   - Kind: SSH Username with private key  
   - Username: `ubuntu`  
   - Private Key: contents of your PEM file

3. **Generate SSH Key Pair on Jenkins Master:**

   ```bash
   sudo -u jenkins ssh-keygen -t rsa
   ```

   Copy public key to the slave’s `~/.ssh/authorized_keys` for user `jenkins`.

4. **Verify SSH Connection:**

   ```bash
   sudo -u jenkins ssh jenkins@<slave-ip>
   ```

5. **Update Jenkins with Master’s Private Key:**  
   - Go to Credentials  
   - Add new: SSH Username with private key (`jenkins`)  
   - Paste contents of: `/var/lib/jenkins/.ssh/id_rsa`

---

### Phase 6: Trigger CI/CD Pipeline via Jenkins

1. **Create Jenkins Pipeline Job:**  
   - Name: `infrastructure-setup`  
   - Type: Pipeline  
   - Definition: Pipeline script from SCM  
   - SCM: Git  
   - Repo URL: `<your_git_repo_url>`  
   - Branch: `*/project-1-cicd`  
   - Script Path: `Jenkinsfile`

2. **Run the Job:**  
   - Click **Build Now**  
   - This will provision:
     - SonarQube
     - Nexus
     - Tomcat

---

## ✅ Setup Complete

Your infrastructure is now ready to support a full CI/CD lifecycle. Customize playbooks, roles, and Jenkinsfiles as needed to deploy and manage the `ezlearn` Java application.
