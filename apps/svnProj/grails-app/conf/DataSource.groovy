dataSource {
    dialect  = "org.hibernate.dialect.Oracle10gDialect"
    //logSql = true
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings

environments {
    development {
        dataSource {
           jndiName= "java:comp/env/jdbc/svnProjDS"
         }
    }
    test {
        dataSource {
            jndiName= "java:comp/env/jdbc/svnProjDS"
        }
    }
    production {
        dataSource {
            jndiName= "java:comp/env/jdbc/svnProjDS"
        }
    }
}

