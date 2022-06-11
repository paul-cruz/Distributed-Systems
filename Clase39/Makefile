################################################################################
# 
# Nombre: Juan Paul Cruz Cruz
# Grupo: 4CM12
# Proyecto 3 
# Desarrollo de Sistemas Dstribuidos
################################################################################

JFLAGS = -d
FLAGS = -cf


JC = javac


RUTA = ./
CLASSES = 	LoadBalancer.java \
		ProcessServer.java \
		Queue.java \
		RequestsHandler.java \
		SerializationUtils.java \
		StorageServer.java \
		ThreadTask.java \
		Utils.java \
		WebClient.java \
		WordRegistry.java

.SUFFIXES: .java .class .jar

.java.class:
	$(JC) $(JFLAGS) $(RUTA) $*.java


default: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
	
run:
	java WebServer











