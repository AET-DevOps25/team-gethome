import React from 'react';
import { Link } from 'react-router-dom';

interface AuthLayoutProps {
  children: React.ReactNode;
}

const AuthLayout: React.FC<AuthLayoutProps> = ({ children }) => {
  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex">
              <div className="flex-shrink-0 flex items-center">
                <Link to="/" className="text-2xl font-bold text-indigo-600">
                  GetHome
                </Link>
              </div>
            </div>
            <div className="flex items-center">
              <Link
                to="/login"
                className="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium"
              >
                Sign in
              </Link>
              <Link
                to="/register"
                className="ml-4 bg-indigo-600 text-white hover:bg-indigo-700 px-4 py-2 rounded-md text-sm font-medium"
              >
                Sign up
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Main content */}
      <main>{children}</main>

      {/* Footer */}
      <footer className="bg-white">
        <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="text-center text-sm text-gray-500">
            <p>&copy; {new Date().getFullYear()} GetHome. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default AuthLayout; 