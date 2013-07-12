package usf

import edu.yale.its.tp.cas.client.filter.CASFilter
import grails.converters.JSON

class FacultyController {
    def facultyService
    def showFacultyInfo = {
        JSON jsn
        println "in controller, showFacultyInfo before getting user name"
        String username = session?.getAttribute(CASFilter.CAS_FILTER_USER)
        println "in controller, showFacultyInfo before getting faculty info"
        jsn = facultyService.getInfo(params.netId, username)
        println "in controller, showFacultyInfo before rendering jsn"
        render jsn
    }

    def showLookupInfo ={
        println "in controller, showLookupInfo before getting user name"
        JSON jsn
        String username = session?.getAttribute(CASFilter.CAS_FILTER_USER)

        println "in controller, showLookupInfo before calling deptIdLookup"
        jsn = facultyService.deptIdLookup( username)
        render jsn
    }
}
