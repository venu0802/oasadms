package usf

import org.zkoss.zkgrails.*
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Groupbox
import org.zkoss.zul.Combobox
import org.zkoss.zul.Button
import edu.yale.its.tp.cas.client.filter.CASFilter
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Label
import org.zkoss.zk.ui.event.MouseEvent
import org.zkoss.zul.Listbox
import org.zkoss.zk.ui.event.Event

class AdminComposer extends GrailsComposer {
    def roleStaffRadioBtn
    org.zkoss.zul.Listbox availableList
    def assignedList
    def availableLbl
    def assignedLbl
    Groupbox detailGroupBox
    Button createBtn
    Button deleteBtn
    Combobox roleStaffCombo
    def vboxBtn
    def vboxAvailable
    Button addToAssignedBtn
    Button rmvFromAssignedBtn
    Button renameBtn
    Label renameLabl
    String oldValue
    def staffRoleDetailsDiv
    def staffRoleDetails
    def vers
    def svnService

    def init() {
        detailGroupBox.visible = false
        deleteBtn.visible = false
        createBtn.visible = false
        roleStaffCombo.getChildren().clear()
        roleStaffCombo.value = ""

        assignedList.clear()
        availableList.clear()
        availableList.multiple = true
        renameLabl.visible = false
    }

    def setLabels() {

        if (roleStaffRadioBtn.selectedItem.label == "Role") {
            availableLbl.value = "Available Environments:"
            assignedLbl.value = "Assigned Environments:"
        }
        else {
            availableLbl.value = "Available Roles:"
            assignedLbl.value = "Assigned Role:"
        }
    }

    def setRoleStaffCombo(String roleStaffValue) {
        def rollStaffList
        if (roleStaffRadioBtn.selectedItem.label == "Role") {
            rollStaffList = (Role.findAllByNameIlike("%${roleStaffValue}%", [max: 5]))*.name

        }
        else {
            rollStaffList = (Staff.findAllByUserIdIlike("%${roleStaffValue}%", [max: 5]))*.userId
        }
        if (rollStaffList?.size() > 0) {
            roleStaffCombo.setModel(new ListModelList(rollStaffList))
        }

    }

def setDetail() {
        if (roleStaffRadioBtn.selectedItem.label == "Role") {
            availableList.multiple = true
            def assingedEnvs = (Role.findByName(roleStaffCombo.value)?.environments)*.name
            def vers = Repository.executeQuery("""select distinct rep.environmnt from Repository rep """)
            vers = vers - assingedEnvs
            availableList.setModel(new ListModelList(vers))
            if (assingedEnvs?.size() > 0) {

                assignedList.setModel(new ListModelList(assingedEnvs))
            }
            else {
                assignedList.clear()
            }
            addToAssignedBtn.disabled = false
            rmvFromAssignedBtn.disabled = false
        }
        else {
            availableList.multiple = false
            def vers = Role.list()*.name
            ArrayList al = []
            al << Staff.findByUserId(roleStaffCombo.value)?.role?.name
            assignedList.setModel(new ListModelList(al))
            vers = vers - Staff.findByUserId(roleStaffCombo.value)?.role?.name
            availableList.setModel(new ListModelList(vers))
             if (svnService.roleExistsForStaff(roleStaffCombo.value)) {
                addToAssignedBtn.disabled = true
                rmvFromAssignedBtn.disabled = false
            }
            else {
                addToAssignedBtn.disabled = false
                rmvFromAssignedBtn.disabled = true
            }


        }

    }


    def displayDetail() {
        createBtn.visible = false
        deleteBtn.visible = true
        renameBtn.visible = true
        detailGroupBox.visible = true
        setDetail()
        vboxBtn.height = vboxAvailable.height
    }

       def displaySave() {
        createBtn.visible = true
        deleteBtn.visible = false
        detailGroupBox.visible = false
        renameBtn.visible = false
        if (session.hasAttribute("oldValue")) {
            createBtn.visible = false
            renameBtn.visible = true
            renameLabl.visible = true
        }

    }
    //---------------------------------------------------------------------------------
    def afterCompose = { window ->
        // initialize components here
        def username = session?.getAttribute(CASFilter.CAS_FILTER_USER)

               if (!Staff.findByUserId(username)?.role.name == "admin") {
                   Executions.sendRedirect("/error.zul")
               }


               init()

    }

    def onClick_roleStaffRadioBtn() {
        init()

        setLabels()
        staffRoleDetails.getChildren().clear()
    }

     def onChanging_roleStaffCombo(InputEvent ie) {
        deleteBtn.visible = false
        createBtn.visible = false
        roleStaffCombo.getChildren().clear()
        setRoleStaffCombo("${ie.value}")
    }

    //handle rollStaffCombo when entered
    def onBlur_roleStaffCombo() {
        // alert "old value is ${session.getAttribute("oldValue")}"
        assignedList.clear()
        availableList.clear()
        availableList.multiple = true

        if (roleStaffRadioBtn.selectedItem.label == "Role") {
            staffRoleDetails.getChildren().clear()
            if (Role.findByName(roleStaffCombo.value)) {
                displayDetail()
                def staffs = Staff.withCriteria {
                    role {
                        eq("name", "${roleStaffCombo.value}")
                    }
                }
                if (staffs?.size() > 0) {
                    //staffRoleDetailsDiv.getChildren().clear()
                    staffRoleDetails.getChildren().clear()
                    staffRoleDetails.append {
                        staffs.each {
                            label(value: "${it.userId}");
                        }
                    }
                }
            }
            else {
                 staffRoleDetails.getChildren().clear()
                displaySave()
            }
        }
        else {
            if (Staff.findByUserId(roleStaffCombo.value)) {
                displayDetail()
            }
            else {
                displaySave()
            }
        }

    }

 // handle rollStaff save button

    def onClick_createBtn() {
        if (roleStaffRadioBtn.selectedItem.label == "Role") {
            alert(svnService.saveRole(roleStaffCombo.value))
            displayDetail()
        }
        else {
            alert(svnService.saveStaff(roleStaffCombo.value))
            displayDetail()
        }
    }

    // handle rollStaff delete button

    def onClick_deleteBtn() {

        if (roleStaffRadioBtn.selectedItem.label == "Role") {
            alert(svnService.deleteRole(roleStaffCombo.value))
        }
        else {
            def stff = Staff.findByUserId(roleStaffCombo.value)
            stff.delete(flush: true)
            alert("Staff deleted")
        }
        roleStaffCombo.value = null
        displaySave()
    }

    // handle rollStaff rename button
    def onClick_renameBtn() {
        if (session.hasAttribute("oldValue")) {
            alert """changing ${session.getAttribute("oldValue")} to ${roleStaffCombo.value}"""
            if (roleStaffRadioBtn.selectedItem.label == "Staff") {
                def stff = Staff.findByUserId(session.getAttribute("oldValue"))
                stff.userId = roleStaffCombo.value
                stff.save(flush: true)

            }
            else {
                def rol = Role.findByName(session.getAttribute("oldValue"))
                rol.name = roleStaffCombo.value
                rol.save(flush: true)

            }
            session.removeAttribute("oldValue")
            renameLabl.visible = false
            //displaySave()
            displayDetail()


        }
        else {
            session.setAttribute("oldValue", "${roleStaffCombo.value}")
            renameLabl.visible = true
        }
    }

 // handle assign button

    def onClick_addToAssignedBtn() {
        if (roleStaffRadioBtn.selectedItem.label == "Role") {

            svnService.saveRoleEnvs(roleStaffCombo.value, availableList.selectedItems*.value)
        } else {
            // check if role exits for staff. If it does, then do not allow
            svnService.saveStaffRole(roleStaffCombo.value, availableList.selectedItem.value)


        }
        // else allow to apply only one role.

        displayDetail()


    }

    def onClick_rmvFromAssignedBtn() {

        if (roleStaffRadioBtn.selectedItem.label == "Role") {

            svnService.removeRoleEnvs(roleStaffCombo.value, assignedList.selectedItems*.value)
        }
        else {
            svnService.removeStaffRole(roleStaffCombo.value)
        }
        displayDetail()


    }



}
