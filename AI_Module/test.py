import torch
import torchvision.transforms as transforms
from PIL import Image
from models.models import deit_small_patch16_224

transform = transforms.Compose([
    transforms.Resize(256),
    transforms.CenterCrop(224),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

model = deit_small_patch16_224(num_classes=4)

checkpoint = torch.load('results/best_model__vit_lite.pth', map_location="cpu", weights_only=False)  # 替换为实际的检查点路径
pretrained_dict = checkpoint['model']
model_dict = model.state_dict()
pretrained_dict = {k: v for k, v in pretrained_dict.items() if
                   k in model_dict and model_dict[k].shape == v.shape}
model_dict.update(pretrained_dict)
model.load_state_dict(model_dict)
model.eval()
