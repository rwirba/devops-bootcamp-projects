# ECS Fargate: Tomcat 9 + ALB (Console Guide)

This guide deploys a Tomcat 9 container to **AWS ECS (Fargate)** behind an **Application Load Balancer** so you can access it in a browser. It uses:
- **ECR** for container registry
- **CodeBuild** (from GitHub) to build & push your image
- **ECS Fargate** Service + **ALB** (HTTP :80) to run the app
- Your WAR is baked into the image as `ROOT.war` so it serves at `/`

> **Prereqs**
> - An AWS account with permissions for ECR, CodeBuild, ECS, IAM, EC2, ELBv2, CloudWatch Logs
> - A Nexus (or other) URL to your WAR **(publicly reachable or with appropriate access)**
> - Your GitHub repo contains: `Dockerfile`, `buildspec.yml`, `.dockerignore` (this README is optional)

---

## 1) Create / Connect the GitHub repo (Console)
1. Switch ti git branch AWS-ECS in  GitHub).
2. Confirm it contains `Dockerfile`, `buildspec.yml`, `.dockerignore`.

---

**AWS Console → ECR → Private registry → Repositories → Create repository**

Fill the page exactly like this:
- **Repository name:** `ezlearn-app`
- **Image tag mutability:** **Mutable** ✅  
  (Easier walkthrough; lets CodeBuild update `:latest` on each build.)
- **Encryption settings:** **AES-256 (default)** ✅  
  (Choose **KMS** only if you require a CMK.)
- **Image scanning settings:** section is **deprecated**; leave as is.

Click **Create**.  
On the repo details page, copy the **Repository URI** (looks like `123456789012.dkr.ecr.us-east-1.amazonaws.com/ezlearn-app`).

> **Tip:** If your org enforces immutable tags, see the “Immutable tags (optional)” section later.

---

## 3) Create a CodeBuild project (Console)
1. Go to **CodeBuild → Build projects → Create project**.
2. **Project name**: `ezlearn-app-build`
3. **Source**: GitHub → Connect your repo (choose the repository).
4. **Environment**:
   - Environment image: **Managed image**
   - Operating system: **Ubuntu**
   - Runtime: **Standard**
   - Image: `aws/codebuild/standard:7.0` (or latest)
   - **Privileged**: ✅ (required for Docker build)
   - Service role: *Create new role* (accept defaults)
5. **Buildspec**: Use a buildspec file → `buildspec.yml` (already in repo)
6. **Environment variables** (Plaintext):
   - `APP_REPO_NAME` = `ezlearn-app` (must match your ECR repo name)
   - `WAR_URL` = `http://YOUR-NEXUS/.../yourapp-1.0.0.war`  
     > If your app should be served at `/`, ensure the Dockerfile renames to **ROOT.war** (it does).
7. Create project.
8. Click **Start build** and watch logs. At the end, confirm the image is pushed to ECR (tags `latest` and the short commit SHA).

---

## 4) Create an ECS cluster (Console)
1. Go to **ECS → Clusters → Create cluster**.
2. **EC2 or Fargate?** Choose **Networking only (Fargate)** (new console: “Create cluster” → name only).
3. **Cluster name**: `ezlearn-app`
4. Create cluster.

---

## 5) Create a Task Definition (Fargate) (Console)
1. **ECS → Task definitions → Create new task definition**.
2. **Family**: `ezlearn-app`
3. **Launch type**: Fargate
4. **Task size**: CPU `0.5 vCPU (512)`, Memory `1 GB (1024)`
5. **Task role**: *None* (not needed unless your app calls AWS APIs)
6. **Task execution role**: *Create new* or select **ecsTaskExecutionRole**
7. **Container**: Add container
   - **Name**: `ezlearn-app`
   - **Image URI**: your ECR image (e.g., `123456789012.dkr.ecr.us-east-1.amazonaws.com/ezlearn-app:latest`)
   - **Port mappings**: container port **8080**, protocol **tcp**
   - **Log configuration**: awslogs  
     - Log group: `/ecs/ezlearn-app` (create if prompted)  
     - Region: your region  
     - Stream prefix: `ezlearn`
8. Create the task definition.

---

## 6) Create an ALB, Target Group, and ECS Service (Console)
> The Service wizard can create the TG/ALB for you; below is a clear checklist.

1. **EC2 → Target Groups → Create target group**
   - **Type**: IP
   - **Name**: `ezlearn-tg`
   - **Protocol**: HTTP, **Port**: 8080
   - **VPC**: default VPC
   - **Health checks**: protocol HTTP, **Path**: `/`  
     *(Because we deploy `ROOT.war`. If you deploy as `ezlearn.war`, use `/ezlearn/`.)*
   - Create.

2. **EC2 → Load Balancers → Create load balancer**
   - **Application Load Balancer**
   - **Name**: `ezlearn-alb`
   - **Scheme**: Internet-facing
   - **IP address type**: IPv4
   - **Network mappings**: choose **2+ public subnets**
   - **Security groups**: create/select one that allows **HTTP 80** from `0.0.0.0/0`
   - **Listeners**: HTTP :80 → forward to **ezlearn-tg**
   - Create and note the **ALB DNS name** (e.g., `ezlearn-alb-123456.us-east-1.elb.amazonaws.com`)

3. **ECS → Clusters → (your cluster) → Create service**
   - **Compute**: Fargate
   - **Task definition**: `ezlearn-app:1` (your latest)
   - **Service name**: `ezlearn-app`
   - **Desired tasks**: 1
   - **Networking**:
     - **Cluster VPC**: default VPC
     - **Subnets**: select **public subnets**
     - **Security group**: same SG used by the ALB or another that **allows inbound from the ALB SG** on port **8080** (tip: easiest is allow all, better is ALB-only).
     - **Public IP**: **ENABLED**
   - **Load balancing**:
     - **Application Load Balancer**: choose `ezlearn-alb`
     - **Container to load balance**: `ezlearn-app:8080`
     - **Target group**: select `ezlearn-tg`
     - Health check grace period: `60` seconds
   - Create service and wait until the task is **RUNNING** and target shows **healthy** in the target group.

---

## 7) Access in browser
- Open: `http://<your-alb-dns-name>/`  
  *(because the Dockerfile deploys your WAR as `ROOT.war`)*

If you see 502 for a minute, give the task time to pass the target group health checks. Watch:
- **ECS → Tasks → Logs** (awslogs)
- **EC2 → Target Groups → Targets** (health)

---

## 8) Update the app (new WAR)
1. Update the `WAR_URL` in **CodeBuild → Project → Edit → Environment variables** (or update your Nexus URL).
2. **Start build** again.
3. After the image pushes, go to **ECS → Services → ezlearn-app → Deploy → Force new deployment** to roll out the new image.

---

## Notes & Tips
- For HTTPS: create an **ACM certificate**, add **:443** HTTPS listener on the ALB and attach the cert; forward to the same target group.
- Auto scaling: enable target tracking on the ECS Service or ALB 5xx/latency metrics later.
- If your Nexus requires auth, switch to fetching the WAR during build with credentials or push artifacts to ECR first.
- SonarQube & Jenkins need persistent storage; consider **ECS on EC2 + EBS/EFS** or move them to **EKS** later. Fargate can run Jenkins/Nexus with **EFS** volumes; SonarQube needs `vm.max_map_count` (not supported on Fargate).

---

## Troubleshooting
- **ALB 5xx / Unhealthy targets**: Confirm task security group allows traffic **from ALB SG** to **port 8080**; health check path matches your app.
- **Image not found**: Verify ECR URI and that CodeBuild pushed `:latest`.
- **App context mismatch**: If you didn’t use `ROOT.war`, change the target group health check path to your context, e.g. `/ezlearn/`.
- **Stuck in PROVISIONING**: Make sure subnets are **public** and service has **Assign public IP = ENABLED** (or use private subnets + NAT).
