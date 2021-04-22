file(GLOB_RECURSE JAVA_SOURCES CONFIGURE_DEPENDS "*.java")

add_jar(donceykongjr-server
	VERSION 1.0.0
	MANIFEST Manifest.txt 
	INCLUDE_JARS /usr/share/java/json-simple/json-simple.jar
	SOURCES ${JAVA_SOURCES})
