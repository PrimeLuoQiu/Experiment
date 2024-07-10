package User;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;

public class MusicSearchApp extends Application {

    private ListView<String> musicList;
    private TextField searchBox;

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox();
        Scene scene = new Scene(root, 400, 300);

        searchBox = new TextField();
        Button searchButton = new Button("Search");
        searchButton.setOnAction(event -> searchMusic());

        musicList = new ListView<>();

        root.getChildren().addAll(searchBox, searchButton, musicList);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Music Search");
        primaryStage.show();
    }

    private void searchMusic() {
        String query = searchBox.getText();
        if (!query.trim().isEmpty()) {
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String apiUrl = "https://api.musicprovider.com/search?q=" + encodedQuery;

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String jsonResponse = reader.lines().collect(Collectors.joining());
                reader.close();

                // Parse JSON response and update musicList
                updateMusicList(jsonResponse);

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateMusicList(String jsonResponse) {
        // Parse JSON and update musicList
        // Example: Assuming jsonResponse contains an array of songs
        // JSONArray songs = new JSONArray(jsonResponse);
        // for each song in songs {
        //     musicList.getItems().add(song.getString("name"));
        // }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
