"""
FastAPI ML microservice. Spring Boot calls this over HTTP; this is where
the actual model lives (trained via train_model.py against MySQL data).

Run:
    uvicorn app.main:app --reload --port 8000
"""
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import List

from .recommender import TutorRecommender

app = FastAPI(title="AI Tutor - Recommendation Service", version="0.2.0")
recommender = TutorRecommender()


class RecommendationItem(BaseModel):
    topic_id: int
    name: str
    subject: str
    score: float
    reason: str


class WeakTopicItem(BaseModel):
    topic_id: int
    name: str
    score: float


class InteractionIn(BaseModel):
    topic_id: int
    score: float = Field(..., ge=0, le=100)
    attempts: int = 1


@app.get("/health")
def health():
    return {
        "status": "ok",
        "model_loaded": recommender.model is not None,
        "students_known": len(recommender.matrix.index),
        "topics": len(recommender.topics),
    }


@app.get("/students/{student_id}/recommendations", response_model=List[RecommendationItem])
def get_recommendations(student_id: int, top_n: int = 5):
    if top_n < 1 or top_n > 20:
        raise HTTPException(status_code=400, detail="top_n must be between 1 and 20")
    return recommender.recommend(student_id, top_n=top_n)


@app.get("/students/{student_id}/recommendations-narrative")
def get_recommendations_narrative(student_id: int, top_n: int = 5):
    if top_n < 1 or top_n > 20:
        raise HTTPException(status_code=400, detail="top_n must be between 1 and 20")
    return {"narrative": recommender.narrative(student_id, top_n=top_n)}


@app.get("/students/{student_id}/weak-topics", response_model=List[WeakTopicItem])
def get_weak_topics(student_id: int):
    return recommender.get_weak_topics(student_id)


@app.post("/students/{student_id}/interactions", status_code=201)
def post_interaction(student_id: int, interaction: InteractionIn):
    recommender.add_interaction(student_id, interaction.topic_id, interaction.score, interaction.attempts)
    return {"message": "interaction recorded", "student_id": student_id, "topic_id": interaction.topic_id}


@app.post("/train")
def retrain():
    """Retrains the model against whatever's currently in MySQL and reloads it."""
    recommender.retrain()
    return {"message": "model retrained"}