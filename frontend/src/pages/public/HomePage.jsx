import { useQuery } from '@tanstack/react-query';
import { articleApi } from '../../services/api';
import ArticleCard from '../../components/article/ArticleCard';
import { Link } from 'react-router-dom';
import { formatDistanceToNow } from 'date-fns';
import { TrendingUp, Eye } from 'lucide-react';

export default function HomePage() {
  const { data: featuredData } = useQuery({
    queryKey: ['featured-articles'],
    queryFn: () => articleApi.getFeatured(),
    staleTime: 60_000,
  });

  const { data: latestData, isLoading: latestLoading } = useQuery({
    queryKey: ['latest-articles'],
    queryFn: () => articleApi.getAll({ page: 0, size: 12 }),
    staleTime: 60_000,
  });

  const { data: trendingData, isLoading: trendingLoading } = useQuery({
    queryKey: ['trending-articles'],
    queryFn: () => articleApi.getTrending(),
    staleTime: 60_000,
  });

  const featured = featuredData?.data?.data || [];
  const latest = latestData?.data?.data?.content || [];
  const trending = trendingData?.data?.data || [];

  return (
    <div>
      {/* Featured / Hero */}
      {featured.length > 0 && (
        <section className="mb-10">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2">
              <ArticleCard article={featured[0]} />
            </div>
            <div className="flex flex-col gap-4">
              {featured.slice(1, 4).map((a) => (
                <ArticleCard key={a.id} article={a} />
              ))}
            </div>
          </div>
        </section>
      )}

      {/* Latest News + Trending Sidebar */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Latest News */}
        <section className="lg:col-span-2">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-gray-900 border-l-4 border-primary-600 pl-3">
              Latest News
            </h2>
          </div>

          {latestLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
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
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {latest.map((article) => (
                <ArticleCard key={article.id} article={article} />
              ))}
            </div>
          )}
        </section>

        {/* Trending Sidebar */}
        <aside>
          <div className="flex items-center gap-2 mb-6">
            <TrendingUp size={20} className="text-primary-600" />
            <h2 className="text-2xl font-bold text-gray-900 border-l-4 border-primary-600 pl-3">
              Trending
            </h2>
          </div>

          {trendingLoading ? (
            <div className="space-y-4">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="flex gap-3 animate-pulse">
                  <div className="w-8 h-8 bg-gray-200 rounded flex-shrink-0" />
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-full" />
                    <div className="h-4 bg-gray-200 rounded w-3/4" />
                    <div className="h-3 bg-gray-200 rounded w-1/2" />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <ol className="space-y-4">
              {trending.slice(0, 5).map((article, index) => (
                <li key={article.id} className="flex gap-3 group">
                  <span className="text-3xl font-black text-gray-200 group-hover:text-primary-200 leading-none flex-shrink-0 w-8 transition-colors">
                    {index + 1}
                  </span>
                  <div className="flex-1 min-w-0">
                    {article.category && (
                      <Link
                        to={`/category/${article.category.slug}`}
                        className="text-xs font-semibold text-primary-600 uppercase tracking-wide hover:text-primary-800"
                      >
                        {article.category.name}
                      </Link>
                    )}
                    <h3 className="mt-0.5 text-sm font-semibold text-gray-900 leading-snug line-clamp-2">
                      <Link to={`/article/${article.slug}`} className="hover:text-primary-700 transition-colors">
                        {article.title}
                      </Link>
                    </h3>
                    <div className="mt-1 flex items-center gap-3 text-xs text-gray-400">
                      <span className="flex items-center gap-1">
                        <Eye size={11} />
                        {article.viewCount?.toLocaleString()}
                      </span>
                      {article.publishedAt && (
                        <span>
                          {formatDistanceToNow(new Date(article.publishedAt), { addSuffix: true })}
                        </span>
                      )}
                    </div>
                  </div>
                </li>
              ))}
            </ol>
          )}
        </aside>
      </div>
    </div>
  );
}
