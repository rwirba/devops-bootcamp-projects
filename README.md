# SonarQube Integration with Jenkins for `ezlearn` Project

This guide documents the full configuration of SonarQube to enforce code quality standards through Jenkins pipelines for the `ezlearn` project. It includes setting up a custom quality gate, assigning it to the project, configuring analysis parameters, and ensuring compliance with security and coverage requirements.

---

## 🌐 SonarQube URL
> [http://sonarqube.mitechnology.org:9000/dashboard?id=ezlearn](http://sonarqube.mitechnology.org:9000/dashboard?id=ezlearn)

---

## 🔧 Prerequisites

- Jenkins is up and running.
- SonarQube is installed and accessible.
- SonarQube scanner is integrated with Jenkins.
- Jenkins has the **Warnings Next Generation** plugin installed.

---

## 🛠️ Step-by-Step Configuration

### Step 1: Create Custom Quality Gate

- Navigate to **Quality Gates** in SonarQube.
- Click **Create** and name it:  
  `eclean-prod-quality-gate`
- Add the following conditions:
  - **Coverage** < `80%`
  - **Duplicated Lines (%)** > `3%`
  - **Maintainability Rating** worse than `A`
  - **Reliability Rating** worse than `A`
  - **Security Rating** worse than `A`
  - **Security Hotspots Reviewed** < `100%`

**Optional:**  
Click the 3-dots beside your new gate → Select **Set as Default**

---

### Step 2: Assign Quality Gate to the Project

- Go to the project `ezlearn`
- Click **Project Settings (gear icon)**
- Select **Quality Gate**
- Choose: `Always use a specific Quality Gate`
- Select: `eclean-prod-quality-gate`
- Click **Save**

---

### Step 3: Configure Quality Profile

- Go to **Quality Profiles**
- Select **Java**
- Choose a profile like **Sonar way** or a custom one
- Click **Set as Default** if required

---

### Step 4: Set Up Security Hotspots Review

- Navigate to **Security Hotspots** tab in your project
- Review each hotspot and mark them as:
  - `Reviewed` – if the issue is acceptable
  - `Fixed` – if you addressed it

---

### Step 5: Configure Source Code Exclusions (Optional)

- Go to **Project Settings → Analysis Scope**
- Add the following paths if needed:
  - `/generated/`
  - `/test/`

---

### Step 6: Configure Webhooks (Jenkins Integration)

- Go to **Administration → Configuration → Webhooks**
- Click **Create**
- Use the following:
  - **Name:** `Jenkins CI`
  - **URL:** `https://your-jenkins-url/sonarqube-webhook/`
  - **Secret:** _(optional if secured)_

---

### Step 7: Set Analysis Parameters for Jenkins

Go to **Project Settings → General Settings** and set:

```properties
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.java.binaries=target/classes
sonar.sources=src/main/java
```

---

### Step 8: Check Project Permissions

- Go to **Project Settings → Permissions**
- Ensure:
  - Jenkins service account has **Execute Analysis**
  - Developers, QA, and other roles have proper access

---

### Step 9: Configure Duplication Settings

- Go to **Administration → General Settings → Duplications**
- Verify:
  - **Minimum tokens** = `100` (default)

---

### Step 10: Configure Notifications (Optional)

- Go to **Administration → Configuration → Notifications**
- Set up alerts for:
  - Quality Gate changes
  - New issues
- Delivery options:
  - Email
  - Slack integration

---

### Step 11: Configure Branch Analysis (If Using Branches)

- Go to **Project Settings → Branches**
- Ensure:
  - **Main branch:** `main` or `master`
  - **Branch type:** Long-lived
  - Quality Gate applies to all branches

---

### Step 12: Verify Quality Gate Conditions

- Go to **Quality Gates**
- Click on `eclean-prod-quality-gate`
- Ensure all conditions are correctly listed:
  - Coverage < 80%
  - Duplicated Lines > 3%
  - Maintainability Rating worse than A
  - Reliability Rating worse than A
  - Security Rating worse than A
  - Security Hotspots Reviewed < 100%

---

### Step 13: Run Jenkins Pipeline and Monitor SonarQube

- Trigger your Jenkins pipeline
- Once analysis completes:
  - Check results on [SonarQube Dashboard](http://sonarqube.mitechnology.org:9000/dashboard?id=ezlearn)
  - Validate Quality Gate status (Passed or Failed)
  - Ensure metrics like **Coverage**, **Duplications**, and **Security Hotspots** are displayed

---

## Quality Gate Metrics Summary

| Metric                        | Threshold      |
|------------------------------|----------------|
| Coverage                     | ≥ 80%          |
| Duplicated Lines (%)         | ≤ 3%           |
| Maintainability Rating       | A              |
| Reliability Rating           | A              |
| Security Rating              | A              |
| Security Hotspots Reviewed   | 100%           |

---

## Notes

- Ensure your `pom.xml` or Gradle script includes Jacoco configuration.
- Use the SonarQube Jenkins plugin or CLI for integration.
- Custom rules or thresholds can be applied per project/team needs.

---

**Maintainer:** Ryan Wirba 
**Last Updated:** July 2025
