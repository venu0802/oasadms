package usf

class Repository {
    String objectName
    String packageName
    String environmnt
    int versn
    int id
    String createdBy
    String created
    String modifiedOn
    String modifiedBy
    String appliedOn




    static mapping = {
        table 'harvest_objects'
        version false
        objectName column:'object_name';
        packageName column:'package_name';
        id generator :'assigned', column:'version_id';
        environmnt column:'environment';
        versn column:'version_nbr'  ;
        createdBy column:'created_by' ;
        created column:'created'
        modifiedOn column:'modified'
        modifiedBy colomn:'modified_by'
        appliedOn column: 'applied'

    }

    static constraints = {
    }
}
