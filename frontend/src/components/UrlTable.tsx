import type { UrlListItem } from '../types/api';
import {API_BASE} from "../services/api.ts";

interface UrlTableProps {
  urls: UrlListItem[];
  onDelete: (alias: string) => void;
}

export function UrlTable({ urls, onDelete }: UrlTableProps) {
  // TODO: Build URL table UI and wire delete actions.
  void urls;
  void onDelete;

  return (
      <div className="url-table-wrapper">
        <table className="url-table">
          <thead>
          <tr>
            <th>#</th>
            <th>Original FullURL</th>
            <th>Alias</th>
            <th className="actions-cell">Action</th>
          </tr>
          </thead>
          <tbody>
          {urls.map((item, index) => (
              <tr key={item.alias}>
                <td>{index + 1}</td>
                <td>
                <span className="full-url" title={item.fullUrl}>
                  {item.fullUrl}
                </span>
                </td>
                <td>
                  <a className="short-url-link"
                      //href={`http://localhost:8080/${item.alias}`}
                     href={`${API_BASE}/${item.alias}`}
                      target="_blank"
                      rel="noopener noreferrer">
                    {item.alias}
                  </a>
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
