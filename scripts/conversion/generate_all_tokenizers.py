"""
Генерирует оба токенизатора:
- tokenizer.json                 — для embedding-модели (all-mpnet-base-v2)
- cross_encoder_tokenizer.json   — для Cross-Encoder (ms-marco-MiniLM-L-6-v2)
"""

from transformers import AutoTokenizer
import os
import shutil
import json


def generate_tokenizer(model_name: str, final_filename: str, output_dir: str) -> dict:
    """Загружает токенизатор, сохраняет и копирует tokenizer.json."""
    print(f"\n{'='*70}")
    print(f"📥 Модель: {model_name}")
    print(f"{'='*70}")

    tokenizer = AutoTokenizer.from_pretrained(model_name)
    tokenizer.save_pretrained(output_dir)

    src = os.path.join(output_dir, "tokenizer.json")
    if not os.path.exists(src):
        raise RuntimeError(f"❌ Не найден {src}")

    shutil.copy(src, final_filename)

    with open(final_filename, "r", encoding="utf-8") as f:
        data = json.load(f)

    info = {
        "file": final_filename,
        "size_kb": os.path.getsize(final_filename) / 1024,
        "model_type": data.get("model", {}).get("type"),
        "vocab_size": len(data.get("model", {}).get("vocab", {})),
        "post_processor": data.get("post_processor", {}).get("type"),
    }

    print(f"✅ {final_filename}")
    print(f"   Размер:          {info['size_kb']:.1f} KB")
    print(f"   model.type:      {info['model_type']}")
    print(f"   vocab size:      {info['vocab_size']}")
    print(f"   post_processor:  {info['post_processor']}")

    shutil.rmtree(output_dir)
    return tokenizer


# ============================================================
# 1. Embedding-токенизатор
# ============================================================
embedding_tok = generate_tokenizer(
    model_name="sentence-transformers/all-mpnet-base-v2",
    final_filename="tokenizer.json",
    output_dir="embedding_tokenizer_out",
)

# ============================================================
# 2. Cross-Encoder токенизатор
# ============================================================
ce_tok = generate_tokenizer(
    model_name="cross-encoder/ms-marco-MiniLM-L-6-v2",
    final_filename="../../worker/src/main/resources/models/cross_encoder_tokenizer.json",
    output_dir="cross_encoder_tokenizer_out",
)

# ============================================================
# Финальные тесты — главное отличие CE от embedding
# ============================================================
print(f"\n{'='*70}")
print("🧪 ФИНАЛЬНЫЕ ТЕСТЫ")
print(f"{'='*70}")

query = "docker portability"
doc = "Docker packages applications with their dependencies into portable images."

print(f"\n[Embedding] tokenize('{query}'):")
emb_ids = embedding_tok.encode(query)
print(f"   input_ids[:10]: {emb_ids[:10]}")
print(f"   seqLen:         {len(emb_ids)}")

print(f"\n[Cross-Encoder] tokenize_pair(query, doc):")
ce_encoded = ce_tok(query, doc)
print(f"   input_ids[:10]:  {ce_encoded['input_ids'][:10]}")
print(f"   token_type_ids:  {ce_encoded['token_type_ids'][:10]}   ← важно: 0 для query, 1 для doc")
print(f"   seqLen:          {len(ce_encoded['input_ids'])}")

# Проверка, что CE-токенизатор генерирует token_type_ids
assert any(t == 1 for t in ce_encoded['token_type_ids']), \
    "❌ Cross-Encoder токенизатор не сгенерировал token_type_ids=1 для документа!"
print("\n✅ Cross-Encoder генерирует token_type_ids (0 для query, 1 для doc)")

# Финальная проверка: оба файла на месте
print(f"\n{'='*70}")
for f in ["tokenizer.json", "cross_encoder_tokenizer.json"]:
    if os.path.exists(f):
        print(f"✅ {f}  ({os.path.getsize(f) / 1024:.1f} KB)")
    else:
        print(f"❌ {f}  ОТСУТСТВУЕТ")