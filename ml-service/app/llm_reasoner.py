import os
import json
import requests

GROQ_API_KEY = os.environ.get("GROQ_API_KEY")
GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"


def explain_recommendations(items):
    if not GROQ_API_KEY or not items:
        return None

    facts = [
        {
            "topic": it["name"],
            "subject": it["subject"],
            "predicted_fit_percent": round(it["score"] * 100),
            "difficulty_out_of_5": it["difficulty"],
            "prerequisite_status": it["prereq_status"],
            "previous_score": it["previous_score"],
            "similar_students_average": it["peer_average"],
        }
        for it in items
    ]

    prompt = (
            "You're an AI tutor explaining why each topic below is recommended to a "
            "student. Write one short, natural sentence per topic (max 18 words), "
            "grounded only in the facts given - never invent a number that isn't there. "
            "Vary the phrasing across topics, don't reuse the same sentence structure. "
            "Return ONLY a JSON array of strings, same order as the input, no markdown.\n\n"
            + json.dumps(facts)
    )

    try:
        resp = requests.post(
            GROQ_URL,
            headers={"Authorization": f"Bearer {GROQ_API_KEY}", "Content-Type": "application/json"},
            json={
                "model": "llama-3.3-70b-versatile",
                "temperature": 0.6,
                "messages": [{"role": "user", "content": prompt}],
            },
            timeout=12,
        )
        resp.raise_for_status()
        content = resp.json()["choices"][0]["message"]["content"]
        cleaned = content.strip().removeprefix("```json").removesuffix("```").strip()
        reasons = json.loads(cleaned)
        if isinstance(reasons, list) and len(reasons) == len(items):
            return reasons
    except Exception:
        return None
    return None


def generate_narrative(items):
    if not GROQ_API_KEY or not items:
        return None

    facts = [
        {
            "topic": it["name"],
            "subject": it["subject"],
            "predicted_fit_percent": round(it["score"] * 100),
            "difficulty_out_of_5": it["difficulty"],
            "prerequisite_status": it["prereq_status"],
            "previous_score": it["previous_score"],
            "similar_students_average": it["peer_average"],
        }
        for it in items
    ]

    prompt = (
            "You're an AI tutor talking directly to a student about what to study next. "
            "Write ONE flowing paragraph, 4-6 sentences, in a warm and direct tone - not "
            "a list, not bullet points, no headers. Walk through the recommended topics "
            "below in order, weaving in the reasoning naturally like a real tutor would "
            "speak. Ground every claim only in the facts given, never invent a number. "
            "Return ONLY the paragraph text, nothing else.\n\n"
            + json.dumps(facts)
    )

    try:
        resp = requests.post(
            GROQ_URL,
            headers={"Authorization": f"Bearer {GROQ_API_KEY}", "Content-Type": "application/json"},
            json={
                "model": "llama-3.3-70b-versatile",
                "temperature": 0.7,
                "messages": [{"role": "user", "content": prompt}],
            },
            timeout=12,
        )
        resp.raise_for_status()
        content = resp.json()["choices"][0]["message"]["content"]
        return content.strip().strip('"')
    except Exception:
        return None