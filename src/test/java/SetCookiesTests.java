import com.codeborne.selenide.WebDriverRunner;
import io.restassured.response.Response;
import io.restassured.response.ResponseBodyExtractionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;

public class SetCookiesTests {

    private static final String BASE_URL = "http://demowebshop.tricentis.com/";

    @Test
    void setCookieTest() {

        step("Получить куки и подставить в браузер", () -> {
            // @formatter:off
            String authorizationCookie =
                    given()
                            .baseUri(BASE_URL)
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .body("product_attribute_72_5_18=53&product_attribute_72_6_19=54&" +
                                    "product_attribute_72_3_20=58&addtocart_72.EnteredQuantity=4").
                    when()
                            .post("addproducttocart/details/72/1").
                    then()
                            .statusCode(200)
                            .extract()
                            .cookie("Nop.customer");
            // @formatter:on

            step("Открыть небольшой контент, чтобы была сессия браузера, куда можно подставить куки", () ->
                    open("http://demowebshop.tricentis.com/Themes/DefaultClean/Content/images/mobile-menu-collapse.png"));

            step("Подставить куки в браузер", () ->
                    getWebDriver().manage().addCookie(new Cookie("Nop.customer", authorizationCookie)));
        });

        step("Открыть веб интерфейс", () ->
                open("http://demowebshop.tricentis.com/"));

        step("Проверить количество товаров в корзине", () -> {
            $(".cart-qty").shouldHave(text("4"));
        });
    }

    @Test
    void setFullCookieTest() {

        step("Получить куки и подставить в браузер", () -> {
            // @formatter:off
            Map<String,String> cookies =
                    given ()
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .body("product_attribute_72_5_18=53&product_attribute_72_6_19=54&" +
                                    "product_attribute_72_3_20=58&addtocart_72.EnteredQuantity=6").
                    when()
                            .post("http://demowebshop.tricentis.com/addproducttocart/details/72/1").
                    then()
                            .statusCode(200)
                            .extract()
                            .cookies();
            // @formatter:off

            step("Открыть небольшой контент, чтобы была сессия браузера, куда можно подставить куки", () ->
                    open("http://demowebshop.tricentis.com/Themes/DefaultClean/Content/images/mobile-menu-collapse.png"));

            step("Подставить полные куки в браузер", () -> {
                cookies.entrySet().stream()
                        .forEach(cookie -> WebDriverRunner.getWebDriver().manage()
                                .addCookie(new Cookie(cookie.getKey(), cookie.getValue())));
            });
        });

        step("Открыть веб интерфейс и убедиться, что кука подставилась успешно, товар добавлен в корзину", () ->
                open("http://demowebshop.tricentis.com/"));

        step("Проверить количество товаров", () -> {
            $(".cart-qty").shouldHave(text("6"));
        });

    }

    private Response addProductToCart(int qtyItems) {
        return given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body("addtocart_31.EnteredQuantity=" + qtyItems)
                .when()
                .post(BASE_URL + "/addproducttocart/details/31/1")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .response();
    }
}
