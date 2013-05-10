package usf

import edu.yale.its.tp.cas.client.filter.CASFilter
import grails.converters.JSON

class LoginController {
    def facultyService
    def check = {
        String username = session?.getAttribute(CASFilter.CAS_FILTER_USER)
        JSON jsn
        jsn = facultyService.checkUser(username)
        render jsn
    }
}
