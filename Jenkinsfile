stage("Check out") {
	node {
		checkout scm
		stash "sources"
	}
}

stage("Build") {
	node("Linux") {
		unstash "sources"

		withCredentials([[$class: 'StringBinding', credentialsId: '03a932ce-06c6-494e-9da2-809f9e797c18', variable: 'GITHUB_ACCESS_TOKEN']]) {
            sh("./ci.sh -g $GITHUB_ACCESS_TOKEN")
        }
	}
}
