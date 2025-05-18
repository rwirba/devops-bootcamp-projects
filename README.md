# EzLearn-Class: CI/CD Bootcamp Project

This project provides a complete, end-to-end DevOps CI/CD pipeline for deploying a Java web application (`ezlearn`). It’s designed for DevOps bootcamp participants to gain real-world, production-level experience in automation, CI/CD, and infrastructure management — skills highly valued in technical interviews.

---

## 📦 Project Overview

- **Application Name:** `ezlearn`
- **Project Codebase:** `ezlearn-class`
- **Project Type:** Real-world CI/CD for Java WAR deployment

---

## 🧰 Tools and Roles

| Tool        | Ansible Role       | Description                                |
|-------------|--------------------|--------------------------------------------|
| Java        | `common`           | Installs OpenJDK 11                        |
| Maven       | `maven`            | Installs Apache Maven                      |
| Jenkins     | `jenkins_master`, `jenkins_slave` | Jenkins CI setup (Master + Agent) |
| SonarQube   | `sonarqube`        | Static code analysis and quality gate      |
| Nexus       | `nexus`            | Artifact repository for WAR file storage   |
| Tomcat      | `tomcat`           | Application server to host WAR             |
| Ansible     | n/a                | Automates provisioning and deployment      |
| Deployment  | `deploy_app`       | WAR file deployment to Tomcat              |

---

## 📁 Folder Structure

```
ezlearn-class/
├── inventory/
│   ├── nonprod/
│   └── prod/
│       └── hosts.ini
├── playbooks/
│   ├── bootstrap_jenkins.yml
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

## 🚀 Setup & Execution Steps

### ✅ Phase 1: Bootstrap Jenkins (Run Manually Once)

```bash
ansible-playbook -i inventory/prod/hosts.ini playbooks/bootstrap_jenkins.yml
```

### ✅ Phase 2: Access Jenkins UI

1. Visit: `http://<jenkins-master-ip>:8080`
2. Unlock Jenkins:

```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

3. Install plugins and set admin user
4. Configure Jenkins Slave Node (label: `builder`)

---

### ✅ Phase 3: Infrastructure Setup from Jenkins

#### Option 1: Freestyle Job

```bash
ansible-playbook -i inventory/prod/hosts.ini playbooks/site.yml
```

#### Option 2: Jenkins Pipeline

```groovy
pipeline {
  agent { label 'builder' }

  stages {
    stage('Provision Infrastructure') {
      steps {
        sh 'ansible-playbook -i inventory/prod/hosts.ini playbooks/site.yml'
      }
    }
  }
}
```

---

### ✅ Phase 4: Deploy Application

```bash
ansible-playbook -i inventory/prod/hosts.ini playbooks/deploy.yml
```

Deploys `ezlearn.war` to Tomcat and restarts the service.

---

## 🌐 CI/CD Architecture Diagram

```
+---------------------------+
|     Developer (GitHub)    |
+------------+-------------+
             |
             v (Push Code)
   +---------+---------+
   |     Jenkins Master |
   |  - Pulls code      |
   |  - Triggers build   |
   +---------+----------+
             |
             v
   +---------------------+         +-------------------+
   |  Jenkins Slave Node | ----->  |   SonarQube Server |
   |  - Builds WAR       |         |   - Static Analysis|
   +---------+-----------+         +---------+---------+
             |                               |
             |                               v
             |                    +-------------------+
             |                    |   Nexus Repository |
             |                    | - Stores WAR       |
             |                    +-------------------+
             |
             v
     +-------------------------+
     |   Ansible Controller    |
     | - Deploys WAR to Tomcat |
     +-------------------------+
             |
             v
     +-------------------------+
     |     Tomcat Server       |
     | - Hosts ezlearn.war     |
     +-------------------------+
             |
             v
     +-------------------------+
     |    app.ezlearn.com      |
     | (Routed via Cloudflare) |
     +-------------------------+
```

---

## ✅ Useful Commands

| Task                        | Command |
|-----------------------------|---------|
| Bootstrap Jenkins setup     | `ansible-playbook -i inventory/prod/hosts.ini playbooks/bootstrap_jenkins.yml` |
| Provision Infra (via Jenkins)| `ansible-playbook -i inventory/prod/hosts.ini playbooks/site.yml` |
| Deploy WAR manually         | `ansible-playbook -i inventory/prod/hosts.ini playbooks/deploy.yml` |

---

## 💡 Best Practices

- Use `ansible.builtin.*` modules only
- Configure SSH trust between Jenkins master ↔ slave
- Secure Jenkins, SonarQube, Nexus using NGINX + SSL (optional)
- Use Cloudflare for DNS routing and optional WAF/CDN

---

## 🎯 Learning Outcomes

- CI/CD pipeline automation with Jenkins + Ansible
- Infrastructure as Code (IaC) using Ansible roles
- Artifact management with Nexus
- Quality control using SonarQube
- WAR deployment on Tomcat
- Production-grade DevOps experience

---
