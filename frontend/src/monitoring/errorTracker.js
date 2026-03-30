import api from '../api/axios';

class ErrorTracker {
  constructor() {
    this.errors = [];
    this.maxBatchSize = 10;
    this.flushInterval = 30000; // 30 seconds

    // Global error handler
    window.addEventListener('error', (event) => {
      this.captureError({
        type: 'js_error',
        message: event.message,
        filename: event.filename,
        line: event.lineno,
        column: event.colno,
        stack: event.error?.stack,
      });
    });

    // Unhandled promise rejections
    window.addEventListener('unhandledrejection', (event) => {
      this.captureError({
        type: 'unhandled_promise',
        message: event.reason?.message || String(event.reason),
        stack: event.reason?.stack,
      });
    });

    // Periodic flush
    setInterval(() => this.flush(), this.flushInterval);
  }

  captureError(errorData) {
    this.errors.push({
      ...errorData,
      timestamp: new Date().toISOString(),
      url: window.location.href,
      userAgent: navigator.userAgent,
    });

    console.error('[ErrorTracker]', errorData.message);

    if (this.errors.length >= this.maxBatchSize) {
      this.flush();
    }
  }

  async flush() {
    if (this.errors.length === 0) return;

    const batch = [...this.errors];
    this.errors = [];

    try {
      await api.post('/monitoring/client-errors', { errors: batch });
    } catch (e) {
      // Silently fail — don't create error loop
      console.warn('[ErrorTracker] Failed to send errors');
    }
  }
}

export const errorTracker = new ErrorTracker();