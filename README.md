To properly configure your SonarQube UI to work with your Jenkins pipeline and quality gates, follow these steps:

Install warning plugin in jenkins

1. Configure Project Settings
Go to your project in SonarQube (http://sonarqube.mitechnology.org:9000/dashboard?id=ezlearn)

Create it manually:

Go to Quality Gates (top menu)

Click Create

Name it "eclean-prod-quality-gate"

Add these conditions (as per your requirements):

Coverage < 80%

Duplicated Lines > 3%

Maintainability Rating worse than A

Reliability Rating worse than A

Security Hotspots Reviewed < 100%

Security Rating worse than A

4. Set as Default (Optional)
In Quality Gates page

Click the three dots next to your quality gate

Select Set as Default

Click on Project Settings (gear icon in top-right)

2. Assign Quality Gate
Go to your project ("ezlearn")

Click Project Settings (gear icon)

Select Quality Gate

Choose "Always use a specific Quality Gate"

Select "eclean-prod-quality-gate" from dropdown



Click Save

3. Configure Quality Profile
Go to Quality Profiles (top menu)

Select Java language

Find and set your preferred profile (likely "Sonar way" or custom one)

Click Set as Default if needed

4. Set Up Security Hotspots Review
Go to Security Hotspots tab in your project

Review all findings and mark them as:

Reviewed (if acceptable)

Fixed (if you've addressed them)

5. Configure Exclusions (Optional)
In Project Settings → Analysis Scope

Add exclusions if needed (e.g., generated code):

**/generated/**

**/test/** (if you don't want test code analyzed)

6. Set Up Webhooks (Recommended)
Go to Administration → Configuration → Webhooks

Add webhook for Jenkins:

Name: Jenkins CI

URL: https://your-jenkins-url/sonarqube-webhook/

Secret: [Add if secured]

7. Verify Analysis Parameters
In Project Settings → General Settings

Ensure these parameters are set:

sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml

sonar.java.binaries=target/classes

sonar.sources=src/main/java

8. Check Permissions
Go to Project Settings → Permissions

Ensure:

Jenkins service account has Execute Analysis permission

Your team has appropriate access

9. Configure Duplication Settings
Go to Administration → General Settings → Duplications

Verify "Minimum tokens" is set to 100 (default)

10. Set Up Notifications (Optional)
Go to Administration → Configuration → Notifications

Add email/Slack notifications for:

Quality Gate changes

New issues

11. Verify Quality Gate Conditions
Go to Quality Gates (top menu)

Click on "eclean-prod-quality-gate"

Verify all required conditions are present (as shown in your screenshot)

12. Configure Branch Analysis (If Using Branches)
Go to Project Settings → Branches

Set up branch analysis strategy:

Main branch: main or master

Branch type: Long-lived branches

Important Checks:
Ensure your quality gate conditions match your requirements:

Coverage ≥ 80%

Duplications ≤ 3%

Security Hotspots reviewed = 100%

No new vulnerabilities/bugs

After configuration:

Run your Jenkins pipeline

Monitor the SonarQube project dashboard for results

Check the Quality Gate status on the project homepage

This configuration will ensure your SonarQube analysis properly enforces your quality standards through the Jenkins pipeline. The quality gate will now fail if any of your conditions aren't met (like the current 0% coverage and 0% security review status).