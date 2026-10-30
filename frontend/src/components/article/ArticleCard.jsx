import { Link } from 'react-router-dom';
import { formatDistanceToNow } from 'date-fns';

export default function ArticleCard({ article }) {
  const timeAgo = article.publishedAt
    ? formatDistanceToNow(new Date(article.publishedAt), { addSuffix: true })
    : '';

  return (
    <div className="article-card">
      {article.featuredImage && (
        <Link to={`/article/${article.slug}`}>
          <img
            src={article.featuredImage}
            alt={article.featuredImageCaption || article.title}
            className="w-full h-48 object-cover"
            loading="lazy"
          />
        </Link>
      )}

      <div className="p-4">
        {article.category && (
          <Link
            to={`/category/${article.category.slug}`}
            className="text-xs font-semibold text-primary-600 uppercase tracking-wide hover:text-primary-800"
          >
            {article.category.name}
          </Link>
        )}

        {article.breaking && (
          <span className="breaking-badge ml-2">Breaking</span>
        )}

        <h2 className="mt-1 text-lg font-bold text-gray-900 leading-tight">
          <Link to={`/article/${article.slug}`} className="hover:text-primary-700 transition-colors">
            {article.title}
          </Link>
        </h2>

        {article.summary && (
          <p className="mt-2 text-sm text-gray-600 line-clamp-2">{article.summary}</p>
        )}

        <div className="mt-3 flex items-center justify-between text-xs text-gray-400">
          <span>{article.author?.fullName}</span>
          <span>{timeAgo}</span>
        </div>
      </div>
    </div>
  );
}
