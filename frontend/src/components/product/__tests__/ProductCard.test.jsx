import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ProductCard from '../ProductCard';

const mockProduct = {
  id: '1',
  name: 'Organic Tomatoes',
  slug: 'organic-tomatoes',
  price: 3.99,
  unit: 'KG',
  stockQuantity: 50,
  isOrganic: true,
  avgRating: 4.5,
  reviewCount: 12,
  totalSold: 100,
  images: [{ url: '/test.jpg', isPrimary: true }],
  farm: { id: '1', farmName: 'Green Valley Farm' },
  category: { name: 'Vegetables', slug: 'vegetables' },
};

const wrapper = ({ children }) => <BrowserRouter>{children}</BrowserRouter>;

describe('ProductCard', () => {
  it('renders product name and price', () => {
    render(<ProductCard product={mockProduct} />, { wrapper });
    expect(screen.getByText('Organic Tomatoes')).toBeInTheDocument();
    expect(screen.getByText('$3.99')).toBeInTheDocument();
  });

  it('shows organic badge', () => {
    render(<ProductCard product={mockProduct} />, { wrapper });
    expect(screen.getByText('Organic')).toBeInTheDocument();
  });

  it('shows farm name', () => {
    render(<ProductCard product={mockProduct} />, { wrapper });
    expect(screen.getByText('Green Valley Farm')).toBeInTheDocument();
  });

  it('shows out of stock when quantity is 0', () => {
    const outOfStock = { ...mockProduct, stockQuantity: 0 };
    render(<ProductCard product={outOfStock} />, { wrapper });
    expect(screen.getByText('Out of Stock')).toBeInTheDocument();
  });
});