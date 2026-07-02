import React from "react";

export type SortDirection = "asc" | "desc";
export type SortState<K extends string> = { key: K; direction: SortDirection };

export const TABLE_PAGE_SIZE = 5;
export const COMPACT_TABLE_PAGE_SIZE = 4;

function compareSortValues(left: string | number | undefined, right: string | number | undefined) {
  if (typeof left === "number" && typeof right === "number") return left - right;
  return String(left ?? "").localeCompare(String(right ?? ""), "ru", { numeric: true, sensitivity: "base" });
}

export function sortItems<T, K extends string>(items: T[], sort: SortState<K>, accessors: Record<K, (item: T) => string | number | undefined>) {
  return [...items].sort((left, right) => {
    const result = compareSortValues(accessors[sort.key](left), accessors[sort.key](right));
    return sort.direction === "asc" ? result : -result;
  });
}

export function pageItems<T>(items: T[], page: number, pageSize: number) {
  const totalPages = Math.max(1, Math.ceil(items.length / pageSize));
  const currentPage = Math.min(Math.max(page, 1), totalPages);
  const start = (currentPage - 1) * pageSize;
  return { currentPage, totalPages, rows: items.slice(start, start + pageSize) };
}

export function SortHeader<K extends string>({ label, sortKey, sort, onSort, className, testId }: { label: string; sortKey: K; sort: SortState<K>; onSort: (key: K) => void; className?: string; testId: string }) {
  const active = sort.key === sortKey;
  const directionLabel = active ? (sort.direction === "asc" ? "↑" : "↓") : "";
  return <button
    className={`sort-header ${className ?? ""}`}
    type="button"
    onClick={() => onSort(sortKey)}
    aria-sort={active ? (sort.direction === "asc" ? "ascending" : "descending") : "none"}
    data-testid={testId}
  >
    {label}<span>{directionLabel}</span>
  </button>;
}

export function TablePagination({ page, totalPages, onPage, testId }: { page: number; totalPages: number; onPage: (page: number) => void; testId: string }) {
  if (totalPages <= 1) return null;
  return <div className="table-pagination" data-testid={testId}>
    <button type="button" onClick={() => onPage(page - 1)} disabled={page <= 1}>Назад</button>
    <span>Страница {page} из {totalPages}</span>
    <button type="button" onClick={() => onPage(page + 1)} disabled={page >= totalPages}>Вперед</button>
  </div>;
}
