import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { cmsApi } from '../../services/api';
import toast from 'react-hot-toast';

export default function MediaLibraryPage() {
  const [dragging, setDragging] = useState(false);
  const [uploadedFiles, setUploadedFiles] = useState([]);

  const uploadMutation = useMutation({
    mutationFn: (file) => {
      const formData = new FormData();
      formData.append('file', file);
      return cmsApi.uploadMedia(formData);
    },
    onSuccess: (res) => {
      const media = res.data.data;
      setUploadedFiles((prev) => [media, ...prev]);
      toast.success('File uploaded!');
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Upload failed'),
  });

  const handleFileChange = (e) => {
    const files = Array.from(e.target.files);
    files.forEach((file) => uploadMutation.mutate(file));
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    const files = Array.from(e.dataTransfer.files);
    files.forEach((file) => uploadMutation.mutate(file));
  };

  const copyUrl = (url) => {
    navigator.clipboard.writeText(url);
    toast.success('URL copied to clipboard!');
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Media Library</h1>

      {/* Upload zone */}
      <div
        onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
        onDragLeave={() => setDragging(false)}
        onDrop={handleDrop}
        className={`border-2 border-dashed rounded-xl p-12 text-center mb-8 transition-colors ${
          dragging ? 'border-primary-500 bg-primary-50' : 'border-gray-300 bg-white'
        }`}
      >
        <p className="text-gray-500 mb-3">Drag & drop files here, or</p>
        <label className="btn-primary cursor-pointer">
          Browse Files
          <input type="file" multiple accept="image/*,application/pdf" onChange={handleFileChange} className="hidden" />
        </label>
        <p className="text-xs text-gray-400 mt-3">Max 10MB per file. Supported: JPG, PNG, GIF, WebP, SVG, PDF</p>
        {uploadMutation.isPending && (
          <p className="text-primary-600 mt-3 font-medium">Uploading...</p>
        )}
      </div>

      {/* Uploaded files grid */}
      {uploadedFiles.length > 0 && (
        <div>
          <h2 className="text-lg font-semibold text-gray-700 mb-4">Uploaded this session</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
            {uploadedFiles.map((media) => (
              <div key={media.id} className="bg-white rounded-lg shadow-sm overflow-hidden group">
                {media.mimeType?.startsWith('image/') ? (
                  <img src={media.url} alt={media.filename}
                    className="w-full h-24 object-cover" />
                ) : (
                  <div className="w-full h-24 bg-gray-100 flex items-center justify-center text-gray-400 text-xs">
                    PDF
                  </div>
                )}
                <div className="p-2">
                  <p className="text-xs text-gray-600 truncate">{media.filename}</p>
                  <button
                    onClick={() => copyUrl(media.url)}
                    className="text-xs text-primary-600 hover:underline mt-1"
                  >
                    Copy URL
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
