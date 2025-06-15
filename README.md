# EzLearn-Class CI/CD Bootcamp Project

This project provides a full-scale CI/CD pipeline setup for a Java web application (`ezlearn`) using Jenkins, Ansible, Nexus, SonarQube, and Tomcat. Designed as a hands-on project for bootcamp participants, it enables learners with **no prior DevOps experience** to develop real-world skills in infrastructure provisioning, configuration management, CI/CD, and application deployment.

---

## Project Overview

- **Application Name:** `ezlearn`
- **Project Name:** `ezlearn-class`

### Tools and Roles

| Tool         | Role Name            | Purpose                                      |
|--------------|----------------------|----------------------------------------------|
| Java         | `common`             | Installs OpenJDK 11                          |
| Maven        | `maven`              | Installs Maven for Java build                |
| Jenkins      | `jenkins_master`, `jenkins_slave` | Automates builds, testing, deployments       |
| Ansible      | N/A                  | Automates server configuration and provisioning |
| SonarQube    | `sonarqube`          | Analyzes code quality and security           |
| Nexus        | `nexus`              | Stores and versions application artifacts    |
| Tomcat       | `tomcat`             | Hosts the web application                    |
| Deployment   | `deploy_app`         | Deploys the WAR file to Tomcat server        |

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

---

### Phase 1: Provision EC2 Infrastructure to install all required tools in target environments

1. **Login to AWS Console**  
   Go to https://console.aws.amazon.com and sign in and create iam user and give console access.

2. **Create a Key Pair**  
   - Go to EC2 → Key Pairs → Create Key Pair  
   - Name it (e.g., `devops-key`) and download the `.pem` file.  
   - This key allows you to SSH into your servers.

3. **Launch 5 EC2 Instances (Ubuntu 22.04)**  
   - Instance type: `t2.medium`  
   - Image: Ubuntu Server 22.04  
   - Instances needed:  
     - Jenkins Master  
     - Jenkins Slave (Agent)  
     - SonarQube Server  
     - Nexus Repository  
     - Tomcat Application Server

4. **Tag Instances for Identification**  
   - Jenkins Master → `Name = jenkins-master`  
   - Jenkins Slave → `Name = jenkins-slave`  
   - SonarQube → `Name = sonarqube`  
   - Nexus → `Name = nexus`  
   - Tomcat → `Name = tomcat`

5. **Configure Security Groups**  
   Allow these ports:  
   - Port 22: SSH for all  
   - Port 8080: Jenkins & Tomcat  
   - Port 9000: SonarQube  
   - Port 8081: Nexus

6. **Connect via SSH**  
   From your terminal:

   ```bash
   ssh -i /path/to/devops-key.pem ubuntu@<EC2-PUBLIC-IP>
   ```

7. **Update Servers**

   ```bash
   sudo apt update && sudo apt upgrade -y
   ```

---

### Phase 2: Setup Jenkins Slave the Agent to run Ansible + pipeline jobs

1. **Install Java & Ansible on Jenkins Slave**

   ```bash
   sudo apt install fontconfig openjdk-21-jre -y
   sudo apt install software-properties-common -y
   sudo add-apt-repository --yes --update ppa:ansible/ansible
   sudo apt install ansible -y
   ```

2. **Create Jenkins User on slave**

   ```bash
   sudo mkdir -p /home/jenkins/.ssh
   sudo touch /home/jenkins/.ssh/authorized_keys
   sudo chown -R jenkins:jenkins /home/jenkins/.ssh
   sudo chmod 700 /home/jenkins/.ssh
   sudo chmod 600 /home/jenkins/.ssh/authorized_keys
   echo "jenkins ALL=(ALL) NOPASSWD:ALL" | sudo tee /etc/sudoers.d/jenkins
   ```

3. **Allow Jenkins Passwordless Sudo**

   ```bash
   sudo visudo
   ```

   Add this line at the end:

   ```
   jenkins ALL=(ALL) NOPASSWD:ALL
   ```

---

### Phase 3: Setup Ansible Dynamic Inventory so Ansible can discover EC2 servers automatically

1. **Create IAM User in AWS Console**

   - IAM → Users → Add user  
   - Name: `jenkins-ec2-access`  
   - Enable **programmatic access**  
   - Attach policy: `AmazonEC2ReadOnlyAccess`  
   - Create and **download Access Key ID and Secret**

2. **Store Credentials in Jenkins**

   - Install **Credentials** and **Credentials Binding** plugins  
   - Go to: Jenkins → Manage → Credentials → Global → Add Credentials  
   - Kind: `Username with password`  
   - ID: `jenkins-ec2-access`  
   - Username: AWS Access Key  
   - Password: AWS Secret Key

---

### Phase 4: Install Jenkins on Master  Jenkins will drive all automation

1. **Install Jenkins**

   ```bash
   sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
   sudo echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
   sudo apt update
   sudo apt install jenkins -y
   ```

2. **Install Java**

   ```bash
   sudo apt install fontconfig openjdk-21-jre -y
   ```

3. **Enable & Start Jenkins**

   ```bash
   sudo systemctl enable jenkins
   sudo systemctl start jenkins
   ```

4. **Access Jenkins UI**

   - Visit: `http://<Jenkins-IP>:8080`  
   - Get the unlock password:

     ```bash
     sudo cat /var/lib/jenkins/secrets/initialAdminPassword
     ```

   - Install suggested plugins and create admin user.
   - Install ssh agent plugin

---

### Phase 5: Connect Jenkins Slave ( Delegate workload to slave agent)

1. **Generate SSH Keys on Jenkins Master**

   ```bash
   sudo -u jenkins ssh-keygen -t ed25519 -f /var/lib/jenkins/.ssh/id_ed25519 -N ""
   ```

2. **Add Key to Slave’s `jenkins` User**

   ```bash
   ssh-copy-id -i /var/lib/jenkins/.ssh/id_rsa.pub jenkins@<slave-ip>
   ```

3. **Add Jenkins Node in UI**

   - Jenkins → Manage Nodes → New Node  
   - Name: `infra-build-node`  
   - Launch via SSH  
   - Remote root: `/home/jenkins`  
   - Credentials: Add SSH key from `/var/lib/jenkins/.ssh/id_rsa`

---

### Phase 6: Provision DevOps Tools Automatically

1. **Create Jenkins Pipeline Job**

   - Name: `infrastructure-setup`  
   - Type: Pipeline  
   - Definition: Pipeline script from SCM  
   - SCM: Git  
   - Repo URL: `https://github.com/<your-org>/<repo>`  
   - Branch: `*/project-1-cicd`  
   - Script Path: `Jenkinsfile`

2. **Run the Job**

   - Jenkins will use Ansible to provision:\n
     - SonarQube (port 9000)  
     - Nexus (port 8081)  
     - Tomcat (port 8080)

---

## ✅ Final Outcome

After successful execution:

- Jenkins is set up with a connected slave
- SonarQube is accessible for code analysis
- Nexus is hosting artifacts
- WAR files are deployed to Tomcat
- Jenkins pipelines automate the full lifecycle from build to deployment

---

🎯 **Next Steps:**

- Add SonarQube analysis in your Jenkinsfile
- Deploy custom WAR from Maven build to Tomcat
- Schedule regular builds with polling
- Add unit tests and reports to Jenkins
