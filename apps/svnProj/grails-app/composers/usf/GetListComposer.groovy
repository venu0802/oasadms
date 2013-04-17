package usf

import edu.yale.its.tp.cas.client.filter.CASFilter
import groovy.sql.Sql
import oracle.sql.BLOB
import org.zkoss.zk.ui.event.ForwardEvent
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zkgrails.GrailsComposer

import java.sql.Connection
import javax.sql.DataSource

import org.zkoss.zul.*

class GetListComposer extends GrailsComposer {
    // variables defined in checkOut.zul
    Combobox environmentCombo
    Combobox packageObjectCombo
    Listbox objectList
    Paging ObjPaging
    Button clearAllBtn
    Button clearPackBtn
    Button downLoadBtn
    Radiogroup inProdVerRadioBtn
    def errorBlock
    def errorMessage
    Label createdByVal
    Label createdBy
    Label created
    Label createdVal
    Label modifiedBy
    Label modifiedByVal
    Label applied
    Label appliedVal
    Label modifiedOn
    Label modifiedOnVal
    def objInfoGroup
    def ObjectListGroup
    Textbox fetchText
    //services
    def svnService
    // local variables
    ArrayList environmentArrayList
    ListModelList environmentModelList
    String username
    String onlyProd



    def afterCompose = { window ->
        // initialize components here
        clearObjInfo("")
        clearAll()
        setProdIndicator()
        objInfoGroup.height = ObjectListGroup.height
        try {
            username = session?.getAttribute(CASFilter.CAS_FILTER_USER)
        } catch (Exception e) {


            errorBlock.visible = true
            errorMessage.value = "Please authenticate."

        }

            environmentArrayList = Staff.findByUserId(username).role.environments*.name.sort()
        environmentModelList = new ListModelList(environmentArrayList);
        environmentCombo.setModel(environmentModelList)
    }

    def onChange_packageObjectCombo() {
        setTotalCount()
        objectRefresh()
    }


    def onPaging_ObjPaging(ForwardEvent fe) {
        def e = fe.origin
        objectRefresh(e.activePage)
    }

    def onClick_inProdVerRadioBtn() {
        setTotalCount()
        objectRefresh()
    }

    def onChange_fetchText(){
        setTotalCount()
        objectRefresh()
    }

    def onClick_clearAllBtn() {
        clearAll()
    }

    def onClick_clearPackBtn() {
        clearPackageObject()
    }

    def onClick_downLoadBtn() {
        objInfoGroup.height = ObjectListGroup.height
        Repository rep = objectList.getSelectedItem().value
        // dataSource = (DataSource) WebApplicationContextUtils.getWebApplicationContext(ServletContextHolder.servletContext).getBean('dataSource')
        DataSource dataSource = (DataSource) svnService.getDataSource()
        Connection conn =  dataSource.getConnection()
        conn.setAutoCommit(false)
        def sql = new groovy.sql.Sql(conn)
        //def sql = new groovy.sql.Sql(dataSource)
        sql.rows("select 'x' from dual").each{   r-> println r[0]  }

        def storedProcCall = """{? = call harv_import.f_get_blob($rep.id)}"""
        println "before calling stopred proc"
        sql.call(storedProcCall, [Sql.BLOB])
                {res ->
                    println "before file download.."
                    BLOB blb = res
                    //java.sql.Blob blb = res
                    InputStream ist = blb.getBinaryStream()
                    Filedownload.save(ist, "text/html", "$rep.objectName")
                }
        //sql.close()
    }

    //---------------------------------------------------
    def clearAll() {
        // clear key block
        clearEnvironment()

    }

    def clearEnvironment() {
        // environmentCombo.getChildren().clear()
        environmentCombo.value = ""
        clearPackageObject()
    }

    def clearPackageObject() {
        /*packageObjectText.getChildren().clear()
        packageObjectText.value = ""*/
        objectList.clear()
        packageObjectCombo.getChildren().clear()
        packageObjectCombo.value=""
        ObjPaging.totalSize = 1


    }

    def onChanging_packageObjectCombo(InputEvent ie) {
        clearPackageObject()
        def packObjArray = Repository.executeQuery("""select distinct rep.objectName from Repository rep where rep.environmnt = '${environmentCombo.value}' and  rep.objectName like '%${ie.value}%'""", [max: 5])
        packObjArray += Repository.executeQuery("""select distinct rep.packageName from Repository rep where rep.environmnt = '${environmentCombo.value}' and  rep.packageName like '%${ie.value}%'""", [max: 5])
        ListModelList packObjModel = new ListModelList(packObjArray);
        packageObjectCombo.setModel(packObjModel)
    }

    def setProdIndicator() {
        onlyProd = ""
        if (inProdVerRadioBtn.selectedItem.label == "Prod Versions Only") {
            onlyProd = "and rep.appliedOn is not null"
        }

    }

    def setTotalCount() {
        setProdIndicator()
        //def cnt = Repository.executeQuery("""select count(*)  from Repository rep where rep.environmnt = '${environmentCombo.value}' ${onlyProd} and  (  lower(rep.objectName) like lower('%${packageObjectText.value.value}%') or lower(rep.packageName) like lower('%${packageObjectText.value.value}%')) """)
        def cnt = Repository.executeQuery("""select count(*)  from Repository rep where rep.environmnt = '${environmentCombo.value}' ${onlyProd} and  (  lower(rep.objectName) like lower('%${packageObjectCombo.value.value}%') or lower(rep.packageName) like lower('%${packageObjectCombo.value.value}%')) """)
        ObjPaging.totalSize = cnt[0]
        ObjPaging.pageSize =   fetchText.value.toInteger()
    }

    def clearObjInfo(def something) {
        createdBy.value = ""
        createdByVal.value = ""
        created.value = ""
        createdVal.value = ""

        modifiedBy.value = ""
        modifiedByVal.value = ""
        applied.value = ""
        appliedVal.value = ""
        modifiedOn.value = ""
        modifiedOnVal.value = ""
    }
    def displayObjInfo(def item) {
        objInfoGroup.height = ObjectListGroup.height
        clearObjInfo("")
        createdBy.value = "Created By"
        createdByVal.value = item.createdBy
        created.value = "Created On:"
        createdVal.value = item.created

        modifiedBy.value = "Modified By:"
        modifiedByVal.value = item.modifiedBy
        if (item?.appliedOn) {
            applied.value = "Package Applied On:"
            appliedVal.value = item.appliedOn
        }
        modifiedOn.value = "Modified On:"
        modifiedOnVal.value = item.modifiedOn
    }


    def objectRefresh(page = 0) {

        setProdIndicator()

        //def packObjArray = Repository.executeQuery(""" from Repository rep where rep.environmnt = '${environmentCombo.value}'  ${onlyProd}  and  (  lower(rep.objectName) like lower('%${packageObjectText.value.value}%') or lower(rep.packageName) like lower('%${packageObjectText.value.value}%'))  order by rep.objectName, rep.versn desc """, [offset: page * ObjPaging.pageSize, max: ObjPaging.pageSize])
        def packObjArray = Repository.executeQuery(""" from Repository rep where rep.environmnt = '${environmentCombo.value}'  ${onlyProd}  and  (  lower(rep.objectName) like lower('%${packageObjectCombo.value.value}%') or lower(rep.packageName) like lower('%${packageObjectCombo.value.value}%'))  order by rep.objectName, rep.versn desc """, [offset: page * ObjPaging.pageSize, max: ObjPaging.pageSize])
        objectList.clear()

        packObjArray.each {objToAdd ->
            objectList.append {
                listitem(value: objToAdd,) {
                    listcell(label: "$objToAdd.objectName", onMouseOver: {displayObjInfo objToAdd}, onMouseOut: {clearObjInfo ''});
                    listcell(label: "$objToAdd.packageName");
                    listcell(label: "$objToAdd.versn");
                    if (objToAdd?.appliedOn) {
                        listcell(label: "Y")
                    }
                    else {
                        listcell(label: "N")
                    }
                }
            }

        }

    }
}
