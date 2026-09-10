export interface ShortenUrlRequest {
  fullUrl: string;
  customAlias?: string;
}

export interface ShortenUrlResponse {
  alias: string;
  fullUrl: string;
  shortUrl: string;
}

export interface UrlListItem {
  alias: string;
  fullUrl: string;
  shortUrl: string;
  createdAt: string;
}

export interface ApiError {
  error: string;
}

export interface PagedResponse<UrlListItem> {
  urlList: UrlListItem[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}
