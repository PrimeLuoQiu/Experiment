package User;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

//import nl.siegmann.*;
//import nl.siegmann.epublib.domain.Book;
//import nl.siegmann.epublib.domain.Resource;
//import nl.siegmann.epublib.epub.EpubReader;

public class Index implements Initializable {

    @FXML
    private ListView<String> listView;

    @FXML
    private Label userName;

    @FXML
    private Label setTimes, aboutMe;

    @FXML
    private TextField addWork, searchField;

    @FXML
    private ListView<String> workList, epubListView;

    @FXML
    private Button addNewWork, searchButton, prevButton, playPauseButton, nextButton, openEpubButton;

    @FXML
    private Pane userPane, findSongs, findSource, aboutUs;

    @FXML
    private ListView<String> musicList;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private MediaView musicPlayer;

    @FXML
    private WebView webView;

    private MediaPlayer mediaPlayer;

    private double originalWidth = 35;
    private double expandedWidth = 100;
    private static final String MUSIC_DIRECTORY = "C:\\Users\\LuoQiu\\Desktop\\Learn\\Experiment\\WJH\\Music";

    private boolean isPlaying = false;

    private Map<Duration, String> lyricsMap = new TreeMap<>();

    @FXML
    private TextField lyricsField;

    private ObservableList<String> musicFiles = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        File directory = new File("C:\\Users\\LuoQiu\\Desktop\\Learn\\Experiment\\WJH\\EBook");
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".epub"));
        if (files != null) {
            for (File file : files) {
                epubListView.getItems().add(file.getName());
            }
        }


        listView.getItems().addAll(
                "用户中心",
                "放松一下",
                "资源查找",
                "关于我们"
        );

        listView.setOnMouseEntered(event -> {
            smoothResizeListView(expandedWidth);
        });

        listView.setOnMouseExited(event -> {
            smoothResizeListView(originalWidth);
        });

        // 设置单元格工厂
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    private ImageView imageView = new ImageView();

                    {
                        imageView.setFitWidth(24);
                        imageView.setFitHeight(24);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);

                            String iconPath = "/icons/" + item + ".png";
                            URL iconUrl = getClass().getResource(iconPath);
                            if (iconUrl != null) {
                                Image image = new Image(iconUrl.toString());
                                imageView.setImage(image);
                                setGraphic(imageView);
                            } else {
                                System.err.println("Failed to load icon: " + iconPath);
                                imageView.setImage(null);
                            }
                        }
                    }
                };
            }
        });

        epubListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // 双击事件
                String selectedEpub = epubListView.getSelectionModel().getSelectedItem();
                if (selectedEpub != null) {
                    String epubFilePath = "C:\\Users\\LuoQiu\\Desktop\\Learn\\Experiment\\WJH\\EBook\\" + selectedEpub;
                    //displayEpubFile(epubFilePath);
                }
            }
        });

        // 获取用户名并显示
        userName.setText("欢迎, " + Login.UserSession.getLoggedInUsername());


        // 启动实时更新时钟
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            setTimes.setText(dateFormat.format(new Date()));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // 添加新工作项
        addNewWork.setOnAction(event -> {
            String work = addWork.getText();
            if (!work.trim().isEmpty()) {
                workList.getItems().add(work);
                addWork.clear();
            }
        });

        // 设置工作列表单元格工厂
        workList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    private Button deleteButton = new Button("删除");

                    {
                        deleteButton.setOnAction(event -> {
                            getListView().getItems().remove(getItem());
                        });

                        setOnMouseEntered(e -> deleteButton.setVisible(true));
                        setOnMouseExited(e -> deleteButton.setVisible(false));
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setGraphic(deleteButton);
                        }
                    }
                };
            }
        });

        // 添加音乐列表监听器
        musicList.setOnMouseClicked(event -> {
            String selectedItem = musicList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                playMusic(selectedItem);
            }
        });

        musicList.setItems(musicFiles);

        musicList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String selectedSongName = newValue;
                loadLyrics(selectedSongName.replace(".mp3", ".lrc"));
                //String imagePath = Paths.get(MUSIC_DIRECTORY, selectedSongName.replace(".mp3", ".jpg")).toString();
                //albumImageView.setImage(new Image(new File(imagePath).toURI().toString()));
            }
        });

        searchButton.setOnAction(event -> searchMusic());

        prevButton.setOnAction(event -> playPrevious());
        playPauseButton.setOnAction(event -> togglePlayPause());
        nextButton.setOnAction(event -> playNext());

        // 添加ListView选择监听器
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if ("放松一下".equals(newValue)) {
                findSongs.setVisible(true);
                userPane.setVisible(false);
                findSource.setVisible(false);
                aboutUs.setVisible(false);
            } else if ("资源查找".equals(newValue)) {
                findSongs.setVisible(false);
                findSource.setVisible(true);
                userPane.setVisible(false);
                aboutUs.setVisible(false);
            } else if ("关于我们".equals(newValue)) {
                aboutUs.setVisible(true);
                userPane.setVisible(false);
                findSongs.setVisible(false);
                findSource.setVisible(false);
            } else {
                findSongs.setVisible(false);
                findSource.setVisible(false);
                aboutUs.setVisible(false);
                userPane.setVisible(true);
            }
        });

        aboutMe.setText("联系我:\nWechat:LuoQiu-LQ \n QQ:431756152");
        aboutMe.setStyle("-fx-font-size: 30; -fx-font-family: 华文楷体");
    }



    private void searchMusic() {
        try (Stream<Path> paths = Files.walk(Paths.get(MUSIC_DIRECTORY))) {
            musicList.getItems().clear();
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".mp3") || path.toString().endsWith(".flac"))
                    .forEach(path -> musicList.getItems().add(path.getFileName().toString().replaceAll("\\.mp3$|\\.flac$", "")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMusic(String musicFileName) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        String musicPath = Paths.get(MUSIC_DIRECTORY, musicFileName + ".mp3").toString();
        File musicFile = new File(musicPath);
        if (!musicFile.exists()) {
            musicPath = Paths.get(MUSIC_DIRECTORY, musicFileName + ".flac").toString();
            musicFile = new File(musicPath);
        }

        if (musicFile.exists()) {
            Media media = new Media(musicFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            // 设置播放器就绪后播放音乐
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
                isPlaying = true;
                playPauseButton.setText("||");
                prevButton.setText("<<");
                nextButton.setText(">>");
            });

            mediaPlayer.currentTimeProperty().addListener((observable, oldDuration, newDuration) -> {
                progressBar.setProgress(newDuration.toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
                updateLyrics(newDuration);
            });

            mediaPlayer.setOnEndOfMedia(this::playNext);
        }
    }

    private void loadLyrics(String musicFileName) {
        lyricsMap.clear();
        String lyricsPath = Paths.get(MUSIC_DIRECTORY, musicFileName + ".lrc").toString();
        File lyricsFile = new File(lyricsPath);

        if (lyricsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(lyricsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.matches("\\[\\d{2}:\\d{2}\\.\\d{2}].*")) {
                        String[] parts = line.split("]", 2);
                        String timePart = parts[0].substring(1);
                        String textPart = parts[1];

                        String[] timeParts = timePart.split("[:.]");
                        int minutes = Integer.parseInt(timeParts[0]);
                        int seconds = Integer.parseInt(timeParts[1]);
                        int milliseconds = Integer.parseInt(timeParts[2]);

                        Duration timestamp = Duration.seconds(minutes * 60 + seconds + milliseconds / 100.0);
                        lyricsMap.put(timestamp, textPart);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Lyrics file not found: " + lyricsPath);
        }
    }

    private void updateLyrics(Duration currentTime) {
        String currentLyric = null;

        for (Map.Entry<Duration, String> entry : lyricsMap.entrySet()) {
            if (currentTime.greaterThanOrEqualTo(entry.getKey())) {
                currentLyric = entry.getValue();
            } else {
                break;
            }
        }

        String finalCurrentLyric = currentLyric;
        Platform.runLater(() -> {
            if (finalCurrentLyric != null) {
                lyricsField.setText(finalCurrentLyric);
            } else {
                lyricsField.setText("");
            }
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                playPauseButton.setText(">");
            } else {
                mediaPlayer.play();
                playPauseButton.setText("||");
            }
            isPlaying = !isPlaying;
        }
    }

    private void playNext() {
        int currentIndex = musicList.getSelectionModel().getSelectedIndex();
        if (currentIndex < musicList.getItems().size() - 1) {
            musicList.getSelectionModel().select(currentIndex + 1);
            playMusic(musicList.getSelectionModel().getSelectedItem());
        }
    }

    private void playPrevious() {
        int currentIndex = musicList.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            musicList.getSelectionModel().select(currentIndex - 1);
            playMusic(musicList.getSelectionModel().getSelectedItem());
        }
    }

    private void smoothResizeListView(double targetWidth) {
        Timeline timeline = new Timeline();

        // Resize ListView
        KeyValue keyValueListView = new KeyValue(listView.prefWidthProperty(), targetWidth);
        KeyFrame keyFrameListView = new KeyFrame(Duration.seconds(0.4), keyValueListView);

        // Move right Panes
        double offset = targetWidth - listView.getPrefWidth();
        KeyValue keyValueUserPane = new KeyValue(userPane.layoutXProperty(), userPane.getLayoutX() + offset);
        KeyValue keyValueFindSongs = new KeyValue(findSongs.layoutXProperty(), findSongs.getLayoutX() + offset);
        KeyValue keyValueFindSource = new KeyValue(findSource.layoutXProperty(), findSource.getLayoutX() + offset);
        KeyValue keyValueAboutUs = new KeyValue(aboutUs.layoutXProperty(), aboutUs.getLayoutX() + offset);

        KeyFrame keyFramePanes = new KeyFrame(Duration.seconds(0.4), keyValueUserPane, keyValueFindSongs, keyValueFindSource, keyValueAboutUs);

        timeline.getKeyFrames().addAll(keyFrameListView, keyFramePanes);
        timeline.play();
    }

}
