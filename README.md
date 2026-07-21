# Tutor

Tutor is an AI study helper for students. It helps students know what topic they should study next. The system looks at quiz scores and gives study recommendations. It also explains why a topic is important and creates quizzes for practice.

## Features

- Topic recommendations based on quiz scores
- AI explanation for each recommendation
- AI quiz generation
- Score history
- Weak topic analysis
- Simple and fast web interface

## Technologies

- Spring Boot
- Java
- Python
- FastAPI
- scikit-learn (Random Forest)
- Groq API (Llama 3.3)
- Spring AI + Gemini
- MySQL
- HTML
- CSS
- JavaScript

## How It Works

1. The student takes a quiz.
2. The score is saved in MySQL.
3. The ML model checks previous scores.
4. The system recommends the next topic.
5. AI writes a simple explanation.
6. The student can take more quizzes and improve weak topics.

## Project Structure

```
Frontend (HTML, CSS, JS)
        │
        ▼
Spring Boot Backend
        │
 ┌──────┴──────┐
 ▼             ▼
MySQL      FastAPI ML Service
                 │
                 ▼
      Random Forest Model
```

## Challenges

- Fixed missing Groq API key.
- Solved duplicate quiz history caused by multiple clicks.
- Fixed scikit-learn version mismatch.
- Connected Spring Boot and FastAPI with one database safely.

## What We Learned

- Good logging makes debugging easier.
- Disable buttons while sending requests.
- Keep ML library versions the same.
- AI and machine learning can work together to improve learning.

## Future Work

- User login
- Automatic model retraining
- Spaced repetition
- Better mobile design
- Smarter recommendations

## Team

Built for the Devpost Hackathon.