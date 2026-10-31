# Semantic Search Engine & ML Inference Architecture

Высоконагруженная микросервисная система семантического поиска на базе векторных эмбеддингов с двухэтапным ранжированием (Bi-Encoder + Cross-Encoder Reranking), локальным инференсом ML-моделей через **ONNX Runtime** и инфраструктурой в **Kubernetes**.

Система спроектирована на **Java 21** с разделением потоков записи (асинхронная индексация) и чтения (Low-Latency синхронный поиск).

---

## Архитектура и Технологический стек

Система разделена на независимые слабосвязанные сервисы:

*   **Java 21 / Spring Boot 3 / Gradle** — основной стек разработки сервисов.
*   **Semantic-API Service** — легковесный шлюз (Бэкенд), принимающий запросы от клиентов. Он отправляет новые документы на индексацию в шину данных и делает прямые gRPC-запросы для поиска.
*   **Apache Kafka** — распределенный лог событий, обеспечивающий гарантированную, отказоустойчивую и асинхронную передачу документов на индексацию.
*   **Semantic-Worker Service** — изолированный вычислительный движок. Выполняет локальный инференс трансформеров без Python-окружения через **Microsoft ONNX Runtime Java API**. Работает в двух режимах:
    1. *Асинхронный (Consumer):* Вычитывает документы из Kafka, генерирует 768-мерные эмбеддинги (`all-mpnet-base-v2`) и сохраняет их в БД.
    2. *Синхронный (gRPC Server):* Принимает поисковый запрос, запрашивает кандидатов из БД по косинусному расстоянию и производит их финальное переранжирование через модель **Cross-Encoder**.
*   **gRPC** — высокопроизводительный протокол для синхронного Low-Latency взаимодействия между API и Worker при операциях поиска.
*   **PostgreSQL 15 + pgvector** — база данных с расширением для эффективного хранения векторов и поиска ближайших соседей по косинусному расстоянию (Cosine Distance).
*   **Kubernetes (K8s)** — оркестрация всей инфраструктуры (Deployment, StatefulSet для БД, PVC, Secrets).
*   **Prometheus & Grafana** — сквозной мониторинг метрик JVM, потребления CPU/памяти и бизнес-метрик инференса (HTTP queries, Client/Server errors).

---

## Схема потоков данных (Data Flow)

### 1. Асинхронный контур индексации (Запись)

[ Клиент ] ──> (HTTP POST /api/embed) ──> [ semantic-search-api ] ──> (Publish в Kafka) ──> [ Apache Kafka ] ──> (Consume из Kafka) ──> [ semantic-search-worker ] (Inference: all-mpnet-base-v2) ──> [ PostgreSQL (pgvector) ] (Save vector(768))

### 2. Синхронный контур поиска (Чтение с Reranking)

[ Клиент ] ──> (HTTP POST /api/search) ──> [ semantic-search-api ] ──> (gRPC: Search Request) ──> [ semantic-search-worker ] (1. Поиск ТОП-N по Cosine Distance 2. Запуск Cross-Encoder Reranker) ──> [ semantic-search-api ] (Возврат ТОП) ──> [ Клиент ]

---

## Локальное окружение и Тестирование (minikube)

Вся инфраструктура функционирует в локальном кластере **minikube** с драйвером `docker` и прямым пробросом портов.

### Генерация ONNX-моделей
Модели `all-mpnet-base-v2` и `ms-marco-MiniLM-L-6-v2` не включены в репозиторий из-за большого размера. Чтобы сгенерировать их локально необходимо:
1. Установить Python 3.10+ и Miniconda.
2. Создать и активировать окружение:
   ```bash
   conda create -n ml_env python=3.10 -y
   conda activate ml_env
   ```
3. Установить зависимости:
   ```bash
   pip install sentence-transformers onnx onnxruntime transformers torch
   ```
4. Перейти в папку скриптов:
   ```bash
   cd path-to-semantic-search-engine\scripts\conversion
   ```
5. Запустить скрипты конвертации:
   ```bash
   python convert_all_mpnet_base_v2.py
   python convert_cross_encoder.py
   ```
6. Скопировать полученные файлы *.onnx и *.onnx.data в worker/src/main/resources/models/.

### Быстрый запуск кластера:
   ```bash
   minikube start --driver=docker --listen-address=0.0.0.0 --apiserver-ips=<IP сервера> --ports=8443:8443,30080:30080,30300:30300
   ```

### Порядок применения манифестов:
Выполните последовательное развертывание из корневой папки проекта:
```bash
kubectl apply -f k8s/models-pvc.yaml
!!! при необходимости копируем модели через временный под [описано ниже]*
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/prometheus-rbac.yaml
kubectl apply -f k8s/kafka.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/semantic-search-worker.yaml
kubectl apply -f k8s/semantic-search-api.yaml
kubectl apply -f k8s/prometheus.yaml
kubectl apply -f k8s/grafana-pvc.yaml
kubectl apply -f k8s/grafana.yaml
```
* копирование моделей
```bash
[создаем временный под]
kubectl apply -f k8s/model-uploader.yaml
[копируем модели]
kubectl cp worker/src/main/resources/models/. model-uploader:/mnt/models/
[проверка, что файлы скопировались]
kubectl exec -it model-uploader -- ls -la /mnt/models
[удаление временного пода]
kubectl delete pod model-uploader
```

### Опционально
Скрипт generate_all_tokenizers.py генерирует токенизаторы. Токенизаторы появятся в корневой папке, побочные директории удалить.

### Мониторинг:
После инициализации подов дашборды Grafana доступны: `http://<IP сервера>:30300`. Готовые конфигурации панелей находятся в директории `dashboards/`