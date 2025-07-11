import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import axios from 'axios';
import { 
  Alert, 
  Box, 
  CircularProgress, 
  LinearProgress, 
  Stepper, 
  Step, 
  StepLabel,
  Snackbar,
  Typography,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemIcon,
  ListItemText
} from '@mui/material';
import { 
  CheckCircle, 
  ErrorOutline, 
  Visibility, 
  VisibilityOff, 
  Security,
  Email,
  VpnKey,
  PersonAdd
} from '@mui/icons-material';

interface RegistrationSteps {
  validation: 'pending' | 'success' | 'error';
  submission: 'pending' | 'loading' | 'success' | 'error';
  redirect: 'pending' | 'loading' | 'success' | 'error';
}

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [registrationSteps, setRegistrationSteps] = useState<RegistrationSteps>({
    validation: 'pending',
    submission: 'pending',
    redirect: 'pending'
  });
  const [errors, setErrors] = useState<{[key: string]: string}>({});
  const [successMessage, setSuccessMessage] = useState('');
  const [detailedError, setDetailedError] = useState('');
  const [showSteps, setShowSteps] = useState(false);

  const validateForm = () => {
    const newErrors: {[key: string]: string} = {};
    
    if (!formData.name.trim()) {
      newErrors.name = 'Name is required';
    } else if (formData.name.trim().length < 2) {
      newErrors.name = 'Name must be at least 2 characters';
    }
    
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
    }
    
    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(formData.password)) {
      newErrors.password = 'Password must contain at least one uppercase letter, one lowercase letter, and one number';
    }
    
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
    
    // Clear specific field error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
    
    // Clear general errors
    setDetailedError('');
  };

  const getPasswordStrength = (password: string) => {
    let strength = 0;
    if (password.length >= 6) strength += 25;
    if (password.length >= 8) strength += 25;
    if (/(?=.*[a-z])(?=.*[A-Z])/.test(password)) strength += 25;
    if (/(?=.*\d)/.test(password)) strength += 25;
    
    if (strength <= 25) return { strength, label: 'Weak', color: '#f44336' };
    if (strength <= 50) return { strength, label: 'Fair', color: '#ff9800' };
    if (strength <= 75) return { strength, label: 'Good', color: '#2196f3' };
    return { strength, label: 'Strong', color: '#4caf50' };
  };

  const passwordStrength = getPasswordStrength(formData.password);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setDetailedError('');
    setSuccessMessage('');
    setShowSteps(true);
    
    // Step 1: Validation
    setRegistrationSteps(prev => ({ ...prev, validation: 'pending' }));
    
    if (!validateForm()) {
      setRegistrationSteps(prev => ({ ...prev, validation: 'error' }));
      toast.error('Please fix the form errors before submitting');
      return;
    }
    
    setRegistrationSteps(prev => ({ ...prev, validation: 'success' }));
    setIsLoading(true);

    // Step 2: Account Creation
    setRegistrationSteps(prev => ({ ...prev, submission: 'loading' }));
    
    try {
      const response = await axios.post('http://localhost:8081/api/v1/auth/register', {
        name: formData.name,
        email: formData.email,
        password: formData.password,
      });
      
      setRegistrationSteps(prev => ({ ...prev, submission: 'success' }));
      setSuccessMessage('Account created successfully! 🎉');
      
      // Step 3: Redirect
      setRegistrationSteps(prev => ({ ...prev, redirect: 'loading' }));
      
      setTimeout(() => {
        setRegistrationSteps(prev => ({ ...prev, redirect: 'success' }));
        toast.success('Welcome to GetHome! Redirecting to complete your profile...');
        
        setTimeout(() => {
          navigate('/user/profile-completion');
        }, 1500);
      }, 1000);
      
    } catch (error) {
      setRegistrationSteps(prev => ({ ...prev, submission: 'error' }));
      
      if (axios.isAxiosError(error)) {
        const status = error.response?.status;
        const message = error.response?.data?.message;
        
        if (status === 409) {
          setDetailedError('An account with this email already exists. Please try logging in instead.');
          setErrors({ email: 'Email already registered' });
        } else if (status === 400) {
          setDetailedError(message || 'Invalid registration data. Please check your information.');
        } else if (status === 500) {
          setDetailedError('Server error occurred. Please try again later.');
        } else if (error.code === 'ERR_NETWORK') {
          setDetailedError('Unable to connect to the server. Please check your internet connection.');
        } else {
          setDetailedError(message || 'Registration failed. Please try again.');
        }
        
        toast.error(error.response?.data?.message || "Registration failed. Please try again.");
      } else {
        setDetailedError("An unexpected error occurred. Please try again.");
        toast.error("Unexpected error occurred");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const steps = ['Validate Information', 'Create Account', 'Setup Profile'];
  
  const getActiveStep = () => {
    if (registrationSteps.redirect !== 'pending') return 2;
    if (registrationSteps.submission !== 'pending') return 1;
    if (registrationSteps.validation !== 'pending') return 0;
    return 0;
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Create your account
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Or{' '}
          <Link to="/login" className="font-medium text-indigo-600 hover:text-indigo-500">
            sign in to your account
          </Link>
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          
          {/* Registration Progress */}
          {showSteps && (
            <Box sx={{ mb: 4 }}>
              <Stepper activeStep={getActiveStep()} alternativeLabel>
                {steps.map((label, index) => (
                  <Step key={label}>
                    <StepLabel
                      error={
                        (index === 0 && registrationSteps.validation === 'error') ||
                        (index === 1 && registrationSteps.submission === 'error')
                      }
                    >
                      {label}
                    </StepLabel>
                  </Step>
                ))}
              </Stepper>
              {isLoading && <LinearProgress sx={{ mt: 2 }} />}
            </Box>
          )}

          {/* Success Message */}
          {successMessage && (
            <Alert severity="success" sx={{ mb: 2 }} icon={<CheckCircle />}>
              {successMessage}
            </Alert>
          )}

          {/* Detailed Error Message */}
          {detailedError && (
            <Alert severity="error" sx={{ mb: 2 }} icon={<ErrorOutline />}>
              <Typography variant="body2" component="div">
                {detailedError}
              </Typography>
              {detailedError.includes('email already exists') && (
                <Box sx={{ mt: 1 }}>
                  <Link to="/login" className="text-blue-600 underline">
                    Go to login page
                  </Link>
                </Box>
              )}
            </Alert>
          )}

          <form className="space-y-6" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                Full Name
              </label>
              <div className="mt-1">
                <input
                  id="name"
                  name="name"
                  type="text"
                  required
                  value={formData.name}
                  onChange={handleChange}
                  className={`appearance-none block w-full px-3 py-2 border ${
                    errors.name ? 'border-red-500' : 'border-gray-300'
                  } rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm`}
                  placeholder="Enter your full name"
                />
                {errors.name && (
                  <p className="mt-1 text-sm text-red-600">{errors.name}</p>
                )}
              </div>
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                Email address
              </label>
              <div className="mt-1">
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  value={formData.email}
                  onChange={handleChange}
                  className={`appearance-none block w-full px-3 py-2 border ${
                    errors.email ? 'border-red-500' : 'border-gray-300'
                  } rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm`}
                  placeholder="you@example.com"
                />
                {errors.email && (
                  <p className="mt-1 text-sm text-red-600">{errors.email}</p>
                )}
              </div>
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <div className="mt-1 relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={formData.password}
                  onChange={handleChange}
                  className={`appearance-none block w-full px-3 py-2 pr-10 border ${
                    errors.password ? 'border-red-500' : 'border-gray-300'
                  } rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm`}
                  placeholder="Create a strong password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <VisibilityOff /> : <Visibility />}
                </button>
              </div>
              
              {/* Password Strength Indicator */}
              {formData.password && (
                <Box sx={{ mt: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <LinearProgress
                      variant="determinate"
                      value={passwordStrength.strength}
                      sx={{
                        flex: 1,
                        height: 6,
                        borderRadius: 3,
                        '& .MuiLinearProgress-bar': {
                          backgroundColor: passwordStrength.color
                        }
                      }}
                    />
                    <Typography variant="caption" style={{ color: passwordStrength.color }}>
                      {passwordStrength.label}
                    </Typography>
                  </Box>
                </Box>
              )}
              
              {errors.password && (
                <p className="mt-1 text-sm text-red-600">{errors.password}</p>
              )}
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                Confirm password
              </label>
              <div className="mt-1 relative">
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  required
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className={`appearance-none block w-full px-3 py-2 pr-10 border ${
                    errors.confirmPassword ? 'border-red-500' : 'border-gray-300'
                  } rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm`}
                  placeholder="Confirm your password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                >
                  {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className="mt-1 text-sm text-red-600">{errors.confirmPassword}</p>
              )}
            </div>

            <div>
              <button
                type="submit"
                disabled={isLoading}
                className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white ${
                  isLoading 
                    ? 'bg-gray-400 cursor-not-allowed' 
                    : 'bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500'
                }`}
              >
                {isLoading ? (
                  <div className="flex items-center">
                    <CircularProgress size={20} color="inherit" sx={{ mr: 1 }} />
                    Creating account...
                  </div>
                ) : (
                  'Create account'
                )}
              </button>
            </div>
          </form>
          
          {/* Registration Status Information */}
          {showSteps && (
            <Card sx={{ mt: 3 }}>
              <CardContent sx={{ p: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Registration Status
                </Typography>
                <List dense>
                  <ListItem>
                    <ListItemIcon>
                      {registrationSteps.validation === 'success' ? (
                        <CheckCircle color="success" />
                      ) : registrationSteps.validation === 'error' ? (
                        <ErrorOutline color="error" />
                      ) : (
                        <Security color="disabled" />
                      )}
                    </ListItemIcon>
                    <ListItemText 
                      primary="Form Validation"
                      secondary={
                        registrationSteps.validation === 'success' ? 'All information validated' :
                        registrationSteps.validation === 'error' ? 'Please fix errors above' :
                        'Checking form data...'
                      }
                    />
                  </ListItem>
                  
                  <ListItem>
                    <ListItemIcon>
                      {registrationSteps.submission === 'success' ? (
                        <CheckCircle color="success" />
                      ) : registrationSteps.submission === 'error' ? (
                        <ErrorOutline color="error" />
                      ) : registrationSteps.submission === 'loading' ? (
                        <CircularProgress size={20} />
                      ) : (
                        <PersonAdd color="disabled" />
                      )}
                    </ListItemIcon>
                    <ListItemText 
                      primary="Account Creation"
                      secondary={
                        registrationSteps.submission === 'success' ? 'Account created successfully' :
                        registrationSteps.submission === 'error' ? 'Account creation failed' :
                        registrationSteps.submission === 'loading' ? 'Creating your account...' :
                        'Waiting to create account'
                      }
                    />
                  </ListItem>
                  
                  <ListItem>
                    <ListItemIcon>
                      {registrationSteps.redirect === 'success' ? (
                        <CheckCircle color="success" />
                      ) : registrationSteps.redirect === 'loading' ? (
                        <CircularProgress size={20} />
                      ) : (
                        <VpnKey color="disabled" />
                      )}
                    </ListItemIcon>
                    <ListItemText 
                      primary="Profile Setup"
                      secondary={
                        registrationSteps.redirect === 'success' ? 'Redirecting to profile setup' :
                        registrationSteps.redirect === 'loading' ? 'Preparing profile setup...' :
                        'Ready for profile completion'
                      }
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};

export default RegisterPage; 