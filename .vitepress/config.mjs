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
      { text: 'Api', link: '/apidocs/index.html', target: '_self' },
    ],
    outline: {
      level: [2, 3],
    },
    sidebar: [
      { text: '简介', link: '/concept' },
      { text: '快速开始', link: '/getting-started' },
      { text: 'Dba 对象', link: '/dba' },
      { text: 'Sql 对象', link: '/sql/sql' },
      {
        text: '查询',
        items: [
          { text: '查询条件', link: '/sql/cnd' },
          { text: '子查询', link: '/sql/subQuery' },
          {
            text: '查询结果',
            items: [
              { text: 'RowMapper', link: '/sql/query/rowMapper' },
              { text: '查询单个字段', link: '/sql/query/queryValue' },
              { text: '查询多个字段', link: '/sql/query/queryObject' },
              { text: '高级结果', link: '/sql/query/advanced' },
              { text: '分页查询', link: '/sql/query/pageQuery' },
              { text: 'COUNT', link: '/sql/query/count' },
              { text: '树形结构查询', link: '/sql/query/tree' },
            ],
          },
        ],
      },
      {
        text: '增删改',
        items: [
          { text: 'INSERT', link: '/sql/insert' },
          { text: 'DELETE', link: '/sql/delete' },
          { text: 'UPDATE', link: '/sql/update' },
        ],
      },
      {
        text: '对象映射',
        items: [
          { text: 'ORM 基本操作', link: '/orm/basic' },
          { text: '枚举支持', link: '/orm/enum' },
          { text: 'LOB支持', link: '/orm/lob' },
          { text: 'FieldMapper', link: '/orm/fieldMapper' },
          { text: 'ObjectSql', link: '/orm/sql' },
        ],
      },
      {
        text: '其它',
        items: [
          { text: '数据库方言', link: '/other/dialect' },
          { text: '事务', link: '/other/transaction' },
          { text: 'Range', link: '/other/range' },
          { text: '树形结构数据', link: '/other/tree' },
          { text: '内存Dba', link: '/other/memoryDba' },
          { text: '单元测试', link: '/other/unit-test' },
          { text: 'StringBuilderX', link: '/other/stringBuilderX' },
        ],
      },
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/jinghui70/rainbow-dbaccess' },
    ],
  },
})
