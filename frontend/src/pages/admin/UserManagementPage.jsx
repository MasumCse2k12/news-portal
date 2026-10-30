import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../../services/api';
import { useState } from 'react';
import toast from 'react-hot-toast';

export default function UserManagementPage() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users', page],
    queryFn: () => adminApi.getUsers({ page, size: 20 }),
  });

  const deactivateMutation = useMutation({
    mutationFn: (id) => adminApi.deactivateUser(id),
    onSuccess: () => {
      toast.success('User deactivated');
      queryClient.invalidateQueries(['admin-users']);
    },
    onError: () => toast.error('Failed to deactivate user'),
  });

  const users = data?.data?.data?.content || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">User Management</h1>

      {isLoading ? (
        <p className="text-gray-500">Loading...</p>
      ) : (
        <div className="bg-white rounded-xl shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="text-left p-4 font-medium text-gray-500">Name</th>
                <th className="text-left p-4 font-medium text-gray-500">Username</th>
                <th className="text-left p-4 font-medium text-gray-500">Email</th>
                <th className="text-left p-4 font-medium text-gray-500">Role</th>
                <th className="text-left p-4 font-medium text-gray-500">Status</th>
                <th className="text-left p-4 font-medium text-gray-500">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {users.map((u) => (
                <tr key={u.id} className="hover:bg-gray-50">
                  <td className="p-4 font-medium text-gray-800">{u.fullName}</td>
                  <td className="p-4 text-gray-600">{u.username}</td>
                  <td className="p-4 text-gray-600">{u.email}</td>
                  <td className="p-4">
                    <span className="text-xs font-semibold px-2 py-1 rounded-full bg-primary-50 text-primary-700">
                      {u.role}
                    </span>
                  </td>
                  <td className="p-4">
                    <span className={`text-xs font-semibold px-2 py-1 rounded-full ${
                      u.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-600'
                    }`}>
                      {u.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="p-4">
                    {u.active && u.role !== 'ADMIN' && (
                      <button
                        onClick={() => deactivateMutation.mutate(u.id)}
                        className="text-xs text-red-600 hover:underline"
                      >
                        Deactivate
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
