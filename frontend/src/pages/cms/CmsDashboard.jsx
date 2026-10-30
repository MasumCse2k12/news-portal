import { useQuery } from '@tanstack/react-query';
import { cmsApi } from '../../services/api';
import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectCurrentUser } from '../../store/authSlice';

export default function CmsDashboard() {
  const user = useSelector(selectCurrentUser);

  const { data } = useQuery({
    queryKey: ['my-articles-summary'],
    queryFn: () => cmsApi.getMyArticles({ page: 0, size: 5 }),
  });

  const articles = data?.data?.data?.content || [];
  const total = data?.data?.data?.totalElements || 0;

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-2">
        Welcome, {user?.fullName}
      </h1>
      <p className="text-gray-500 mb-8">Role: {user?.role}</p>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white rounded-xl shadow-sm p-6">
          <div className="text-3xl font-bold text-primary-600">{total}</div>
          <div className="text-sm text-gray-500 mt-1">Total Articles</div>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-800">Recent Articles</h2>
          <Link to="/cms/articles" className="text-sm text-primary-600 hover:underline">View all</Link>
        </div>

        {articles.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-500 mb-4">No articles yet.</p>
            <Link to="/cms/articles/new" className="btn-primary">Write your first article</Link>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left border-b border-gray-100">
                <th className="pb-2 font-medium text-gray-500">Title</th>
                <th className="pb-2 font-medium text-gray-500">Status</th>
                <th className="pb-2 font-medium text-gray-500">Category</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {articles.map((a) => (
                <tr key={a.id}>
                  <td className="py-3">
                    <Link to={`/cms/articles/${a.id}/edit`} className="font-medium text-gray-800 hover:text-primary-600">
                      {a.title}
                    </Link>
                  </td>
                  <td className="py-3">
                    <StatusBadge status={a.status} />
                  </td>
                  <td className="py-3 text-gray-500">{a.category?.name || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

function StatusBadge({ status }) {
  const colors = {
    DRAFT:     'bg-gray-100 text-gray-600',
    REVIEW:    'bg-yellow-100 text-yellow-700',
    PUBLISHED: 'bg-green-100 text-green-700',
    SCHEDULED: 'bg-blue-100 text-blue-700',
    ARCHIVED:  'bg-red-100 text-red-600',
  };
  return (
    <span className={`text-xs font-semibold px-2 py-1 rounded-full ${colors[status] || ''}`}>
      {status}
    </span>
  );
}
