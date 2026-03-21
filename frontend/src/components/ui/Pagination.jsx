import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';

export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-center gap-2 mt-8">
      <button
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
        className="p-2 border rounded-lg disabled:opacity-50 hover:bg-gray-50"
      >
        <FiChevronLeft className="w-4 h-4" />
      </button>

      {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
        let pageNum;
        if (totalPages <= 7) {
          pageNum = i;
        } else if (page < 4) {
          pageNum = i;
        } else if (page > totalPages - 5) {
          pageNum = totalPages - 7 + i;
        } else {
          pageNum = page - 3 + i;
        }

        return (
          <button
            key={pageNum}
            onClick={() => onPageChange(pageNum)}
            className={`w-10 h-10 rounded-lg text-sm font-medium
              ${page === pageNum
                ? 'bg-green-600 text-white'
                : 'border hover:bg-gray-50'}`}
          >
            {pageNum + 1}
          </button>
        );
      })}

      <button
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="p-2 border rounded-lg disabled:opacity-50 hover:bg-gray-50"
      >
        <FiChevronRight className="w-4 h-4" />
      </button>
    </div>
  );
}