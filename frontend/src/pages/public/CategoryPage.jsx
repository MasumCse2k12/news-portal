import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { articleApi } from '../../services/api';
import ArticleCard from '../../components/article/ArticleCard';
import { useState } from 'react';

export default function CategoryPage() {
  const { slug } = useParams();
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['category-articles', slug, page],
    queryFn: () => articleApi.getByCategory(slug, { page, size: 12 }),
  });

  const articles = data?.data?.data?.content || [];
  const totalPages = data?.data?.data?.totalPages || 0;
  const categoryName = articles[0]?.category?.name || slug;

  return (
    <div>
      <h1 className="text-3xl font-extrabold text-gray-900 mb-8 border-b pb-4">
        {categoryName}
      </h1>

      {isLoading ? (
        <p className="text-gray-500">Loading...</p>
      ) : articles.length === 0 ? (
        <p className="text-gray-500">No articles found in this category.</p>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {articles.map((a) => <ArticleCard key={a.id} article={a} />)}
          </div>

          {totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-8">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
                className="btn-secondary disabled:opacity-40"
              >
                Previous
              </button>
              <span className="px-4 py-2 text-sm text-gray-500">
                Page {page + 1} of {totalPages}
              </span>
              <button
                disabled={page + 1 >= totalPages}
                onClick={() => setPage((p) => p + 1)}
                className="btn-secondary disabled:opacity-40"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
