import { useQuery } from '@tanstack/react-query';
import { articleApi } from '../../services/api';
import ArticleCard from '../../components/article/ArticleCard';
import { Link } from 'react-router-dom';

export default function HomePage() {
  const { data: featuredData } = useQuery({
    queryKey: ['featured-articles'],
    queryFn: () => articleApi.getFeatured(),
    staleTime: 60_000,
  });

  const { data: latestData, isLoading } = useQuery({
    queryKey: ['latest-articles'],
    queryFn: () => articleApi.getAll({ page: 0, size: 12 }),
    staleTime: 60_000,
  });

  const featured = featuredData?.data?.data || [];
  const latest = latestData?.data?.data?.content || [];

  return (
    <div>
      {/* Featured / Hero */}
      {featured.length > 0 && (
        <section className="mb-10">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Main feature */}
            <div className="lg:col-span-2">
              <ArticleCard article={featured[0]} />
            </div>
            {/* Side features */}
            <div className="flex flex-col gap-4">
              {featured.slice(1, 4).map((a) => (
                <ArticleCard key={a.id} article={a} />
              ))}
            </div>
          </div>
        </section>
      )}

      {/* Latest News */}
      <section>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900 border-l-4 border-primary-600 pl-3">
            Latest News
          </h2>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="article-card animate-pulse">
                <div className="h-48 bg-gray-200 rounded-t-xl" />
                <div className="p-4 space-y-3">
                  <div className="h-4 bg-gray-200 rounded w-1/3" />
                  <div className="h-5 bg-gray-200 rounded w-full" />
                  <div className="h-4 bg-gray-200 rounded w-4/5" />
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {latest.map((article) => (
              <ArticleCard key={article.id} article={article} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
