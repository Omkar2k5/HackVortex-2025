/** @type {import('next').NextConfig} */
const nextConfig = {
  // Optimize images
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'lh3.googleusercontent.com',
        port: '',
        pathname: '/**',
      },
      {
        protocol: 'https',
        hostname: 'prod.spline.design',
        port: '',
        pathname: '/**',
      }
    ],
    formats: ['image/webp', 'image/avif'],
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048, 3840],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
    minimumCacheTTL: 60 * 60 * 24 * 30, // 30 days
    dangerouslyAllowSVG: true,
    contentSecurityPolicy: "default-src 'self'; script-src 'none'; sandbox;",
  },

  // Enable experimental features for better performance
  experimental: {
    optimizePackageImports: [
      'lucide-react',
      '@radix-ui/react-icons',
      'recharts',
      'firebase/auth',
      'firebase/database',
      'firebase/firestore',
      'framer-motion',
      '@react-pdf/renderer'
    ],
  },



  // Compiler options for better performance
  compiler: {
    removeConsole: process.env.NODE_ENV === 'production',
    // Enable SWC minification
    styledComponents: true,
  },

  // Enable gzip compression
  compress: true,

  // Webpack configuration for bundle optimization
  webpack: (config, { buildId, dev, isServer, defaultLoaders, webpack }) => {
    // Optimize bundle splitting
    if (!dev && !isServer) {
      config.optimization.splitChunks = {
        chunks: 'all',
        cacheGroups: {
          default: false,
          vendors: false,
          // Firebase chunk
          firebase: {
            name: 'firebase',
            chunks: 'all',
            test: /[\\/]node_modules[\\/](firebase|@firebase)[\\/]/,
            priority: 40,
          },
          // UI components chunk
          ui: {
            name: 'ui',
            chunks: 'all',
            test: /[\\/]node_modules[\\/](@radix-ui|@headlessui|lucide-react)[\\/]/,
            priority: 30,
          },
          // Charts chunk
          charts: {
            name: 'charts',
            chunks: 'all',
            test: /[\\/]node_modules[\\/](recharts|d3|victory)[\\/]/,
            priority: 25,
          },
          // Animation chunk
          animations: {
            name: 'animations',
            chunks: 'all',
            test: /[\\/]node_modules[\\/](framer-motion|@splinetool)[\\/]/,
            priority: 20,
          },
          // Common vendor chunk
          vendor: {
            name: 'vendor',
            chunks: 'all',
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
          },
        },
      }
    }

    // Optimize imports
    config.resolve.alias = {
      ...config.resolve.alias,
      // Remove lodash alias to fix recharts compatibility
    }

    return config
  },

  // Headers for better caching
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'X-XSS-Protection',
            value: '1; mode=block',
          },
        ],
      },
      {
        source: '/images/(.*)',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, max-age=31536000, immutable',
          },
        ],
      },
      {
        source: '/_next/static/(.*)',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, max-age=31536000, immutable',
          },
        ],
      },
    ]
  },
}

module.exports = nextConfig