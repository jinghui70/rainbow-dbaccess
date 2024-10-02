import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  base: '/rainbow-dbaccess/',
  title: 'Rainbow DBAccess',
  description:
    'Lightweight and simple-to-use database access tool for efficient data management',
  themeConfig: {
    nav: [
      { text: '指南', link: '/concept' },
      { text: '参考', link: '/api' },
    ],

    sidebar: [
      {
        text: '简介',
        items: [
          { text: '数据库访问', link: '/concept' },
          { text: '快速开始', link: '/getting-started' },
        ],
      },
      {
        text: 'OR 映射',
        items: [
          { text: 'ORM 基本操作', link: '/orm/basic' },
          { text: '枚举支持', link: '/orm/enum' },
          { text: 'LOB支持', link: '/orm/lob' },
          { text: '自定义属性', link: '/orm/custom' },
        ],
      },
      {
        text: 'SQL 编写',
        items: [
          { text: 'SQL 编写', link: '/sql/basic' },
          { text: '参数化查询', link: '/sql/parameter' },
          { text: '查询结果处理', link: '/sql/result' },
          { text: '分页查询', link: '/sql/pageQuery' },
        ],
      },
      {
        text: '其它',
        items: [
          { text: '内存数据库', link: '/other/memoryDba' },
          { text: '单元测试', link: '/other/unit-test' },
        ],
      },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/jinghui70/rainbow-dbaccess' },
    ],
  },
})
