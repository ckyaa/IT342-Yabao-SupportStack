import { render, screen } from '@testing-library/react';
import App from './App';

test('renders supportstack landing heading', () => {
  render(<App />);
  const headingElement = screen.getByText(/supportstack/i);
  expect(headingElement).toBeInTheDocument();
});
