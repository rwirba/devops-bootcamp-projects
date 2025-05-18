# EzLearn-Class CI/CD Bootcamp Project

This project provides a complete, production-grade CI/CD pipeline setup for a Java web application (`ezlearn`) using Jenkins, Ansible, Nexus, SonarQube, Tomcat, and other DevOps tools. It is structured to enable bootcamp participants to learn and demonstrate real-world skills for job interviews.

---

##  Project Overview

### Application Name: `ezlearn`
### Project Name: `ezlearn-class`

### Tools & Roles Used:

| Tool         | Role Name         | Purpose                                      |
|--------------|-------------------|----------------------------------------------|
| Java         | `common`          | Installs OpenJDK 11                          |
| Maven        | `maven`           | Installs Maven                               |
| Jenkins      | `jenkins_master`, `jenkins_slave` | Installs Jenkins and configures master/agent |
| Ansible      | n/a               | Configuration management and provisioning    |
| SonarQube    | `sonarqube`       | Static code analysis                         |
| Nexus        | `nexus`           | Artifact storage and versioning              |
| Tomcat       | `tomcat`          | App server for WAR deployment                |
| Deployment   | `deploy_app`      | Deploys the WAR to Tomcat                    |

---

##  Folder Structure

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

### Step 1: Create a keypair from aws console and download to your local PC and then Launch EC2 Ubuntu Servers (AWS)
You will need **5 Ubuntu 22.04 EC2 instances**:
- 1 for Jenkins Master
- 1 for Jenkins Slave
- 1 for SonarQube
- 1 for Nexus
- 1 for Tomcat

#### Security Group Configuration:
Allow the following ports on **all servers**:
- Port 22: SSH

Allow the following **where needed**:
- Jenkins: 8080
- SonarQube: 9000
- Nexus: 8081
- Tomcat: 8080 or 8081

###  Step 2: Connect to EC2 Instances via SSH
On your local machine (Mac/Linux/Windows Git Bash):

```bash
ssh -i /path/to/your-key.pem ubuntu@<public-ip-address>
```

Repeat this for each instance. Update system packages:

```bash
sudo apt update && sudo apt upgrade -y
```

### Step 3: Install Git and Clone the Project
On the instance where you want to run Ansible:

```bash
sudo apt install git -y
git clone https://github.com/yourusername/ezlearn-class.git
cd ezlearn-class
```

###  Step 4: Install Ansible

```bash
sudo apt install software-properties-common -y
sudo add-apt-repository --yes --update ppa:ansible/ansible
sudo apt install ansible -y
ansible --version
```

---

## Execution Steps (Automated with Ansible)

###  Phase 1: Jenkins and Slave (Run manually)

```bash
ansible-playbook -i inventory/prod/hosts.ini playbooks/jenkins_install.yml
```

- This installs Jenkins on the master
- Sets up Java, Maven, SSH, and agent on the slave

### Phase 2: Configure Jenkins UI

1. Open your browser and visit Jenkins `http://<jenkins-master-ip>:8080`

2. On first load, Jenkins will ask for the administrator password. Run this command on your Jenkins master server:

```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

3. Copy the password shown and paste it into the Jenkins setup screen.

4. When prompted, choose Install suggested plugins.

5. Create your admin username and password, then finish setup.

6. To add a Jenkins slave node:

   - Go to Manage Jenkins → Manage Nodes → New Node

   - Name: builder, click Permanent Agent, then click OK

   - Fill in the form:

      Remote root directory: /home/jenkins

      Labels: builder

      Launch method: Launch agent via SSH

      Host: IP address of the slave node

      Credentials: Add credentials with username jenkins and private key from Jenkins master

 - Click Save

 7. Jenkins will try to connect to the agent. If the agent status shows Connected, your slave is ready.

      Name: builder

      Use SSH to connect (user: jenkins)

      Remote root: /home/jenkins

###  Phase 3: Provision All Tools from Jenkins Job

From Jenkins UI:

- Create a Freestyle or Pipeline job

# How to Create a Jenkins Job:

  - From Jenkins Dashboard, click New Item
  
  - Enter an item name: infrastructure-setup
  
  - Select Freestyle project and click OK
  
  - Scroll to Build Environment, tick "Use secret text(s) or file(s)" if needed
  
  - Scroll to Build → Click Add build step → Execute shell

Paste the command below:
```bash
ansible-playbook -i inventory/prod/hosts.ini playbooks/site.yml
```
- Click save 
- click Build Now

This will install:
- SonarQube
- Nexus
- Tomcat (App Server)

### Phase 4: Deploy the Application

Once Jenkins builds the `ezlearn.war`, deploy it:

```bash
ansible-playbook -i inventory/prod/hosts.ini playbooks/deploy.yml
```

---

## Architecture Diagram

![CI/CD Architecture](docs/architecture.png)

---

## Sample Commands

Provision Jenkins Only:

```bash
ansible-playbook -i inventory/prod/hosts.ini playbooks/bootstrap_jenkins.yml
```

Run Full Setup via Jenkins:

```bash
ansible-playbook -i inventory/prod/hosts.ini playbooks/site.yml
```

Deploy WAR:

```bash
ansible-playbook -i inventory/prod/hosts.ini playbooks/deploy.yml
```

---

##  Bonus Tips

- Use `ansible.builtin.*` modules only (dependency-free)
- Store Jenkins SSH key in `~/.ssh` and copy to slave using Ansible
- Default ports: Jenkins (8080), SonarQube (9000), Nexus (8081), Tomcat (8080/8081)
- Future enhancement: add HTTPS + NGINX for SSL termination

---

## Learning Outcome

By completing this project, you will confidently demonstrate:
- CI/CD automation using Jenkins and Ansible
- Artifact lifecycle with Maven and Nexus
- Static analysis with SonarQube
- Infrastructure automation and provisioning
- Real-world DevOps practices suitable for production

For any issues, contributions, or feedback, reach out to the bootcamp lead.

Happy Learning 
