import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPet {

    public static final String BASE_URL = "http://5.181.109.28:9090/api/v3";

    @Test
    @Feature("Pet")
    @Tag("smoke")
    @Owner("artem melnikov")
    public void testDeleteNonexistentPet() {
        Response response = step("Отправить запрос DELETE на удаление несуществующего Pet", () ->
                given()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .when()
                .delete(BASE_URL + "/pet/9999")
        );

        String responseBody = response.getBody().asString();

        step("Проверить, что статус-код ответа == 200", () ->
                assertEquals(200, response.getStatusCode(),
                        "Код ответа не совпал с ожидаемым. Ответ: " + responseBody)
        );

        step("Проверить, что текст ответа 'Pet deleted'", () ->
                assertEquals("Pet deleted",  responseBody,
                        "Текст ошибки не совпал с ожидаемым. Получен: " + responseBody)
        );
    }
}