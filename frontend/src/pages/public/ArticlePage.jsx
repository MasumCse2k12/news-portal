import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { articleApi } from '../../services/api';
import { formatDistanceToNow } from 'date-fns';
import DOMPurify from 'dompurify';

export default function ArticlePage() {
  const { slug } = useParams();

  const { data, isLoading, error } = useQuery({
    queryKey: ['article', slug],
    queryFn: () => articleApi.getBySlug(slug),
  });

  const article = data?.data?.data;

  if (isLoading) {
    return (
      <div className="max-w-4xl mx-auto animate-pulse space-y-4">
        <div className="h-8 bg-gray-200 rounded w-3/4" />
        <div className="h-64 bg-gray-200 rounded" />
        <div className="space-y-2">
          {[...Array(8)].map((_, i) => <div key={i} className="h-4 bg-gray-200 rounded" />)}
        </div>
      </div>
    );
  }

  if (error || !article) {
    return (
      <div className="text-center py-20">
        <h2 className="text-2xl font-bold text-gray-700">Article not found</h2>
      </div>
    );
  }

  const safeContent = DOMPurify.sanitize(article.content);

  return (
    <article className="max-w-4xl mx-auto">
      {/* Category + Breaking badge */}
      <div className="flex items-center gap-2 mb-3">
        {article.category && (
          <span className="text-sm font-semibold text-primary-600 uppercase tracking-wide">
            {article.category.name}
          </span>
        )}
        {article.breaking && <span className="breaking-badge">Breaking</span>}
      </div>

      {/* Title */}
      <h1 className="text-4xl font-extrabold text-gray-900 leading-tight mb-4">
        {article.title}
      </h1>

      {/* Meta */}
      <div className="flex items-center gap-4 text-sm text-gray-500 mb-6">
        <span>By <strong className="text-gray-700">{article.author?.fullName}</strong></span>
        {article.publishedAt && (
          <span>{formatDistanceToNow(new Date(article.publishedAt), { addSuffix: true })}</span>
        )}
        <span>{article.viewCount?.toLocaleString()} views</span>
      </div>

      {/* Featured image */}
      {article.featuredImage && (
        <figure className="mb-8">
          <img
            src={article.featuredImage}
            alt={article.featuredImageCaption || article.title}
            className="w-full rounded-xl max-h-[500px] object-cover"
          />
          {article.featuredImageCaption && (
            <figcaption className="text-sm text-gray-500 mt-2 text-center">
              {article.featuredImageCaption}
            </figcaption>
          )}
        </figure>
      )}

      {/* Summary */}
      {article.summary && (
        <p className="text-xl text-gray-600 font-medium leading-relaxed mb-8 border-l-4 border-primary-500 pl-4">
          {article.summary}
        </p>
      )}

      {/* Content */}
      <div
        className="prose prose-lg max-w-none text-gray-800"
        dangerouslySetInnerHTML={{ __html: safeContent }}
      />

      {/* Tags */}
      {article.tags && article.tags.length > 0 && (
        <div className="mt-8 pt-6 border-t border-gray-200 flex flex-wrap gap-2">
          {article.tags.map((tag) => (
            <a
              key={tag.id}
              href={`/tag/${tag.slug}`}
              className="px-3 py-1 bg-gray-100 text-gray-600 text-sm rounded-full hover:bg-gray-200 transition-colors"
            >
              #{tag.name}
            </a>
          ))}
        </div>
      )}
    </article>
  );
}
