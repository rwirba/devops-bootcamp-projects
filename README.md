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

** Your EC2 instances should have tags like

Name = jenkins-slave
Name = sonarqube
Name = nexus
Name = tomcat


#### Create 4 Security Groups:
Allow the following ports on **all servers**:
- Port 22: SSH


Allow the following for the respective servers and open ports for them
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

###  Step 4: Install Ansible on Jenkins Slave only 

Jenkins jobs will run on the slave node (agent), so Ansible must be installed on the Jenkins Slave where jobs are executed. Connect to your slave node and run:

```bash
sudo apt install fontconfig openjdk-21-jre -y
sudo apt install software-properties-common -y
sudo add-apt-repository --yes --update ppa:ansible/ansible
sudo apt install ansible -y
ansible --version

```

Give jenkins Passwordless Sudo (Best for CI/CD)
On your Jenkins slave EC2 instance, Run the following command

```bash
sudo update-alternatives --config editor    # To always use vim for editing sudoers
```

You’ll see options like:

There are 3 choices for the alternative editor (providing /usr/bin/editor).

  Selection    Path                Priority   Status
------------------------------------------------------------
* 0            /bin/nano            40        auto mode
  1            /usr/bin/vim.basic   30        manual mode
  2            /usr/bin/vim.tiny    15        manual mode

Press enter to keep the current choice[*], or type selection number: 3

Enter the number for vim.basic or vim and press Enter.

Then run:

```bash
sudo visudo
```
Add this line at the end:

jenkins ALL=(ALL) NOPASSWD:ALL

Save and exit

This will enable sudo not to prompt for a password in Jenkins jobs.


---
# We will not be hard codeing any values in playbook so we need dynamic inventory

Create IAM User for Ansible Dynamic Inventory

Step 1: Login to AWS Console
Go to https://console.aws.amazon.com/iam

On the left sidebar, click Users → Add users

🔹 Step 2: Create IAM User
User name: jenkins-ec2-access

Access type: ✅ Programmatic access (check this)

Click Next: Permissions

🔹 Step 3: Attach Permissions
Select:
✅ Attach existing policies directly

Search and attach:

AmazonEC2ReadOnlyAccess

Click Next → Next → Create user

🔹 Step 4: Save Credentials
Once created, copy and save:

Access Key ID

Secret Access Key

You'll use these in Jenkins.

✅ PART 2: Store AWS Credentials in Jenkins (Securely)
🔹 Step 1: Install Plugins (if not installed)
From Jenkins Dashboard:

Go to Manage Jenkins → Plugins

Install:

Credentials Plugin

Credentials Binding Plugin

🔹 Step 2: Add AWS Credentials to Jenkins
Go to Manage Jenkins → Credentials → (Global)

Click Add Credentials

Fill the form like this:

Kind:  → select “AWS Credentials”

ID: jenkins-aws-creds

Access Key ID: your_access_key_id

Secret Access Key: your_secret_access_key


Description: AWS creds for Ansible EC2 dynamic inventory

Click Create

✅ PART 3: Inject These Credentials in Your Jenkins Job


Click on configure

Go to Build Environment

✅ Check “Use secret text(s) or file(s)”

Click Add:

Bindings → “Username and Password (separated)”

Select the credential you created (aws-ec2-credentials)

Set:

Username Variable: AWS_ACCESS_KEY_ID

Password Variable: AWS_SECRET_ACCESS_KEY

In Build → Execute shell, use:

```bash

export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY

ansible-playbook -i inventory/aws_ec2.yml playbooks/site.yml
```
You can test inventory manually:
```bash
ansible-inventory -i inventory/aws_ec2.yml --list
```
## Execution Steps (Automated with Ansible)

###  Phase 1: Jenkins and Slave (Run manually)

Install Jenkins manually on the Jenkins Master EC2 instance:

# Run these commands on Jenkins master node:

```bash
sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt-get update
sudo apt-get install jenkins
sudo apt update
sudo apt install fontconfig openjdk-21-jre
sudo systemctl enable jenkins
sudo systemctl start jenkins
sudo systemctl status jenkins
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

# Run the follow command on jenkins master to generate keys and authenticate master and slave node

 ```bash
sudo su - jenkins
ssh-keygen -t rsa -b 4096 -C "jenkins@slave" -N "" -f ~/.ssh/id_rsa
```

This creates:

~/.ssh/id_rsa → private key

~/.ssh/id_rsa.pub → public key

- Show the public key: run the following command

```bash
cat /var/lib/jenkins/.ssh/id_rsa.pub
```

# On Jenkins Slave (as ubuntu or root)
Create the jenkins user if not already:

```bash
sudo useradd -m -s /bin/bash jenkins
sudo mkdir -p /home/jenkins/.ssh
sudo vim /home/jenkins/.ssh/authorized_keys
```
Paste the contents of the master's id_rsa.pub into that file.

Set correct permissions:
```bash
sudo chown -R jenkins:jenkins /home/jenkins/.ssh
sudo chmod 700 /home/jenkins/.ssh
sudo chmod 600 /home/jenkins/.ssh/authorized_keys
```

# Test the SSH Connection

Back on the Jenkins master as jenkins user:
```bash
ssh jenkins@<slave-ip>
```

# Next Update Jenkins Credential
Go to Manage Jenkins → Credentials → (global) → your slave credential

Click Add Credentials:

Kind: SSH Username with private key

Username: jenkins

Private Key: Select “Enter directly” → paste contents of:
cat /var/lib/jenkins/.ssh/id_rsa

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
  
  - Select pipeline and click OK
  
  - Scroll to Pipeline, Select Pipeline script from SCM

  - SCM = Select Git

  - Repositories =  Copy your git repo and insert on Repository URL

  - Next step Branches to Build

  - Select Add Branch

  - Change */master to */project-1-cicd

  - Script Path = by dafault you'll see Jenkinsfile. Don't change it
  
  - Scroll down and click save

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
