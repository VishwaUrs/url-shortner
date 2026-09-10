import { useState, useCallback, useEffect } from 'react';
import { api } from '../services/api';
import { ShortenUrlResponse, UrlListItem} from '../types/api';

const DEFAULT_PAGE_SIZE = 10;

interface UseUrlShortenerReturn {
  urls: UrlListItem[];
  loading: boolean;
  error: string | null;
  lastCreated: ShortenUrlResponse | null;
  page: number;
  totalPages: number;
  totalItems: number;
  setPage: (page: number) => void;
  shorten: (fullUrl: string, customAlias?: string) => Promise<void>;
  deleteUrl: (alias: string) => Promise<void>;
  clearLastCreated: () => void;
}

export function useUrlShortener(pageSize: number = DEFAULT_PAGE_SIZE): UseUrlShortenerReturn {
  const [urls, setUrls] = useState<UrlListItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastCreated, setLastCreated] = useState<ShortenUrlResponse | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);

  const fetchPage = useCallback(async (pageToLoad: number) => {
    setLoading(true);
    setError(null);
    try {
      const result = await api.listAll(pageToLoad, pageSize);
      setUrls(result.urlList);
      setTotalPages(result.totalPages);
      setTotalItems(result.totalItems);
      setPage(result.page);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load URLs.');
    } finally {
      setLoading(false);
    }
  }, [pageSize]);

  useEffect(() => {
    fetchPage(page);
    // Only re-run when the page number changes, not when fetchPage itself
    // is recreated (pageSize rarely changes at runtime).
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const shorten = useCallback(async (fullUrl: string, customAlias?: string) => {
    setError(null);
    setLastCreated(null);
    try {
      const result = await api.shorten({ fullUrl, customAlias: customAlias || undefined });
      setLastCreated(result);
      // A new URL was added — jump back to page 0 so the user sees it
      // (assuming the backend sorts by createdAt DESC).
      if (page === 0) {
        await fetchPage(0);
      } else {
        setPage(0);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to shorten URL.');
    }
  }, [fetchPage, page]);

  const deleteUrl = useCallback(async (alias: string) => {
    setError(null);
    try {
      await api.delete(alias);

      // If this was the only item on a page beyond the first, step back
      // a page after deleting; otherwise just refetch the current page.
      const isLastItemOnPage = urls.length === 1;
      const targetPage = isLastItemOnPage && page > 0 ? page - 1 : page;

      if (targetPage === page) {
        await fetchPage(page);
      } else {
        setPage(targetPage);
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to delete URL.');
    }
  }, [fetchPage, page, urls.length]);

  const clearLastCreated = useCallback(() => setLastCreated(null), []);

  return {
    urls,
    loading,
    error,
    lastCreated,
    page,
    totalPages,
    totalItems,
    setPage,
    shorten,
    deleteUrl,
    clearLastCreated,
  };
}