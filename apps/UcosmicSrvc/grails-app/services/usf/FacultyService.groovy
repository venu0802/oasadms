package usf

import grails.converters.JSON
import groovy.sql.Sql

class FacultyService {
    def grailsApplication

    static transactional = true
    def dataSource

      JSON checkUser(String username){


          JSON   jsn
          println "in check user, user is ${username}"
         if (grailsApplication.config.admin.users.contains(username))
          {
              jsn= [status:"success",action:"proceed to /facultyInfo/<<netId>>"] as JSON
          }
          else
          {
              println "json bad"
              jsn =  [status:"failure",error:"Un Authorized User"]    as JSON
          }
           return jsn
      }

    JSON getInfo(String netId, String username) {
        JSON jsn;
        jsn = checkUser(username)



        if (JSON.parse(jsn.toString()).status == "success"){
            Sql sql = new groovy.sql.Sql(dataSource)
                jsn=null
            sql.eachRow(""" select ci.NETID
         , a.LAST_NAME
         , a.FIRST_NAME
         , a.MIDDLE_NAME
         , a.NAME_SUFFIX
         , count(distinct a.JOBCODE_DESC) as num_jobcodes, min(a.JOBCODE_DESC) as min_jobcode_desc, max(a.JOBCODE_DESC) as max_jobcode_desc /* for RANK in uCosmic */
         , ci.CIMS_EMAIL
         , count(distinct a.DEPTID) as num_gems_deptids, min(a.DEPTID) as min_gems_deptid, max(a.DEPTID) as max_gems_deptid  /* for mapping to Dept-name, college, campus in uCosmic */
         , min(a.DEPTID_DESC), max(a.DEPTID_DESC)
/* Other values you may also want */
, a.EMPLID
, count(distinct a.LOCATION_CODE) as num_location_codes, min(a.LOCATION_CODE) as min_location_code, max(a.LOCATION_DESC) as max_location_code
, count(distinct a.HIRE_DATE) as num_hire_dates, min(a.HIRE_DATE) as min_hire_date, max(a.HIRE_DATE) as max_hire_date
, count(distinct a.TERMINATION_DATE) as num_term_dates, min(a.TERMINATION_DATE) as min_term_date, max(a.TERMINATION_DATE) as max_term_date
, a.SEX_CODE, a.SEX_DESC
, ci.EDUPERS_PRIMARY_AFFIL
      from hub_cims_curr_id_v ci
      left outer join gems_appointment_curr_view a on a.EMPLID = ci.EMPLID and a.EMPL_STATUS_CODE in ('A','L','P','S','R'/*Retired too*/)  /* Just to skip former jobs */
      where ci.NETID = ?
      group by ci.NETID
         , a.LAST_NAME
         , a.FIRST_NAME
         , a.MIDDLE_NAME
         , a.NAME_SUFFIX
         , ci.CIMS_EMAIL
/* Other values you may also want */
, a.EMPLID
, a.SEX_CODE, a.SEX_DESC
, ci.EDUPERS_PRIMARY_AFFIL
""", [netId]) {
                jsn = [LastName: "${it.last_name}", firstName: "${it.first_name}", middleName: "${it.MIDDLE_NAME}", nameSuffix: "${it.NAME_SUFFIX}", numOfJobCodes: "${it.num_jobcodes}", minJobCodeDesc: "${it.min_jobcode_desc}",] as JSON
            }
            sql.close()

            if (jsn){jsn=jsn} else{
                jsn=[error:"Not Valid NetId"]   as JSON
            }
        }



        return jsn

    }
}
