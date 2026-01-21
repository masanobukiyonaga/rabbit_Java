package com.example; // GitHub Upload Check

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.loader.ResourceLocator;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        // Load .env file if it exists
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            System.out.println("No .env file found or error loading it: " + e.getMessage());
        }

        SpringApplication.run(Main.class, args);
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(
                "jdbc:mysql://april-diary-db.c9ouqcm6qmdp.ap-northeast-1.rds.amazonaws.com:3306/april_diary?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo");
        dataSource.setUsername("admin");
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null) {
            dbPassword = "8108za10";
        }
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public Jinjava jinjava() {
        Jinjava jinjava = new Jinjava();
        jinjava.setResourceLocator(new ResourceLocator() {
            @Override
            public String getString(String fullName, Charset encoding, JinjavaInterpreter interpreter)
                    throws IOException {
                try (InputStream in = getClass().getResourceAsStream("/templates/" + fullName)) {
                    if (in == null) {
                        throw new FileNotFoundException("Template not found: " + fullName);
                    }
                    String template = new String(in.readAllBytes(), encoding);
                    // Polyfill for Flask's url_for in included/extended templates
                    template = template.replaceAll("url_for\\(['\"]static['\"],\\s*filename=['\"]([^'\"]+)['\"]\\)",
                            "'/static/$1'");
                    template = template.replaceAll("url_for\\(['\"]index['\"]\\)", "'/'");
                    template = template.replaceAll("url_for\\(['\"]login['\"]\\)", "'/login'");
                    template = template.replaceAll("url_for\\(['\"]logout['\"]\\)", "'/logout'");
                    template = template.replaceAll("url_for\\(['\"]new_diary['\"]\\)", "'/diary/new'");
                    template = template.replaceAll("url_for\\(['\"]diary_entry['\"],\\s*entry_id\\s*=\\s*([^)]+)\\)",
                            "'/diary/' ~ $1");
                    template = template.replaceAll("url_for\\(['\"]edit_diary['\"],\\s*entry_id\\s*=\\s*([^)]+)\\)",
                            "'/diary/' ~ $1 ~ '/edit'");
                    template = template.replaceAll("url_for\\(['\"]delete_diary['\"],\\s*entry_id\\s*=\\s*([^)]+)\\)",
                            "'/diary/' ~ $1 ~ '/delete'");
                    return template;
                }
            }
        });
        jinjava.getGlobalContext().registerFilter(new Filter() {
            @Override
            public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
                if (var == null)
                    return "";
                String s = var.toString();
                return s.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br>\n");
            }

            @Override
            public String getName() {
                return "nl2br";
            }
        });
        return jinjava;
    }
}

@Configuration
class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/diary/new", "/diary/*/edit", "/diary/*/delete");
    }
}

class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute("logged_in") == null) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}

@Controller
class AppController {

    private final JdbcTemplate jdbcTemplate;
    private final S3Client s3Client;
    private final Jinjava jinjava;

    private static final String S3_BUCKET = "april-static";
    private static final String CLOUDFRONT_URL = "https://d2sf57sw5vr2iz.cloudfront.net";
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    public AppController(JdbcTemplate jdbcTemplate, S3Client s3Client, Jinjava jinjava) {
        this.jdbcTemplate = jdbcTemplate;
        this.s3Client = s3Client;
        this.jinjava = jinjava;
    }

    private String render(String templateName, Map<String, Object> context) {
        try {
            // Inject session into context
            try {
                ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes();
                HttpSession session = attr.getRequest().getSession(false);
                Map<String, Object> sessionMap = new HashMap<>();
                if (session != null) {
                    Enumeration<String> attributeNames = session.getAttributeNames();
                    while (attributeNames.hasMoreElements()) {
                        String name = attributeNames.nextElement();
                        sessionMap.put(name, session.getAttribute(name));
                    }
                }
                context.put("session", sessionMap);
            } catch (Exception e) {
                // Ignore if no request context
            }

            // Read template from classpath
            String template = new String(getClass().getResourceAsStream("/templates/" + templateName).readAllBytes(),
                    StandardCharsets.UTF_8);

            // Polyfill for Flask's url_for
            template = template.replaceAll("url_for\\(['\"]static['\"],\\s*filename=['\"]([^'\"]+)['\"]\\)",
                    "'/static/$1'");
            template = template.replaceAll("url_for\\(['\"]index['\"]\\)", "'/'");
            template = template.replaceAll("url_for\\(['\"]login['\"]\\)", "'/login'");
            template = template.replaceAll("url_for\\(['\"]logout['\"]\\)", "'/logout'");
            template = template.replaceAll("url_for\\(['\"]new_diary['\"]\\)", "'/diary/new'");
            template = template.replaceAll("url_for\\(['\"]diary_entry['\"],\\s*entry_id\\s*=\\s*([^)]+)\\)",
                    "'/diary/' ~ $1");
            template = template.replaceAll("url_for\\(['\"]edit_diary['\"],\\s*entry_id\\s*=\\s*([^)]+)\\)",
                    "'/diary/' ~ $1 ~ '/edit'");
            template = template.replaceAll("url_for\\(['\"]delete_diary['\"],\\s*entry_id\\s*=\\s*([^)]+)\\)",
                    "'/diary/' ~ $1 ~ '/delete'");

            return jinjava.render(template, context);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error loading template: " + e.getMessage();
        }
    }

    private String uploadToS3(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null)
                return null;
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            String uniqueFilename = "images/" + UUID.randomUUID().toString() + "." + ext;

            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(S3_BUCKET)
                    .key(uniqueFilename)
                    .contentType(file.getContentType())
                    .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return CLOUDFRONT_URL + "/" + uniqueFilename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/")
    @ResponseBody
    public String index() {
        Map<String, Object> rabbit = new HashMap<>();
        rabbit.put("name", "うづきちゃん");
        rabbit.put("age", "4歳");
        rabbit.put("gender", "女の子");
        rabbit.put("favorite_food", "乾燥りんご");
        rabbit.put("hobby", "チモシーを散らかす");
        rabbit.put("image", "0513.jpg");

        Map<String, Object> context = new HashMap<>();
        context.put("rabbit", rabbit);
        return render("index.html", context);
    }

    @GetMapping("/diary")
    @ResponseBody
    public String diary() {
        try {
            List<Map<String, Object>> entries = jdbcTemplate.queryForList("SELECT * FROM diaries ORDER BY id DESC");
            Map<String, Object> context = new HashMap<>();
            context.put("entries", entries);
            return render("diary.html", context);
        } catch (Exception e) {
            e.printStackTrace();
            return "Internal Server Error: " + e.getMessage();
        }
    }

    @GetMapping("/debug/env")
    @ResponseBody
    public String debugEnv() {
        StringBuilder sb = new StringBuilder();
        sb.append("DB_PASSWORD_ENV: ").append(System.getenv("DB_PASSWORD") != null ? "SET" : "NULL").append("<br>");
        sb.append("DB_PASSWORD_PROP: ").append(System.getProperty("DB_PASSWORD") != null ? "SET" : "NULL")
                .append("<br>");
        return sb.toString();
    }

    @GetMapping("/debug/css")
    @ResponseBody
    public String debugCss() {
        try (InputStream in = getClass().getResourceAsStream("/static/css/style.css")) {
            if (in == null)
                return "CSS NOT FOUND in /static/css/style.css";
            return "CSS FOUND! Size: " + in.readAllBytes().length;
        } catch (IOException e) {
            return "Error reading CSS: " + e.getMessage();
        }
    }

    // 投げ銭ページ
    @GetMapping("/timothy")
    @ResponseBody
    public String timothy() {
        Map<String, Object> context = new HashMap<>();
        context.put("date", LocalDate.now().toString());
        return render("timothy.html", context);
    }

    // ギャラリーページを表示する命令
    @GetMapping("/gallery")
    @ResponseBody
    public String gallery() {
        // 日記データを全て取得する
        List<Map<String, Object>> entries = jdbcTemplate.queryForList("SELECT * FROM diaries ORDER BY id DESC");
        Map<String, Object> context = new HashMap<>();
        context.put("entries", entries);
        // gallery.html を表示する
        return render("gallery.html", context);
    }

    @GetMapping("/diary/{id}")
    @ResponseBody
    public String diaryEntry(@PathVariable int id, HttpServletResponse response) {
        try {
            Map<String, Object> entry = jdbcTemplate.queryForMap("SELECT * FROM diaries WHERE id=?", id);
            Map<String, Object> context = new HashMap<>();
            context.put("entry", entry);
            return render("diary_entry.html", context);
        } catch (Exception e) {
            response.setStatus(404);
            return "日記が見つかりません";
        }
    }

    @GetMapping("/diary/new")
    @ResponseBody
    public String newDiaryForm() {
        return render("new_diary.html", new HashMap<>());
    }

    @PostMapping("/diary/new")
    public String newDiarySubmit(@RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile file) {
        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = uploadToS3(file);
        }

        ZonedDateTime dateObj = ZonedDateTime.now(JST);

        jdbcTemplate.update("INSERT INTO diaries (title, content, image_url, `date`) VALUES (?, ?, ?, ?)",
                title,
                content.replace("\r\n", "\n").replace("\r", "\n"),
                imageUrl,
                java.sql.Timestamp.from(dateObj.toInstant()));

        return "redirect:/diary";
    }

    @GetMapping("/diary/{id}/edit")
    @ResponseBody
    public String editDiaryForm(@PathVariable int id, HttpServletResponse response) {
        try {
            Map<String, Object> entry = jdbcTemplate.queryForMap("SELECT * FROM diaries WHERE id=?", id);
            Map<String, Object> context = new HashMap<>();
            context.put("entry", entry);
            return render("edit_diary.html", context);
        } catch (Exception e) {
            response.setStatus(404);
            return "日記が見つかりません";
        }
    }

    @PostMapping("/diary/{id}/edit")
    public String editDiarySubmit(@PathVariable int id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile file) {

        String imageUrl = null;
        try {
            Map<String, Object> entry = jdbcTemplate.queryForMap("SELECT * FROM diaries WHERE id=?", id);
            imageUrl = (String) entry.get("image_url");
        } catch (Exception e) {
            return "redirect:/diary";
        }

        if (file != null && !file.isEmpty()) {
            String newUrl = uploadToS3(file);
            if (newUrl != null) {
                imageUrl = newUrl;
            }
        }

        jdbcTemplate.update("UPDATE diaries SET title=?, content=?, image_url=? WHERE id=?",
                title,
                content.replace("\r\n", "\n").replace("\r", "\n"),
                imageUrl,
                id);

        return "redirect:/diary";
    }

    @PostMapping("/diary/{id}/delete")
    public String deleteDiary(@PathVariable int id) {
        jdbcTemplate.update("DELETE FROM diaries WHERE id=?", id);
        return "redirect:/diary";
    }

    @GetMapping("/test-s3")
    @ResponseBody
    public String testS3() {
        try {
            s3Client.listObjectsV2(b -> b.bucket(S3_BUCKET).maxKeys(1));
            return "S3にアクセス成功！";
        } catch (Exception e) {
            return "接続エラー: " + e.getMessage();
        }
    }

    @GetMapping("/thanks")
    @ResponseBody
    public String thanks() {
        return render("thanks.html", new HashMap<>());
    }

    @GetMapping("/login")
    @ResponseBody
    public String loginForm() {
        return render("login.html", new HashMap<>());
    }

    @PostMapping("/login")
    public void loginSubmit(@RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        String appUsername = System.getenv("APP_USERNAME");
        if (appUsername == null)
            appUsername = "admin";

        String appPassword = System.getenv("APP_PASSWORD");
        if (appPassword == null)
            appPassword = "8108za10";

        if (appUsername.equals(username) && appPassword.equals(password)) {
            session.setAttribute("logged_in", true);
            response.sendRedirect("/");
        } else {
            Map<String, Object> context = new HashMap<>();
            context.put("error", "ユーザー名またはパスワードが間違っています");
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(render("login.html", context));
        }
    }

    @GetMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) throws IOException {
        session.removeAttribute("logged_in");
        response.sendRedirect("/login");
    }
}
