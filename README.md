# 🔁 Auto Webhook Trigger – Spring Boot Application

This project is built for the **Bajaj Finserv Health Programming Challenge**. The Spring Boot application automatically interacts with a remote API on startup, processes a problem statement, and posts the result to a webhook with JWT authentication.

---

## 🚀 Features

- Auto execution on application startup (no manual REST call)
- Remote API interaction using `RestTemplate` or `WebClient`
- Dynamic problem selection based on registration number
- Retry mechanism for webhook posting (up to 4 times)
- Secure requests with JWT-based authorization

---

## 📋 Problem Statement

1. On startup, the application must:
   - Call the `/generateWebhook` endpoint
   - Receive a webhook URL, JWT access token, and user data
   - Solve one of two problems based on `regNo`'s last two digits
   - POST the result to the webhook with the provided token

2. The two problems are:

### 🧠 Question 1 – Mutual Followers (Odd `regNo` ending)
Find pairs `[minId, maxId]` where both users follow each other (2-node cycles).

### 🔗 Question 2 – Nth-Level Followers (Even `regNo` ending)
Given `findId` and `n`, return all user IDs exactly `n` levels away in the follow graph.

---

## 📦 API Details

### 🔹 Step 1: Generate Webhook

**Endpoint:**  
`POST https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook`

**Sample Request Body:**
```json
{
  "name": "John Doe",
  "regNo ": "REG12347",
  "email ": "john@example.com"
}
Sample Response:

json
Copy
Edit
{
  "webhook": "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook",
  "accessToken": "your-access-token",
  "data": {
    "users": [...]
  }
}
🔹 Step 2: Submit Output
Method:
POST to webhook from Step 1

Headers:

Authorization: <accessToken>

Content-Type: application/json

Sample Output Body:

json
Copy
Edit
{
  "regNo ": "REG12347",
  "outcome ": [[1, 2]]
}
🛠️ Tech Stack
Java 17+

Spring Boot

Maven

Jackson (JSON parsing)

RestTemplate / WebClient

Retry Logic (custom or Spring Retry)

🧪 How to Run
bash
Copy
Edit
# Build the project
./mvnw clean package

# Run the JAR
java -jar target/auto-webhook-app.jar
Ensure your internet connection is active as it communicates with external APIs.

📁 Project Structure
css
Copy
Edit
├── src
│   └── main
│       ├── java
│       │   └── com.example.autowebhook
│       │       ├── AutoWebhookApplication.java
│       │       ├── service
│       │       ├── model
│       │       └── config
│       └── resources
│           └── application.yml
├── pom.xml
└── README.md
✅ Submission
Public GitHub repository

JAR file in target/

Raw downloadable GitHub link to the final JAR

🔗 Submission Form

🏁 Good Luck!
Made with ❤️ for the Bajaj Finserv Health Challenge.

yaml
Copy
Edit

---

Let me know if you want this in an actual `.md` file or want to include badges, logs, or example logs.







