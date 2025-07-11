from fastapi import FastAPI, HTTPException, Header, Path
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, Dict, Any
from jose import jwt, JWTError
import httpx
import uuid
from datetime import datetime
import os
from langchain.chains import ConversationChain
from langchain.memory import ConversationBufferMemory
from langchain_openai import ChatOpenAI

app = FastAPI()

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",  # React frontend
        "http://react-client",    # Docker container name
        "http://react-client:80", # Docker container with port
    ],
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type", "Accept"],
)

# --- CONFIG ---
JWT_SECRET = os.getenv("JWT_SECRET")  # Match other services
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
USER_SERVICE_URL = os.getenv("USER_SERVICE_URL", "http://usermanagement-service:8080/api/users/{user_id}/profile")
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
    try:
        # Initialize ChatOpenAI with proper configuration
        llm = ChatOpenAI(
            openai_api_key=OPENAI_API_KEY,
            model_name="gpt-3.5-turbo",
            temperature=0.7,
            max_tokens=150
        )
        
        # Create memory for conversation history
        memory = ConversationBufferMemory()
        
        # Create conversation chain
        chain = ConversationChain(
            llm=llm,
            memory=memory,
            verbose=False
        )
        
        # Add system prompt to memory as initial context
        memory.chat_memory.add_ai_message(system_prompt)
        
        return chain
    except Exception as e:
        print(f"Error creating LangChain chain: {str(e)}")
        raise e

# --- ENDPOINTS ---

@app.post("/api/chat/sessions", response_model=SessionResponse)
def start_chat_session(Authorization: str = Header(...)):
    try:
        # Validate JWT
        token = Authorization.split(" ")[-1]
        payload = validate_jwt(token)
        user_id = payload.get("sub")
        if not user_id:
            raise HTTPException(status_code=400, detail="User ID not found in JWT")
        
        print(f"DEBUG: Creating session for user {user_id}")
        
        # Fetch user profile
        try:
            profile = get_user_profile(token, user_id)
            print(f"DEBUG: User profile retrieved: {profile}")
        except Exception as e:
            print(f"ERROR: Failed to get user profile: {str(e)}")
            # Use a default profile if user profile fetch fails
            profile = {
                "alias": "User",
                "interests": [],
                "ageGroup": "unknown",
                "aiTone": "friendly",
                "talkativeness": "medium",
                "socialDistance": "normal"
            }
            print("DEBUG: Using default profile")
        
        # Build system prompt
        try:
            system_prompt = build_system_prompt(profile)
            print("DEBUG: System prompt built successfully")
        except Exception as e:
            print(f"ERROR: Failed to build system prompt: {str(e)}")
            raise HTTPException(status_code=500, detail=f"Failed to build system prompt: {str(e)}")
        
        # Create LangChain session
        try:
            # Check if OpenAI API key is properly configured
            if OPENAI_API_KEY == "sk-xxx" or not OPENAI_API_KEY:
                print("WARNING: OpenAI API key not configured, using mock responses")
                # Use mock chain when API key is not configured
                chain = {"type": "mock", "system_prompt": system_prompt}
            else:
                # Create real LangChain chain with OpenAI
                chain = get_langchain_chain(system_prompt)
                print("DEBUG: Real LangChain chain created successfully")
        except Exception as e:
            print(f"ERROR: Failed to create LangChain chain: {str(e)}")
            print("WARNING: Falling back to mock responses")
            # Fallback to mock on any error
            chain = {"type": "mock", "system_prompt": system_prompt}
        
        # Store session
        session_id = str(uuid.uuid4())
        sessions[session_id] = {
            "user_id": user_id,
            "chain": chain,
            "created_at": datetime.now(),
        }
        print(f"DEBUG: Session {session_id} created successfully")
        
        return {"session_id": session_id}
        
    except HTTPException:
        raise
    except Exception as e:
        print(f"UNEXPECTED ERROR in start_chat_session: {str(e)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")

@app.post("/api/chat/sessions/{session_id}/message", response_model=ChatMessageResponse)
def chat_message(session_id: str = Path(...), Authorization: str = Header(...), req: ChatMessageRequest = None):
    try:
        token = Authorization.split(" ")[-1]
        payload = validate_jwt(token)
        user_id = payload.get("sub")
        session = sessions.get(session_id)
        if not session or session["user_id"] != user_id:
            raise HTTPException(status_code=404, detail="Session not found or unauthorized")
        
        chain = session["chain"]
        
        # Handle different types of chains
        if isinstance(chain, dict) and chain.get("type") == "mock":
            # Return a simple response for testing/fallback
            reply = f"Hello! I'm your GetHome AI companion. You said: '{req.message}'. I'm here to keep you safe on your journey. How can I help you today?"
        else:
            # Send message to real LangChain
            try:
                reply = chain.run(req.message)
            except Exception as e:
                print(f"ERROR: Failed to get response from LangChain: {str(e)}")
                # Fallback to mock response on error
                reply = f"I'm having trouble thinking right now, but I'm still here with you! You said: '{req.message}'. Let me try to help you stay safe on your journey."
        
        return ChatMessageResponse(reply=reply, timestamp=datetime.now())
        
    except HTTPException:
        raise
    except Exception as e:
        print(f"ERROR in chat_message: {str(e)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Failed to process message: {str(e)}")

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

# Health endpoint
@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "ai-service"}