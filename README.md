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
```

📌 Endpoints
Method	Endpoint	Description
POST	/api/embed	Accepts text, generates embedding, stores in DB
POST	/api/search	Accepts text, finds top 5 similar texts
GET	/	UI for testing
📄 License
MIT

### Генерация модели ONNX

Модель `all-mpnet-base-v2` не включена в репозиторий из-за большого размера (~0.5 ГБ). Чтобы сгенерировать её локально:
Модель `ms-marco-MiniLM-L-6-v2` не включена в репозиторий из-за большого размера (~0.5 ГБ). Чтобы сгенерировать её локально:

1. Убедись, что у тебя есть Python 3.10+ и установлен Miniconda.
2. Создай и активируй окружение:
   ```bash
   conda create -n ml_env python=3.10 -y
   conda activate ml_env
Установи зависимости:

```bash
pip install sentence-transformers onnx onnxruntime transformers torch
````
Перейди в папку скриптов:

```bash
D:
cd cd learn\LLM\dev\semantic-search-engine\scripts\conversion
````
Запусти скрипт конвертации:

```bash
python convert_all_mpnet_base_v2.py
python convert_cross_encoder.py
```
Скопируй полученные файлы embedding_model.onnx и embedding_model.onnx.data в worker/src/main/resources/models/.

# Сборка контейнера
### Собрать API
docker build -t ubuntu-registry.local/semantic-search-api:latest ./api

### Собрать Worker
docker build -t ubuntu-registry.local/semantic-search-worker:latest ./worker

docker build -t semantic-search:latest .

# Передача в registry
docker push ubuntu-registry.local/semantic-search-api:latest
docker push ubuntu-registry.local/semantic-search-worker:latest

### Запуск контейнера
docker run -p 8080:8080 --name semantic-search -e SPRING_PROFILES_ACTIVE=docker semantic-search:latest
docker start semantic-search 
старт контейнера с привязкой к потоку вывода
docker start -a semantic-search

### Остановка контейнера
docker stop semantic-search

посмотреть все контейнеры, включая остановленные (-a)
docker ps -a
посмотреть образы
docker images
Удалить остановленный контейнер
docker rm semantic-search
Удалить образ по имени/тегу:
docker rmi <имя_образа>

### порядок запуска
# 1. Базовые ресурсы (от них зависят все поды)
kubectl apply -f k8s/secrets.yaml             # Секреты (пароли БД)
kubectl apply -f k8s/prometheus-rbac.yaml     # Права доступа для Prometheus

# 2. База данных и хранилища
kubectl apply -f k8s/postgres.yaml            # PostgreSQL (StatefulSet + PVC)

# 3. Основные приложения (зависят от БД и секретов)
kubectl apply -f k8s/semantic-search-api.yaml
kubectl apply -f k8s/semantic-search-worker.yaml

# 4. Мониторинг (не зависит от приложений, но полезно поднять после)
kubectl apply -f k8s/prometheus.yaml          # Prometheus (Deployment + Service)
kubectl apply -f k8s/grafana.yaml             # Grafana (Deployment + Service)

### Добавление Connections.Data soursces в Grafana
Connection URL http://prometheus-service:9090
### Добавление дашборда
```text
Запросы для панелей:
память JVM jvm_memory_used_bytes{application="semantic-search"}
CPU system_cpu_usage{application="semantic-search"}
HTTP queries http_server_requests_seconds_count{application="semantic-search"}
Errors http_server_requests_seconds_count{application="semantic-search", outcome="CLIENT_ERROR"}
```
## !!!!
🚀 Что можно сделать дальше (по желанию)
Настроить алерты — чтобы получать уведомления при ошибках или высокой нагрузке.
Добавить кастомные бизнес-метрики — например, счётчики запросов к /api/embed, время генерации эмбеддинга.
Подключить Loki для логов — чтобы собирать и анализировать логи вместе с метриками.
Настроить дашборд под себя — добавить панели с нужными метриками, убрать лишние.