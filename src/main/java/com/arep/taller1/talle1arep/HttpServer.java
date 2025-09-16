package com.arep.taller1.talle1arep;

import java.net.*;
import java.io.*;
import java.nio.file.*;

import java.util.HashMap;
import java.util.Map;

// Interfaz funcional para los handlers de rutas
@FunctionalInterface
interface RouteHandler {
    String handle(Request req, Response res);
}

// Clases Request y Response básicas para el framework
class Request {
    private final String path;
    private final String query;
    private final String method;
    private final String body;
    public Request(String path, String query, String method, String body) {
        this.path = path;
        this.query = query;
        this.method = method;
        this.body = body;
    }
    public String getPath() { return path; }
    public String getQuery() { return query; }
    public String getMethod() { return method; }
    public String getBody() { return body; }
    public String getValues(String key) {
        String params = method.equals("POST") ? body : query;
        if (params == null) return null;
        for (String param : params.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(key)) return pair[1];
        }
        return null;
    }
}

class Response {
    // Se puede expandir en el futuro
}


public class HttpServer {
    // Mapas para almacenar rutas GET y POST y sus handlers
    private static final Map<String, RouteHandler> getRoutes = new HashMap<>();
    private static final Map<String, RouteHandler> postRoutes = new HashMap<>();

    /**
     * Devuelve el handler registrado para una ruta GET específica (solo para pruebas).
     */
    public static RouteHandler getGetRouteHandler(String path) {
        return getRoutes.get(path);
    }

    /**
     * Devuelve el handler registrado para una ruta POST específica (solo para pruebas).
     */
    public static RouteHandler getPostRouteHandler(String path) {
        return postRoutes.get(path);
    }

    // Método para registrar rutas GET
    public static void get(String path, RouteHandler handler) {
        getRoutes.put(path, handler);
    }
    // Método para registrar rutas POST
    public static void post(String path, RouteHandler handler) {
        postRoutes.put(path, handler);
    }


    private static String RESOURCE_PATH = "/usrapp/bin/classes/webroot";


    public static void main(String[] args) throws IOException, URISyntaxException {


        int port = 8080;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (Exception ignored) {}
        }
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(1);
        }

        // Ejemplo de REST con prefijo /App
        get("/App/hello", (req, res) -> {
            String nombre = req.getValues("name");
            if (nombre == null) nombre = "desconocido";
            return "Hello " + nombre;
        });
        get("/App/pi", (req, res) -> String.valueOf(Math.PI));
        // Ejemplo POST
        post("/App/hellopost", (req, res) -> {
            String nombre = req.getValues("name");
            if (nombre == null) nombre = "desconocido";
            return "Hello (POST) " + nombre;
        });

        boolean running = true;
        while (running) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 OutputStream rawOut = clientSocket.getOutputStream();
                 PrintWriter out = new PrintWriter(rawOut, true)) {

                System.out.println("Listo para recibir ...");

                String inputLine, path = null, method = null;
                boolean firstLine = true;
                String query = null;
                int contentLength = 0;
                StringBuilder bodyBuilder = new StringBuilder();

                // Leer headers y primera línea
                while ((inputLine = in.readLine()) != null) {
                    if (firstLine) {
                        String[] parts = inputLine.split(" ");
                        method = parts[0];
                        path = parts[1];
                        if (path.contains("?")) {
                            query = path.substring(path.indexOf("?") + 1);
                            path = path.substring(0, path.indexOf("?"));
                        }
                        System.out.println("Path: " + path);
                        firstLine = false;
                    }
                    if (inputLine.toLowerCase().startsWith("content-length:")) {
                        try {
                            contentLength = Integer.parseInt(inputLine.split(":")[1].trim());
                        } catch (Exception ignored) {}
                    }
                    if (inputLine.isEmpty()) break; // Fin de headers
                }

                // Leer body si es POST
                if ("POST".equals(method) && contentLength > 0) {
                    char[] bodyChars = new char[contentLength];
                    int read = in.read(bodyChars, 0, contentLength);
                    if (read > 0) {
                        bodyBuilder.append(bodyChars, 0, read);
                    }
                }
                String body = bodyBuilder.length() > 0 ? bodyBuilder.toString() : null;

                // Buscar si la ruta está registrada como GET o POST
                if ("GET".equals(method) && getRoutes.containsKey(path)) {
                    Request req = new Request(path, query, method, null);
                    Response res = new Response();
                    String respBody = getRoutes.get(path).handle(req, res);
                    String contentType = respBody.trim().startsWith("{") ? "application/json" : "text/plain";
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + contentType + "\r\n\r\n" +
                            respBody;
                    out.println(response);
                } else if ("POST".equals(method) && postRoutes.containsKey(path)) {
                    Request req = new Request(path, query, method, body);
                    Response res = new Response();
                    String respBody = postRoutes.get(path).handle(req, res);
                    String contentType = respBody.trim().startsWith("{") ? "application/json" : "text/plain";
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + contentType + "\r\n\r\n" +
                            respBody;
                    out.println(response);
                } else {
                    serveStaticFile(path, rawOut, out);
                }

            } catch (IOException e) {
                System.err.println("Error manejando cliente: " + e.getMessage());
            }
        }
        serverSocket.close();
    }

    
    // Eliminada función no utilizada serveJsonHello

    
    private static void serveStaticFile(String path, OutputStream rawOut, PrintWriter out) throws IOException {
        if (path == null) {
            String response = "HTTP/1.1 400 Bad Request\r\n"
                    + "Content-Type: text/html\r\n\r\n"
                    + "<h1>400 Bad Request</h1>";
            out.println(response);
            return;
        }
        String resourcePath = path.equals("/") ? "/index.html" : path;

        Path filePath = Paths.get(RESOURCE_PATH + resourcePath);

        if (Files.exists(filePath)) {
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) mimeType = "application/octet-stream";

            byte[] fileData = Files.readAllBytes(filePath);

            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: " + mimeType + "\r\n");
            out.print("Content-Length: " + fileData.length + "\r\n");
            out.print("\r\n");
            out.flush();

            rawOut.write(fileData);
            rawOut.flush();

        } else {
            String response = "HTTP/1.1 404 Not Found\r\n"
                    + "Content-Type: text/html\r\n\r\n"
                    + "<h1>404 Not Found</h1>";
            out.println(response);
        }
    }
}
