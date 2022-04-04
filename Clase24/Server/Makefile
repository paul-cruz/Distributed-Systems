################################################################################
# definiendo variables a usar en el makefile
################################################################################

# banderas para compilar
JFLAGS = -d
FLAGS = -cf

# compiladores
JC = javac
JR = jar

# otras variables
RUTA = ./
CLASSES = 	WebServer.java \
		SerializationUtils.java \
		Demo.java

################################################################################
# definiendo sufijos y reglas de sufijos
################################################################################

# definiendo sufijos que van a utilizarse en el makefile
.SUFFIXES: .java .class .jar

# empleando el sufijo "-d" para compilar archivo del WebClient
# NOTA: ES NECESARIO DEJAR EL SALTO DE LÍNEA O MARCA ERROR
.java.class:
	$(JC) $(JFLAGS) $(RUTA) $*.java

################################################################################
# definiendo los comandos posibles de usar por el usuario
################################################################################

# comando para compilar códigos si el usuario escribe "make"
default: $(CLASSES:.java=.class) $(APPCL:.class=.jar)

# comando para eliminar los archivos ".class", ".jar" y directorio "networking"
# NOTA: ES NECESARIO DEJAR EL SALTO DE LÍNEA O MARCA ERROR
clean:
	$(RM) *.class
	
# comando para ejecutar código principal del cliente
# NOTA: ES NECESARIO DEJAR EL SALTO DE LÍNEA O MARCA ERROR
run:
	java WebServer











