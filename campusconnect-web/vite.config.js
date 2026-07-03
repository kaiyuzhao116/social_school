import path from 'path'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')

  return {
    server: {
      port: 3000,
      host: '0.0.0.0',
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
          configure: (proxy, options) => {
            proxy.on('error', (err, req, res) => {
              console.log('Proxy error:', err)
            })

            proxy.on('proxyReq', (proxyReq, req, res) => {
              console.log(
                  'Proxying:',
                  req.method,
                  req.url,
                  '->',
                  options.target + req.url
              )
            })
          }
        },

        '/uploads': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
          rewrite: (path) => `/api${path}`
        }
      }
    },

    plugins: [
      vue({
        template: {
          compilerOptions: {
            isCustomElement: (tag) => {
              return tag === 'vue-advanced-chat' || tag === 'emoji-picker'
            }
          }
        }
      })
    ],

    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  }
})