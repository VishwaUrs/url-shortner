import type { ShortenUrlRequest, ShortenUrlResponse, UrlListItem, PagedResponse} from '../types/api';

export const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.error) message = body.error;
    } catch {
      // ignore parse errors
    }
    throw new Error(message);
  }
  // 204 No Content has no body
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export const api = {
  shorten(request: ShortenUrlRequest): Promise<ShortenUrlResponse> {
    return fetch(`${API_BASE}/shorten`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    }).then((r) => handleResponse<ShortenUrlResponse>(r));
  },

  listAll(page: number = 0, size: number = 10): Promise<PagedResponse<UrlListItem>> {
    return fetch(`${API_BASE}/urls?page=${page}&size=${size}`)
        .then((r) => handleResponse<PagedResponse<UrlListItem>>(r));
  },

  delete(alias: string): Promise<void> {
    return fetch(`${API_BASE}/${encodeURIComponent(alias)}`, {
      method: 'DELETE',
    }).then((r) => handleResponse<void>(r));
  },

  getFullURL(alias: string) : Promise<UrlListItem> {
    return fetch(`${API_BASE}/${encodeURIComponent(alias)}`, {
      method: 'GET',
    }).then((r) => handleResponse<UrlListItem>(r));
  },


};
