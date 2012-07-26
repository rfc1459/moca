#!/bin/sh
# Use this ONLY if you need to deploy the bugsense jar to your local repository
set -e

BASE_URL="http://www.bugsense.com/static/libraries"

GROUPID="com.bugsense"
ARTIFACTID="bugsense"
VERSION="1.9"
BUGSENSE_JAR="${ARTIFACTID}-${VERSION}.jar"

cd /tmp

wget -4 -c ${BUGSENSE_URL}/${BUGSENSE_JAR}

mvn install:file -Dfile=${BUGSENSE_JAR} \
                 -DgroupId=${GROUPID} \
                 -DartifactId=${ARTIFACTID} \
                 -Dversion=${VERSION} \
                 -Dpackaging=jar \
                 -DgeneratePom=true

rm ${BUGSENSE_JAR}

exit 0
