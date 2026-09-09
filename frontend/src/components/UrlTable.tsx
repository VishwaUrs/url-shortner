import type { UrlListItem } from '../types/api';
import {API_BASE} from "../services/api.ts";
import { useMemo, useState } from 'react';

interface UrlTableProps {
  urls: UrlListItem[];
  onDelete: (alias: string) => void;
}

type SortDirection = 'asc' | 'desc';

export function UrlTable({ urls, onDelete }: UrlTableProps) {
  // TODO: Build URL table UI and wire delete actions.
  void urls;
  void onDelete;
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');

  const sortedUrls = useMemo(() => {return [...urls].sort((a, b) => {
      const diff = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      return sortDirection === 'asc' ? diff : -diff;
    });
  }, [urls, sortDirection]);
  console.log("sortedurls", sortedUrls);
  const toggleSort = () => {
    setSortDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'));
  };

  function truncateTimestamp(timestamp: string): string {
    return new Date(timestamp).toISOString().split('.')[0] + 'Z';
  }

  return (
      <div className="url-table-wrapper">
        <table className="url-table">
          <thead>
          <tr>
            <th>#</th>
            <th>Original FullURL</th>
            <th>Short URL</th>
            <th className="sortable-header" onClick={toggleSort}>
              Created At {sortDirection === 'desc' ? '▼' : '▲'}
            </th>
            <th className="actions-cell">Action</th>
          </tr>
          </thead>
          <tbody>
          {sortedUrls.map((item, index) => (
              <tr key={item.alias}>
                <td>{index + 1}</td>
                <td>
                <span className="full-url" title={item.fullUrl}>
                  {item.fullUrl}
                </span>
                </td>
                <td>
                  <a className="short-url-link"
                     href={`${API_BASE}/${item.alias}`}
                      target="_blank"
                      rel="noopener noreferrer">
                    {item.alias}
                  </a>
                </td>
                <td>
                <span className="created-at" title={item.createdAt}>
                  {truncateTimestamp(item.createdAt)}
                </span>
                </td>
                <td className="actions-cell">
                  <button type="submit" id="deleteButton" className="btn-danger" onClick={() => onDelete(item.alias)}>
                    DELETE
                  </button>
                </td>
              </tr>
          ))}

          {urls.length === 0 && (
              <tr>
                <td colSpan={4} className="url-table-empty">
                  No shortened URLs yet.
                </td>
              </tr>
          )}
          </tbody>
        </table>
      </div>
  );
}
