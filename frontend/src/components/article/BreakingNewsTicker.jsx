import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { articleApi } from '../../services/api';

export default function BreakingNewsTicker() {
  const { data } = useQuery({
    queryKey: ['breaking-news'],
    queryFn: () => articleApi.getBreaking(),
    refetchInterval: 60_000,
  });

  const articles = data?.data?.data || [];

  if (articles.length === 0) return null;

  return (
    <div className="bg-red-600 text-white flex items-center overflow-hidden h-9">
      <span className="breaking-badge shrink-0 mx-3">Breaking</span>
      <div className="flex-1 overflow-hidden">
        <div className="flex gap-8 animate-marquee whitespace-nowrap">
          {articles.map((article) => (
            <Link
              key={article.id}
              to={`/article/${article.slug}`}
              className="text-sm hover:underline shrink-0"
            >
              {article.title}
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
