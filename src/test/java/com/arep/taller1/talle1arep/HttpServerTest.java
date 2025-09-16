package com.arep.taller1.talle1arep;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HttpServerTest {
    @Before
    public void setUp() {
        // Limpiar rutas antes de cada test (no hay método público, pero se puede mejorar el diseño para test)
        // Por ahora, registrar rutas de prueba sobrescribirá las anteriores
    }

    /**
     * Prueba que la ruta GET /pi retorna el valor de PI como String.
     */
    @Test
    public void testGetPiRoute() {
    HttpServer.get("/pi", (req, res) -> String.valueOf(Math.PI));
    Request req = new Request("/pi", null, "GET", null);
    Response resp = new Response();
    String result = HttpServer.getGetRouteHandler("/pi").handle(req, resp);
    assertEquals(String.valueOf(Math.PI), result);
    }

    /**
     * Prueba que la ruta GET /hello retorna el saludo con el nombre pasado por query.
     */
    @Test
    public void testGetHelloRoute() {
    HttpServer.get("/hello", (req, res) -> "Hello " + req.getValues("name"));
    Request req = new Request("/hello", "name=Juan", "GET", null);
    Response resp = new Response();
    String result = HttpServer.getGetRouteHandler("/hello").handle(req, resp);
    assertEquals("Hello Juan", result);
    }

    /**
     * Prueba que la ruta POST /hellopost retorna el saludo con el nombre pasado por body.
     */
    @Test
    public void testPostHelloRoute() {
    HttpServer.post("/hellopost", (req, res) -> "Hello (POST) " + req.getValues("name"));
    Request req = new Request("/hellopost", null, "POST", "name=Ana");
    Response resp = new Response();
    String result = HttpServer.getPostRouteHandler("/hellopost").handle(req, resp);
    assertEquals("Hello (POST) Ana", result);
    }
}
