import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300 mt-12">
      <div className="container mx-auto px-4 max-w-7xl py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <h3 className="text-white font-bold text-xl mb-4">NewsPortal BD</h3>
            <p className="text-sm text-gray-400">
              Bangladesh's trusted source for local, national, and international news.
            </p>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-3">Bangladesh</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/category/dhaka" className="hover:text-white transition-colors">Dhaka</Link></li>
              <li><Link to="/category/chittagong" className="hover:text-white transition-colors">Chittagong</Link></li>
              <li><Link to="/category/sylhet" className="hover:text-white transition-colors">Sylhet</Link></li>
              <li><Link to="/category/bd-politics" className="hover:text-white transition-colors">Politics</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-3">Topics</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/category/international" className="hover:text-white transition-colors">International</Link></li>
              <li><Link to="/category/sports" className="hover:text-white transition-colors">Sports</Link></li>
              <li><Link to="/category/business" className="hover:text-white transition-colors">Business</Link></li>
              <li><Link to="/category/technology" className="hover:text-white transition-colors">Technology</Link></li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-semibold mb-3">Company</h4>
            <ul className="space-y-2 text-sm">
              <li><Link to="/about" className="hover:text-white transition-colors">About Us</Link></li>
              <li><Link to="/contact" className="hover:text-white transition-colors">Contact</Link></li>
              <li><Link to="/privacy" className="hover:text-white transition-colors">Privacy Policy</Link></li>
              <li><Link to="/terms" className="hover:text-white transition-colors">Terms of Use</Link></li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-700 mt-8 pt-6 text-center text-sm text-gray-500">
          &copy; {new Date().getFullYear()} NewsPortal BD. All rights reserved.
        </div>
      </div>
    </footer>
  );
}
