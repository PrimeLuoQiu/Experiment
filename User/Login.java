package User;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    @FXML
    private Button login, exit;

    @FXML
    private TextField username, password;

    // 数据库连接信息
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/lanxinacademy";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "123456";

    // 初始化方法，在FXML加载后调用
    @FXML
    private void initialize() {
        // 设置用户名和密码文本框的鼠标悬停效果
        setupTextFieldHoverEffect(username);
        setupTextFieldHoverEffect(password);
    }

    // 设置文本框鼠标悬停时的效果
    private void setupTextFieldHoverEffect(TextField textField) {
        textField.setOnMouseEntered(event -> {
            animateBorderColor(textField, "linear-gradient(to right, blue, green)");
        });
        textField.setOnMouseExited(event -> {
            animateBorderColor(textField, "black");
        });
    }

    // 动画效果：边框颜色渐变
    private void animateBorderColor(TextField textField, String targetColor) {
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(textField.styleProperty(), "-fx-border-color: " + targetColor + ";");
        KeyFrame keyFrame = new KeyFrame(Duration.millis(10), keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    // 登录按钮点击事件处理方法
    @FXML
    private void handleLogin() {
        String inputUsername = username.getText();
        String inputPassword = password.getText();

        if (validateLogin(inputUsername, inputPassword)) {
            // 登录成功，跳转到Index界面
            UserSession.setLoggedInUsername(inputUsername);
            showIndex();
        } else {
            // 登录失败，显示警告信息
            showAlert("登录失败", "用户名或密码错误！");
        }
    }

    // 验证用户名和密码是否匹配数据库中的记录
    private boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM user WHERE username = ? AND userpwd = ?";
        try (
                Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next(); // 如果查询结果集有数据，则返回true，否则返回false
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 显示警告对话框
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 显示Index界面
    private void showIndex() {
        try {
            // 加载Index界面的FXML文件
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("Index.fxml"));
            Stage stage = (Stage) login.getScene().getWindow(); // 获取当前窗口的Stage
            stage.setScene(new javafx.scene.Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class UserSession {
        private static String loggedInUsername;
        public static String getLoggedInUsername() {
            return loggedInUsername;
        }
        public static void setLoggedInUsername(String userName) {
            loggedInUsername = userName;
        }
    }
}
