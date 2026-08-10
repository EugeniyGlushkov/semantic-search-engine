import torch
import onnx
from sentence_transformers import SentenceTransformer
from transformers import AutoTokenizer
import os

# 1. Загружаем модель и токенизатор
model_name = 'sentence-transformers/all-mpnet-base-v2'
print(f"📥 Загрузка модели {model_name}...")
model = SentenceTransformer(model_name)
model.eval()
tokenizer = AutoTokenizer.from_pretrained(model_name)

# 2. Подготавливаем тестовый вход
example_text = ["Hello, world! This is a test sentence for embedding."]
inputs = tokenizer(example_text, return_tensors='pt', padding=True, truncation=True)
print(f"📝 Пример входа: {example_text}")
print(f"🔢 Длина токенов: {inputs['input_ids'].shape[1]}")

# 3. Экспортируем в ONNX
onnx_path = os.path.join(os.path.dirname(__file__), 'embedding_model.onnx')
print("🔄 Конвертация в ONNX...")
torch.onnx.export(
    model._modules['0'].auto_model,
    (inputs['input_ids'], inputs['attention_mask']),
    onnx_path,
    input_names=['input_ids', 'attention_mask'],
    output_names=['embeddings'],
    dynamic_axes={
        'input_ids': {0: 'batch_size', 1: 'sequence_length'},
        'attention_mask': {0: 'batch_size', 1: 'sequence_length'},
        'embeddings': {0: 'batch_size'}
    },
    opset_version=14,
    do_constant_folding=True
)
print(f"✅ Модель сохранена в: {onnx_path}")

# 4. Проверка модели
print("🔍 Проверка загруженной ONNX-модели...")
onnx_model = onnx.load(onnx_path)
onnx.checker.check_model(onnx_model)
print("✅ ONNX-модель валидна и готова к использованию в Java!")