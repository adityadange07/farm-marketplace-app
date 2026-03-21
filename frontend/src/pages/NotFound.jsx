import { Link } from 'react-router-dom';
import Button from '../components/ui/Button';

export default function NotFound() {
  return (
    <div className="min-h-[70vh] flex items-center justify-center text-center px-4">
      <div>
        <p className="text-8xl mb-4">🥕</p>
        <h1 className="text-4xl font-bold text-gray-900">404</h1>
        <p className="text-gray-500 mt-2 text-lg">
          Oops! This page doesn't exist.
        </p>
        <Link to="/">
          <Button className="mt-6">Go Home</Button>
        </Link>
      </div>
    </div>
  );
}