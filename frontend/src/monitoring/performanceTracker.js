class PerformanceTracker {
  constructor() {
    this.metrics = [];

    // Track page load
    window.addEventListener('load', () => {
      setTimeout(() => this.capturePageLoad(), 0);
    });

    // Track navigation timing
    if ('PerformanceObserver' in window) {
      // Long tasks (>50ms)
      const longTaskObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.metrics.push({
            type: 'long_task',
            duration: entry.duration,
            startTime: entry.startTime,
            timestamp: new Date().toISOString(),
          });
        }
      });
      longTaskObserver.observe({ entryTypes: ['longtask'] });

      // Largest Contentful Paint
      const lcpObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        const lastEntry = entries[entries.length - 1];
        this.metrics.push({
          type: 'lcp',
          value: lastEntry.startTime,
          element: lastEntry.element?.tagName,
          timestamp: new Date().toISOString(),
        });
      });
      lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });

      // First Input Delay
      const fidObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          this.metrics.push({
            type: 'fid',
            value: entry.processingStart - entry.startTime,
            timestamp: new Date().toISOString(),
          });
        }
      });
      fidObserver.observe({ entryTypes: ['first-input'] });

      // Cumulative Layout Shift
      const clsObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (!entry.hadRecentInput) {
            this.metrics.push({
              type: 'cls',
              value: entry.value,
              timestamp: new Date().toISOString(),
            });
          }
        }
      });
      clsObserver.observe({ entryTypes: ['layout-shift'] });
    }
  }

  capturePageLoad() {
    const nav = performance.getEntriesByType('navigation')[0];
    if (!nav) return;

    this.metrics.push({
      type: 'page_load',
      dns: nav.domainLookupEnd - nav.domainLookupStart,
      tcp: nav.connectEnd - nav.connectStart,
      ttfb: nav.responseStart - nav.requestStart,
      domLoaded: nav.domContentLoadedEventEnd - nav.startTime,
      fullLoad: nav.loadEventEnd - nav.startTime,
      url: window.location.pathname,
      timestamp: new Date().toISOString(),
    });
  }

  // Track API call duration
  trackApiCall(method, url, duration, status) {
    this.metrics.push({
      type: 'api_call',
      method,
      url: url.replace(/\/[a-f0-9-]{36}/g, '/{id}'),
      duration,
      status,
      timestamp: new Date().toISOString(),
    });
  }
}

export const performanceTracker = new PerformanceTracker();