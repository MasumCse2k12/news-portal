import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { articleApi } from '../../services/api';
import ArticleCard from '../../components/article/ArticleCard';

export default function SearchPage() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';

  const { data, isLoading } = useQuery({
    queryKey: ['search', query],
    queryFn: () => articleApi.search({ q: query }),
    enabled: !!query,
  });

  const articles = data?.data?.data?.content || [];

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">
        {query ? `Search results for: "${query}"` : 'Search'}
      </h1>

      {!query && (
        <p className="text-gray-500">Enter a search term in the search bar above.</p>
      )}

      {isLoading && <p className="text-gray-500">Searching...</p>}

      {!isLoading && query && articles.length === 0 && (
        <p className="text-gray-500">No results found for "{query}".</p>
      )}

      {articles.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {articles.map((a) => <ArticleCard key={a.id} article={a} />)}
        </div>
      )}
    </div>
  );
}
