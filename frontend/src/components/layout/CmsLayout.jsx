import { Outlet, Link, useLocation } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout, selectCurrentUser } from '../../store/authSlice';
import { useNavigate } from 'react-router-dom';

const navItems = [
  { path: '/cms',          label: 'Dashboard',     roles: ['JOURNALIST', 'EDITOR', 'ADMIN'] },
  { path: '/cms/articles', label: 'My Articles',   roles: ['JOURNALIST', 'EDITOR', 'ADMIN'] },
  { path: '/cms/media',    label: 'Media Library',  roles: ['JOURNALIST', 'EDITOR', 'ADMIN'] },
  { path: '/admin',        label: 'Admin Panel',    roles: ['ADMIN'] },
  { path: '/admin/users',  label: 'User Mgmt',      roles: ['ADMIN'] },
];

export default function CmsLayout() {
  const location = useLocation();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector(selectCurrentUser);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/');
  };

  const visibleItems = navItems.filter(item => item.roles.includes(user?.role));

  return (
    <div className="min-h-screen flex bg-gray-100">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-900 text-white flex flex-col">
        <div className="p-4 border-b border-gray-700">
          <Link to="/" className="text-lg font-bold text-white">NewsPortal CMS</Link>
          <div className="text-xs text-gray-400 mt-1">{user?.fullName} · {user?.role}</div>
        </div>

        <nav className="flex-1 p-4 space-y-1">
          {visibleItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`block px-3 py-2 rounded-lg text-sm transition-colors ${
                location.pathname === item.path
                  ? 'bg-primary-600 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`}
            >
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="p-4 border-t border-gray-700">
          <Link to="/cms/articles/new" className="btn-primary w-full text-center block text-sm mb-2">
            New Article
          </Link>
          <button
            onClick={handleLogout}
            className="w-full text-sm text-gray-400 hover:text-white transition-colors py-1"
          >
            Logout
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 p-8 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
