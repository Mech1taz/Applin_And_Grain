import { useCallback, useEffect, useState } from 'react';
import { catalogService } from '../services/catalogService.js';

/** Carga genérica: fetcher() -> { data, loading, error, reload } */
export function useAsync(fetcher, deps = []) {
  const [state, setState] = useState({ data: null, loading: true, error: null });

  const run = useCallback(() => {
    let cancelled = false;
    setState((s) => ({ ...s, loading: true, error: null }));
    fetcher()
      .then((data) => !cancelled && setState({ data, loading: false, error: null }))
      .catch((error) => !cancelled && setState({ data: null, loading: false, error }));
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(run, [run]);
  return { ...state, reload: run };
}

export const useProducts = () => useAsync(() => catalogService.getProducts(), []);
export const useProduct = (id) => useAsync(() => catalogService.getProductById(id), [id]);
