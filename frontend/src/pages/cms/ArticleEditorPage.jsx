import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { cmsApi, categoryApi } from '../../services/api';
import toast from 'react-hot-toast';

export default function ArticleEditorPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEditing = !!id;

  const [form, setForm] = useState({
    title: '', titleBn: '', summary: '', content: '',
    categoryId: '', tagSlugs: '', featuredImage: '',
    metaTitle: '', metaDescription: '',
  });

  const { data: categoriesData } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryApi.getAll(),
  });

  const categories = categoriesData?.data?.data || [];

  const createMutation = useMutation({
    mutationFn: (data) => cmsApi.createArticle(data),
    onSuccess: () => {
      toast.success('Article saved as draft!');
      queryClient.invalidateQueries(['my-articles-summary']);
      navigate('/cms/articles');
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to save'),
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      categoryId: Number(form.categoryId),
      tagSlugs: form.tagSlugs ? form.tagSlugs.split(',').map((t) => t.trim()) : [],
    };
    createMutation.mutate(payload);
  };

  const handleChange = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <div className="max-w-4xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">
        {isEditing ? 'Edit Article' : 'New Article'}
      </h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="bg-white rounded-xl shadow-sm p-6 space-y-4">
          <h2 className="font-semibold text-gray-700 border-b pb-2">Content</h2>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Title (English)*</label>
            <input
              type="text"
              value={form.title}
              onChange={handleChange('title')}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Title (Bengali)</label>
            <input
              type="text"
              value={form.titleBn}
              onChange={handleChange('titleBn')}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm font-bengali"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Summary</label>
            <textarea
              value={form.summary}
              onChange={handleChange('summary')}
              rows={3}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Content (HTML)*</label>
            <textarea
              value={form.content}
              onChange={handleChange('content')}
              rows={12}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-primary-500"
              required
            />
            <p className="text-xs text-gray-400 mt-1">Paste HTML or plain text. Rich editor (Quill) will be integrated here.</p>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm p-6 space-y-4">
          <h2 className="font-semibold text-gray-700 border-b pb-2">Settings</h2>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Category*</label>
            <select
              value={form.categoryId}
              onChange={handleChange('categoryId')}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
              required
            >
              <option value="">Select a category</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tags (comma-separated slugs)</label>
            <input
              type="text"
              value={form.tagSlugs}
              onChange={handleChange('tagSlugs')}
              placeholder="e.g. dhaka, flood, 2024"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Featured Image URL</label>
            <input
              type="text"
              value={form.featuredImage}
              onChange={handleChange('featuredImage')}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
            />
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-sm p-6 space-y-4">
          <h2 className="font-semibold text-gray-700 border-b pb-2">SEO</h2>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Meta Title</label>
            <input type="text" value={form.metaTitle} onChange={handleChange('metaTitle')}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Meta Description</label>
            <textarea value={form.metaDescription} onChange={handleChange('metaDescription')}
              rows={2} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm" />
          </div>
        </div>

        <div className="flex gap-3">
          <button type="submit" disabled={createMutation.isPending} className="btn-primary">
            {createMutation.isPending ? 'Saving...' : 'Save Draft'}
          </button>
          <button type="button" onClick={() => navigate('/cms/articles')} className="btn-secondary">
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
