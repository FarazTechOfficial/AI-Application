import numpy as np
from sqlalchemy import create_engine, text
from datetime import datetime, timedelta
import random

engine = create_engine("mysql+pymysql://root:root@localhost:3306/ai_tutor")

TOPICS = [
    (1, "Math", "Whole Numbers", 1, None),
    (2, "Math", "Fractions", 2, 1),
    (3, "Math", "Decimals", 2, 2),
    (4, "Math", "Ratios & Proportions", 3, 3),
    (5, "Math", "Linear Equations", 3, 4),
    (6, "Math", "Quadratic Equations", 4, 5),
    (7, "Math", "Basic Trigonometry", 4, 5),
    (8, "Math", "Calculus Intro", 5, 6),
    (9, "Science", "Matter & Materials", 1, None),
    (10, "Science", "Atoms & Molecules", 2, 9),
    (11, "Science", "Chemical Reactions", 3, 10),
    (12, "Science", "Forces & Motion", 2, None),
    (13, "Science", "Energy", 3, 12),
    (14, "Science", "Electricity Basics", 4, 13),
    (15, "English", "Grammar Basics", 1, None),
    (16, "English", "Sentence Structure", 2, 15),
    (17, "English", "Essay Writing", 3, 16),
    (18, "English", "Literary Analysis", 4, 17),
]

np.random.seed(7)
random.seed(7)

with engine.begin() as conn:
    conn.execute(text("SET FOREIGN_KEY_CHECKS=0"))
    conn.execute(text("TRUNCATE TABLE interactions"))
    conn.execute(text("TRUNCATE TABLE students"))
    conn.execute(text("DELETE FROM topics"))

    for tid, subject, name, diff, prereq in TOPICS:
        conn.execute(
            text("INSERT INTO topics (id, subject, name, difficulty, prerequisite_id) "
                 "VALUES (:id, :subject, :name, :diff, :prereq)"),
            {"id": tid, "subject": subject, "name": name, "diff": diff, "prereq": prereq},
        )

    subjects = ["Math", "Science", "English"]
    n_students = 80
    student_ids = []
    for i in range(n_students):
        result = conn.execute(
            text("INSERT INTO students (name, email, grade_level) VALUES (:n, :e, :g)"),
            {"n": f"Student {i+1}", "e": f"student{i+1}@example.com", "g": random.choice(["6th", "7th", "8th", "9th"])},
        )
        student_ids.append(result.lastrowid)

    ability = {sid: {s: np.random.uniform(0.3, 1.0) for s in subjects} for sid in student_ids}

    rows = []
    for sid in student_ids:
        n_attempts = np.random.randint(6, 14)
        chosen_topics = random.sample(TOPICS, k=min(n_attempts, len(TOPICS)))
        base_date = datetime.now() - timedelta(days=180)
        for tid, subject, name, diff, prereq in chosen_topics:
            a = ability[sid][subject]
            penalty = (diff - 1) * 0.12
            score = np.clip(a - penalty + np.random.normal(0, 0.08), 0.05, 1.0) * 100
            attempts = np.random.choice([1, 1, 1, 2, 2, 3], p=[0.4, 0.2, 0.15, 0.15, 0.05, 0.05])
            recorded_at = base_date + timedelta(days=int(np.random.randint(0, 180)))
            rows.append({"sid": sid, "tid": tid, "score": round(float(score), 1),
                         "att": int(attempts), "ts": recorded_at})

    for r in rows:
        conn.execute(
            text("INSERT INTO interactions (student_id, topic_id, score, attempts, recorded_at) "
                 "VALUES (:sid, :tid, :score, :att, :ts)"),
            r,
        )

    conn.execute(text("SET FOREIGN_KEY_CHECKS=1"))

print(f"Seeded {len(TOPICS)} topics, {n_students} students, {len(rows)} interactions into MySQL.")
