interface PaginationControlsProps {
    page: number;          // current page, 0-indexed
    totalPages: number;
    onPageChange: (page: number) => void;
}

export function PaginationControls({ page, totalPages, onPageChange }: PaginationControlsProps) {
    if (totalPages <= 1) return null;

    const isFirstPage = page === 0;
    const isLastPage = page >= totalPages - 1;

    // Build a compact list of page numbers to show, e.g. 1 ... 4 5 [6] 7 8 ... 12
    const pageNumbers = getVisiblePageNumbers(page, totalPages);

    return (
        <div className="pagination">
            <button
                className="pagination-btn"
                onClick={() => onPageChange(page - 1)}
                disabled={isFirstPage}
                aria-label="Previous page"
            >
                Prev
            </button>

            {pageNumbers.map((p, idx) =>
                    p === null ? (
                        <span key={`ellipsis-${idx}`} className="pagination-ellipsis">
            &hellip;
          </span>
                    ) : (
                        <button
                            key={p}
                            className={`pagination-btn${p === page ? ' pagination-btn--active' : ''}`}
                            onClick={() => onPageChange(p)}
                            aria-current={p === page ? 'page' : undefined}
                        >
                            {p + 1}
                        </button>
                    )
            )}

            <button
                className="pagination-btn"
                onClick={() => onPageChange(page + 1)}
                disabled={isLastPage}
                aria-label="Next page"
            >
                Next
            </button>
        </div>
    );
}

/**
 * Returns an array of page indices (0-indexed) to display, using `null`
 * as a placeholder for an ellipsis gap. Always shows first, last, current,
 * and a couple neighbors around current.
 */
function getVisiblePageNumbers(current: number, totalPages: number): (number | null)[] {
    const delta = 1; // how many neighbors to show on each side of current
    const range: (number | null)[] = [];
    const rangeStart = Math.max(1, current - delta);
    const rangeEnd = Math.min(totalPages - 2, current + delta);

    range.push(0); // always show first page

    if (rangeStart > 1) {
        range.push(null); // ellipsis gap
    }

    for (let i = rangeStart; i <= rangeEnd; i++) {
        range.push(i);
    }

    if (rangeEnd < totalPages - 2) {
        range.push(null); // ellipsis gap
    }

    if (totalPages > 1) {
        range.push(totalPages - 1); // always show last page
    }

    return range;
}