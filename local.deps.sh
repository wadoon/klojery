#!/bin/sh

KEY_VERSION=2.5
GRP=org.key-project

mkdir -p repo

function install ()
{
    name=$(basename ${1%%.jar})
    #lein localrepo install -r lib $1 $GRP/$name $KEY_VERSION &

	mvn deploy:deploy-file -DgroupId=$GRP -DartifactId=$name \
		  -Dversion=$KEY_VERSION -Dpackaging=jar -Dfile=$1 \
		    -Durl=file:repo

    echo "[$GRP/$name $KEY_VERSION]"
}

install resources/components/key.core.jar
install resources/components/key.core.proof_references.jar
install resources/components/key.core.jar
install resources/components/key.core.symbolic_execution.jar
install resources/components/key.removegenerics.jar
install resources/components/key.ui.jar
install resources/components/key.util.jar
install resources/libs/antlr.jar
install resources/libs/recoderKey.jar
