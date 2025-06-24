from fastapi import FastAPI, HTTPException, Header, Path
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional, Dict, Any
from jose import jwt, JWTError
import httpx
import uuid
from datetime import datetime
import os
from langchain.chains import ConversationChain
from langchain.memory import ConversationBufferMemory
from langchain.llms import OpenAI

app = FastAPI()

# --- CONFIG ---
JWT_SECRET = os.getenv("JWT_SECRET", "your_jwt_secret")  # Should be set in env
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
USER_SERVICE_URL = os.getenv("USER_SERVICE_URL", "http://usermanagement-service:8080/api/user/{user_id}/profile")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "sk-xxx")  # Should be set in env

# --- MODELS ---
class ChatMessageRequest(BaseModel):
    message: str

class ChatMessageResponse(BaseModel):
    reply: str
    timestamp: datetime

class SessionResponse(BaseModel):
    session_id: str

# --- SESSION STORAGE ---
sessions: Dict[str, Dict[str, Any]] = {}

# --- UTILS ---
def validate_jwt(token: str) -> dict:
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        return payload
    except JWTError as e:
        raise HTTPException(status_code=401, detail="Invalid JWT token")

def get_user_profile(token: str, user_id: str) -> dict:
    headers = {"Authorization": f"Bearer {token}"}
    url = USER_SERVICE_URL.format(user_id=user_id)
    try:
        with httpx.Client() as client:
            resp = client.get(url, headers=headers, timeout=5)
            if resp.status_code != 200:
                raise HTTPException(status_code=resp.status_code, detail="Failed to fetch user profile")
            return resp.json()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"User profile fetch error: {str(e)}")

def build_system_prompt(profile: dict) -> str:
    # Fill in the prompt template with user profile data
    prompt = f"""
You are an AI companion walking alongside the user to make them feel safe on their journey home.

User Profile:
- Name: {profile.get('alias', 'User')}
- Interests: {', '.join(profile.get('interests', []))}
- Age-Group: {profile.get('ageGroup', 'unknown')}

Current Context:
- Date: {datetime.now().strftime('%Y-%m-%d')}
- Time: {datetime.now().strftime('%H:%M')}
- Journey: Guiding the user on foot toward their destination

Boundaries & Tone:
- Tone: {profile.get('aiTone', 'friendly')}
- Talkativeness: {profile.get('talkativeness', 'medium')}
- Social Distance: {profile.get('socialDistance', 'normal')}

Your Job:
- Engage the user based on their day —ask questions about their evening or share relevant topics, but never stray outside their comfort zones.
- Keep replies aligned with the specified tone, verbosity, and level of personal engagement. Your answers may never exceed 4 sentences.
- The interests named are incidental. They should help you estimate the type of person you are talking to. Do not lean too much into it. If he talks about it, you know you can start a conversation about this topic.
- Don't promise the users action you can not fulfill. Your only way of interacting with the user is through the Emergency Protocol and your talking.
- Always answer in english.

Emergency Protocol (must be enforced without exception):
If you detect any signs of distress, danger, or urgent need for help, begin your response exactly as:
! EMERGENCY DETECTED ! [ CONTEXT: <brief reason> ]
Replace <brief reason> by a one-sentence summary of why you believe it's an emergency. After that, immediately provide calm reassurance.
Always respect the user's boundaries and preferences; your primary goal is to keep them feeling safe, heard, and engaged.
"""
    return prompt

def get_langchain_chain(system_prompt: str) -> ConversationChain:
    llm = OpenAI(openai_api_key=OPENAI_API_KEY, temperature=0.7)
    memory = ConversationBufferMemory()
    chain = ConversationChain(llm=llm, memory=memory, verbose=False)
    # Optionally, you can set the system prompt in the chain if supported
    chain.prompt = system_prompt
    return chain

# --- ENDPOINTS ---

@app.post("/api/chat/sessions", response_model=SessionResponse)
def start_chat_session(Authorization: str = Header(...)):
    # Validate JWT
    token = Authorization.split(" ")[-1]
    payload = validate_jwt(token)
    user_id = payload.get("sub")
    if not user_id:
        raise HTTPException(status_code=400, detail="User ID not found in JWT")
    # Fetch user profile
    profile = get_user_profile(token, user_id)
    # Build system prompt
    system_prompt = build_system_prompt(profile)
    # Create LangChain session
    chain = get_langchain_chain(system_prompt)
    # Store session
    session_id = str(uuid.uuid4())
    sessions[session_id] = {
        "user_id": user_id,
        "chain": chain,
        "created_at": datetime.now(),
    }
    return {"session_id": session_id}

@app.post("/api/chat/sessions/{session_id}/message", response_model=ChatMessageResponse)
def chat_message(session_id: str = Path(...), Authorization: str = Header(...), req: ChatMessageRequest = None):
    token = Authorization.split(" ")[-1]
    payload = validate_jwt(token)
    user_id = payload.get("sub")
    session = sessions.get(session_id)
    if not session or session["user_id"] != user_id:
        raise HTTPException(status_code=404, detail="Session not found or unauthorized")
    chain = session["chain"]
    # Send message to LangChain
    reply = chain.run(req.message)
    return ChatMessageResponse(reply=reply, timestamp=datetime.now())

@app.post("/api/chat/sessions/{session_id}", status_code=204)
def close_session(session_id: str = Path(...), Authorization: str = Header(...)):
    token = Authorization.split(" ")[-1]
    payload = validate_jwt(token)
    user_id = payload.get("sub")
    session = sessions.get(session_id)
    if not session or session["user_id"] != user_id:
        raise HTTPException(status_code=404, detail="Session not found or unauthorized")
    # Tear down session
    del sessions[session_id]
    return JSONResponse(status_code=204, content=None)

# Root endpoint
@app.get("/")
def read_root():
    return {"message": "Hello from the Python service!"}