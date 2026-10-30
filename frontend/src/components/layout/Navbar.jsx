import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { categoryApi } from '../../services/api';
import { logout, selectCurrentUser, selectIsAuthenticated } from '../../store/authSlice';

export default function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const user = useSelector(selectCurrentUser);
  const [searchQuery, setSearchQuery] = useState('');

  const { data: categoriesData } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryApi.getAll(),
    staleTime: 3600_000,
  });

  const categories = categoriesData?.data?.data || [];

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleLogout = () => {
    dispatch(logout());
    navigate('/');
  };

  return (
    <header className="bg-primary-900 text-white shadow-lg">
      {/* Top bar */}
      <div className="container mx-auto px-4 max-w-7xl">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="text-2xl font-bold tracking-tight">
            NewsPortal BD
          </Link>

          {/* Search */}
          <form onSubmit={handleSearch} className="hidden md:flex items-center gap-2">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search news..."
              className="px-4 py-2 rounded-lg text-gray-900 text-sm w-64 focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
            <button type="submit" className="btn-primary text-sm py-2">Search</button>
          </form>

          {/* Auth */}
          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <>
                {user?.role !== 'READER' && (
                  <Link to="/cms" className="text-sm hover:text-primary-300 transition-colors">
                    CMS
                  </Link>
                )}
                {user?.role === 'ADMIN' && (
                  <Link to="/admin" className="text-sm hover:text-primary-300 transition-colors">
                    Admin
                  </Link>
                )}
                <button onClick={handleLogout} className="text-sm hover:text-primary-300 transition-colors">
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm hover:text-primary-300 transition-colors">Login</Link>
                <Link to="/register" className="btn-primary text-sm">Register</Link>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Category Nav */}
      <nav className="bg-primary-800">
        <div className="container mx-auto px-4 max-w-7xl">
          <div className="flex items-center gap-1 overflow-x-auto py-1 scrollbar-hide">
            <Link to="/" className="px-3 py-2 text-sm whitespace-nowrap hover:bg-primary-700 rounded transition-colors">
              Home
            </Link>
            {categories.map((cat) => (
              <Link
                key={cat.id}
                to={`/category/${cat.slug}`}
                className="px-3 py-2 text-sm whitespace-nowrap hover:bg-primary-700 rounded transition-colors"
              >
                {cat.name}
              </Link>
            ))}
          </div>
        </div>
      </nav>
    </header>
  );
}
