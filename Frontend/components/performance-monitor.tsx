"use client"

import { useEffect } from 'react'

interface PerformanceMetrics {
  fcp?: number // First Contentful Paint
  lcp?: number // Largest Contentful Paint
  fid?: number // First Input Delay
  cls?: number // Cumulative Layout Shift
  ttfb?: number // Time to First Byte
}

export function PerformanceMonitor() {
  useEffect(() => {
    // Only run in production and if performance API is available
    if (process.env.NODE_ENV !== 'production' || typeof window === 'undefined' || !window.performance) {
      return
    }

    const metrics: PerformanceMetrics = {}

    // Measure Core Web Vitals
    const measureWebVitals = () => {
      // First Contentful Paint
      const fcpEntry = performance.getEntriesByName('first-contentful-paint')[0] as PerformanceEntry
      if (fcpEntry) {
        metrics.fcp = fcpEntry.startTime
      }

      // Time to First Byte
      const navigationEntry = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
      if (navigationEntry) {
        metrics.ttfb = navigationEntry.responseStart - navigationEntry.requestStart
      }

      // Log metrics for monitoring
      console.log('Performance Metrics:', metrics)
    }

    // Use Web Vitals library if available, otherwise fallback to Performance API
    if ('web-vitals' in window) {
      // @ts-ignore
      import('web-vitals').then(({ getCLS, getFID, getFCP, getLCP, getTTFB }) => {
        getCLS((metric: any) => {
          metrics.cls = metric.value
        })
        getFID((metric: any) => {
          metrics.fid = metric.value
        })
        getFCP((metric: any) => {
          metrics.fcp = metric.value
        })
        getLCP((metric: any) => {
          metrics.lcp = metric.value
        })
        getTTFB((metric: any) => {
          metrics.ttfb = metric.value
        })
      }).catch(() => {
        // Fallback to basic performance measurement
        measureWebVitals()
      })
    } else {
      measureWebVitals()
    }

    // Measure bundle loading performance
    const measureBundlePerformance = () => {
      const resources = performance.getEntriesByType('resource') as PerformanceResourceTiming[]
      const jsResources = resources.filter(resource => 
        resource.name.includes('.js') && 
        (resource.name.includes('_next') || resource.name.includes('chunks'))
      )

      const totalJSSize = jsResources.reduce((total, resource) => {
        return total + (resource.transferSize || 0)
      }, 0)

      const avgLoadTime = jsResources.reduce((total, resource) => {
        return total + (resource.loadEnd - resource.loadStart)
      }, 0) / jsResources.length

      console.log('Bundle Performance:', {
        totalJSSize: `${(totalJSSize / 1024).toFixed(2)} KB`,
        avgLoadTime: `${avgLoadTime.toFixed(2)} ms`,
        resourceCount: jsResources.length
      })
    }

    // Measure after page load
    if (document.readyState === 'complete') {
      setTimeout(() => {
        measureBundlePerformance()
      }, 1000)
    } else {
      window.addEventListener('load', () => {
        setTimeout(() => {
          measureBundlePerformance()
        }, 1000)
      })
    }

    // Monitor route changes for SPA performance
    const originalPushState = history.pushState
    const originalReplaceState = history.replaceState

    const measureRouteChange = (url: string) => {
      const startTime = performance.now()
      
      // Use requestIdleCallback if available, otherwise setTimeout
      const callback = () => {
        const endTime = performance.now()
        console.log(`Route change to ${url}: ${(endTime - startTime).toFixed(2)}ms`)
      }

      if ('requestIdleCallback' in window) {
        requestIdleCallback(callback)
      } else {
        setTimeout(callback, 0)
      }
    }

    history.pushState = function(...args) {
      measureRouteChange(args[2] as string)
      return originalPushState.apply(this, args)
    }

    history.replaceState = function(...args) {
      measureRouteChange(args[2] as string)
      return originalReplaceState.apply(this, args)
    }

    // Cleanup
    return () => {
      history.pushState = originalPushState
      history.replaceState = originalReplaceState
    }
  }, [])

  // This component doesn't render anything
  return null
}

// Hook for measuring component render performance
export function useRenderPerformance(componentName: string) {
  useEffect(() => {
    if (process.env.NODE_ENV !== 'production') {
      const startTime = performance.now()
      
      return () => {
        const endTime = performance.now()
        console.log(`${componentName} render time: ${(endTime - startTime).toFixed(2)}ms`)
      }
    }
  })
}

// Hook for measuring async operations
export function useAsyncPerformance() {
  const measureAsync = (operationName: string, asyncOperation: () => Promise<any>) => {
    const startTime = performance.now()
    
    return asyncOperation().finally(() => {
      const endTime = performance.now()
      console.log(`${operationName} completed in: ${(endTime - startTime).toFixed(2)}ms`)
    })
  }

  return { measureAsync }
}
