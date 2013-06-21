class UrlMappings {

	static mappings = {
        "/lookUpInfo/"(controller: "faculty"){
            action=[GET:"showLookupInfo"]
        }

        "/facultyInfo/$netId?"(controller: "faculty"){
            action=[GET:"showFacultyInfo"]
        }

        "/login/$tick?"(controller: "login"){
            action=[GET:"check"]
        }


		"500"(view:'/error')
	}
}
