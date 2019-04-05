#!/bin/sh

_debug() {
	if [ -n "${DEBUG}" ]
	then
		echo "DEBUG:   $1" >&2 
		shift
		for text in "$@"
		do
			echo "         ${text}" >&2
		done
	fi
}

_error() {
	echo "ERROR:   $1" >&2
	shift
	for text in "$@"
	do
		echo "         ${text}" >&2
	done
}

findjava() {
	if [ -n "${JAVA_HOME}" ] && [ -x "${JAVA_HOME}/bin/java" ]
	then
		JAVACMD="${JAVA_HOME}/bin/java"
		_debug "Using \$JAVA_HOME to find java virtual machine."
	else
		JAVACMD=$(which java)
		if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]
		then
			_debug "Using \$PATH to find java virtual machine."
		elif [ -x /usr/bin/java ]
		then
			_debug "Using /usr/bin/java to find java virtual machine."
			JAVACMD=/usr/bin/java
		fi
	fi

	if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]
	then
		_debug "Using '$JAVACMD' as java virtual machine..."
		if [ -n "${DEBUG}" ]
		then
			"$JAVACMD" -version
		fi
		return 0
	else
		_error "Couldn't find a java virtual machine," \
		       "define JAVA_HOME or PATH."
		return 1
	fi
}

_debug "aCCinaPDF parameters are '${@}'."
_debug "$(uname -a)"

findjava
if [ $? -ne 0 ]
then
	exit 1
fi

aCCinaPDFpath=$(dirname "$0")
if [ ! -f "${aCCinaPDFpath}/aCCinaPDF.jar" ]
then
	_error "Couldn't find aCCinaPDF under '${aCCinaPDFpath}'."
	exit 1
else	
	_debug "aCCinaPDF directory is '${aCCinaPDFpath}'."
	break
fi

_debug "Calling: '${JAVACMD} -jar ${aCCinaPDFpath}/aCCinaPDF.jar $@'."
"${JAVACMD}" -jar ${aCCinaPDFpath}/aCCinaPDF.jar "$@"
