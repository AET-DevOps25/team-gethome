#!/bin/bash

# Setup environment variables for GetHome services
echo "Setting up environment variables for GetHome services..."

# Create .env file from example if it doesn't exist
if [ ! -f .env ]; then
    echo "Creating .env file from env.example..."
    cp env.example .env
    echo "✅ .env file created successfully!"
    echo "⚠️  Please review and update the .env file with your actual credentials"
else
    echo "✅ .env file already exists"
fi

# Export environment variables for current session
if [ -f .env ]; then
    echo "Loading environment variables..."
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ Environment variables loaded successfully!"
else
    echo "❌ .env file not found. Please create it first."
    exit 1
fi

echo ""
echo "🎉 Environment setup complete!"
echo "You can now run the services with:"
echo "  ./gradlew bootRun (for each service)"
echo ""
echo "Remember to:"
echo "  1. Never commit the .env file to version control"
echo "  2. Update the .env file with your actual credentials"
echo "  3. Use different credentials for different environments" 