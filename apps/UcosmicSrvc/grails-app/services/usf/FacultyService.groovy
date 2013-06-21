package usf

import grails.converters.JSON
import groovy.sql.Sql

class FacultyService {
    def grailsApplication

    static transactional = true
    def dataSource
    String   query


    String getLastUpdate(Sql sql){
        String lastUpdate
        query =   "select to_char(MAX(a.swrrule_activity_date),'MM-DD-YYYY') lupdate from swrrule@${grailsApplication.config.oasis.dblink} a where a.swrrule_business_area LIKE 'UCOSMIC%'"
        println "query is ${query}"
        sql.eachRow(query)   {
            lastUpdate = it.lupdate
        }
        sql.close()
        return lastUpdate

    }

    JSON checkUser(String username) {


        JSON jsn


        println "in check user, user is ${username}"
        if (grailsApplication.config.admin.users.contains(username)) {
            jsn = [status: "success", action: "proceed to /facultyInfo/<<netId>>"] as JSON
        }
        else {
            println "json bad"
            jsn = [status: "failure", error: "Un Authorized User"] as JSON
        }
        return jsn
    }

    JSON deptIdLookup(String username){
        JSON jsn;
        println "in dept lookup and user "
        jsn = checkUser(username)
        Map mymap =[:]

        if (JSON.parse(jsn.toString()).status == "success") {
            Sql sql = new groovy.sql.Sql(dataSource)
            jsn = null
            String lastUpdate = getLastUpdate(sql)
             query =    "select a.swrrule_rule deptId, c.swrrule_value Institution , substr(a.swrrule_value,1,instr(a.swrrule_value,'~')-1) College ,substr(a.swrrule_value,instr(a.swrrule_value,'~')+1) Department from swrrule@${grailsApplication.config.oasis.dblink} a, (select b.* from swrrule@${grailsApplication.config.oasis.dblink} b where b.swrrule_business_area = 'UCOSMICINS') c where a.swrrule_business_area = 'UCOSMICDEP'\n" +
                     " and c.swrrule_rule = substr(a.swrrule_rule,1,1)"
            println "query is ${query}"
            List ls = sql.rows( query )
                    /*{
               ["deptId":it.deptId,"Institution":it.Institution ,"College":it.College, "Department":it.Department ]
            }        */
            mymap.put("lastUpdate",lastUpdate)
            mymap.put("lookup",ls)
            sql.close()

        }

       jsn = mymap as JSON

    }

    JSON getInfo(String netId, String username) {
        JSON jsn;
        jsn = checkUser(username)
        Map mymap     =[:]


        if (JSON.parse(jsn.toString()).status == "success") {
            Sql sql = new groovy.sql.Sql(dataSource)
            jsn = null

            String lastUpdate= getLastUpdate(sql)
            println "in getinfo lastUpdate is ${lastUpdate}"

            query ="""
SELECT distinct  ci.netid "NetId"
       ,a.last_name
       ,a.first_name
       ,a.middle_name
       ,a.name_suffix
       ,a.sex_desc
       ,ci.cims_email
      FROM hub_cims_curr_id_v ci
  LEFT OUTER JOIN gems_appointment_curr_view a
    ON a.emplid = ci.emplid
 WHERE ci.netid = ?
      AND a.location_code IN ('01', '03', '04', '38', '39')
         AND a.paygroup_code <> 'PS3' -- exclude faculty summer appointments
   AND a.empl_status_code IN ('A', 'L', 'P', 'S', 'R')
"""
            println "query is ${query}"
            sql.eachRow(query, [netId]) {
                mymap =   ["Last Name": "${it.last_name}", "First Name": "${it.first_name}", "Middle Name": "${it.MIDDLE_NAME}", "Suffix": "${it.NAME_SUFFIX}", "Gender": "${it.sex_desc}", "USF Email Address": "${it.cims_email}"] as Map
            }
            mymap.put("lastUpdate",lastUpdate)
             query =   """
SELECT  a.deptid deptId,
nvl(substr(job_code_rule.swrrule_value, 0,
                  instr(job_code_rule.swrrule_value, '~', 1, 1) - 1), 5) "Faculty Rank"
       ,nvl(substr(job_code_rule.swrrule_value,
                  instr(job_code_rule.swrrule_value, '~', 1, 1) + 1),
           'Other') "Position Title"
       ,nvl(inst_rule.swrrule_value, ' ') "Institutional Affiliation"
       ,nvl(substr(dept_rule.swrrule_value, 1,
                  instr(dept_rule.swrrule_value, '~', 1, 1) - 1), NULL) "College"
       ,nvl(substr(dept_rule.swrrule_value,
                  instr(dept_rule.swrrule_value, '~', 1, 1) + 1), NULL) "Department/Program"
  FROM hub_cims_curr_id_v ci
  LEFT OUTER JOIN gems_appointment_curr_view a
    ON a.emplid = ci.emplid
  LEFT OUTER JOIN swrrule@${grailsApplication.config.oasis.dblink} job_code_rule
    ON job_code_rule.swrrule_rule = a.jobcode
   AND job_code_rule.swrrule_business_area = 'UCOSMICJOB'
  LEFT OUTER JOIN swrrule@${grailsApplication.config.oasis.dblink} dept_rule
    ON dept_rule.swrrule_rule = a.deptid
   AND dept_rule.swrrule_business_area = 'UCOSMICDEP'
  LEFT OUTER JOIN swrrule@${grailsApplication.config.oasis.dblink} inst_rule
    ON inst_rule.swrrule_rule = substr(a.deptid, 1, 1)
   AND inst_rule.swrrule_business_area = 'UCOSMICINS'
 WHERE ci.netid =?
   AND a.location_code IN ('01', '03', '04', '38', '39')
   AND a.paygroup_code <> 'PS3' -- exclude faculty summer appointments
   AND a.empl_status_code IN ('A', 'L', 'P', 'S', 'R')
-- include only active or retired employees
 ORDER BY nvl(substr(job_code_rule.swrrule_value, 0,
                     instr(job_code_rule.swrrule_value, '~', 1, 1) - 1), 5) -- Faculty Rank
         ,nvl(substr(dept_rule.swrrule_value, 1,
                     instr(dept_rule.swrrule_value, '~', 1, 1) - 1), NULL) --"College"
         ,nvl(substr(dept_rule.swrrule_value,
                     instr(dept_rule.swrrule_value, '~', 1, 1) + 1), NULL)
 """
            println "query is ${query}"
            List ls = sql.rows(query, [netId])
            if (mymap) {
                mymap.put("profile", ls)

                jsn = mymap as JSON
            }
            sql.close()

            if (jsn) {jsn = jsn} else {
                jsn = [error: "Not Valid NetId"] as JSON
            }
        }



        return jsn

    }
}
