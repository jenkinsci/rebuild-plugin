package dsl

/**
 * Created by beazlr02 on 23/04/16.
 */
def call(body = {}) {
    def config = [:]
    if (body != null) {
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = new StagePromotionDelegate(config)
        body()
    }

    def jenkinsUrl = "${env.JENKINS_URL}"
    def jobPath = currentBuild.getAbsoluteUrl().replace(jenkinsUrl, '/')
    def promoUrl = jobPath + "promoterebuild"
    echo promoUrl

    manager.addBadge('clock.png', config.message, promoUrl)

}

class StagePromotionDelegate
{
    def map

    StagePromotionDelegate(map)
    {
        this.map = map
        this.map['message'] = 'Promote to RELEASE'

    }

    def message(msg)
    {
        this.map['message'] = msg
    }
}

