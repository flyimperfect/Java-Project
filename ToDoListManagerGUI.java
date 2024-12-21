import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

class Task {
    private String name;
    private String description;
    private String dueDate;
    private boolean completed;

    public Task(String name, String description, String dueDate) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = false;
    }

    public void markCompleted() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String toString() {
        return name + " - " + (completed ? "Completed" : "Pending");
    }

    public String detailedString() {
        return "Task: " + name + "\nDescription: " + description + "\nDue Date: " + dueDate +
               "\nStatus: " + (completed ? "Completed" : "Pending");
    }
}

public class ToDoListManagerGUI {
    private static final String FILE_PATH = "C:\\Users\\iyera\\Desktop\\java projet_\\tasks.json";
    private static final String API_KEY = "rARPj6b3eQuO1ueH7fG1Bbkxd3LkTFR5"; // Calendarific API Key
    private ArrayList<Task> tasks = new ArrayList<>();
    private DefaultListModel<Task> taskModel = new DefaultListModel<>();
    private JFrame frame;
    private Gson gson = new Gson();
    private HashMap<String, String> timetable;

    public ToDoListManagerGUI() {
        initializeTimetable();
        loadTasks();

        frame = new JFrame("To-Do List Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Create Panels
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();

        // Task List
        JList<Task> taskList = new JList<>(taskModel);
        JScrollPane scrollPane = new JScrollPane(taskList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JButton addButton = new JButton("Add Task");
        JButton markButton = new JButton("Mark Completed");
        JButton deleteButton = new JButton("Delete Task");
        JButton fetchHolidaysButton = new JButton("Fetch Holidays");
        JButton viewTimetableButton = new JButton("View Timetable");

        buttonPanel.add(addButton);
        buttonPanel.add(markButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(fetchHolidaysButton);
        buttonPanel.add(viewTimetableButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);

        // Button Actions
        addButton.addActionListener(e -> addTask());
        markButton.addActionListener(e -> markTaskCompleted(taskList));
        deleteButton.addActionListener(e -> deleteTask(taskList));
        fetchHolidaysButton.addActionListener(e -> fetchHolidays());
        viewTimetableButton.addActionListener(e -> viewTimetable());

        frame.setVisible(true);
    }

    private void initializeTimetable() {
        timetable = new HashMap<>();
        timetable.put("Monday", "9 AM - Math\n10 AM - Physics\n11 AM - Chemistry");
        timetable.put("Tuesday", "9 AM - English\n10 AM - Biology\n11 AM - History");
        timetable.put("Wednesday", "9 AM - Computer Science\n10 AM - Math\n11 AM - Physics");
        timetable.put("Thursday", "9 AM - Chemistry\n10 AM - English\n11 AM - Biology");
        timetable.put("Friday", "9 AM - History\n10 AM - Computer Science\n11 AM - Math");
        timetable.put("Saturday", "9 AM - Physics Lab\n10 AM - Chemistry Lab\n11 AM - Free Period");
        timetable.put("Sunday", "No Classes");
    }

    private void viewTimetable() {
        String day = JOptionPane.showInputDialog(frame, "Enter the day (e.g., Monday):");
        if (day != null) {
            String schedule = timetable.getOrDefault(day, "Invalid day! Please enter a valid weekday.");
            JOptionPane.showMessageDialog(frame, "Timetable for " + day + ":\n" + schedule);
        }
    }

    private void addTask() {
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField dueDateField = new JTextField();

        Object[] fields = {
            "Task Name:", nameField,
            "Description:", descField,
            "Due Date (yyyy-mm-dd):", dueDateField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Task", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String description = descField.getText();
            String dueDate = dueDateField.getText();

            Task task = new Task(name, description, dueDate);
            tasks.add(task);
            taskModel.addElement(task);
            saveTasks();
            JOptionPane.showMessageDialog(frame, "Task added successfully!");
        }
    }

    private void markTaskCompleted(JList<Task> taskList) {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask != null) {
            selectedTask.markCompleted();
            taskList.repaint();
            saveTasks();
            JOptionPane.showMessageDialog(frame, "Task marked as completed!");
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a task.");
        }
    }

    private void deleteTask(JList<Task> taskList) {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            tasks.remove(selectedIndex);
            taskModel.remove(selectedIndex);
            saveTasks();
            JOptionPane.showMessageDialog(frame, "Task deleted successfully!");
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a task.");
        }
    }

    private void fetchHolidays() {
        JTextField countryField = new JTextField();
        JTextField yearField = new JTextField();

        Object[] fields = {
            "Country Code (e.g., US):", countryField,
            "Year:", yearField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Fetch Holidays", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String country = countryField.getText();
            String year = yearField.getText();

            try {
                String urlString = "https://calendarific.com/api/v2/holidays?api_key=" + API_KEY +
                                   "&country=" + country + "&year=" + year;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    parseAndDisplayHolidays(response.toString());
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to fetch holidays. Response code: " + responseCode);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error fetching holidays: " + e.getMessage());
            }
        }
    }

    private void parseAndDisplayHolidays(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray holidays = jsonObject.getAsJsonObject("response").getAsJsonArray("holidays");

        StringBuilder holidayList = new StringBuilder("Holidays:\n");
        for (int i = 0; i < holidays.size(); i++) {
            JsonObject holiday = holidays.get(i).getAsJsonObject();
            String name = holiday.get("name").getAsString();
            String date = holiday.getAsJsonObject("date").get("iso").getAsString();
            holidayList.append("- ").append(name).append(" (").append(date).append(")\n");
        }

        JOptionPane.showMessageDialog(frame, holidayList.toString());
    }

    private void saveTasks() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving tasks: " + e.getMessage());
        }
    }

    private void loadTasks() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type taskListType = new TypeToken<ArrayList<Task>>() {}.getType();
                tasks = gson.fromJson(reader, taskListType);
                if (tasks != null) {
                    for (Task task : tasks) {
                        if (!task.isCompleted()) {
                            taskModel.addElement(task);
                        }
                    }
                } else {
                    tasks = new ArrayList<>();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error loading tasks: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListManagerGUI::new);
    }
}
