package com.example.herokupipeexample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerControllerTest
{
    @LocalServerPort
    protected int port = 0;

    @Before
    @After
    public void init()
    {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        given().delete().then().statusCode(200);
    }

    @Test
    public void testCreate()
    {
        String firstName = "foo";
        String lastName = "bar";
        Customer foo = new Customer(firstName,lastName);

        Customer customer = given().contentType(ContentType.JSON)
                .body(foo)
                .post()
                .then()
                .statusCode(200)
                .extract().as(Customer.class);

        assertEquals(customer.getFirstName(), firstName);
        assertEquals(customer.getLastName(), lastName);
    }

    @Test
    public void testGetEmptyList()
    {
        given().accept(ContentType.JSON)
                .get("/list?lastName=bar")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    public void testCreateAndGet()
    {
        String firstName = "foo";
        String lastName = "bar";
        Customer foo = new Customer(firstName,lastName);

        Customer customer = given().contentType(ContentType.JSON)
                .body(foo)
                .post()
                .then()
                .statusCode(200)
                .extract().as(Customer.class);

        given().accept(ContentType.JSON)
                .get("/list?lastName=bar")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("firstName", equalTo(Collections.singletonList(firstName)))
                .body("lastName", equalTo(Collections.singletonList(lastName)));
    }
}


















