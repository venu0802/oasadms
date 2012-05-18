package usf

class Staff {
    String userId
    Role role
    static constraints = {
        role(nullable:true)
    }
}
