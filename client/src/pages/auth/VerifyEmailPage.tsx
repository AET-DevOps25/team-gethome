import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import axios from 'axios';

const VerifyEmailPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  
  const [isLoading, setIsLoading] = useState(true);
  const [isVerified, setIsVerified] = useState(false);

  useEffect(() => {
    const verifyEmail = async () => {
      if (!token) {
        setIsLoading(false);
        return;
      }

      try {
        await axios.post('http://localhost:8081/api/v1/auth/verify-email', { token });
        setIsVerified(true);
        toast.success('Email verified successfully');
      } catch (error: any) {
        toast.error(error.response?.data?.message || 'Failed to verify email');
      } finally {
        setIsLoading(false);
      }
    };

    verifyEmail();
  }, [token]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-900 mb-4">Verifying your email</h2>
              <p className="text-gray-600">Please wait while we verify your email address...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!token) {
    return (
      <div className="min-h-screen bg-gray-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-900 mb-4">Invalid Verification Link</h2>
              <p className="text-gray-600 mb-6">
                This email verification link is invalid or has expired. Please request a new verification link.
              </p>
              <button
                onClick={() => navigate('/login')}
                className="text-indigo-600 hover:text-indigo-500 font-medium"
              >
                Return to login
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (isVerified) {
    return (
      <div className="min-h-screen bg-gray-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-900 mb-4">Email Verified!</h2>
              <p className="text-gray-600 mb-6">
                Your email has been successfully verified. You can now log in to your account.
              </p>
              <button
                onClick={() => navigate('/login')}
                className="text-indigo-600 hover:text-indigo-500 font-medium"
              >
                Go to login
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Verification Failed</h2>
            <p className="text-gray-600 mb-6">
              We couldn't verify your email address. The link may have expired or is invalid.
            </p>
            <button
              onClick={() => navigate('/login')}
              className="text-indigo-600 hover:text-indigo-500 font-medium"
            >
              Return to login
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VerifyEmailPage; 