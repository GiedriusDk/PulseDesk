# Setup Instructions

## Requirements

- Java 17+

---

## 1. Clone the Repository

git clone https://github.com/GiedriusDk/PulseDesk.git
cd PulseDesk

---
## 2. Create an access token at:
https://huggingface.co/settings/tokens

---

## 3. Set Environment Variables

Mac / Linux:

export HF_API_KEY=hf_your_token_here
export HF_MODEL=HuggingFaceTB/SmolLM3-3B:hf-inference

Windows (PowerShell):

setx HF_API_KEY "hf_your_token_here"
setx HF_MODEL "HuggingFaceTB/SmolLM3-3B:hf-inference"

---

## 4. Start the Application

./mvnw spring-boot:run

The application will start at:

http://localhost:8080

Use the web interface or send HTTP requests to the /comments endpoint.
