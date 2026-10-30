import { BrowserRouter, Routes, Route } from 'react-router-dom';
import PublicLayout from './components/layout/PublicLayout';
import CmsLayout from './components/layout/CmsLayout';
import ProtectedRoute from './components/ProtectedRoute';

// Public pages
import HomePage from './pages/public/HomePage';
import ArticlePage from './pages/public/ArticlePage';
import CategoryPage from './pages/public/CategoryPage';
import SearchPage from './pages/public/SearchPage';
import LoginPage from './pages/public/LoginPage';
import RegisterPage from './pages/public/RegisterPage';

// CMS pages
import CmsDashboard from './pages/cms/CmsDashboard';
import ArticleListPage from './pages/cms/ArticleListPage';
import ArticleEditorPage from './pages/cms/ArticleEditorPage';
import MediaLibraryPage from './pages/cms/MediaLibraryPage';

// Admin pages
import AdminDashboard from './pages/admin/AdminDashboard';
import UserManagementPage from './pages/admin/UserManagementPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route element={<PublicLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/article/:slug" element={<ArticlePage />} />
          <Route path="/category/:slug" element={<CategoryPage />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        {/* CMS routes (JOURNALIST, EDITOR, ADMIN) */}
        <Route
          path="/cms"
          element={
            <ProtectedRoute roles={['JOURNALIST', 'EDITOR', 'ADMIN']}>
              <CmsLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<CmsDashboard />} />
          <Route path="articles" element={<ArticleListPage />} />
          <Route path="articles/new" element={<ArticleEditorPage />} />
          <Route path="articles/:id/edit" element={<ArticleEditorPage />} />
          <Route path="media" element={<MediaLibraryPage />} />
        </Route>

        {/* Admin routes */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <CmsLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<AdminDashboard />} />
          <Route path="users" element={<UserManagementPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
