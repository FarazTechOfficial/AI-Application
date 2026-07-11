"""
Recommendation engine - now backed by MySQL and an actual trained model.

For each candidate topic, we ask the trained RandomForestRegressor
"what score would this student probably get here?" and blend that with
a collaborative signal (what similar students scored on this topic).
Same 60/40 blend as before, but the readiness number now comes from a
real model instead of a hand-written formula.
"""
import os
import joblib
import numpy as np
import pandas as pd
from sqlalchemy import create_engine
from sklearn.metrics.pairwise import cosine_similarity

from .llm_reasoner import explain_recommendations, generate_narrative

DB_URL = os.environ.get("DB_URL", "mysql+pymysql://root:root@localhost:3306/ai_tutor")
MODEL_PATH = os.path.join(os.path.dirname(__file__), "data", "model.joblib")

MASTERY_THRESHOLD = 70.0
WEAK_THRESHOLD = 60.0


class TutorRecommender:
    def __init__(self):
        self.engine = create_engine(DB_URL)
        self._load_model()
        self.reload_data()

    def _load_model(self):
        if os.path.exists(MODEL_PATH):
            bundle = joblib.load(MODEL_PATH)
            self.model = bundle["model"]
            self.feature_columns = bundle["feature_columns"]
        else:
            self.model = None
            self.feature_columns = []

    def reload_data(self):
        self.topics = pd.read_sql("SELECT * FROM topics", self.engine).set_index("id")
        self.interactions = pd.read_sql("SELECT * FROM interactions", self.engine)
        self.matrix = self.interactions.pivot_table(
            index="student_id", columns="topic_id", values="score", aggfunc="mean"
        )
        for tid in self.topics.index:
            if tid not in self.matrix.columns:
                self.matrix[tid] = np.nan
        self.matrix = self.matrix[sorted(self.matrix.columns)]

    def add_interaction(self, student_id: int, topic_id: int, score: float, attempts: int = 1):
        self.reload_data()

    def retrain(self):
        from . import train_model
        train_model.main()
        self._load_model()

    def _similar_students(self, student_id: int, k: int = 5):
        if student_id not in self.matrix.index:
            return []
        filled = self.matrix.fillna(self.matrix.mean(numeric_only=True))
        sims = cosine_similarity(filled)
        idx = list(self.matrix.index).index(student_id)
        ranked = sorted(enumerate(sims[idx]), key=lambda x: x[1], reverse=True)
        return [self.matrix.index[i] for i, _ in ranked if i != idx][:k]

    def _collaborative_signal(self, student_id: int, topic_id: int) -> float:
        peers = self._similar_students(student_id)
        if not peers:
            return 0.5
        peer_scores = self.matrix.loc[peers, topic_id].dropna()
        return float(np.clip(peer_scores.mean() / 100.0, 0, 1)) if not peer_scores.empty else 0.5

    def _model_readiness(self, student_id: int, topic_id: int) -> float:
        """Predicted score / 100, using the trained model. Falls back to a simple
        rule if there's no model yet or the student has no history."""
        topic = self.topics.loc[topic_id]

        if student_id not in self.matrix.index or self.model is None:
            return 0.5

        student_scores = self.matrix.loc[student_id].dropna()
        avg_other = student_scores.mean() if not student_scores.empty else 50.0

        prereq = topic["prerequisite_id"]
        prereq_mastered = 0
        if pd.notna(prereq) and prereq in student_scores.index and student_scores[prereq] >= MASTERY_THRESHOLD:
            prereq_mastered = 1

        row = {"difficulty": topic["difficulty"], "student_avg_other": avg_other,
               "prereq_mastered": prereq_mastered, "attempts": 1}
        for col in self.feature_columns:
            if col.startswith("subject_"):
                row[col] = 1 if col == f"subject_{topic['subject']}" else 0

        X = pd.DataFrame([row])[self.feature_columns]
        predicted_score = self.model.predict(X)[0]
        return float(np.clip(predicted_score / 100.0, 0, 1))

    def get_weak_topics(self, student_id: int):
        self.reload_data()
        if student_id not in self.matrix.index:
            return []
        row = self.matrix.loc[student_id]
        weak = row[(row.notna()) & (row < WEAK_THRESHOLD)]
        return [{"topic_id": int(tid), "name": self.topics.loc[tid, "name"], "score": float(sc)}
                for tid, sc in weak.items()]

    def _build_candidates(self, student_id: int, top_n: int):
        self.reload_data()
        attempted = self.matrix.loc[student_id].dropna()
        mastered = set(attempted[attempted >= MASTERY_THRESHOLD].index)
        candidates = [tid for tid in self.topics.index if tid not in mastered]

        results = []
        for tid in candidates:
            readiness = self._model_readiness(student_id, tid)
            peer_signal = self._collaborative_signal(student_id, tid)
            final_score = 0.6 * readiness + 0.4 * peer_signal

            topic = self.topics.loc[tid]
            already_tried = tid in attempted.index
            previous_score = round(float(attempted[tid]), 1) if already_tried else None

            if pd.notna(topic["prerequisite_id"]):
                prereq_name = self.topics.loc[topic["prerequisite_id"], "name"]
                prereq_status = f"prerequisite '{prereq_name}' mastered" if topic["prerequisite_id"] in mastered \
                    else f"prerequisite '{prereq_name}' not yet mastered"
            else:
                prereq_status = "no prerequisite"

            if already_tried and attempted[tid] < WEAK_THRESHOLD:
                fallback_reason = f"Review recommended - previous score {attempted[tid]:.0f}/100."
                final_score += 0.15
            elif pd.notna(topic["prerequisite_id"]) and topic["prerequisite_id"] in attempted.index:
                fallback_reason = f"Prerequisite '{self.topics.loc[topic['prerequisite_id'], 'name']}' mastered - ready for next step."
            else:
                fallback_reason = "Model predicts a good fit based on your history."

            results.append({
                "topic_id": int(tid), "name": topic["name"], "subject": topic["subject"],
                "score": round(float(np.clip(final_score, 0, 1)), 3), "reason": fallback_reason,
                "difficulty": int(topic["difficulty"]), "prereq_status": prereq_status,
                "previous_score": previous_score, "peer_average": round(peer_signal * 100),
            })

        results.sort(key=lambda r: r["score"], reverse=True)
        return results[:top_n]

    def recommend(self, student_id: int, top_n: int = 5):
        self.reload_data()
        if student_id not in self.matrix.index:
            starter = self.topics[self.topics["prerequisite_id"].isna()].sort_values("difficulty")
            return [
                {"topic_id": int(tid), "name": r["name"], "subject": r["subject"], "score": 1.0,
                 "reason": "New student - starting with a foundational topic."}
                for tid, r in starter.head(top_n).iterrows()
            ]

        top_results = self._build_candidates(student_id, top_n)

        llm_reasons = explain_recommendations(top_results)
        if llm_reasons:
            for item, reason in zip(top_results, llm_reasons):
                item["reason"] = reason

        for item in top_results:
            item.pop("difficulty", None)
            item.pop("prereq_status", None)
            item.pop("previous_score", None)
            item.pop("peer_average", None)

        return top_results

    def narrative(self, student_id: int, top_n: int = 5):
        self.reload_data()
        if student_id not in self.matrix.index:
            return ("You're just getting started - there's no history yet, so let's begin "
                    "with the foundational topics in each subject before anything else.")

        top_results = self._build_candidates(student_id, top_n)
        text = generate_narrative(top_results)
        if text:
            return text

        parts = [f"{r['name']} ({r['subject']}, {round(r['score'] * 100)}% fit)" for r in top_results]
        return "Based on your history, here's what's recommended next: " + "; ".join(parts) + "."