import pytest
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
from app.main import app

client = TestClient(app)

def test_root_endpoint():
    """Test the root endpoint"""
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "Hello from the Python service!"}

@patch('app.main.validate_jwt')
@patch('app.main.get_user_profile')
@patch('app.main.get_langchain_chain')
def test_start_chat_session(mock_chain, mock_profile, mock_jwt):
    """Test starting a chat session"""
    # Mock the JWT validation
    mock_jwt.return_value = {"sub": "test-user-id"}
    
    # Mock the user profile
    mock_profile.return_value = {
        "alias": "TestUser",
        "interests": ["technology", "music"],
        "ageGroup": "young",
        "aiTone": "friendly",
        "talkativeness": "medium",
        "socialDistance": "normal"
    }
    
    # Mock the LangChain chain
    mock_chain_instance = MagicMock()
    mock_chain.return_value = mock_chain_instance
    
    response = client.post(
        "/api/chat/sessions",
        headers={"Authorization": "Bearer test-token"}
    )
    
    assert response.status_code == 200
    assert "session_id" in response.json()

@patch('app.main.validate_jwt')
def test_start_chat_session_invalid_token(mock_jwt):
    """Test starting a chat session with invalid token"""
    from fastapi import HTTPException
    mock_jwt.side_effect = HTTPException(status_code=401, detail="Invalid JWT token")
    
    response = client.post(
        "/api/chat/sessions",
        headers={"Authorization": "Bearer invalid-token"}
    )
    
    assert response.status_code == 401

def test_start_chat_session_missing_auth():
    """Test starting a chat session without authorization header"""
    response = client.post("/api/chat/sessions")
    assert response.status_code == 422

@pytest.mark.asyncio
async def test_ai_service_structure():
    """Test that the AI service has the expected structure"""
    # Test that the app is properly configured
    assert app is not None
    assert hasattr(app, 'routes')

def test_ai_service_available():
    """Test that the AI service is available"""
    response = client.get("/")
    assert response.status_code == 200 