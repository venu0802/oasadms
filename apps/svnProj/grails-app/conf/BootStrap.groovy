import usf.Environment
import usf.Repository
import usf.Role
import usf.Staff

class BootStrap {


    def init = { servletContext ->
        if (!Environment.get(1)) {
            Repository.executeQuery("""select distinct rep.environmnt from Repository rep """).sort().each{
                new Environment(name:it).save(flush: true)
            }
        }
        if (!Role.findByName('admin')){
            Role rl = new Role(name: 'admin')
            Environment.findAll().each {
                rl.addToEnvironments(it)
            }
            rl.save(flush: true)
        }
        if (!Staff.findByUserId('vbangalo'))  {
              Role vrole = Role.findByName('admin')
              new Staff(userId: 'vbangalo',role: vrole ).save(flush: true)
        }
        if (!Staff.findByUserId('rfturner'))  {
            Role vrole = Role.findByName('admin')
            new Staff(userId: 'rfturner',role: vrole ).save(flush: true)
        }

        if (!Staff.findByUserId('whurley'))  {
            Role vrole = Role.findByName('admin')
            new Staff(userId: 'whurley',role: vrole ).save(flush: true)
        }

    }
    def destroy = {



    }
}
