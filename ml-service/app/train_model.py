import numpy as np
import pandas as pd
import joblib
import os
from sqlalchemy import create_engine
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error

DB_URL = os.environ.get("DB_URL", "mysql+pymysql://root:root@localhost:3306/ai_tutor")
MODEL_PATH = os.path.join(os.path.dirname(__file__), "data", "model.joblib")


def load_data(engine):
    topics = pd.read_sql("SELECT * FROM topics", engine)
    interactions = pd.read_sql("SELECT * FROM interactions", engine)
    return topics, interactions


def build_features(interactions: pd.DataFrame, topics: pd.DataFrame) -> pd.DataFrame:
    df = interactions.merge(topics, left_on="topic_id", right_on="id", suffixes=("", "_topic"))

    # student's average score on OTHER topics - leaking the current score would be cheating
    df["student_avg_other"] = df.groupby("student_id")["score"].transform(
        lambda s: (s.sum() - s) / (len(s) - 1) if len(s) > 1 else 50.0
    )

    # did the student master this topic's prerequisite? (score >= 70 on prereq_id)
    mastered = set(
        tuple(x) for x in df.loc[df["score"] >= 70, ["student_id", "topic_id"]].values
    )
    df["prereq_mastered"] = df.apply(
        lambda r: 1 if pd.notna(r["prerequisite_id"]) and (r["student_id"], r["prerequisite_id"]) in mastered else 0,
        axis=1,
    )

    subject_dummies = pd.get_dummies(df["subject"], prefix="subject")
    features = pd.concat([
        df[["difficulty", "student_avg_other", "prereq_mastered", "attempts"]],
        subject_dummies,
    ], axis=1)

    return features, df["score"]


def main():
    engine = create_engine(DB_URL)
    topics, interactions = load_data(engine)

    if len(interactions) < 20:
        raise SystemExit("Not enough interactions in the DB to train on yet (need 20+).")

    X, y = build_features(interactions, topics)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    model = RandomForestRegressor(n_estimators=200, max_depth=6, random_state=42)
    model.fit(X_train, y_train)

    preds = model.predict(X_test)
    mae = mean_absolute_error(y_test, preds)
    print(f"Trained on {len(X_train)} rows, tested on {len(X_test)} rows. MAE: {mae:.2f} points (out of 100).")

    os.makedirs(os.path.dirname(MODEL_PATH), exist_ok=True)
    joblib.dump({"model": model, "feature_columns": list(X.columns)}, MODEL_PATH)
    print(f"Saved trained model -> {MODEL_PATH}")


if __name__ == "__main__":
    main()
