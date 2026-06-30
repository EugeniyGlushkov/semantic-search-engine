# semantic-search-engine

**Spring Boot service for text embeddings and semantic search with ONNX and pgvector**

## 🧠 What it does
- Converts text into 384-dimensional vectors (embeddings) using `all-MiniLM-L6-v2` model via ONNX Runtime.
- Stores embeddings in PostgreSQL with `pgvector` extension.
- Performs semantic similarity search: finds the most similar texts by cosine distance.

## 🛠️ Tech Stack
- Java 17 + Spring Boot 3
- ONNX Runtime for Java
- PostgreSQL 15 + pgvector
- Gradle
- Thymeleaf (UI)
- Docker / Kubernetes (planned)

## 🚀 Getting Started
### Prerequisites
- Java 17
- PostgreSQL 15 with pgvector
- Python 3.10 + `sentence-transformers` (for model conversion)

### Run locally
```bash
./gradlew bootRun

📌 Endpoints
Method	Endpoint	Description
POST	/api/embed	Accepts text, generates embedding, stores in DB
POST	/api/search	Accepts text, finds top 5 similar texts
GET	/	UI for testing
📄 License
MIT