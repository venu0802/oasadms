package usf

import org.zkoss.zkgrails.*
import org.zkoss.zk.ui.event.InputEvent;
import edu.yale.its.tp.cas.client.filter.CASFilter
import org.zkoss.zul.Combobox
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Label
import org.zkoss.zul.Listbox
import javax.sql.DataSource
import groovy.sql.Sql
import oracle.sql.BLOB
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Radiogroup

//use for debug alert("$username")
//alert environmentCombo.class.name
// importent  onChanging_ use  org.zkoss.zk.ui.event.InputEvent i other wise it wont work
class CheckOutComposer extends GrailsComposer {
    // zul objects
    Combobox environmentCombo
    Combobox packageObjectCombo
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
    Listbox objectList
    Radiogroup  inProdVerRadioBtn
    def downLoadBtn

    // local objects
    String username
    ArrayList environmentArrayList
    ListModelList environmentModelList
    def svnService
    // triggers
    // initiazie
    def afterCompose = { window ->

        // initialize Environment and Package combo components here
        clearAll()
        username = session?.getAttribute(CASFilter.CAS_FILTER_USER)
        // set Environment como based on staff user name
        environmentArrayList =  (Staff.findByUserId(username).role.environments*.name).sort{ a, b -> a.compareToIgnoreCase b }
        environmentArrayList.each{alert(it)}
        environmentModelList = new ListModelList(environmentArrayList.sort{ a, b -> a.compareToIgnoreCase b });
        environmentCombo.setModel(environmentModelList.sort{ a, b -> a.compareToIgnoreCase b })
    }

    def onChanging_environmentCombo(InputEvent ie) {
        alert("hi")
        message("hi")

        def envSubList = environmentArrayList.findAll {it.toLowerCase().contains("${ie.value}".toLowerCase())}
        environmentModelList = new ListModelList(envSubList.sort{ a, b -> a.compareToIgnoreCase b });
        clearAll()
        environmentCombo.setModel(environmentModelList)
    }



    def onChange_environmentCombo() {
        clearPackageObject()
    }

    def onChanging_packageObjectCombo(InputEvent ie) {
        clearPackageObject()
        def packObjArray = Repository.executeQuery("""aselect distinct rep.objectName from Repository rep where rep.environmnt = '${environmentCombo.value}' and  rep.objectName like '%${ie.value}%'""", [max: 5])
        packObjArray += Repository.executeQuery("""select distinct rep.packageName from Repository rep where rep.environmnt = '${environmentCombo.value}' and  rep.packageName like '%${ie.value}%'""", [max: 5])
        ListModelList packObjModel = new ListModelList(packObjArray);
        packageObjectCombo.setModel(packObjModel)
    }

    def onChange_packageObjectCombo() {

          displayObjects()
    }

    def onClick_inProdVerRadioBtn() {
        displayObjects()
    }

    def onClick_downLoadBtn() {
        alert('downloading')
        Repository rep = objectList.getSelectedItem().value
        // dataSource = (DataSource) WebApplicationContextUtils.getWebApplicationContext(ServletContextHolder.servletContext).getBean('dataSource')
        DataSource dataSource = (DataSource) svnService.getDataSource()
        def sql = new groovy.sql.Sql(dataSource)

        def storedProcCall = """{? = call harv_import.f_get_blob($rep.id)}"""
        sql.call(storedProcCall, [Sql.BLOB])
                {res ->
                    BLOB blb = res
                    InputStream ist = blb.getBinaryStream()
                    Filedownload.save(ist, "text/html", "$rep.objectName")
                }
        sql.close()
    }


    //--------------------------------------------------------------------------------------------------------
    // procs
    def clearAll() {
        // clear key block
        clearEnvironment()
        //clear object info
        clearObjInfo()
        // clear detail block
        clearDetails()
    }

    def clearEnvironment() {
        clearObjInfo()
        environmentCombo.getChildren().clear()
        packageObjectCombo.getChildren().clear()
    }

    def clearPackageObject() {
        packageObjectCombo.getChildren().clear()
        packageObjectCombo.value = ""
        clearObjInfo()
        clearDetails()

    }

    def clearDetails() {
        objectList.clear()
        clearObjInfo()
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

    def displayObjects() {
        def onlyProd = ""
        if (inProdVerRadioBtn.selectedItem.label == "Prod Versions Only") {
            onlyProd = "and rep.appliedOn is not null"
        }

        def objArray = Repository.executeQuery("""from Repository rep where rep.environmnt = '${environmentCombo.value}'  ${onlyProd}  and   rep.objectName = '${packageObjectCombo.value}' order by rep.objectName, rep.versn desc""")
        objArray += Repository.executeQuery("""from Repository rep where rep.environmnt = '${environmentCombo.value}'  ${onlyProd}  and  rep.packageName =  '${packageObjectCombo.value}' order by rep.objectName, rep.versn desc""")


        objectList.clear()
        objArray.each {objToAdd ->
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

    def displayObjInfo(def item) {
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
}
