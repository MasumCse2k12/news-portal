import { Link } from 'react-router-dom';

export default function AdminDashboard() {
  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Admin Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Link to="/admin/users"
          className="bg-white rounded-xl shadow-sm p-6 hover:shadow-md transition-shadow">
          <div className="text-2xl mb-2">Users</div>
          <p className="text-gray-500 text-sm">Manage journalists, editors, readers</p>
        </Link>
        <Link to="/cms/articles"
          className="bg-white rounded-xl shadow-sm p-6 hover:shadow-md transition-shadow">
          <div className="text-2xl mb-2">All Articles</div>
          <p className="text-gray-500 text-sm">Review, publish, moderate content</p>
        </Link>
        <div className="bg-white rounded-xl shadow-sm p-6">
          <div className="text-2xl mb-2">Comments</div>
          <p className="text-gray-500 text-sm">Moderate pending comments</p>
        </div>
      </div>
    </div>
  );
}
