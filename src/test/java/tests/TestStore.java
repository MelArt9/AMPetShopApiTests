package tests;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.Order;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Feature("Store")
@Owner("artem melnikov")
public class TestStore {

    public static final String BASE_URL = "https://petstore.swagger.rv-school.ru/api/v3";

    @Test
    public void testPlaceOrder() {
        Order order = new Order();
        order.setId(1);
        order.setPetId(1);
        order.setQuantity(1);
        order.setStatus("placed");
        order.setComplete(true);

        Response response = step("Отправить POST запрос на оформление заказа", () ->
                given()
                        .contentType(ContentType.JSON)
                        .header("Accept", "application/json")
                        .body(order)
                .when()
                        .post(BASE_URL + "/store/order"));

        String responseBody = response.getBody().asString();

        step("Проверить, что статус-код ответа == 200", () ->
                assertEquals(200, response.getStatusCode(),
                        "Код ответа не совпал с ожидаемым. Ответ: " + responseBody));

        step("Проверить параметры созданного заказа", () -> {
            Order createdOrder = response.as(Order.class);
            assertEquals(order.getId(), createdOrder.getId(), "id заказа не совпадает");
            assertEquals(order.getPetId(), createdOrder.getPetId(), "petId не совпадает");
            assertEquals(order.getQuantity(), createdOrder.getQuantity(), "quantity не совпадает");
            assertEquals(order.getStatus(), createdOrder.getStatus(), "status не совпадает");
            assertEquals(order.isComplete(), createdOrder.isComplete(), "complete не совпадает");
        });
    }

    @Test
    public void testGetOrder() {
        Order order = new Order();
        order.setId(1);
        order.setPetId(1);
        order.setQuantity(1);
        order.setStatus("placed");
        order.setComplete(true);

        step("Предусловие: создать заказ", () ->
                given()
                        .contentType(ContentType.JSON)
                        .header("Accept", "application/json")
                        .body(order)
                .when()
                        .post(BASE_URL + "/store/order"));

        Response getResponse = step("Отправить GET запрос на получение информации о заказе", () ->
                given()
                        .header("Accept", "application/json")
                .when()
                        .get(BASE_URL + "/store/order/" + order.getId()));

        step("Проверить код ответа и параметры заказа", () -> {
            assertEquals(200, getResponse.getStatusCode(),
                    "Код ответа не совпал. Ответ: " + getResponse.getBody().asString());
            Order getOrderResponse = getResponse.as(Order.class);
            assertEquals(order.getId(), getOrderResponse.getId(), "Id заказа не совпадает");
            assertEquals(order.getPetId(), getOrderResponse.getPetId(), "PetId заказа не совпадает");
            assertEquals(order.getQuantity(), getOrderResponse.getQuantity(), "Quantity заказа не совпадает");
            assertEquals(order.getStatus(), getOrderResponse.getStatus(), "Status заказа не совпадает");
            assertEquals(order.isComplete(), getOrderResponse.isComplete(), "Complete заказа не совпадает");
        });
    }

    @Test
    public void testDeleteOrder() {
        Order order = new Order();
        order.setId(1);
        order.setPetId(1);
        order.setQuantity(1);
        order.setStatus("placed");
        order.setComplete(true);

        step("Предусловие: создать заказ", () ->
                given()
                        .contentType(ContentType.JSON)
                        .header("Accept", "application/json")
                        .body(order)
                .when()
                        .post(BASE_URL + "/store/order"));

        Response deleteResponse = step("Отправить DELETE запрос на удаление заказа", () ->
                given()
                        .header("Accept", "application/json")
                .when()
                        .delete(BASE_URL + "/store/order/" + order.getId()));

        step("Проверить, что статус-код ответа == 200", () ->
                assertEquals(200, deleteResponse.getStatusCode(),
                        "Код ответа не совпал с ожидаемым. Ответ: " + deleteResponse.getBody().asString()));

        step("Проверить GET запросом факт удаления заказа", () -> {
            Response getResponse = given().header("Accept", "application/json")
                    .when().get(BASE_URL + "/store/order/" + order.getId());

            assertEquals(404, getResponse.getStatusCode(),
                    "Заказ все еще существует. Ответ: " + getResponse.getBody().asString());
        });
    }

    @Test
    public void testGetNonexistentOrder() {
        Response response = step("Отправить GET запрос на получение информации о несуществующем заказе", () ->
            given()
                    .header("Accept", "application/json")
            .when()
                    .get(BASE_URL + "/store/order/9999"));

        String responseBody = response.getBody().asString();

        step("Проверить, что статус-код ответа == 404", () ->
                assertEquals(404, response.getStatusCode(),
                        "Код ответа не совпал с ожидаемым. Ответ: " + responseBody));

        step("Проверить, что текст ответа 'Order not found'", () ->
                assertEquals("Order not found", responseBody,
                        "Текст ответа не совпал с ожидаемым. Получен: " + responseBody));
    }

    @Test
    public void testGetInventory() {
        Response response = step("Отправить GET запрос на получение инвентаря", () ->
                given()
                        .header("Accept", "application/json")
                .when()
                        .get(BASE_URL + "/store/inventory"));

        step("Проверить код ответа и наличие данных", () -> {
            step("Проверить, что статус-код ответа == 200", () ->
                    assertEquals(200, response.getStatusCode(),
                            "Код ответа не совпал с ожидаемым. Ответ: " + response.getBody().asString()));

            Integer approved = response.jsonPath().getInt("approved");
            Integer placed = response.jsonPath().getInt("placed");
            Integer delivered = response.jsonPath().getInt("delivered");

            assertNotNull(approved, "Поле 'approved' отсутствует в ответе!");
            assertNotNull(placed, "Поле 'placed' отсутствует в ответе!");
            assertNotNull(delivered, "Поле 'delivered' отсутствует в ответе!");

            assertTrue(approved >= 0, "approved не может быть отрицательным");
            assertTrue(placed >= 0, "placed не может быть отрицательным");
            assertTrue(delivered >= 0, "delivered не может быть отрицательным");
        });
    }
}