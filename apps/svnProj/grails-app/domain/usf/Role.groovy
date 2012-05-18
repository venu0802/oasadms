package usf

class Role {
    String name
    String description
    static def hasMany = [environments: Environment]
    //static belongsTo = [parent:Staff]
    static constraints = {
        environments(nullable: true)
        description(nullable: true, blank:true)
    }
}
