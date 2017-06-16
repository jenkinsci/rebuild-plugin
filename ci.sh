#!/bin/bash

VERSION_NAME=0.2.$BUILD_NUMBER

function log_good
{
    echo -e "\033[32m"$1"\033[39m"
}

function log_warn
{
    echo -e "\033[33m"$1"\033[39m"
}

function log_info
{
    echo -e "\033[36m"$1"\033[39m"
}

function log_bad
{
    echo -e "\033[31m"$1"\033[39m"
}

while getopts "g:" opt; do
    case "$opt" in
        g)  ACCESS_TOKEN=$OPTARG
			log_info "Will upload release to GitHub using access token: $ACCESS_TOKEN"
            ;;
    esac
done
shift "$((OPTIND-1))"

if [ ! -e mvnw ]; then
	log_info "Downloading Maven"
	curl "http://mirror.ox.ac.uk/sites/rsync.apache.org/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip" > maven.zip
	mkdir mvnw
	cd mvnw
	unzip ../maven.zip
	cd ..
	rm maven.zip
	log_info "Maven is now ready to use"
fi

export PATH=`pwd`/mvnw/apache-maven-3.3.9/bin:$PATH

log_info "Building mobileci-support plugin version $VERSION_NAME"

mvn verify "-Dartifact.version=$VERSION_NAME"
mvn hpi:hpi "-Dartifact.version=$VERSION_NAME"

if [ $? != 0 ]; then
    log_bad "Failed to build HPI artifacts"
    exit -1
fi

log_info "`pwd`/target/promotebuild.hpi built"

log_info "Creating Github Release"

if [ ! -z "$ACCESS_TOKEN" ]; then
    
	REPOSITORY_NAME=mobileci-jenkins-support
	VERSION=$VERSION_NAME
	ARTEFACT_FILENAME=promotebuild-$VERSION_NAME.hpi

	# Create a GitHub release
	log_info "Creating release $VERSION on GitHub..."
	API_JSON=$(printf '{ "tag_name": "v%s", "target_commitish": "master", "name": "v%s", "body": "%s", "draft": false, "prerelease": false}' $VERSION $VERSION $VERSION)
	
	RELEASE_URL=$(curl -i --data "$API_JSON" https://api.github.com/repos/bbc/$REPOSITORY_NAME/releases?access_token=$ACCESS_TOKEN | tr -d '\r' | sed -En 's/^Location: (.*)/\1/p')
	RELEASE_ID="${RELEASE_URL##*/}"

	if [ -z "$RELEASE_ID" ]; then
	    log_bad "Failed to create Github release"
	    exit -1
    fi

	# Upload binary artefacts
	HPI_UPLOAD="`pwd`/target/promotebuild.hpi"
	HPI_UPLOAD_NAME="$ARTEFACT_FILENAME"
	log_info "Uploading HPI artefact as $HPI_UPLOAD_NAME for GitHub release ID $RELEASE_ID..."
	curl -v -XPOST -H "Authorization:token $ACCESS_TOKEN" -H "Content-Type:application/octet-stream" --data-binary "@$HPI_UPLOAD" https://uploads.github.com/repos/bbc/$REPOSITORY_NAME/releases/$RELEASE_ID/assets?name=$HPI_UPLOAD_NAME
else
    log_info "No Github access token provided so no Github release will be made."
fi
