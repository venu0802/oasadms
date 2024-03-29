grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]
// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        cas.loginUrl = 'https://webauth.usf.edu:443/login'
        cas.validateUrl = 'https://webauth.usf.edu:443/serviceValidate'
        cas.serviceUrl = 'https://usfpro12.forest.usf.edu:8443/svnProj/checkOut.zul'
        cas.disabled = false
        cas.mocking = false
    }
    development {
        cas.loginUrl = 'https://authtest.it.usf.edu:444/login'
        cas.validateUrl = 'https://authtest.it.usf.edu:444/serviceValidate'
        cas.serviceUrl = 'https://usfdev6.forest.usf.edu:8443/svnProj/checkOut.zul'
        cas.disabled = false
        cas.mocking = false

    }
    test {
        cas.loginUrl = 'https://authtest.it.usf.edu:444/login'
        cas.validateUrl = 'https://authtest.it.usf.edu:444/serviceValidate'
        cas.serviceUrl = 'https://usfuat1.forest.usf.edu:8443/svnProj/checkOut.zul'
        cas.disabled = false
        cas.mocking = false
    }

}

// log4j configuration
log4j = {
     error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

    warn 'org.mortbay.log'

    appenders {
        file name:'file', file:"/var/log/tomcat6/${appName}_stacktrace.log"
    }
    root {
        debug 'stdout', 'file'
        additivity = true
    }


}
cas {
    urlPattern = '/*'
    disabled = false
}
