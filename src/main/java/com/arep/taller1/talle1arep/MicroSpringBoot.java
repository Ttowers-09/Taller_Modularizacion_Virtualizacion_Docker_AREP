package com.arep.taller1.talle1arep;
import java.io.File;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MicroSpringBoot {

    public static void main(String[] args) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();


        if (args != null && args.length > 0) {
            for (String className : args) {
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(RestController.class)) {
                        controllers.add(clazz);
                    } else {
                        System.out.println("Advertencia: " + className + " no tiene @RestController. Se ignora.");
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("No se encontró la clase: " + className);
                }
            }
            if (controllers.isEmpty()) {
                System.out.println("No se registró ningún controlador desde args. Saliendo.");
                return;
            }
            registerControllersAndStart(controllers);
            return; 
        }

        String basePackage = "com.arep.taller1.talle1arep";
        controllers = scanControllersFromClasspath(basePackage);

        if (controllers.isEmpty()) {
            System.out.println("No se encontraron controladores con @RestController en el classpath.");
            return;
        }

        registerControllersAndStart(controllers);
    }

    private static List<Class<?>> scanControllersFromClasspath(String basePackage) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();

        URI rootUri = MicroSpringBoot.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path root = Paths.get(rootUri);

        String basePath = basePackage.replace('.', File.separatorChar);
        Path start = root.resolve(basePath);
        if (!Files.exists(start)) return controllers;

        try (Stream<Path> stream = Files.walk(start)) {
            stream.filter(p -> p.toString().endsWith(".class"))
                  .filter(p -> !p.getFileName().toString().contains("$")) 
                  .forEach(p -> {
                      String rel = root.relativize(p).toString();
                      String fqcn = rel.substring(0, rel.length() - ".class".length())
                                      .replace(File.separatorChar, '.');
                      try {
                          Class<?> clazz = Class.forName(fqcn);
                          if (clazz.isAnnotationPresent(RestController.class)) {
                              controllers.add(clazz);
                          }
                      } catch (Throwable ignored) {}
                  });
        }
        return controllers;
    }

    private static void registerControllersAndStart(List<Class<?>> controllers) throws Exception {
        for (Class<?> controllerClass : controllers) {
            System.out.println("Controlador: " + controllerClass.getName());
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method[] methods = controllerClass.getDeclaredMethods();
            for (java.lang.reflect.Method m : methods) {
                if (m.isAnnotationPresent(GetMapping.class)) {
                    final java.lang.reflect.Method method = m;
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    String path = mapping.value();
                    System.out.println("Registrando endpoint: " + path + " -> " + method.getName());

                    HttpServer.get(path, (req, res) -> {
                        try {
                            java.lang.reflect.Parameter[] params = method.getParameters();
                            Object[] argsForMethod = new Object[params.length];
                            for (int i = 0; i < params.length; i++) {
                                if (params[i].isAnnotationPresent(RequestParam.class)) {
                                    RequestParam rp = params[i].getAnnotation(RequestParam.class);
                                    String paramName = rp.value();
                                    String defaultValue = rp.defaultValue();
                                    String value = req.getValues(paramName);
                                    if (value == null || value.isEmpty()) value = defaultValue;
                                    argsForMethod[i] = value;
                                } else {
                                    argsForMethod[i] = null;
                                }
                            }
                            Object result = method.invoke(controllerInstance, argsForMethod);
                            return result != null ? result.toString() : "";
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "Error interno";
                        }
                    });
                }
            }
        }
        HttpServer.main(new String[]{});
    }
}
