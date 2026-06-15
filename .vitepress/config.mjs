import { defineConfig } from 'vitepress'

export default defineConfig({
  base: '/rainbow-dbaccess/',
  title: 'Rainbow DBAccess',
  description: '基于 Spring JDBC 的轻量级数据库访问工具',
  themeConfig: {
    nav: [
      { text: '指南', link: '/concept' },
      { text: 'API 速查', link: '/api' },
    ],
    outline: {
      level: [2, 3],
    },
    sidebar: [
      { text: '简介', link: '/concept' },
      { text: '快速开始', link: '/getting-started' },
      { text: '实体映射', link: '/entity' },
      { text: 'FieldMapper', link: '/fieldmapper' },
      { text: '查询', link: '/query' },
      { text: 'Cnd 条件系统', link: '/cnd' },
      { text: '插入数据', link: '/insert' },
      { text: '更新数据', link: '/update' },
      { text: '删除数据', link: '/delete' },
      { text: '事务管理', link: '/transaction' },
      { text: '原始 SQL', link: '/raw-sql' },
      { text: 'MemoryDba', link: '/memory-dba' },
      { text: '规则与最佳实践', link: '/tips' },
      { text: 'API 速查', link: '/api' },
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/jinghui70/rainbow-dbaccess' },
    ],
  },
})
