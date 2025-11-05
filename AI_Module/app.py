
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
from models import t2t_vit_14
from models.models import deit_small_patch16_224
from models.vit_model import vit_base_patch16_224
MODEL_PATHS = {    #the address of the models, including the healthy-or-not model and the which-disease model
    "hon": "results/healthyornot.pth",
    "wd": "results/whichdisease.pth",
}
CLASS_NAMES_HON = ["Healthy", "Infected"]  #Healthy or not
CLASS_NAMES_WD = ["Blight", "Common Rust", "Gray Spot", "Spray"]  #Which disease
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
    while True:
        try:
            if torch.cuda.is_available():
                return torch.device("cuda")
            else:
                return torch.device("cpu")
        except:
            pass
try:
    device = select_device()
except:
    pass

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
        # partly load
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
        # partly load
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
    # load all models when starting
    for name, path in MODEL_PATHS.items():
        try:
            if name == "hon":
                models_dict[name] = load_model_hon(path, device)
            elif name == "wd":
                models_dict[name] = load_model_wd(path, device)
        except:
            pass
    yield
app = FastAPI(    #definition of FastAPI
    title="Zcorn AI corn recognition",
    description="Upload a corn picture shot by the camera to automatically detect if the corn in the picture is healthy, and if not, detect which disease the corn is infected",
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
    used_model: str
    prediction: str
    confidence: float
    probabilities: dict
    elapsed_ms: float

@app.post("/predicthon", response_model=PredictionResponse)
async def predicthon(     #To detect whether the corn is healthy or not automatically
    file: UploadFile = File(...),
    model_name: Optional[str] = Query(
        default="hon",
    ),
    top_k: Optional[int] = Query(default=1, ge=1, le=NUM_CLASSES_HON)
):
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
    except UnidentifiedImageError:
        raise HTTPException(status_code=400)
    except Exception as e:
        raise HTTPException(status_code=500)

    try:
        input_tensor = transform(image).unsqueeze(0).to(device)
    except Exception as e:
        raise HTTPException(status_code=500)

    start_time = time.time()
    model_names = []
    outputs = []

    if model_name == "ensemble":
        if len(models_dict) < 2:
            raise HTTPException(status_code=500)
        model_names = list(models_dict.keys())
        for name in model_names:
            model = models_dict.get(name)
            if model is None:
                continue
            try:
                with torch.no_grad():
                    out = model(input_tensor)
                    logits = out[0] if isinstance(out, (list, tuple)) else out
                    outputs.append(logits)
            except Exception as e:
                pass
        if not outputs:
            raise HTTPException(status_code=500)
        try:
            stacked = torch.stack(outputs, dim=0)  # [num_models, 1, C]
            avg_logits = torch.mean(stacked, dim=0)  # [1, C]
            final_logits = avg_logits.squeeze(0)
        except Exception as e:
            final_logits = outputs[0].squeeze(0)
            model_names = [model_names[0]]
    else:
        if model_name not in models_dict:
            raise HTTPException(status_code=400)
        model_names = [model_name]
        model = models_dict[model_name]
        try:
            with torch.no_grad():
                out = model(input_tensor)
                logits = out[0] if isinstance(out, (list, tuple)) else out
                final_logits = logits.squeeze(0)
        except Exception as e:
            raise HTTPException(status_code=500)

    elapsed_ms = (time.time() - start_time) * 1000

    try:
        probs = torch.nn.functional.softmax(final_logits, dim=0).cpu().numpy()
    except Exception as e:
        raise HTTPException(status_code=500)

    top_k = min(top_k, NUM_CLASSES_HON)
    top_indices = np.argsort(probs)[::-1][:top_k]
    top_probs = probs[top_indices]
    top_classes = [CLASS_NAMES_HON[i] for i in top_indices]

    prediction = top_classes[0]
    confidence = float(top_probs[0])
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
            top_k: Optional[int] = Query(default=1, ge=1, le=NUM_CLASSES_WD)
):
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
    except UnidentifiedImageError:
        raise HTTPException(status_code=400)
    except Exception as e:
        raise HTTPException(status_code=500)

    try:
        input_tensor = transform(image).unsqueeze(0).to(device)
    except Exception as e:
        raise HTTPException(status_code=500)

    start_time = time.time()
    model_names = []
    outputs = []

    if model_name == "ensemble":
        if len(models_dict) < 2:
            raise HTTPException(status_code=500)
        model_names = list(models_dict.keys())
        for name in model_names:
            model = models_dict.get(name)
            if model is None:
                continue
            try:
                with torch.no_grad():
                    out = model(input_tensor)
                    logits = out[0] if isinstance(out, (list, tuple)) else out
                    outputs.append(logits)
            except Exception as e:
                pass
        if not outputs:
            raise HTTPException(status_code=500)
        try:
            stacked = torch.stack(outputs, dim=0)  # [num_models, 1, C]
            avg_logits = torch.mean(stacked, dim=0)  # [1, C]
            final_logits = avg_logits.squeeze(0)
        except Exception as e:
            final_logits = outputs[0].squeeze(0)
            model_names = [model_names[0]]
    else:
        if model_name not in models_dict:
            raise HTTPException(status_code=400)
        model_names = [model_name]
        model = models_dict[model_name]
        try:
            with torch.no_grad():
                out = model(input_tensor)
                logits = out[0] if isinstance(out, (list, tuple)) else out
                final_logits = logits.squeeze(0)
        except Exception as e:
            raise HTTPException(status_code=500)

    elapsed_ms = (time.time() - start_time) * 1000

    try:
        probs = torch.nn.functional.softmax(final_logits, dim=0).cpu().numpy()
    except Exception as e:
        raise HTTPException(status_code=500)

    top_k = min(top_k, NUM_CLASSES_WD)
    top_indices = np.argsort(probs)[::-1][:top_k]
    top_probs = probs[top_indices]
    top_classes = [CLASS_NAMES_WD[i] for i in top_indices]

    prediction = top_classes[0]
    confidence = float(top_probs[0])
    probabilities = {cls: float(probs[idx]) for cls, idx in zip(CLASS_NAMES_WD, range(len(CLASS_NAMES_WD)))}

    return PredictionResponse(
        used_model=",".join(model_names),
        prediction=prediction,
        confidence=confidence,
        probabilities=probabilities,
        elapsed_ms=elapsed_ms
    )
if __name__ == "__main__":   #start uvicorn application
    host = "0.0.0.0"   #localhost IP
    port = 8001   #Use port 8001 rather than 8080, considering that Zcorn main system will use that port
    uvicorn.run("app:app", host=host, port=port, reload=True)
