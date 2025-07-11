import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

// Mock the services
jest.mock('./services/authService', () => ({
  authService: {
    isAuthenticated: jest.fn(() => false),
    getCurrentUser: jest.fn(() => ({ id: 'test-user-id' }))
  }
}));

jest.mock('./services/userManagementService', () => ({
  userManagementService: {
    getUserProfile: jest.fn(() => Promise.resolve({
      alias: 'TestUser',
      gender: 'MALE',
      ageGroup: 'YOUNG_ADULT'
    }))
  }
}));

// Mock react-router-dom
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  BrowserRouter: ({ children }) => <div data-testid="router">{children}</div>
}));

const renderWithRouter = (component) => {
  return render(
    <BrowserRouter>
      {component}
    </BrowserRouter>
  );
};

test('renders app without crashing', () => {
  renderWithRouter(<App />);
  // The app should render without crashing
  expect(document.body).toBeInTheDocument();
});

test('renders router component', () => {
  renderWithRouter(<App />);
  expect(screen.getByTestId('router')).toBeInTheDocument();
});
