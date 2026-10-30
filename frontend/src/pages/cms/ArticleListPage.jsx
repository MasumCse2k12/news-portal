import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { cmsApi } from '../../services/api';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { useSelector } from 'react-redux';
import { selectUserRole } from '../../store/authSlice';

export default function ArticleListPage() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();
  const role = useSelector(selectUserRole);
  const canPublish = ['EDITOR', 'ADMIN'].includes(role);

  const { data, isLoading } = useQuery({
    queryKey: ['my-articles', page],
    queryFn: () => cmsApi.getMyArticles({ page, size: 20 }),
  });

  const publishMutation = useMutation({
    mutationFn: (id) => cmsApi.publish(id),
    onSuccess: () => {
      toast.success('Article published!');
      queryClient.invalidateQueries(['my-articles']);
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to publish'),
  });

  const submitMutation = useMutation({
    mutationFn: (id) => cmsApi.submitForReview(id),
    onSuccess: () => {
      toast.success('Submitted for review!');
      queryClient.invalidateQueries(['my-articles']);
    },
  });

  const articles = data?.data?.data?.content || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">My Articles</h1>
        <Link to="/cms/articles/new" className="btn-primary">+ New Article</Link>
      </div>

      {isLoading ? (
        <p className="text-gray-500">Loading...</p>
      ) : articles.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-xl shadow-sm">
          <p className="text-gray-500 mb-4">No articles yet.</p>
          <Link to="/cms/articles/new" className="btn-primary">Write your first article</Link>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                <th className="text-left p-4 font-medium text-gray-500">Title</th>
                <th className="text-left p-4 font-medium text-gray-500">Status</th>
                <th className="text-left p-4 font-medium text-gray-500">Category</th>
                <th className="text-left p-4 font-medium text-gray-500">Views</th>
                <th className="text-left p-4 font-medium text-gray-500">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {articles.map((a) => (
                <tr key={a.id} className="hover:bg-gray-50 transition-colors">
                  <td className="p-4">
                    <Link to={`/cms/articles/${a.id}/edit`}
                      className="font-medium text-gray-800 hover:text-primary-600">
                      {a.title}
                    </Link>
                  </td>
                  <td className="p-4"><StatusBadge status={a.status} /></td>
                  <td className="p-4 text-gray-500">{a.category?.name || '—'}</td>
                  <td className="p-4 text-gray-500">{a.viewCount?.toLocaleString()}</td>
                  <td className="p-4 flex gap-2">
                    {a.status === 'DRAFT' && (
                      <button
                        onClick={() => submitMutation.mutate(a.id)}
                        className="text-xs text-yellow-700 hover:underline"
                      >
                        Submit
                      </button>
                    )}
                    {canPublish && a.status === 'REVIEW' && (
                      <button
                        onClick={() => publishMutation.mutate(a.id)}
                        className="text-xs text-green-700 hover:underline"
                      >
                        Publish
                      </button>
                    )}
                    {a.status === 'PUBLISHED' && (
                      <Link to={`/article/${a.slug}`}
                        target="_blank"
                        className="text-xs text-primary-600 hover:underline">
                        View
                      </Link>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="flex justify-center gap-2 p-4">
              <button disabled={page === 0} onClick={() => setPage((p) => p - 1)} className="btn-secondary text-xs">
                Previous
              </button>
              <span className="text-sm text-gray-500 self-center">
                Page {page + 1} of {totalPages}
              </span>
              <button disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)} className="btn-secondary text-xs">
                Next
              </button>
            </div>
          )}
        </div>
      )}
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
