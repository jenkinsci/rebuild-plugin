stage("Check out") {
	node {
		checkout scm
		stash "sources"
	}
}

stage("Build") {
	node("Linux") {
		unstash "sources"
		sh("./ci.sh")
	}
}
