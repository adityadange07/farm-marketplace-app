import { Link } from 'react-router-dom';
import { GiWheat } from 'react-icons/gi';

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-400">
      <div className="max-w-7xl mx-auto px-4 py-16">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand */}
          <div>
            <Link to="/" className="flex items-center gap-2">
              <GiWheat className="w-8 h-8 text-green-500" />
              <span className="text-xl font-bold text-white">FarmFresh</span>
            </Link>
            <p className="mt-4 text-sm">
              Connecting local farmers directly with consumers for fresher,
              fairer food.
            </p>
          </div>

          {/* Shop */}
          <div>
            <h4 className="text-white font-semibold mb-4">Shop</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/shop/vegetables" className="hover:text-white">Vegetables</Link></li>
              <li><Link to="/shop/fruits" className="hover:text-white">Fruits</Link></li>
              <li><Link to="/shop/dairy-eggs" className="hover:text-white">Dairy & Eggs</Link></li>
              <li><Link to="/shop/meat-poultry" className="hover:text-white">Meat</Link></li>
            </ul>
          </div>

          {/* Company */}
          <div>
            <h4 className="text-white font-semibold mb-4">Company</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-white">About Us</a></li>
              <li><a href="#" className="hover:text-white">For Farmers</a></li>
              <li><a href="#" className="hover:text-white">Blog</a></li>
              <li><a href="#" className="hover:text-white">Careers</a></li>
            </ul>
          </div>

          {/* Support */}
          <div>
            <h4 className="text-white font-semibold mb-4">Support</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-white">Help Center</a></li>
              <li><a href="#" className="hover:text-white">Privacy Policy</a></li>
              <li><a href="#" className="hover:text-white">Terms of Service</a></li>
              <li><a href="#" className="hover:text-white">Contact Us</a></li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 mt-12 pt-8 text-center text-sm">
          <p>© {new Date().getFullYear()} FarmFresh. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}