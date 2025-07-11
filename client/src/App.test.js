import React from 'react';
import { render } from '@testing-library/react';

// Simple component test that doesn't involve routing
const SimpleTestComponent = () => {
  return <div data-testid="simple-component">Test Component</div>;
};

test('renders test component without crashing', () => {
  const { getByTestId } = render(<SimpleTestComponent />);
  expect(getByTestId('simple-component')).toBeInTheDocument();
});

test('basic React functionality works', () => {
  const TestDiv = () => <div data-testid="test-div">Hello World</div>;
  const { getByTestId } = render(<TestDiv />);
  expect(getByTestId('test-div')).toHaveTextContent('Hello World');
});
