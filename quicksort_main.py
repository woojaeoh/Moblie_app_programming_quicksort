# main.py
from fastapi import FastAPI
from pydantic import BaseModel
from ai_module import classify_image

app = FastAPI()

# 요청 형식 (이미지 URL만 받는다고 가정)
class AiRequest(BaseModel):
    image_url: str

# 응답 형식
class AiResponse(BaseModel):
    category: str
    detail: str

@app.get("/ping")
def ping():
    return {"message": "pong"}

@app.post("/predict", response_model = AiResponse)
def predict(req: AiRequest):
    
    print("받은 image_url:", req.image_url)
    
    result = classify_image(req.image_url)

    return AiResponse(
        category = result["category"],
        detail = result["detail"]
        )
    
