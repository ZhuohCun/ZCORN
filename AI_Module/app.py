# main.py

import io
import time
import logging
from typing import List, Optional

import torch
import numpy as np
import uvicorn
from PIL import Image, UnidentifiedImageError
from fastapi import FastAPI, File, UploadFile, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from torchvision import transforms
from contextlib import asynccontextmanager
from timm.data.constants import IMAGENET_DEFAULT_MEAN, IMAGENET_DEFAULT_STD
# 导入你的模型定义，确保与训练时代码一致
from models import t2t_vit_14
from models.models import deit_small_patch16_224
from models.vit_model import vit_base_patch16_224
MODEL_PATHS = {
    "hon": "results/healthyornot.pth",
    "wd": "results/whichdisease.pth",
}
CLASS_NAMES_HON = ["Healthy", "Infected"]
CLASS_NAMES_WD = ["blight", "common_rust", "gray_spot", "spray"]
NUM_CLASSES_HON = len(CLASS_NAMES_HON)
NUM_CLASSES_WD = len(CLASS_NAMES_WD)
transform = transform = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225])
    ])
models_dict = {}
def select_device() -> torch.device:
    return torch.device("cpu")
device = select_device()

def load_model_hon(path: str, device: torch.device):
    model = t2t_vit_14(num_classes=NUM_CLASSES_HON)
    state = torch.load(path, map_location=device)
    if isinstance(state, dict) and ("model_state_dict" in state or "state_dict" in state):
        if "model_state_dict" in state:
            state_dict = state["model_state_dict"]
        else:
            state_dict = state.get("state_dict", state)
    else:
        state_dict = state
    try:
        model.load_state_dict(state_dict)
    except Exception as e:
        # 部分加载逻辑
        local_dict = model.state_dict()
        filtered = {}
        for k, v in state_dict.items():
            if k in local_dict and local_dict[k].shape == v.shape:
                filtered[k] = v
            else:
                pass
        local_dict.update(filtered)
        model.load_state_dict(local_dict)
    model.to(device)
    model.eval()
    return model
def load_model_wd(path: str, device: torch.device):
    model = t2t_vit_14(num_classes=NUM_CLASSES_WD)
    state = torch.load(path, map_location=device)
    if isinstance(state, dict) and ("model_state_dict" in state or "state_dict" in state):
        if "model_state_dict" in state:
            state_dict = state["model_state_dict"]
        else:
            state_dict = state.get("state_dict", state)
    else:
        state_dict = state
    try:
        model.load_state_dict(state_dict)
    except Exception as e:
        # 部分加载逻辑
        local_dict = model.state_dict()
        filtered = {}
        for k, v in state_dict.items():
            if k in local_dict and local_dict[k].shape == v.shape:
                filtered[k] = v
            else:
                pass
        local_dict.update(filtered)
        model.load_state_dict(local_dict)
    model.to(device)
    model.eval()
    return model


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时加载模型
    for name, path in MODEL_PATHS.items():
        try:
            if name == "hon":
                models_dict[name] = load_model_hon(path, device)
            elif name == "wd":
                models_dict[name] = load_model_wd(path, device)
        except Exception as e:
            pass
    yield
app = FastAPI(
    title="玉米病变识别系统",
    description="上传玉米叶片图像，返回病变类型预测",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

class PredictionResponse(BaseModel):
    used_model: str  # 原 model_used 改名为 used_model，避免 Pydantic 警告
    prediction: str
    confidence: float
    probabilities: dict
    elapsed_ms: float

@app.post("/predicthon", response_model=PredictionResponse)
async def predicthon(
    file: UploadFile = File(...),
    model_name: Optional[str] = Query(
        default="hon",
    ),
    top_k: Optional[int] = Query(default=1, ge=1, le=NUM_CLASSES_HON, description="返回 top K 结果")
):
    # 1. 读取并校验图片
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
    except Exception as e:
        pass
    # 2. 预处理
    try:
        input_tensor = transform(image).unsqueeze(0).to(device)
    except Exception as e:
        pass

    # 3. 选择模型并推理
    start_time = time.time()
    model_names = []
    outputs = []

    if model_name == "ensemble":
        if len(models_dict) < 2:
            pass
        model_names = list(models_dict.keys())
        for name in model_names:
            model = models_dict.get(name)
            if model is None:
                continue
        try:
            stacked = torch.stack(outputs, dim=0)  # [num_models, 1, C]
            avg_logits = torch.mean(stacked, dim=0)  # [1, C]
            final_logits = avg_logits.squeeze(0)
        except Exception as e:
            final_logits = outputs[0].squeeze(0)
            model_names = [model_names[0]]
    else:
        model_names = [model_name]
        model = models_dict[model_name]
        try:
            with torch.no_grad():
                out = model(input_tensor)
                logits = out[0] if isinstance(out, (list, tuple)) else out
                final_logits = logits.squeeze(0)
        except Exception as e:
            pass

    elapsed_ms = (time.time() - start_time) * 1000

    # 4. softmax 及 top-k
    try:
        probs = torch.nn.functional.softmax(final_logits, dim=0).cpu().numpy()
    except Exception as e:
        pass

    top_k = min(top_k, NUM_CLASSES_HON)
    top_indices = np.argsort(probs)[::-1][:top_k]
    top_probs = probs[top_indices]
    top_classes = [CLASS_NAMES_HON[i] for i in top_indices]

    prediction = top_classes[0]
    confidence = float(top_probs[0])
    # 返回所有类别概率；如需只返回 top_k，可在前端或改为只填 top_indices
    probabilities = {cls: float(probs[idx]) for cls, idx in zip(CLASS_NAMES_HON, range(len(CLASS_NAMES_HON)))}

    return PredictionResponse(
        used_model=",".join(model_names),
        prediction=prediction,
        confidence=confidence,
        probabilities=probabilities,
        elapsed_ms=elapsed_ms
    )
@app.post("/predictwd", response_model=PredictionResponse)
async def predictwd(
    file: UploadFile = File(...),
    model_name: Optional[str] = Query(
        default="wd",
    ),
    top_k: Optional[int] = Query(default=1, ge=1, le=NUM_CLASSES_HON, description="返回 top K 结果")
):
    # 1. 读取并校验图片
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
    except Exception as e:
        pass
    # 2. 预处理
    try:
        input_tensor = transform(image).unsqueeze(0).to(device)
    except Exception as e:
        pass

    # 3. 选择模型并推理
    start_time = time.time()
    model_names = []
    outputs = []

    if model_name == "ensemble":
        if len(models_dict) < 2:
            pass
        model_names = list(models_dict.keys())
        for name in model_names:
            model = models_dict.get(name)
            if model is None:
                continue
        try:
            stacked = torch.stack(outputs, dim=0)  # [num_models, 1, C]
            avg_logits = torch.mean(stacked, dim=0)  # [1, C]
            final_logits = avg_logits.squeeze(0)
        except Exception as e:
            final_logits = outputs[0].squeeze(0)
            model_names = [model_names[0]]
    else:
        model_names = [model_name]
        model = models_dict[model_name]
        try:
            with torch.no_grad():
                out = model(input_tensor)
                logits = out[0] if isinstance(out, (list, tuple)) else out
                final_logits = logits.squeeze(0)
        except Exception as e:
            pass

    elapsed_ms = (time.time() - start_time) * 1000

    # 4. softmax 及 top-k
    try:
        probs = torch.nn.functional.softmax(final_logits, dim=0).cpu().numpy()
    except Exception as e:
        pass

    top_k = min(top_k, NUM_CLASSES_WD)
    top_indices = np.argsort(probs)[::-1][:top_k]
    top_probs = probs[top_indices]
    top_classes = [CLASS_NAMES_WD[i] for i in top_indices]

    prediction = top_classes[0]
    confidence = float(top_probs[0])
    # 返回所有类别概率；如需只返回 top_k，可在前端或改为只填 top_indices
    probabilities = {cls: float(probs[idx]) for cls, idx in zip(CLASS_NAMES_WD, range(len(CLASS_NAMES_WD)))}

    return PredictionResponse(
        used_model=",".join(model_names),
        prediction=prediction,
        confidence=confidence,
        probabilities=probabilities,
        elapsed_ms=elapsed_ms
    )
if __name__ == "__main__":
    host = "127.0.0.1"
    port = 8001
    while True:
        try:
            uvicorn.run("app:app", host=host, port=port, reload=True)
        except OSError as e:
            pass
        except Exception as e:
            pass
