package usf

import edu.yale.its.tp.cas.client.filter.CASFilter
import grails.converters.JSON

class FacultyController {
    def facultyService
    def showFacultyInfo = {
        JSON jsn
        String username = session?.getAttribute(CASFilter.CAS_FILTER_USER)
        jsn = facultyService.getInfo(params.netId, username)
        render jsn
    }
}
