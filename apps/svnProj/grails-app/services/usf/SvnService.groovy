package usf

class SvnService {

    static transactional = true
    def dataSource

    def  getDataSource(){
        return   dataSource
    }

    def roleExistsForStaff(String userId){
       def stff = Staff.findByUserId(userId)
        if (stff?.role){
            true
        }
        else{
            false
        }
    }

     def String saveStaffRole(String userId,  String roleName){
        def stff = Staff.findByUserId(userId)
        stff.role = Role.findByName(roleName)
        stff.save(flush:true)
   }
    def removeStaffRole(String userId) {
        def stff = Staff.findByUserId(userId)
        stff.role = null
        stff.save(flush:true)
    }

    def String saveRoleEnvs(String roleName, ArrayList envNameArrayToSave) {
          Role rol = Role.findByName(roleName)
          def envs = rol.environments
          envNameArrayToSave.each{
              envs<< new Environment(name:"${it}")
          }
          rol.environments = envs;
          rol.save(flush:true) ;
    }

    def String removeRoleEnvs(String roleName, ArrayList envNameArrayToDelete) {
         Role rol = Role.findByName(roleName)
              println "role is ${rol.name}"
              def envToDeleteStringArray  =  rol.environments*.name -  envNameArrayToDelete
              def envsToDelete = envNameArrayToDelete.collect {nm->Environment.findByName(nm)}
              def envsExisting = rol.environments;
              def envsShouldBe =  envsExisting- envsToDelete;
              rol.environments = null
              rol.save(flush:true)
              Role rol2 = Role.findByName(roleName)
              rol2.environments = []
               envToDeleteStringArray.each {

                   rol2.environments << new Environment(name:it)
               }

               rol2.save(flush:true)

        "removed Environments"
    }
    def String saveStaff(String staffUserId){

            def stf = new Staff(userId: staffUserId)
            stf.save(flush:true)
            "Saved Staff"

        }

    def String saveRole(String roleName){

        def rol = new Role(name:roleName)
        rol.save(flush:true)
        "Saved Role"

    }
      def String deleteRole(String roleName) {
        Role rol = Role.findByName(roleName)
        rol.environments= null;
        rol.save(flush:true)
        rol.delete(flush:true)
         "Deleted Role"
    }

    def serviceMethod() {

    }
}
