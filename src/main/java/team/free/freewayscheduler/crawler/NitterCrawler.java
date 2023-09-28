package team.free.freewayscheduler.crawler;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.text.html.HTML;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class NitterCrawler implements NotificationCrawler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a z");

    @Value("${target.url}")
    private String targetUrl;

    @Override
    public List<NotificationDto> crawlingTwitter() {
        List<NotificationDto> notifications = new ArrayList<>();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = createChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        driver.get(targetUrl);

        try {
            List<WebElement> notificationElements = driver.findElements(By.className("timeline-item"));

            for (WebElement notificationElement : notificationElements) {
                WebElement contentElement = notificationElement.findElement(By.className("tweet-content"));
                String notificationContent = contentElement.getText();
                if (notificationContent.contains("http")) {
                    notificationContent = notificationContent.split(" http")[0];
                }
                if (notificationContent.contains("🔗")) {
                    notificationContent = notificationContent.split("🔗")[0];
                }
                notificationContent = notificationContent.replaceAll("\n\n", " ");

                WebElement dateElement =
                        notificationElement.findElement(By.className("tweet-date")).findElement(By.tagName(HTML.Tag.A.toString()));
                String dateTime = dateElement.getAttribute("title");

                LocalDateTime notificationDate = LocalDateTime.parse(dateTime, FORMATTER).plusHours(9);
                notifications.add(new NotificationDto(notificationContent, notificationDate));
            }
        } finally {
            driver.close();
        }

        return notifications;
    }

    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-default-apps");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("User-Agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");
        return options;
    }
}
