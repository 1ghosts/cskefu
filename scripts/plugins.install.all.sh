#! /bin/bash 
###########################################
#
###########################################

# constants
baseDir=$(cd `dirname "$0"`;pwd)
# functions

# main 
[ -z "${BASH_SOURCE[0]}" -o "${BASH_SOURCE[0]}" = "$0" ] || return
cd $baseDir/..

if [ -d ./private/plugins ]; then
    ./private/plugins/scripts/install-all.sh
fi

cd $baseDir/..
if [ -d ./public/plugins ]; then
    ./public/plugins/scripts/install-all.sh 
fi
