import torch
import onnx
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import os

# 1. Загружаем модель и токенизатор
model_name = 'cross-encoder/ms-marco-MiniLM-L-6-v2'
print(f"📥 Загрузка модели {model_name}...")
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForSequenceClassification.from_pretrained(model_name)
model.eval()
print(f"✅ Модель загружена. Тип: {type(model)}")

# 2. Подготавливаем тестовую пару (запрос, документ)
query = "Как приготовить борщ?"
document = "Для приготовления борща вам понадобится свекла, капуста, картофель, морковь, лук и томатная паста."
print(f"📝 Запрос: {query}")
print(f"📄 Документ: {document}")

# 3. Токенизация пары
inputs = tokenizer(query, document, return_tensors='pt', padding=True, truncation=True, max_length=512)
print(f"🔢 Длина токенов: {inputs['input_ids'].shape[1]}")

# 4. Экспорт в ONNX
onnx_path = os.path.join(os.path.dirname(__file__), 'cross_encoder_model.onnx')
print("🔄 Конвертация в ONNX...")

torch.onnx.export(
    model,
    (inputs['input_ids'], inputs['attention_mask']),
    onnx_path,
    input_names=['input_ids', 'attention_mask'],
    output_names=['logits'],
    dynamic_axes={
        'input_ids': {0: 'batch_size', 1: 'sequence_length'},
        'attention_mask': {0: 'batch_size', 1: 'sequence_length'},
    },
    opset_version=14,
    do_constant_folding=True
)
print(f"✅ Модель сохранена в: {onnx_path}")

# 5. Проверка модели
print("🔍 Проверка загруженной ONNX-модели...")
onnx_model = onnx.load(onnx_path)
onnx.checker.check_model(onnx_model)
print("✅ ONNX-модель валидна и готова к использованию в Java!")