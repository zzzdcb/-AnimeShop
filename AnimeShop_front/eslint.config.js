import js from '@eslint/js'
import globals from 'globals'
import pluginVue from 'eslint-plugin-vue'
import prettierConfig from 'eslint-config-prettier'
import pluginPrettier from 'eslint-plugin-prettier'
import tseslint from 'typescript-eslint'
import vueParser from 'vue-eslint-parser' // ✅ 明确导入 vue 解析器

export default [
  // 基础推荐配置
  js.configs.recommended,

  // Vue 3 基础配置
  ...pluginVue.configs['flat/essential'],

  // TypeScript 配置
  ...tseslint.configs.recommended,

  // Prettier 配置（确保放在最后，以覆盖其他规则）
  prettierConfig,

  // 自定义配置
  {
    files: ['**/*.vue', '**/*.ts', '**/*.js', '**/*.cjs'],
    plugins: {
      prettier: pluginPrettier
    },
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node
      },
      // ⭐ 关键修复：使用 vue-eslint-parser
      parser: vueParser, // 改用导入的解析器
      parserOptions: {
        parser: tseslint.parser,
        sourceType: 'module',
        ecmaVersion: 'latest',
        ecmaFeatures: {
          jsx: true
        }
      }
    },
    rules: {
      // Prettier 规则
      'prettier/prettier': [
        'warn',
        {
          singleQuote: true,
          semi: false,
          printWidth: 100,
          trailingComma: 'none',
          endOfLine: 'auto'
        }
      ],

      // Vue 规则
      'vue/multi-word-component-names': [
        'warn',
        {
          ignores: [
            'index',
            'LoginPage',
            'CartPage',
            'UserHomePage',
            'HomeBanner',
            'HomeHotShop',
            'HomeNewShop',
            'HomePanal',
            'HomeSeckillShop'
          ] // 添加你的组件名
        }
      ],
      'vue/no-setup-props-destructure': 'off',
      'vue/valid-attribute-name': 'off',
      'vue/valid-template-root': 'off', // 临时关闭模板根元素检查
      'vue/no-multiple-template-root': 'off', // Vue 2 需要，Vue 3 不需要

      // 普通 JavaScript/TypeScript 规则
      'no-undef': 'error',
      'no-unused-vars': 'off', // 关闭，使用 TypeScript 版本

      // TypeScript 规则（降低错误级别）
      '@typescript-eslint/no-unused-vars': [
        'warn',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_'
        }
      ],
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/no-empty-object-type': [
        'warn',
        {
          allowInterfaces: 'with-single-extends'
        }
      ]
    }
  },

  // 忽略文件（合并到一个配置中）
  {
    ignores: [
      'dist/**',
      'node_modules/**',
      '.husky/**',
      '*.config.js',
      'vite.config.*',
      'pnpm-lock.yaml',
      '.git/**',
      '**/*.d.ts', // 忽略类型声明文件
      'env.d.ts'
    ]
  }
]
