package dsl

/**
 * Created by beazlr02 on 23/04/16.
 */
def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()


    def jenkinsUrl = "${env.JENKINS_URL}"
    def jobPath = currentBuild.getAbsoluteUrl().replace(jenkinsUrl,'/')
    def promoUrl = jobPath + "promoterebuild"
    echo promoUrl

    manager.addBadge('clock.png','Promote to RELEASE',promoUrl)

}
