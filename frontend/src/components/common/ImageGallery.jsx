import { useState } from 'react';
import { FiChevronLeft, FiChevronRight, FiX } from 'react-icons/fi';

export default function ImageGallery({ images = [] }) {
  const [current, setCurrent] = useState(0);
  const [lightbox, setLightbox] = useState(false);

  if (images.length === 0) return null;

  const next = () => setCurrent((c) => (c + 1) % images.length);
  const prev = () => setCurrent((c) => (c - 1 + images.length) % images.length);

  return (
    <>
      <div>
        <div
          className="aspect-square rounded-2xl overflow-hidden bg-gray-100 cursor-zoom-in"
          onClick={() => setLightbox(true)}
        >
          <img
            src={images[current]?.url}
            alt={images[current]?.altText || ''}
            className="w-full h-full object-cover"
          />
        </div>
        {images.length > 1 && (
          <div className="flex gap-2 mt-3">
            {images.map((img, i) => (
              <button
                key={i}
                onClick={() => setCurrent(i)}
                className={`w-16 h-16 rounded-lg overflow-hidden border-2
                  ${i === current ? 'border-green-500' : 'border-transparent'}`}
              >
                <img src={img.url} alt="" className="w-full h-full object-cover" />
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Lightbox */}
      {lightbox && (
        <div className="fixed inset-0 z-50 bg-black/90 flex items-center justify-center">
          <button
            onClick={() => setLightbox(false)}
            className="absolute top-4 right-4 text-white p-2"
          >
            <FiX className="w-6 h-6" />
          </button>
          <button onClick={prev} className="absolute left-4 text-white p-2">
            <FiChevronLeft className="w-8 h-8" />
          </button>
          <img
            src={images[current]?.url}
            alt=""
            className="max-h-[80vh] max-w-[80vw] object-contain"
          />
          <button onClick={next} className="absolute right-4 text-white p-2">
            <FiChevronRight className="w-8 h-8" />
          </button>
        </div>
      )}
    </>
  );
}