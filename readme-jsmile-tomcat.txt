Put the JNI libraries in the tomcat/shared/lib directory and set the LD_LIBRARY_PATH environment variable with the path to the JNI library. 
Also set the JAVA_OPTS environment variable to include the -Djava.library.path option. Tomcat 7 btw.

You will need to create the shared/lib directories if they do not exist.

Add the following lines in, e.g., catalina.sh:

LD_LIBRARY_PATH=/usr/tomcat/shared/lib:$LD_LIBRARY_PATH
JAVA_OPTS="-Djava.library.path=/usr/tomcat/shared/lib"