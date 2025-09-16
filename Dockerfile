FROM eclipse-temurin:17-jre

WORKDIR /usrapp/bin

ENV PORT 6000

# Copia las clases compiladas y las dependencias
COPY target/classes ./classes
COPY target/lib ./dependency

# Ajusta al nombre completo de tu clase main
CMD ["java","-cp","./classes:./dependency/*","com.arep.taller1.talle1arep.MicroSpringBoot"]
